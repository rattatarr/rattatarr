/**
 * Centralized API Type Definitions
 *
 * This file extracts types from the OpenAPI schema (schema.d.ts) and provides
 * easy-to-use, reusable type exports for the entire application.
 *
 * Usage:
 *   import type { Profile, MovieResponse, RateRequest } from '@/schemas/types/api'
 */

import type { paths, components } from '@/schemas/schema'

// =============================================================================
// Component Schemas (DTOs from backend)
// =============================================================================

/**
 * Extract all schemas from components
 */
type Schemas = components['schemas']

// --- Settings ---
export type Setting = Schemas['SettingResponseDTO']
export type SettingsWrapper = Schemas['SettingsWrapper']
export type SettingWrapper = Schemas['SettingWrapper']
export type CreateSettingRequest = Schemas['CreateSettingRequestDTO']
export type CreateSettingsRequest = Schemas['CreateSettingsRequestDTO']

// --- Profiles ---
export type Profile = Schemas['ProfileResponseDTO']
export type ProfileWrapper = Schemas['ProfileWrapper']
export type CreateProfileRequest = Schemas['CreateProfileRequestDTO']
export type ProfilesWrapper = Schemas['ProfilesWrapper']
export type ProfilesFilters = Schemas['ProfilesFiltersDTO']

// --- Movies ---
export type Movie = Schemas['MovieResponseDTO']
export type MoviesWrapper = Schemas['MoviesResponseWrapper']
export type MovieFilters = Schemas['MoviesFiltersDTO']

// --- Series/Shows ---
export type Show = Schemas['ShowResponseDTO']
export type Season = Schemas['SeasonResponseDTO']
export type Episode = Schemas['EpisodeResponseDTO']
export type SeriesWrapper = Schemas['SeriesResponseWrapper']
export type SeriesFilters = Schemas['SeriesFiltersDTO']

// --- Ratings ---
export type RateRequest = Schemas['RateRequestDTO']
export type DeleteRateRequest = Schemas['DeleteRateRequestDTO']
export type RatingMediaType = 'MEDIA_ITEM' | 'MEDIA_SEASON'

// --- Media Metadata ---
export type MediaItemMetadata = Schemas['MediaItemMetadataResponseDTO']
export type MediaSeasonMetadata = Schemas['MediaSeasonMetadataResponseDTO']
export type MediaType = 'MOVIE' | 'SERIES'

// --- Broken Media Items ---
export type BrokenMediaItem = Schemas['BrokenMediaItemResponseDTO']
export type BrokenMediaItemWrapper = Schemas['BrokenMediaItemResponseWrapper']
export type ResolveBrokenMediaItemRequest = Schemas['ResolveBrokenMediaItemRequestDTO']

// --- Genres ---
export type Genre = Schemas['GenreResponseDTO']
export type GenresWrapper = Schemas['GenresWrapper']
export type GenresFilters = Schemas['GenresFiltersDTO']

// --- Cast & Crew ---
export type CastMember = Schemas['CastMemberResponseDTO']
export type CrewMember = Schemas['CrewMemberResponseDTO']
export type Person = Schemas['PersonResponseDTO']

// --- People ---
export type PeopleFilters = Schemas['PeopleFiltersDTO']
export type PeopleWrapper = Schemas['PeopleWrapper']

// --- TMDB ---
export type TMDbSearchResult = Schemas['SearchTMDbResponseDTO']
export type TMDbSearchWrapper = Schemas['SearchTMDbWrapper']
export type TMDbSearchGroupWrapper = Schemas['SearchGroupTMDbWrapper']
export type SearchFilters = Schemas['SearchFiltersDTO']
export type ImportMediaItemRequest = Schemas['ImportMediaItemRequestDTO']
export type ImportMediaItemResponse = Schemas['ImportMediaItemResponseDTO']
export type TMDbMovieDetails = Schemas['TMDbMovieFullDetailsResponseDTO']
export type TMDbShowDetails = Schemas['TMDbShowFullDetailsResponseDTO']
export type TMDbCredits = Schemas['TMDbCreditsResponseDTO']
export type TMDbCreditCastMember = Schemas['TMDbCreditCastMemberResponseDTO']
export type TMDbCreditCrewMember = Schemas['TMDbCreditCrewMemberResponseDTO']
export type TMDbGenre = Schemas['TMDbGenreResponseDTO']
export type TMDbSeason = Schemas['TMDbSeasonResponseDTO']
export type TMDbSeasonWithEpisodes = Schemas['TMDbSeasonWithEpisodesResponseDTO']
export type TMDbEpisode = Schemas['TMDbEpisodeResponseDTO']

