/**
 * Utility functions for building query parameters that match backend expectations
 *
 * The backend expects flat query parameters, not nested structures:
 * - Correct: ?page=1&size=20&sort=rating,desc&genres=action,comedy
 * - Wrong: ?pageable[page]=1&filters[genres][]=action
 */

import type { JobsFilters } from '@/api/jobs'
import type {
  Pageable,
  MovieFilters,
  SeriesFilters,
  GenresFilters,
  ProfilesFilters,
  PeopleFilters,
  ProfileStatisticsRequest,
  SearchFilters,
} from '@/types'
import type { LogsFilters } from '@/api/logs.ts'

/**
 * Build flat query parameters for movies endpoint
 */
export function buildMoviesQueryParams(pageable: Pageable, filters: MovieFilters) {
  return {
    // Pageable fields (flat at top level)
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params

    // Filter fields (flat at top level)
    id: filters.id,
    title: filters.title || undefined, // Exclude empty strings
    releasedAfter: filters.releasedAfter,
    releasedBefore: filters.releasedBefore,
    genres: filters.genres && filters.genres.length > 0 ? filters.genres.join(',') : undefined, // Comma-separated
    posterSize: filters.posterSize,
    backdropSize: filters.backdropSize,
    profileSize: filters.profileSize,
    profileId: filters.profileId,
    ratingMin: filters.ratingMin,
    ratingMax: filters.ratingMax,
    personId: filters.personId,
    unrated: filters.unrated,
  }
}

/**
 * Build flat query parameters for series endpoint
 */
export function buildSeriesQueryParams(pageable: Pageable, filters: SeriesFilters) {
  return {
    // Pageable fields (flat at top level)
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params

    // Filter fields (flat at top level)
    id: filters.id,
    title: filters.title || undefined, // Exclude empty strings
    releasedAfter: filters.releasedAfter,
    releasedBefore: filters.releasedBefore,
    genres: filters.genres && filters.genres.length > 0 ? filters.genres.join(',') : undefined, // Comma-separated
    withSeasons: filters.withSeasons,
    withEpisodes: filters.withEpisodes,
    posterSize: filters.posterSize,
    backdropSize: filters.backdropSize,
    profileSize: filters.profileSize,
    profileId: filters.profileId,
    ratingMin: filters.ratingMin,
    ratingMax: filters.ratingMax,
    personId: filters.personId,
    unrated: filters.unrated,
  }
}

/**
 * Build flat query parameters for genres endpoint
 */
export function buildGenresQueryParams(pageable: Pageable, filters: GenresFilters) {
  return {
    // Pageable fields (flat at top level)
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params

    // Filter fields (flat at top level)
    name: filters.name || undefined,
  }
}

/**
 * Build flat query parameters for profiles endpoint
 */
export function buildProfilesQueryParams(pageable: Pageable, filters: ProfilesFilters) {
  return {
    // Pageable fields (flat at top level)
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params

    // Filter fields (flat at top level)
    name: filters.name,
    createdAtAfter: filters.createdAtAfter,
    createdAtBefore: filters.createdAtBefore,
    updatedAtAfter: filters.updatedAtAfter,
    updatedAtBefore: filters.updatedAtBefore,
  }
}

/**
 * Build flat query parameters for broken media endpoints (pageable only, no filters)
 */
export function buildPageableQueryParams(pageable: Pageable) {
  return {
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params
  }
}

/**
 * Build flat query parameters for TMDB search endpoints
 */
export function buildSearchQueryParams(filters: SearchFilters) {
  return {
    query: filters.query,
    posterSize: filters.posterSize,
    page: filters.page,
  }
}

/**
 * Build flat query parameters for profile statistics endpoint
 */
export function buildStatisticsQueryParams(filters: ProfileStatisticsRequest) {
  return {
    profileId: filters.profileId,
    ratingThreshold: filters.ratingThreshold,
    minCount: filters.minCount,
    genresLimit: filters.genresLimit,
    actorsLimit: filters.actorsLimit,
    directorsLimit: filters.directorsLimit,
    producersLimit: filters.producersLimit,
    profileImageSize: filters.profileImageSize,
  }
}

/**
 * Build flat query parameters for logs endpoint
 */
export function buildLogsQueryParams(pageable: Pageable, filters?: LogsFilters) {
  return {
    // Pageable fields (flat at top level)
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params

    // Filter fields (flat at top level)
    level: filters?.level,
    startDate: filters?.startDate,
    endDate: filters?.endDate,
    logger: filters?.logger,
  }
}

/**
 * Build flat query parameters for people endpoint
 */
export function buildPeopleQueryParams(pageable: Pageable, filters: PeopleFilters) {
  return {
    // Pageable fields (flat at top level)
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort, // Keep as array - openapi-fetch will make multiple params

    // Filter fields (flat at top level)
    name: filters.name || undefined,
  }
}

export function buildJobsQueryParams(pageable: Pageable, filters?: JobsFilters) {
  return {
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
    type: filters?.type,
    status: filters?.status,
    startDate: filters?.startDate,
    endDate: filters?.endDate,
  }
}
