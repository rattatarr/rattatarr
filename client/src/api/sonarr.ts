import { apiClient, handleResponse } from './client'
import type { BackgroundJob, GenericResponse } from '@/types'
import type { ArrInstance } from './radarr'

export async function importSonarrSeries(
  instance: ArrInstance = 'DEFAULT',
): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/sonarr/import', {
    params: { query: { instance } },
  })
  return handleResponse<BackgroundJob>(response)
}

export async function testSonarrConnection(
  instance: ArrInstance = 'DEFAULT',
): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/sonarr/test', {
    params: { query: { instance } },
  })
  return handleResponse<GenericResponse>(response)
}
