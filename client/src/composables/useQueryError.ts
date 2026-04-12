import { watch, type Ref } from 'vue'
import { useToast } from './useToast'
import type { APIError } from '@/api/client'

/**
 * Composable for handling query errors with toast notifications
 *
 * TanStack Query v5 removed global query error handlers, so errors must be
 * handled in components. This composable makes it easy to show toasts for query errors.
 *
 * @example
 * ```typescript
 * const movies = useMovies(pageable, filters)
 *
 * // Show toast when query fails
 * useQueryError(movies.isError, movies.error, {
 *   message: 'Failed to load movies'
 * })
 * ```
 */
export function useQueryError(
  isError: Ref<boolean>,
  error: Ref<Error | null>,
  options?: {
    /** Custom error message (default: "Failed to load data") */
    message?: string
    /** Whether to show toast on error (default: true) */
    showToast?: boolean
    /** Custom error handler callback */
    onError?: (error: Error) => void
  },
) {
  const toast = useToast()
  const showToast = options?.showToast !== false

  watch(
    [isError, error],
    ([errorState, errorValue]) => {
      if (errorState && errorValue) {
        // Call custom error handler if provided
        if (options?.onError) {
          options.onError(errorValue)
        }

        // Show toast if enabled
        if (showToast) {
          const message = options?.message ?? 'Failed to load data'
          toast.error(errorValue as APIError, { fallbackMessage: message })
        }
      }
    },
    { immediate: true },
  )
}
