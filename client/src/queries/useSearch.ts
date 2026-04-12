import { useQueries, useQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as libraryApi from '@/api/library'
import * as tmdbApi from '@/api/tmdb'
import { movieKeys, seriesKeys, tmdbKeys } from './queryKeys'
import { MediaSource as SearchSource, MediaType, type SearchResultSource } from '@/utils/enums'
import type {
  MoviesWrapper,
  SeriesWrapper,
  TMDbSearchWrapper,
  TMDbMovieDetails,
  TMDbShowDetails,
  Movie,
  Show,
  TMDbSearchResult,
} from '@/types'

// Re-export for convenience
export { SearchSource, type SearchResultSource }

/**
 * Combined search result with source metadata
 * Extensible for multiple external sources (TMDb, IMDb, etc.)
 */
export interface SearchResultItem {
  id: string | number
  title: string
  releaseDate?: string
  posterUrl?: string
  source: SearchResultSource
  mediaType: MediaType.MOVIE | MediaType.SERIES
  // Original item for navigation
  originalItem: Movie | Show | TMDbSearchResult
}

/**
 * Search results organized by media type
 */
export interface SearchResults {
  movies: SearchResultItem[]
  series: SearchResultItem[]
  isLoading: boolean
  hasError: boolean
}

/**
 * Detect if query is numeric (TMDB ID)
 */
function isNumericQuery(query: string): boolean {
  return /^\d+$/.test(query.trim())
}

/**
 * Convert internal Movie to SearchResultItem
 */
function movieToSearchResult(movie: Movie): SearchResultItem {
  const source = movie.jellyfinId ? SearchSource.JELLYFIN : SearchSource.INTERNAL
  return {
    id: movie.id ?? 'unknown',
    title: movie.title ?? 'Untitled',
    releaseDate: movie.productionYear?.toString(),
    posterUrl: movie.metadata?.posterImageUrl,
    source,
    mediaType: MediaType.MOVIE,
    originalItem: movie,
  }
}

/**
 * Convert internal Show to SearchResultItem
 */
function showToSearchResult(show: Show): SearchResultItem {
  const source = show.jellyfinId ? SearchSource.JELLYFIN : SearchSource.INTERNAL
  return {
    id: show.id ?? 'unknown',
    title: show.title ?? 'Untitled',
    releaseDate: show.productionYear?.toString(),
    posterUrl: show.metadata?.posterImageUrl,
    source,
    mediaType: MediaType.SERIES,
    originalItem: show,
  }
}

/**
 * Convert TMDB search result to SearchResultItem
 */
function tmdbToSearchResult(
  tmdb: TMDbSearchResult,
  mediaType: MediaType.MOVIE | MediaType.SERIES,
): SearchResultItem {
  return {
    id: tmdb.id ?? 0,
    title: tmdb.title ?? 'Untitled',
    releaseDate: tmdb.releaseDate,
    posterUrl: tmdb.posterPath,
    source: SearchSource.TMDB,
    mediaType,
    originalItem: tmdb,
  }
}

/**
 * Hook for unified search across internal library and TMDB
 *
 * Executes 4 parallel queries:
 * 1. Internal movies by name
 * 2. Internal series by name
 * 3. TMDB movies search
 * 4. TMDB series search
 *
 * Results are deduplicated (TMDB results filtered out if they exist in internal DB)
 * and organized by media type.
 */
export function useSearch(
  query: MaybeRefOrGetter<string>,
  profileId?: MaybeRefOrGetter<string | null | undefined>,
) {
  const queryValue = computed(() => toValue(query).trim())
  const profileIdValue = computed(() => toValue(profileId) ?? undefined)
  const isEnabled = computed(() => queryValue.value.length > 0)
  const isNumeric = computed(() => isNumericQuery(queryValue.value))

  // For text queries, search all 4 sources in parallel
  const queries = useQueries({
    queries: [
      // 1. Internal movies
      {
        queryKey: computed(() =>
          movieKeys.list(
            { page: 0, size: 20 },
            { title: queryValue.value, posterSize: 'w185', profileId: profileIdValue.value },
          ),
        ),
        queryFn: () =>
          libraryApi.getAllMovies(
            { page: 0, size: 20 },
            { title: queryValue.value, posterSize: 'w185', profileId: profileIdValue.value },
          ),
        enabled: computed(() => isEnabled.value && !isNumeric.value),
      },
      // 2. Internal series
      {
        queryKey: computed(() =>
          seriesKeys.list(
            { page: 0, size: 20 },
            { title: queryValue.value, posterSize: 'w400', profileId: profileIdValue.value },
          ),
        ),
        queryFn: () =>
          libraryApi.getAllSeries(
            { page: 0, size: 20 },
            { title: queryValue.value, posterSize: 'w400', profileId: profileIdValue.value },
          ),
        enabled: computed(() => isEnabled.value && !isNumeric.value),
      },
      // 3. TMDB movies
      {
        queryKey: computed(() =>
          tmdbKeys.movieSearch({ query: queryValue.value, posterSize: 'w185' }),
        ),
        queryFn: () => tmdbApi.searchMovies({ query: queryValue.value, posterSize: 'w185' }),
        enabled: computed(() => isEnabled.value && !isNumeric.value),
      },
      // 4. TMDB series
      {
        queryKey: computed(() =>
          tmdbKeys.seriesSearch({ query: queryValue.value, posterSize: 'w400' }),
        ),
        queryFn: () => tmdbApi.searchSeries({ query: queryValue.value, posterSize: 'w400' }),
        enabled: computed(() => isEnabled.value && !isNumeric.value),
      },
    ],
    combine: (results) => {
      const [internalMovies, internalSeries, tmdbMovies, tmdbSeries] = results

      const isLoading = results.some((r) => r.isLoading)
      const hasError = results.some((r) => r.isError)

      // Extract data with fallbacks
      const internalMoviesData = (internalMovies.data as MoviesWrapper)?.movies || []
      const internalSeriesData = (internalSeries.data as SeriesWrapper)?.series || []
      const tmdbMoviesData = (tmdbMovies.data as TMDbSearchWrapper)?.results || []
      const tmdbSeriesData = (tmdbSeries.data as TMDbSearchWrapper)?.results || []

      // Build set of TMDB IDs from internal results for deduplication
      const internalTMDbIds = new Set<string>()
      internalMoviesData.forEach((m) => {
        if (m.TMDbId) internalTMDbIds.add(m.TMDbId)
      })
      internalSeriesData.forEach((s) => {
        if (s.TMDbId) internalTMDbIds.add(s.TMDbId)
      })

      // Deduplicate TMDB results (TMDB id is number, internal TMDbId is string)
      // Limit to 20 results per source
      const dedupedTMDbMovies = tmdbMoviesData
        .filter((m) => m.id !== undefined && !internalTMDbIds.has(m.id.toString()))
        .slice(0, 20)
      const dedupedTMDbSeries = tmdbSeriesData
        .filter((s) => s.id !== undefined && !internalTMDbIds.has(s.id.toString()))
        .slice(0, 20)

      // Convert to unified format
      const movies: SearchResultItem[] = [
        ...internalMoviesData.map(movieToSearchResult),
        ...dedupedTMDbMovies.map((m) => tmdbToSearchResult(m, MediaType.MOVIE)),
      ]

      const series: SearchResultItem[] = [
        ...internalSeriesData.map(showToSearchResult),
        ...dedupedTMDbSeries.map((s) => tmdbToSearchResult(s, MediaType.SERIES)),
      ]

      return {
        movies,
        series,
        isLoading,
        hasError,
      }
    },
  })

  return queries
}

/**
 * Hook for searching TMDB by ID
 *
 * When a numeric query is detected, this hook fetches both movie and series
 * by TMDB ID to see which one exists.
 */
export function useSearchById(id: MaybeRefOrGetter<string>) {
  const idValue = computed(() => toValue(id).trim())
  const isEnabled = computed(() => isNumericQuery(idValue.value))

  const movieQuery = useQuery<TMDbMovieDetails | null>({
    queryKey: computed(() => tmdbKeys.movieDetail(idValue.value)),
    queryFn: async () => {
      try {
        return await tmdbApi.getMovieByTMDbId(idValue.value)
      } catch {
        return null
      }
    },
    enabled: isEnabled,
  })

  const seriesQuery = useQuery<TMDbShowDetails | null>({
    queryKey: computed(() => tmdbKeys.seriesDetail(idValue.value)),
    queryFn: async () => {
      try {
        return await tmdbApi.getSeriesByTMDbId(idValue.value)
      } catch {
        return null
      }
    },
    enabled: isEnabled,
  })

  const results = computed<SearchResults>(() => {
    const movies: SearchResultItem[] = []
    const series: SearchResultItem[] = []

    // Use .value to unwrap the Ref
    const movieData = movieQuery.data.value
    const seriesData = seriesQuery.data.value

    if (movieData) {
      movies.push({
        id: movieData.id ?? 0,
        title: movieData.title ?? '',
        releaseDate: movieData.release_date, // snake_case in TMDbMovieDetails
        posterUrl: movieData.poster_path, // snake_case in TMDbMovieDetails
        source: SearchSource.TMDB,
        mediaType: MediaType.MOVIE,
        originalItem: movieData as any,
      })
    }

    if (seriesData) {
      series.push({
        id: seriesData.id ?? 0,
        title: seriesData.name ?? '',
        releaseDate: seriesData.first_air_date, // snake_case in TMDbShowDetails
        posterUrl: seriesData.poster_path, // snake_case in TMDbShowDetails
        source: SearchSource.TMDB,
        mediaType: MediaType.SERIES,
        originalItem: seriesData as any,
      })
    }

    return {
      movies,
      series,
      isLoading: movieQuery.isLoading.value || seriesQuery.isLoading.value,
      hasError: movieQuery.isError.value || seriesQuery.isError.value,
    }
  })

  return results
}
