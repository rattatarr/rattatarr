import { useQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as logsApi from '@/api/logs'
import type { LogsFilters } from '@/api/logs'
import { logKeys } from './queryKeys'
import type { LogsResponseWrapper, Pageable } from '@/types'

/**
 * Query hook to get historical application logs with pagination
 *
 * Use this for searching/browsing logs with filtering and pagination.
 * For infinite scroll, use `useInfiniteLogs` instead.
 *
 * @param pageable - Pagination parameters (page, size, sort)
 * @param filters - Optional filters (level, date range, logger)
 */
export function useLogs(
  pageable: MaybeRefOrGetter<Pageable>,
  filters?: MaybeRefOrGetter<LogsFilters | undefined>,
) {
  return useQuery<LogsResponseWrapper>({
    queryKey: computed(() => logKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => logsApi.getLogs(toValue(pageable), toValue(filters)),
    // Keep previous data while refetching for smooth pagination
    placeholderData: (previousData) => previousData,
  })
}
