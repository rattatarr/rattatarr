import { apiClient, handleResponse } from './client'
import { buildJobsQueryParams } from './queryParams'
import type { BackgroundJob, BackgroundJobsWrapper, JobsFilters, Pageable } from '@/types'

export async function getJobs(
  pageable: Pageable,
  filters?: JobsFilters,
): Promise<BackgroundJobsWrapper> {
  const queryParams = buildJobsQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/jobs', {
    params: { query: queryParams as any },
  })
  return handleResponse(response)
}

export async function getJob(id: string): Promise<BackgroundJob> {
  const response = await apiClient.GET('/api/v1/jobs/{id}', {
    params: { path: { id } },
  })
  return handleResponse(response)
}