// --- Generic Responses ---
export type GenericResponse = Schemas['GenericResponseDTO']
export type PaginationMetadata = Schemas['PaginationMetadata']
export type Pageable = Schemas['Pageable']

// --- Logs ---
export type LogEvent = Schemas['LogEvent']
export type LogsResponseWrapper = Schemas['LogsResponseWrapper']

// --- Statistics ---
export type ProfileStatisticsRequest = Schemas['ProfileStatisticsRequestDTO']
export type ProfileStatisticsWrapper = Schemas['ProfileStatisticsWrapper']
export type ProfileStatistics = Schemas['ProfileStatisticsResponseDTO']
export type OverallStats = Schemas['OverallStatsDTO']
export type RatingDistribution = Schemas['RatingDistributionDTO']
export type MediaTypeBreakdown = Schemas['MediaTypeBreakdownDTO']
export type GenreStat = Schemas['GenreStatDTO']
export type PersonStat = Schemas['PersonStatDTO']
export type DecadeStat = Schemas['DecadeStatDTO']
export type RecentTrends = Schemas['RecentTrendsDTO']
export type RatingActivity = Schemas['RatingActivityDTO']
export type RuntimeStats = Schemas['RuntimeStatsDTO']
export type RatingConsistency = Schemas['RatingConsistencyDTO']
export type DayOfWeekActivity = Schemas['DayOfWeekActivityDTO']
export type RatingHeatmapYear = Schemas['RatingHeatmapYearDTO']
export type RatingHeatmapDay = Schemas['RatingHeatmapDayDTO']
export type GenreOverTimeYear = Schemas['GenreOverTimeYearDTO']
export type GenreYearStat = Schemas['GenreYearStatDTO']

// --- Agent ---
export type AgentChatRequest = Schemas['AgentChatRequestDTO']
export type AgentChatResponse = Schemas['AgentChatResponseDTO']
export type AgentSuggestionsResponse = Schemas['AgentSuggestionsResponseDTO']
export type AgentPromptResponse = Schemas['AgentPromptResponseDTO']
export type AgentConversationMessage = Schemas['AgentConversationMessageResponseDTO']
export type AgentConversationWrapper = Schemas['AgentConversationWrapper']

// =============================================================================
// Common Type Utilities
// =============================================================================

/**
 * Helper type to extract query parameters from a path
 */
export type QueryParams<
  Path extends keyof paths,
  Method extends keyof paths[Path],
> = paths[Path][Method] extends { parameters: { query: infer Q } } ? Q : never

/**
 * Helper type to extract path parameters from a path
 */
export type PathParams<
  Path extends keyof paths,
  Method extends keyof paths[Path],
> = paths[Path][Method] extends { parameters: { path: infer P } } ? P : never

/**
 * Helper type to extract request body from a path
 */
export type RequestBody<
  Path extends keyof paths,
  Method extends keyof paths[Path],
> = paths[Path][Method] extends {
  requestBody: { content: { 'application/json': infer B } }
}
  ? B
  : never

/**
 * Helper type to extract response body (200 OK) from a path
 */
export type ResponseBody<
  Path extends keyof paths,
  Method extends keyof paths[Path],
> = paths[Path][Method] extends {
  responses: { 200: { content: { '*/*': infer R } } }
}
  ? R
  : never

// =============================================================================
// Specific API Type Aliases (for convenience)
// =============================================================================

// Note: Some types like Pageable and ProfilesFilters are already exported
// from the schemas above, so we don't need to redefine them here

// =============================================================================
// Type Guards & Helpers
// =============================================================================

/**
 * Media item discriminated union (Movie or Show)
 */
export type MediaItem = Movie | Show

/**
 * Check if a media item is a movie
 */
export function isMovie(item: MediaItem): item is Movie {
  // Check based on properties that only movies have
  return 'mediaType' in item && item.mediaType === 'MOVIE'
}

/**
 * Check if a media item is a show/series
 */
export function isShow(item: MediaItem): item is Show {
  // Check based on properties that only shows have
  return 'mediaType' in item && item.mediaType === 'SERIES'
}
