import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as tmdbApi from '@/api/tmdb'
import * as libraryApi from '@/api/library'
import { tmdbKeys, movieKeys, seriesKeys } from './queryKeys'
import type {
  SearchFilters,
  ImportMediaItemRequest,
  ImportMediaItemResponse,
  TMDbSearchWrapper,
  TMDbMovieDetails,
  TMDbShowDetails,
  TMDbSearchGroupWrapper,
  GenericResponse,
} from '@/types'

export function useTMDbTest() {
  return useQuery<GenericResponse>({
    queryKey: tmdbKeys.test(),
    queryFn: () => tmdbApi.testTMDbConnection(),
    // Don't run automatically, let user trigger
    enabled: false,
    // Don't retry failed connection tests
    retry: false,
  })
}

/**
 * Query hook to search movies on TMDB
 */
export function useTMDbMovieSearch(filters: MaybeRefOrGetter<SearchFilters>) {
  return useQuery<TMDbSearchWrapper>({
    queryKey: computed(() => tmdbKeys.movieSearch(toValue(filters))),
    queryFn: () => tmdbApi.searchMovies(toValue(filters)),
    enabled: computed(() => !!toValue(filters).query),
  })
}

/**
 * Query hook to get movie details from TMDB by ID
 */
export function useTMDbMovie(id: MaybeRefOrGetter<string>) {
  return useQuery<TMDbMovieDetails>({
    queryKey: computed(() => tmdbKeys.movieDetail(toValue(id))),
    queryFn: () => tmdbApi.getMovieByTMDbId(toValue(id)),
    enabled: computed(() => !!toValue(id)),
  })
}

/**
 * Query hook to search series on TMDB
 */
export function useTMDbSeriesSearch(filters: MaybeRefOrGetter<SearchFilters>) {
  return useQuery<TMDbSearchWrapper>({
    queryKey: computed(() => tmdbKeys.seriesSearch(toValue(filters))),
    queryFn: () => tmdbApi.searchSeries(toValue(filters)),
    enabled: computed(() => !!toValue(filters).query),
  })
}

/**
 * Query hook to get series details from TMDB by ID
 */
export function useTMDbSeries(id: MaybeRefOrGetter<string>) {
  return useQuery<TMDbShowDetails>({
    queryKey: computed(() => tmdbKeys.seriesDetail(toValue(id))),
    queryFn: () => tmdbApi.getSeriesByTMDbId(toValue(id)),
    enabled: computed(() => !!toValue(id)),
  })
}

/**
 * Query hook for generic TMDB search (movies and series)
 */
export function useTMDbSearch(filters: MaybeRefOrGetter<SearchFilters>) {
  return useQuery<TMDbSearchGroupWrapper>({
    queryKey: computed(() => tmdbKeys.search(toValue(filters))),
    queryFn: () => tmdbApi.searchTMDb(toValue(filters)),
    enabled: computed(() => !!toValue(filters).query),
  })
}

/**
 * Hook to import TMDb media item and prefetch the internal library data
 *
 * This handles the complete flow:
 * 1. Import the media item from TMDb
 * 2. Prefetch the full data from the library API
 * 3. Return the internal id for navigation
 */
export function useImportTMDbData(profileId: MaybeRefOrGetter<string | null>) {
  const queryClient = useQueryClient()

  return useMutation<string | null, Error, ImportMediaItemRequest>({
    mutationFn: async (request: ImportMediaItemRequest) => {
      const response: ImportMediaItemResponse = await tmdbApi.importTMDbData(request)

      if (!response.id) {
        return null
      }

      const internalId = response.id
      const profileIdValue = toValue(profileId) ?? undefined

      if (request.mediaType === 'MOVIE') {
        await queryClient.prefetchQuery({
          queryKey: movieKeys.detail(internalId, profileIdValue),
          queryFn: () =>
            libraryApi.getMovieById(internalId, {
              profileId: profileIdValue,
              posterSize: 'w500',
              backdropSize: 'original',
              profileSize: 'w185',
            }),
        })
      } else if (request.mediaType === 'SERIES') {
        await queryClient.prefetchQuery({
          queryKey: seriesKeys.detail(internalId, profileIdValue, true),
          queryFn: () =>
            libraryApi.getSeriesById(internalId, {
              profileId: profileIdValue,
              withEpisodes: true,
              posterSize: 'w500',
              backdropSize: 'original',
              profileSize: 'w185',
            }),
        })
      }

      return internalId
    },
  })
}

/**
 * Mutation hook to build/refresh credits for all media items
 */
export function useBuildCredits() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, boolean | undefined>({
    mutationFn: (forceRefresh?: boolean) => tmdbApi.buildCredits(forceRefresh ?? false),
    onSuccess: async () => {
      // Invalidate library queries to show updated credits
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: movieKeys.all }),
        queryClient.invalidateQueries({ queryKey: seriesKeys.all }),
      ])
    },
  })
}
