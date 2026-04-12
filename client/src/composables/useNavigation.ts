import { useRouter } from 'vue-router'

/**
 * Composable for navigation utilities
 *
 * Centralizes common navigation functions that are shared across components.
 *
 * @example
 * ```typescript
 * const { goHome } = useNavigation()
 *
 * // Use in logo click handler
 * <div @click="goHome">Logo</div>
 * ```
 */
export function useNavigation() {
  const router = useRouter()

  function goHome() {
    void router.push('/')
  }

  function goToHowTo() {
    void router.push({ name: 'howto' })
  }

  function goToDashboard() {
    void router.push({ name: 'dashboard' })
  }

  function goToMovies() {
    void router.push({ name: 'movies' })
  }

  function goToSeries() {
    void router.push({ name: 'series' })
  }

  function goToSearch(trimmedQuery: string) {
    void router.push({ name: 'search', query: { q: trimmedQuery } })
  }

  return {
    goHome,
    goToHowTo,
    goToDashboard,
    goToMovies,
    goToSeries,
    goToSearch,
  }
}
