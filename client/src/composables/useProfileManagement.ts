import { computed } from 'vue'
import type { DropdownChangeEvent } from 'primevue/dropdown'
import { useProfiles } from '@/queries'
import { useProfileStore } from '@/stores'
import { useQueryError } from './useQueryError'
import type { Profile } from '@/types'

/**
 * Composable for managing profile selection and data
 *
 * Centralizes profile loading, error handling, and selection state management
 * that is shared across AppHeader and AppHeaderMobile components.
 *
 * @example
 * ```typescript
 * const { profileList, selectedProfile, isLoading, onProfileChange } = useProfileManagement()
 *
 * // Use in Dropdown component
 * <Dropdown
 *   v-model="selectedProfile"
 *   :options="profileList"
 *   :loading="isLoading"
 *   @change="onProfileChange"
 * />
 * ```
 */
export function useProfileManagement() {
  const profileStore = useProfileStore()

  const profiles = useProfiles(
    { page: 0, size: 100 },
    { name: '' }, // Empty filter to get all profiles
  )

  // Handle query errors
  useQueryError(profiles.isError, profiles.error, {
    message: 'Failed to load profiles',
  })

  // Extract profile list from wrapped response
  const profileList = computed<Profile[]>(() => {
    if (!profiles.data.value?.profiles) return []
    return profiles.data.value.profiles ?? []
  })

  // Selected profile (synced with store)
  const selectedProfile = computed({
    get: () => {
      if (!profileStore.selectedProfileId) return null
      return profileStore.getSelectedProfile(profileList.value) ?? null
    },
    set: (profile: Profile | null) => {
      profileStore.setProfile(profile?.id ?? null)
    },
  })

  // Handle profile change
  function onProfileChange(event: DropdownChangeEvent) {
    const profile = event.value as Profile | null
    profileStore.setProfile(profile?.id ?? null)
  }

  return {
    profileList,
    selectedProfile,
    isLoading: profiles.isLoading,
    onProfileChange,
  }
}
