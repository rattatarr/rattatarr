import { computed, type Ref, type ComputedRef } from 'vue'
import type { MediaSourceInfo } from './useMediaSource'

/**
 * Query result interface matching TanStack Query
 */
interface QueryResult<T> {
  data: Ref<T | undefined>
  isLoading: Ref<boolean>
  error: Ref<Error | null>
  refetch: () => Promise<any>
}

/**
 * Combined query state for unified composables
 */
interface UnifiedQueryState {
  isLoading: ComputedRef<boolean>
  error: ComputedRef<Error | null>
  refetch: () => Promise<any>
}

/**
 * Composable for combining query states from internal and TMDb sources
 *
 * Handles source-based switching between two conditional queries.
 * Returns combined loading/error states and appropriate refetch function.
 *
 * @param source - Media source information (internal vs TMDb)
 * @param internalQuery - Query result for internal API
 * @param tmdbQuery - Query result for TMDb API
 * @returns Combined query state (isLoading, error, refetch)
 *
 * @example
 * ```typescript
 * const { isLoading, error, refetch } = useUnifiedQueryState(
 *   source,
 *   internalQuery,
 *   tmdbQuery
 * )
 * ```
 */
export function useUnifiedQueryState<TInternal, TTMDb>(
  source: Ref<MediaSourceInfo>,
  internalQuery: QueryResult<TInternal>,
  tmdbQuery: QueryResult<TTMDb>,
): UnifiedQueryState {
  // Combine loading states
  const isLoading = computed(
    () =>
      (source.value.isInternal && internalQuery.isLoading.value) ||
      (source.value.isTMDb && tmdbQuery.isLoading.value),
  )

  // Combine error states
  const error = computed(() => {
    if (source.value.isInternal) return internalQuery.error.value
    if (source.value.isTMDb) return tmdbQuery.error.value
    return null
  })

  // Refetch function (calls appropriate query based on source)
  const refetch = async () => {
    if (source.value.isInternal) {
      return await internalQuery.refetch()
    }
    if (source.value.isTMDb) {
      return await tmdbQuery.refetch()
    }
  }

  return {
    isLoading,
    error,
    refetch,
  }
}
