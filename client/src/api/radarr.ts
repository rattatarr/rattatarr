import { apiClient, handleResponse } from './client'
import type { BackgroundJob, GenericResponse } from '@/types'

export type ArrInstance = 'DEFAULT' | 'ANIME'

export async function importRadarrMovies(
  instance: ArrInstance = 'DEFAULT',
): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/radarr/import', {
    params: { query: { instance } },
  })
  return handleResponse<BackgroundJob>(response)
}

export async function refreshRadarrRatings(
  instance: ArrInstance = 'DEFAULT',
): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/radarr/refresh-ratings', {
    params: { query: { instance } },
  })
  return handleResponse<BackgroundJob>(response)
}

export async function testRadarrConnection(
  instance: ArrInstance = 'DEFAULT',
): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/radarr/test', {
    params: { query: { instance } },
  })
  return handleResponse<GenericResponse>(response)
}
