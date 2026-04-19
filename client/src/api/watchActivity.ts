import { apiClient, handleResponse } from './client'
import type { Pageable, WatchActivityFilters, WatchActivityWrapper } from '@/types'

export async function getWatchActivity(
  pageable: Pageable,
  filters: WatchActivityFilters,
): Promise<WatchActivityWrapper> {
  const queryParams = {
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
    profileId: filters.profileId,
    startDate: filters.startDate,
    endDate: filters.endDate,
    mediaType: filters.mediaType,
  }
  const response = await apiClient.GET('/api/v1/watch-activity', {
    params: { query: queryParams as any },
  })
  return handleResponse<WatchActivityWrapper>(response)
}
