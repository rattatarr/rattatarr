/**
 * Centralized Query Key Factory
 *
 * Provides stable, type-safe query keys for TanStack Query cache management.
 * Use these factories to ensure consistent cache keys across the application.
 */

import type {
  Pageable,
  ProfilesFilters,
  MovieFilters,
  SeriesFilters,
  GenresFilters,
  PeopleFilters,
  SearchFilters,
  WatchActivityFilters,
  JobsFilters,
} from '@/types'

/**
 * Query key factory for profiles
 */
export const profileKeys = {
  all: ['profiles'] as const,
  lists: () => [...profileKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters: ProfilesFilters) =>
    [...profileKeys.lists(), { pageable, filters }] as const,
  details: () => [...profileKeys.all, 'detail'] as const,
  detail: (id: string) => [...profileKeys.details(), id] as const,
}

/**
 * Query key factory for library movies
 */
export const movieKeys = {
  all: ['movies'] as const,
  lists: () => [...movieKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters: MovieFilters) =>
    [...movieKeys.lists(), { pageable, filters }] as const,
  watchedUnrated: () => [...movieKeys.all, 'watched-unrated'] as const,
  watchedUnratedList: (pageable: Pageable, filters: MovieFilters) =>
    [...movieKeys.watchedUnrated(), { pageable, filters }] as const,
  broken: () => [...movieKeys.all, 'broken'] as const,
  brokenList: (pageable: Pageable) => [...movieKeys.broken(), { pageable }] as const,
  details: () => [...movieKeys.all, 'detail'] as const,
  detail: (id: string, profileId?: string) => [...movieKeys.details(), id, { profileId }] as const,
}

/**
 * Query key factory for library series
 */
export const seriesKeys = {
  all: ['series'] as const,
  lists: () => [...seriesKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters: SeriesFilters) =>
    [...seriesKeys.lists(), { pageable, filters }] as const,
  watchedUnrated: () => [...seriesKeys.all, 'watched-unrated'] as const,
  watchedUnratedList: (pageable: Pageable, filters: SeriesFilters) =>
    [...seriesKeys.watchedUnrated(), { pageable, filters }] as const,
  broken: () => [...seriesKeys.all, 'broken'] as const,
  brokenList: (pageable: Pageable) => [...seriesKeys.broken(), { pageable }] as const,
  details: () => [...seriesKeys.all, 'detail'] as const,
  detail: (id: string, profileId?: string, withEpisodes?: boolean) =>
    [...seriesKeys.details(), id, { profileId, withEpisodes }] as const,
}

/**
 * Query key factory for genres
 */
export const genreKeys = {
  all: ['genres'] as const,
  lists: () => [...genreKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters: GenresFilters) =>
    [...genreKeys.lists(), { pageable, filters }] as const,
}

/**
 * Query key factory for people
 */
export const peopleKeys = {
  all: ['people'] as const,
  lists: () => [...peopleKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters: PeopleFilters) =>
    [...peopleKeys.lists(), { pageable, filters }] as const,
  details: () => [...peopleKeys.all, 'detail'] as const,
  detail: (id: string) => [...peopleKeys.details(), id] as const,
}

/**
 * Query key factory for TMDB integration
 */
export const tmdbKeys = {
  all: ['tmdb'] as const,
  test: () => [...tmdbKeys.all, 'test'] as const,
  movies: () => [...tmdbKeys.all, 'movies'] as const,
  movieSearch: (filters: SearchFilters) => [...tmdbKeys.movies(), 'search', filters] as const,
  movieDetail: (id: string) => [...tmdbKeys.movies(), 'detail', id] as const,
  series: () => [...tmdbKeys.all, 'series'] as const,
  seriesSearch: (filters: SearchFilters) => [...tmdbKeys.series(), 'search', filters] as const,
  seriesDetail: (id: string) => [...tmdbKeys.series(), 'detail', id] as const,
  search: (filters: SearchFilters) => [...tmdbKeys.all, 'search', filters] as const,
}

/**
 * Query key factory for settings
 */
