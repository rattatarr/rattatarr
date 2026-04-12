import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useNavigation } from '@/composables/useNavigation.ts'

/**
 * Composable for media search functionality
 *
 * Provides instant navigation to search view as user types.
 * No autocomplete - just live search with instant navigation.
 *
 * Features:
 * - Instant navigation to /search when user starts typing
 * - Syncs input with URL query parameter
 * - Clears input when navigating away from search
 *
 * @example
 * ```typescript
 * const { searchQuery } = useMediaSearch()
 *
 * // Use in InputText component
 * <InputText v-model="searchQuery" placeholder="Search..." />
 * ```
 */
export function useMediaSearch() {
  const route = useRoute()
  const searchQuery = ref<string>('')
  const { goToSearch, goToDashboard } = useNavigation()

  // Sync searchQuery with URL when on search page
  watch(
    () => route.query.q,
    (newQuery) => {
      if (route.name === 'search') {
        searchQuery.value = (newQuery as string) || ''
      }
    },
    { immediate: true },
  )

  // Watch searchQuery and navigate to search view
  watch(searchQuery, (newValue) => {
    const trimmedValue = newValue.trim()

    // If user is typing, navigate to search with query
    if (trimmedValue) {
      if (route.name !== 'search' || route.query.q !== trimmedValue) {
        goToSearch(trimmedValue)
      }
    } else {
      // If search is cleared and we're on search page, go home
      if (route.name === 'search') {
        goToDashboard()
      }
    }
  })

  // Clear search when navigating away from search page
  watch(
    () => route.name,
    (newRouteName) => {
      if (newRouteName !== 'search') {
        searchQuery.value = ''
      }
    },
  )

  return {
    searchQuery,
  }
}
