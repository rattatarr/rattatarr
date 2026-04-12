import {
  ref,
  computed,
  watch,
  type Ref,
  type ComputedRef,
  type InjectionKey,
  inject,
  provide,
} from 'vue'
import { useSettings, useUpdateSettings } from '@/queries/useSettings'
import { useToast } from '@/composables/useToast'
import type { CreateSettingRequest } from '@/schemas/types/api'

/**
 * Settings form context that can be provided/injected
 */
export interface SettingsFormContext {
  /** Current settings map (may have unsaved changes) */
  settings: Ref<Record<string, string>>
  /** Original saved settings from backend */
  savedSettings: ComputedRef<Record<string, string>>
  /** Whether there are unsaved changes */
  hasChanges: Ref<boolean>
  /** Whether settings are loading from backend */
  isLoading: ComputedRef<boolean>
  /** Whether settings are being saved */
  isSaving: ComputedRef<boolean>
  /** Update a single setting in local state */
  updateSetting: (key: string, value: string) => void
  /** Get a setting value from local state */
  getSetting: (key: string) => string
  /** Save all changed settings to backend */
  saveSettings: () => Promise<void>
  /** Save only specific settings to backend */
  saveSpecificSettings: (keys: string[]) => Promise<boolean>
  /** Reset all settings to original saved values */
  resetSettings: () => void
}

// Injection key for type safety
export const SettingsFormKey: InjectionKey<SettingsFormContext> = Symbol('settings-form-context')

/**
 * Provider: Use this in the parent component (SettingsView)
 * Creates and provides the settings form context
 */
export function provideSettingsForm() {
  const toast = useToast()

  // Fetch all settings from backend
  const { data: settingsData, isLoading: isLoadingQuery } = useSettings()
  const updateMutation = useUpdateSettings()

  // Local state for editing
  const settingsMap = ref<Record<string, string>>({})
  const hasChanges = ref(false)

  // Initialize local state when settings load from backend
  watch(
    () => settingsData.value,
    (data) => {
      if (data?.settings) {
        const newMap: Record<string, string> = {}
        for (const setting of data.settings) {
          if (setting.key && setting.value !== undefined) {
            newMap[setting.key] = setting.value
          }
        }
        settingsMap.value = newMap
        hasChanges.value = false
      }
    },
    { immediate: true },
  )

  // Computed original values (from backend)
  const originalValues = computed(() => {
    const map: Record<string, string> = {}
    if (settingsData.value?.settings) {
      for (const setting of settingsData.value.settings) {
        if (setting.key && setting.value !== undefined) {
          map[setting.key] = setting.value
        }
      }
    }
    return map
  })

  // Loading states
  const isLoading = computed(() => isLoadingQuery.value)
  const isSaving = computed(() => updateMutation.isPending.value)

  /**
   * Update a single setting in local state
   */
  function updateSetting(key: string, value: string) {
    settingsMap.value[key] = value
    hasChanges.value = true
  }

  /**
   * Get a setting value from local state
   */
  function getSetting(key: string): string {
    return settingsMap.value[key] ?? ''
  }

  /**
   * Get only changed settings (for optimized backend update)
   */
  function getChangedSettings(): CreateSettingRequest[] {
    const changed: CreateSettingRequest[] = []
    for (const key in settingsMap.value) {
      const currentValue = settingsMap.value[key] ?? ''
      const originalValue = originalValues.value[key] ?? ''
      if (currentValue !== originalValue) {
        changed.push({ key, value: currentValue })
      }
    }
    return changed
  }

  /**
   * Save all changed settings to backend
   */
  async function saveSettings() {
    const changedSettings = getChangedSettings()

    if (changedSettings.length === 0) {
      toast.info('No changes to save')
      return
    }

    const loadingToast = toast.loading('Saving settings...', 'Updating configuration')

    try {
      await updateMutation.mutateAsync({
        settings: changedSettings,
      })

      toast.success('Settings saved', {
        description: `Updated ${changedSettings.length} setting${changedSettings.length > 1 ? 's' : ''}`,
        dismissLoadingToast: loadingToast,
      })

      hasChanges.value = false
    } catch (error) {
      toast.error(error, {
        fallbackMessage: 'Failed to save settings',
        dismissLoadingToast: loadingToast,
      })
    }
  }

  /**
   * Reset all settings to original saved values
   */
  function resetSettings() {
    settingsMap.value = { ...originalValues.value }
    hasChanges.value = false
    toast.info('Changes discarded')
  }

  /**
   * Save only specific settings to backend (for connection tests)
   * Returns true if save was successful, false otherwise
   */
  async function saveSpecificSettings(keys: string[]): Promise<boolean> {
    const settingsToSave: CreateSettingRequest[] = []

    for (const key of keys) {
      const currentValue = settingsMap.value[key] ?? ''
      const originalValue = originalValues.value[key] ?? ''

      // Only include if changed or if we want to ensure it's saved
      if (currentValue !== originalValue) {
        settingsToSave.push({ key, value: currentValue })
      }
    }

    // No changes to save for these keys
    if (settingsToSave.length === 0) {
      return true
    }

    try {
      await updateMutation.mutateAsync({
        settings: settingsToSave,
      })

      // Update original values to reflect saved state
      for (const setting of settingsToSave) {
        originalValues.value[setting.key] = setting.value
      }

      // Check if there are still unsaved changes elsewhere
      hasChanges.value = getChangedSettings().length > 0

      return true
    } catch {
      return false
    }
  }

  // Create context
  const context: SettingsFormContext = {
    settings: settingsMap,
    savedSettings: originalValues,
    hasChanges,
    isLoading,
    isSaving,
    updateSetting,
    getSetting,
    saveSettings,
    saveSpecificSettings,
    resetSettings,
  }

  // Provide context to children
  provide(SettingsFormKey, context)

  return context
}

/**
 * Consumer: Use this in child components
 * Injects the settings form context provided by parent
 *
 * @throws Error if used outside of a settings form provider
 */
export function useSettingsForm(): SettingsFormContext {
  const context = inject(SettingsFormKey)

  if (!context) {
    throw new Error(
      'useSettingsForm must be used within a component wrapped by provideSettingsForm',
    )
  }

  return context
}