export const settingsKeys = {
  all: ['settings'] as const,
  lists: () => [...settingsKeys.all, 'list'] as const,
  list: () => [...settingsKeys.lists()] as const,
  details: () => [...settingsKeys.all, 'detail'] as const,
  detail: (key: string) => [...settingsKeys.details(), key] as const,
}

/**
 * Query key factory for Jellyfin
 */
export const jellyfinKeys = {
  all: ['jellyfin'] as const,
  test: () => [...jellyfinKeys.all, 'test'] as const,
}

/**
 * Query key factory for Radarr
 */
export const radarrKeys = {
  all: ['radarr'] as const,
  test: () => [...radarrKeys.all, 'test'] as const,
}

/**
 * Query key factory for Sonarr
 */
export const sonarrKeys = {
  all: ['sonarr'] as const,
  test: () => [...sonarrKeys.all, 'test'] as const,
}

/**
 * Query key factory for logs
 */
export const logKeys = {
  all: ['logs'] as const,
  // Historical logs (REST endpoint with pagination)
  lists: () => [...logKeys.all, 'list'] as const,
  list: (
    pageable: Pageable,
    filters?: { level?: string; logger?: string; startDate?: string; endDate?: string },
  ) => [...logKeys.lists(), { pageable, filters }] as const,
  // Infinite logs (REST endpoint with infinite scroll)
  infinites: () => [...logKeys.all, 'infinite'] as const,
  infinite: (
    filters?: { level?: string; logger?: string; startDate?: string; endDate?: string },
    pageSize?: number,
  ) => [...logKeys.infinites(), { filters, pageSize }] as const,
}

/**
 * Query key factory for unified search
 */
export const searchKeys = {
  all: ['search'] as const,
  combined: (query: string) => [...searchKeys.all, 'combined', query] as const,
  byId: (id: string) => [...searchKeys.all, 'byId', id] as const,
}

/**
 * Query key factory for AI agent
 */
export const agentKeys = {
  all: ['agent'] as const,
  suggestions: () => [...agentKeys.all, 'suggestions'] as const,
  prompts: () => [...agentKeys.all, 'prompt'] as const,
  prompt: (profileId: string, agent?: string) =>
    [...agentKeys.prompts(), profileId, { agent }] as const,
  conversations: () => [...agentKeys.all, 'conversation'] as const,
  conversation: (profileId: string, agent?: string) =>
    [...agentKeys.conversations(), profileId, { agent }] as const,
}

/**
 * Query key factory for profile statistics
 */
export const statisticsKeys = {
  all: ['statistics'] as const,
  detail: (profileId: string) => [...statisticsKeys.all, profileId] as const,
}

/**
 * Query key factory for year rewind
 */
export const rewindKeys = {
  all: ['rewind'] as const,
  availableYears: (profileId: string) => [...rewindKeys.all, profileId, 'years'] as const,
  detail: (profileId: string, year: number) =>
    [...rewindKeys.all, profileId, 'year', year] as const,
}

/**
 * Query key factory for background jobs
 */
export const jobKeys = {
  all: ['jobs'] as const,
  lists: () => [...jobKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters?: JobsFilters) =>
    [...jobKeys.lists(), { pageable, filters }] as const,
  details: () => [...jobKeys.all, 'detail'] as const,
  detail: (id: string) => [...jobKeys.details(), id] as const,
}

/**
 * Query key factory for watch activity
 */
export const watchActivityKeys = {
  all: ['watch-activity'] as const,
  lists: () => [...watchActivityKeys.all, 'list'] as const,
  list: (pageable: Pageable, filters: WatchActivityFilters) =>
    [...watchActivityKeys.lists(), { pageable, filters }] as const,
}

/**
 * Utility to invalidate all queries for a specific domain
 */
export const invalidateAll = {
  profiles: profileKeys.all,
  movies: movieKeys.all,
  series: seriesKeys.all,
  genres: genreKeys.all,
  people: peopleKeys.all,
  tmdb: tmdbKeys.all,
  settings: settingsKeys.all,
  jellyfin: jellyfinKeys.all,
  logs: logKeys.all,
  search: searchKeys.all,
  statistics: statisticsKeys.all,
  jobs: jobKeys.all,
}
