import { apiClient, handleResponse } from './client'
import type { BackgroundJob, GenericResponse } from '@/types'

export async function importSonarrSeries(): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/sonarr/import')
  return handleResponse<BackgroundJob>(response)
}

export async function testSonarrConnection(): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/sonarr/test')
  return handleResponse<GenericResponse>(response)
}
