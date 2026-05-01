import { apiClient, handleResponse } from './client'
import { buildStatisticsQueryParams, buildYearRewindQueryParams } from './queryParams'
import type {
  ProfileStatisticsWrapper,
  ProfileStatisticsRequest,
  YearRewindRequest,
  YearRewindWrapper,
  YearRewindAvailableYearsWrapper,
} from '@/types'

export async function getProfileStatistics(
  request: ProfileStatisticsRequest,
): Promise<ProfileStatisticsWrapper> {
  const queryParams = buildStatisticsQueryParams(request)
  const response = await apiClient.GET('/api/v1/profiles/statistics', {
    params: { query: queryParams as any },
  })
  return handleResponse<ProfileStatisticsWrapper>(response)
}

export async function getYearRewind(request: YearRewindRequest): Promise<YearRewindWrapper> {
  const queryParams = buildYearRewindQueryParams(request)
  const response = await apiClient.GET('/api/v1/profiles/statistics/rewind', {
    params: { query: queryParams as any },
  })
  return handleResponse<YearRewindWrapper>(response)
}

export async function getAvailableRewindYears(
  profileId: string,
): Promise<YearRewindAvailableYearsWrapper> {
  const response = await apiClient.GET('/api/v1/profiles/statistics/rewind/years', {
    params: { query: { profileId } },
  })
  return handleResponse<YearRewindAvailableYearsWrapper>(response)
}
