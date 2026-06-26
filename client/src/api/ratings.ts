import { APIError, apiClient, handleResponse } from './client'
import type {
  BackgroundJob,
  RateRequest,
  GenericResponse,
  ReviewRequest,
  DeleteReviewRequest,
} from '@/types'

export interface ExportRatingsCsvResponse {
  csvData: string
  fileName: string
}

function parseContentDispositionFileName(headerValue: string | null): string | null {
  if (!headerValue) {
    return null
  }

  const match = /filename="?([^";]+)"?/i.exec(headerValue)
  return match?.[1] ?? null
}

export async function rateMediaItem(request: RateRequest): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/ratings', {
    body: request,
  })
  return handleResponse<GenericResponse>(response)
}

/**
 * Upsert a review (free-text or structured) for a movie, series, or season
 */
export async function setReview(request: ReviewRequest): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/ratings/review', {
    body: request,
  })
  return handleResponse<GenericResponse>(response)
}

/**
 * Delete a review for a movie, series, or season
 */
export async function deleteReview(request: DeleteReviewRequest): Promise<GenericResponse> {
  const response = await apiClient.DELETE('/api/v1/ratings/review', {
    body: request,
  })
  return handleResponse<GenericResponse>(response)
}

/**
 * Import IMDb ratings from CSV file
 * This uses multipart/form-data for file upload
 */
export async function importIMDbRatings(file: File, profileId: string): Promise<BackgroundJob> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await apiClient.POST('/api/v1/ratings/import/imdb', {
    params: { query: { profileId } },
    // @ts-expect-error - openapi-fetch doesn't handle FormData natively, but it works
    body: formData,
    // Browser automatically sets Content-Type with multipart boundary
  })
  return handleResponse<BackgroundJob>(response)
}

export async function exportRatingsCsv(profileId: string): Promise<ExportRatingsCsvResponse> {
  const url = `/api/v1/ratings/export?profileId=${encodeURIComponent(profileId)}`
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      Accept: 'text/csv',
    },
  })

  if (!response.ok) {
    let body: unknown
    let message = response.statusText || 'Failed to export ratings CSV'
    const responseClone = response.clone()

    try {
      body = await response.json()
      if (body && typeof body === 'object' && 'message' in body) {
        message = String(body.message)
      }
    } catch {
      try {
        const textBody = await responseClone.text()
        if (textBody) {
          message = textBody
          body = textBody
        }
      } catch {
        body = undefined
      }
    }

    throw new APIError({
      status: response.status,
      statusText: response.statusText,
      message,
      body,
      url,
    })
  }

  const csvData = await response.text()
  const fileName =
    parseContentDispositionFileName(response.headers.get('content-disposition')) ??
    'profile-ratings.csv'

  return {
    csvData,
    fileName,
  }
}
