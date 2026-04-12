import { useInfiniteQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as logsApi from '@/api/logs'
import type { LogsFilters } from '@/api/logs'
import { logKeys } from './queryKeys'
import type { LogsResponseWrapper } from '@/types'

/**
 * Infinite query hook for historical logs with pagination
 *
 * @param filters - Optional filters (level, date range, logger)
 * @param pageSize - Number of logs per page (default: 50)
 * @param autoRefresh - Whether to enable auto-refresh polling (default: false)
 */
export function useInfiniteLogs(
  filters?: MaybeRefOrGetter<LogsFilters | undefined>,
  pageSize: number = 50,
  autoRefresh?: MaybeRefOrGetter<boolean>,
) {
  return useInfiniteQuery<LogsResponseWrapper>({
    queryKey: computed(() => logKeys.infinite(toValue(filters), pageSize)),
    queryFn: async ({ pageParam = 0 }) => {
      return logsApi.getLogs(
        {
          page: pageParam as number,
          size: pageSize,
          sort: ['timestamp,desc'], // Newest first
        },
        toValue(filters),
      )
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      const pagination = lastPage.pagination
      if (!pagination) return undefined

      // Return next page number if there are more pages
      return pagination.hasNext ? pagination.currentPage! + 1 : undefined
    },
    // Keep previous data while refetching
    placeholderData: (previousData) => previousData,
    // Polling: refetch every 5 seconds when autoRefresh is enabled
    refetchInterval: computed(() => (toValue(autoRefresh) ? 5000 : false)),
  })
}
