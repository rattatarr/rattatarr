/**
 * Usage in components:
 * ```typescript
 * import { useMovies, useRateMediaItem } from '@/queries'
 *
 * const { data, isLoading, error } = useMovies(pageable, filters)
 * const rateMutation = useRateMediaItem()
 * ```
 */

// Query Keys
export * from './queryKeys'

// Profile Queries
export * from './useProfiles'

// Library Queries
export * from './useLibrary'
export * from './useInfiniteLibrary'

// People Queries
export * from './usePeople'

// Settings Queries
export * from './useSettings'

// Ratings Mutations
export * from './useRatings'

// TMDB Queries
export * from './useTMDb'

// Jellyfin Queries
export * from './useJellyfin'

// Logs Queries
export * from './useLogs'

// Broken Media Queries
export * from './useBrokenMedia'

// Statistics Queries
export * from './useStatistics'
