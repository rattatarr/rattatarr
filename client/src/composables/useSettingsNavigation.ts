import { ref, type Ref, provide, inject, type InjectionKey } from 'vue'

/**
 * Context for navigating to and highlighting specific settings
 */
export interface SettingsNavigationContext {
  /**
   * Register a setting field element for navigation
   */
  registerField: (key: string, element: HTMLElement | null) => void
  /**
   * Scroll to and highlight a specific setting field
   */
  scrollToSetting: (key: string) => void
  /**
   * Currently highlighted setting key
   */
  highlightedKey: Ref<string | null>
}

// Injection key for type safety
export const SettingsNavigationKey: InjectionKey<SettingsNavigationContext> = Symbol(
  'settings-navigation-context',
)

/**
 * Provider: Use this in the parent component (SettingsView)
 * Creates and provides the settings navigation context
 */
export function provideSettingsNavigation() {
  // Map of setting keys to their DOM elements
  const fieldElements = new Map<string, HTMLElement>()

  // Currently highlighted field key
  const highlightedKey = ref<string | null>(null)

  /**
   * Register a setting field element for later navigation
   */
  function registerField(key: string, element: HTMLElement | null) {
    if (element) {
      fieldElements.set(key, element)
    } else {
      fieldElements.delete(key)
    }
  }

  /**
   * Scroll to a setting field and highlight it with animation
   */
  function scrollToSetting(key: string) {
    const element = fieldElements.get(key)

    if (!element) {
      console.warn(`Setting field with key "${key}" not found`)
      return
    }

    // Scroll to the element with smooth behavior
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'center',
    })

    // Trigger highlight animation
    highlightedKey.value = key

    // Remove highlight after animation completes
    setTimeout(() => {
      highlightedKey.value = null
    }, 2000)
  }

  const context: SettingsNavigationContext = {
    registerField,
    scrollToSetting,
    highlightedKey,
  }

  // Provide context to children
  provide(SettingsNavigationKey, context)

  return context
}

/**
 * Consumer: Use this in child components to access navigation
 *
 * @throws Error if used outside of a settings navigation provider
 */
export function useSettingsNavigation(): SettingsNavigationContext {
  const context = inject(SettingsNavigationKey)

  if (!context) {
    throw new Error(
      'useSettingsNavigation must be used within a component wrapped by provideSettingsNavigation',
    )
  }

  return context
}
