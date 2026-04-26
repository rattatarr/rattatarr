import { apiClient, handleResponse } from './client'
import type { BackgroundJob, GenericResponse } from '@/types'

export async function importRadarrMovies(): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/radarr/import')
  return handleResponse<BackgroundJob>(response)
}

export async function refreshRadarrRatings(): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/radarr/refresh-ratings')
  return handleResponse<BackgroundJob>(response)
}

export async function testRadarrConnection(): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/radarr/test')
  return handleResponse<GenericResponse>(response)
}
