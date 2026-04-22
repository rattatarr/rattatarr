import { useQueryClient } from '@tanstack/vue-query'
import { getMovieById, getSeriesById } from '@/api/library'
import { getMovieByTMDbId, getSeriesByTMDbId } from '@/api/tmdb'
import { movieKeys, seriesKeys, tmdbKeys } from '@/queries/queryKeys'
import { useProfileStore } from '@/stores/profileStore'
import { computed } from 'vue'
import { MediaSource, MediaType, type SearchResultSource } from '@/utils/enums'
import type { MoviesWrapper, SeriesWrapper } from '@/schemas/types/api'

export interface CachedRatings {
  imdbRating?: number
  rottenTomatoesRating?: number
}

export function useDataPreload() {
  const queryClient = useQueryClient()
  const profileStore = useProfileStore()

  const selectedProfileId = computed(() => profileStore.selectedProfileId ?? undefined)

  /**
   * Prefetch a movie's details
   */
  const prefetchMovie = async (movieId: string) => {
    if (!movieId) return

    try {
      await queryClient.prefetchQuery({
        queryKey: movieKeys.detail(movieId, selectedProfileId.value),
        queryFn: () =>
          getMovieById(movieId, {
            profileId: selectedProfileId.value,
            posterSize: 'w500',
            backdropSize: 'original',
            profileSize: 'w185',
          }),
        staleTime: 5 * 60 * 1000, // 5 minutes
      })
    } catch (error) {
      // Silently fail - prefetching is an optimization
      console.debug(`Failed to prefetch movie: ${movieId}`, error)
    }
  }

  /**
   * Prefetch a series' details
   */
  const prefetchSeries = async (seriesId: string) => {
    if (!seriesId) return

    try {
      await queryClient.prefetchQuery({
        queryKey: seriesKeys.detail(seriesId, selectedProfileId.value, true),
        queryFn: () =>
          getSeriesById(seriesId, {
            profileId: selectedProfileId.value,
            withEpisodes: true,
            posterSize: 'w500',
            backdropSize: 'original',
            profileSize: 'w185',
          }),
        staleTime: 5 * 60 * 1000, // 5 minutes
      })
    } catch (error) {
      // Silently fail - prefetching is an optimization
      console.debug(`Failed to prefetch series: ${seriesId}`, error)
    }
  }

  /**
   * Prefetch TMDb movie details
   */
  const prefetchTMDbMovie = async (tmdbId: string) => {
    if (!tmdbId) return

    try {
      await queryClient.prefetchQuery({
        queryKey: tmdbKeys.movieDetail(tmdbId),
        queryFn: () => getMovieByTMDbId(tmdbId),
        staleTime: 5 * 60 * 1000, // 5 minutes
      })
    } catch (error) {
      // Silently fail - prefetching is an optimization
      console.debug(`Failed to prefetch TMDb movie: ${tmdbId}`, error)
    }
  }

  /**
   * Prefetch TMDb series details
   */
  const prefetchTMDbSeries = async (tmdbId: string) => {
    if (!tmdbId) return

    try {
      await queryClient.prefetchQuery({
        queryKey: tmdbKeys.seriesDetail(tmdbId),
        queryFn: () => getSeriesByTMDbId(tmdbId),
        staleTime: 5 * 60 * 1000, // 5 minutes
      })
    } catch (error) {
      // Silently fail - prefetching is an optimization
      console.debug(`Failed to prefetch TMDb series: ${tmdbId}`, error)
    }
  }

  /**
   * Prefetch media item (movie or series) from appropriate source
   */
  const prefetchMediaItem = async (
    id: string,
    type: MediaType.MOVIE | MediaType.SERIES,
    source?: SearchResultSource,
  ) => {
    // Route to appropriate API based on source
    if (source === MediaSource.TMDB || source === MediaSource.IMDB) {
      // External sources - use TMDb API
      if (type === MediaType.MOVIE) {
        await prefetchTMDbMovie(id)
      } else {
        await prefetchTMDbSeries(id)
      }
    } else {
      // Internal/Jellyfin sources - use library API
      if (type === MediaType.MOVIE) {
        await prefetchMovie(id)
      } else {
        await prefetchSeries(id)
      }
    }
  }

  /**
   * Check if data is already cached
   */
  const isDataCached = (
    id: string,
    type: MediaType.MOVIE | MediaType.SERIES,
    source?: SearchResultSource,
  ): boolean => {
    let queryKey: readonly unknown[]

    if (source === MediaSource.TMDB || source === MediaSource.IMDB) {
      // Check TMDb cache
      queryKey = type === MediaType.MOVIE ? tmdbKeys.movieDetail(id) : tmdbKeys.seriesDetail(id)
    } else {
      // Check internal library cache
      queryKey =
        type === MediaType.MOVIE
          ? movieKeys.detail(id, selectedProfileId.value)
          : seriesKeys.detail(id, selectedProfileId.value, true)
    }

    return queryClient.getQueryData(queryKey) !== undefined
  }

  const getCachedRatings = (
    id: string,
    type: MediaType.MOVIE | MediaType.SERIES,
    source?: SearchResultSource,
  ): CachedRatings | undefined => {
    if (source === MediaSource.TMDB || source === MediaSource.IMDB) return undefined

    if (type === MediaType.MOVIE) {
      const data = queryClient.getQueryData<MoviesWrapper>(
        movieKeys.detail(id, selectedProfileId.value),
      )
      const movie = data?.movies?.[0]
      if (!movie) return undefined
      return {
        imdbRating: movie.metadata?.imdbRating ?? undefined,
        rottenTomatoesRating: movie.metadata?.rottenTomatoesRating ?? undefined,
      }
    } else {
      const data = queryClient.getQueryData<SeriesWrapper>(
        seriesKeys.detail(id, selectedProfileId.value, true),
      )
      const show = data?.series?.[0]
      if (!show) return undefined
      return {
        imdbRating: show.metadata?.imdbRating ?? undefined,
        rottenTomatoesRating: show.metadata?.rottenTomatoesRating ?? undefined,
      }
    }
  }

  return {
    prefetchMovie,
    prefetchSeries,
    prefetchTMDbMovie,
    prefetchTMDbSeries,
    prefetchMediaItem,
    isDataCached,
    getCachedRatings,
  }
}
