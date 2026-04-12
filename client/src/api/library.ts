import { apiClient, handleResponse } from './client'
import type {
  Pageable,
  MovieFilters,
  SeriesFilters,
  GenresFilters,
  MoviesWrapper,
  SeriesWrapper,
  BrokenMediaItemWrapper,
  GenresWrapper,
  GenericResponse,
} from '@/types'
import {
  buildMoviesQueryParams,
  buildSeriesQueryParams,
  buildGenresQueryParams,
  buildPageableQueryParams,
} from './queryParams'

export async function getAllMovies(
  pageable: Pageable,
  filters: MovieFilters,
): Promise<MoviesWrapper> {
  const queryParams = buildMoviesQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/library/movies', {
    params: { query: queryParams as any },
  })
  return handleResponse<MoviesWrapper>(response)
}

/**
 * Get a single movie by ID
 */
export interface GetMovieByIdOptions {
  profileId?: string
  posterSize?: string
  backdropSize?: string
  profileSize?: string
}

export async function getMovieById(
  id: string,
  options: GetMovieByIdOptions = {},
): Promise<MoviesWrapper> {
  const queryParams = {
    page: 0,
    size: 1,
    id,
    profileId: options.profileId,
    posterSize: options.posterSize || 'w500',
    backdropSize: options.backdropSize || 'w780',
    profileSize: options.profileSize || 'w185',
  }
  const response = await apiClient.GET('/api/v1/library/movies', {
    params: { query: queryParams as any },
  })
  return handleResponse<MoviesWrapper>(response)
}

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

export async function getAllSeries(
  pageable: Pageable,
  filters: SeriesFilters,
): Promise<SeriesWrapper> {
  const queryParams = buildSeriesQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/library/series', {
    params: { query: queryParams as any },
  })
  return handleResponse<SeriesWrapper>(response)
}

/**
 * Get a single series by ID
 */
export interface GetSeriesByIdOptions {
  profileId?: string
  withEpisodes?: boolean
  posterSize?: string
  backdropSize?: string
  profileSize?: string
}

export async function getSeriesById(
  id: string,
  options: GetSeriesByIdOptions = {},
): Promise<SeriesWrapper> {
  const queryParams = {
    page: 0,
    size: 1,
    id,
    profileId: options.profileId,
    withEpisodes: options.withEpisodes ?? true,
    posterSize: options.posterSize || 'w500',
    backdropSize: options.backdropSize || 'w780',
    profileSize: options.profileSize || 'w185',
  }
  const response = await apiClient.GET('/api/v1/library/series', {
    params: { query: queryParams as any },
  })
  return handleResponse<SeriesWrapper>(response)
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

export async function refreshAllStaleSeries(): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/library/series/refresh')
  return handleResponse<GenericResponse>(response)
}

export async function refreshSeriesById(id: string): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/library/series/{id}/refresh', {
    params: { path: { id } },
  })
  return handleResponse<GenericResponse>(response)
}

export async function getAllGenres(
  pageable: Pageable,
  filters: GenresFilters,
): Promise<GenresWrapper> {
  const queryParams = buildGenresQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/library/genres', {
    params: { query: queryParams as any },
  })
  return handleResponse<GenresWrapper>(response)
}
