import { useInfiniteQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as watchActivityApi from '@/api/watchActivity'
import { watchActivityKeys } from './queryKeys'
import type { WatchActivityFilters, WatchActivityWrapper } from '@/types'

export function useInfiniteWatchActivity(
  filters: MaybeRefOrGetter<WatchActivityFilters>,
  pageSize: MaybeRefOrGetter<number> = 20,
) {
  return useInfiniteQuery({
    queryKey: computed(() =>
      watchActivityKeys.list({ page: 0, size: toValue(pageSize) }, toValue(filters)),
    ),
    queryFn: async ({ pageParam = 0 }) => {
      return watchActivityApi.getWatchActivity(
        { page: pageParam, size: toValue(pageSize) },
        toValue(filters),
      )
    },
    getNextPageParam: (lastPage: WatchActivityWrapper) => {
      const pagination = lastPage.pagination
      if (!pagination || pagination.isLast || pagination.currentPage === undefined) {
        return undefined
      }
      return pagination.currentPage + 1
    },
    initialPageParam: 0,
  })
}
