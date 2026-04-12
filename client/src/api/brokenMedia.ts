import { apiClient, handleResponse } from './client'
import type { BrokenMediaItemWrapper, BrokenMediaItem, Pageable } from '@/types'
import type { ResolveBrokenMediaItemRequest } from '@/types'
import { buildPageableQueryParams } from './queryParams'

/**
 * Get broken movies (missing metadata)
 */
export async function getBrokenMovies(pageable: Pageable): Promise<BrokenMediaItemWrapper> {
  const queryParams = buildPageableQueryParams(pageable)
  const response = await apiClient.GET('/api/v1/library/movies/broken', {
    params: { query: queryParams as any },
  })
  return handleResponse<BrokenMediaItemWrapper>(response)
}

/**
 * Get broken series (missing metadata)
 */
export async function getBrokenSeries(pageable: Pageable): Promise<BrokenMediaItemWrapper> {
  const queryParams = buildPageableQueryParams(pageable)
  const response = await apiClient.GET('/api/v1/library/series/broken', {
    params: { query: queryParams as any },
  })
  return handleResponse<BrokenMediaItemWrapper>(response)
}

/**
 * Resolve a broken media item by linking it to an existing media item
 */
export async function resolveItem(
  id: string,
  request: ResolveBrokenMediaItemRequest,
): Promise<BrokenMediaItem> {
  const response = await apiClient.PATCH('/api/v1/library/broken-media-items/{id}/resolve', {
    params: { path: { id } },
    body: request,
  })
  return handleResponse<BrokenMediaItem>(response)
}
