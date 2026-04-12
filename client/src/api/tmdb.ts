import { apiClient, handleResponse } from './client'
import type {
  ImportMediaItemRequest,
  ImportMediaItemResponse,
  SearchFilters,
  GenericResponse,
  TMDbSearchWrapper,
  TMDbMovieDetails,
  TMDbShowDetails,
  TMDbSearchGroupWrapper,
} from '@/types'
import { buildSearchQueryParams } from './queryParams'

export async function importTMDbData(
  request: ImportMediaItemRequest,
): Promise<ImportMediaItemResponse> {
  const response = await apiClient.PUT('/api/v1/tmdb/import', {
    body: request,
  })
  return handleResponse<ImportMediaItemResponse>(response)
}

export async function buildCredits(forceRefresh = false): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/tmdb/build-credits', {
    params: { query: { forceRefresh } },
  })
  return handleResponse<GenericResponse>(response)
}

export async function searchMovies(filters: SearchFilters): Promise<TMDbSearchWrapper> {
  const queryParams = buildSearchQueryParams(filters)
  const response = await apiClient.GET('/api/v1/tmdb/movies/search', {
    params: { query: queryParams as any },
  })
  return handleResponse<TMDbSearchWrapper>(response)
}

export async function getMovieByTMDbId(TMDbId: string): Promise<TMDbMovieDetails> {
  const response = await apiClient.GET('/api/v1/tmdb/movies/{TMDbId}', {
    params: { path: { TMDbId } },
  })
  return handleResponse<TMDbMovieDetails>(response)
}

export async function searchSeries(filters: SearchFilters): Promise<TMDbSearchWrapper> {
  const queryParams = buildSearchQueryParams(filters)
  const response = await apiClient.GET('/api/v1/tmdb/series/search', {
    params: { query: queryParams as any },
  })
  return handleResponse<TMDbSearchWrapper>(response)
}

export async function getSeriesByTMDbId(TMDbId: string): Promise<TMDbShowDetails> {
  const response = await apiClient.GET('/api/v1/tmdb/series/{TMDbId}', {
    params: { path: { TMDbId } },
  })
  return handleResponse<TMDbShowDetails>(response)
}

export async function searchTMDb(filters: SearchFilters): Promise<TMDbSearchGroupWrapper> {
  const queryParams = buildSearchQueryParams(filters)
  const response = await apiClient.GET('/api/v1/tmdb/search', {
    params: { query: queryParams as any },
  })
  return handleResponse<TMDbSearchGroupWrapper>(response)
}

export async function testTMDbConnection(): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/tmdb/test')
  return handleResponse<GenericResponse>(response)
}
