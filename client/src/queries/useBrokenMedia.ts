import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as brokenMediaApi from '@/api/brokenMedia'
import { movieKeys, seriesKeys } from './queryKeys'
import type {
  Pageable,
  BrokenMediaItemWrapper,
  BrokenMediaItem,
  ResolveBrokenMediaItemRequest,
} from '@/types'

/**
 * Query hook to get broken movies (missing metadata)
 */
export function useBrokenMovies(pageable: MaybeRefOrGetter<Pageable>) {
  return useQuery<BrokenMediaItemWrapper>({
    queryKey: computed(() => movieKeys.brokenList(toValue(pageable))),
    queryFn: () => brokenMediaApi.getBrokenMovies(toValue(pageable)),
  })
}

/**
 * Query hook to get broken series (missing metadata)
 */
export function useBrokenSeries(pageable: MaybeRefOrGetter<Pageable>) {
  return useQuery<BrokenMediaItemWrapper>({
    queryKey: computed(() => seriesKeys.brokenList(toValue(pageable))),
    queryFn: () => brokenMediaApi.getBrokenSeries(toValue(pageable)),
  })
}

/**
 * Mutation hook to resolve a broken media item by linking it to an existing media item
 * Invalidates both movie and series broken queries on success
 */
export function useResolveBrokenMediaItem() {
  const queryClient = useQueryClient()

  return useMutation<
    BrokenMediaItem,
    Error,
    { id: string; request: ResolveBrokenMediaItemRequest }
  >({
    mutationFn: ({ id, request }) => brokenMediaApi.resolveItem(id, request),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: movieKeys.broken() }),
        queryClient.invalidateQueries({ queryKey: seriesKeys.broken() }),
      ])
    },
  })
}
