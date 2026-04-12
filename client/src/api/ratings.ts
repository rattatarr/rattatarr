import { apiClient, handleResponse } from './client'
import type { RateRequest, GenericResponse } from '@/types'

export async function rateMediaItem(request: RateRequest): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/ratings', {
    body: request,
  })
  return handleResponse<GenericResponse>(response)
}

/**
 * Import IMDb ratings from CSV file
 * This uses multipart/form-data for file upload
 */
export async function importIMDbRatings(file: File, profileId: string): Promise<GenericResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await apiClient.POST('/api/v1/ratings/import/imdb', {
    params: { query: { profileId } },
    // @ts-expect-error - openapi-fetch doesn't handle FormData natively, but it works
    body: formData,
    // Browser automatically sets Content-Type with multipart boundary
  })
  return handleResponse<GenericResponse>(response)
}
