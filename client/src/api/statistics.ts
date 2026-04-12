import { apiClient, handleResponse } from './client'
import { buildStatisticsQueryParams } from './queryParams'
import type { ProfileStatisticsWrapper, ProfileStatisticsRequest } from '@/types'

/**
 * Fetch profile statistics data
 */
export async function getProfileStatistics(
  request: ProfileStatisticsRequest,
): Promise<ProfileStatisticsWrapper> {
  const queryParams = buildStatisticsQueryParams(request)
  const response = await apiClient.GET('/api/v1/profiles/statistics', {
    params: { query: queryParams as any },
  })
  return handleResponse<ProfileStatisticsWrapper>(response)
}
