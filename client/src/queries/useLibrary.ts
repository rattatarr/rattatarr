import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as libraryApi from '@/api/library'
import { MediaType } from '@/utils/enums'
import { movieKeys, seriesKeys, genreKeys } from './queryKeys'
import type {
  Pageable,
  MovieFilters,
  SeriesFilters,
  GenresFilters,
  MoviesWrapper,
  SeriesWrapper,
  GenresWrapper,
  GenericResponse,
} from '@/types'

/**
 * Query hook to get all movies
 */
export function useMovies(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<MovieFilters>,
) {
  return useQuery<MoviesWrapper>({
    queryKey: computed(() => movieKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => libraryApi.getAllMovies(toValue(pageable), toValue(filters)),
  })
}

/**
 * Query hook to get recently watched unrated movies
 */
export function useRecentlyWatchedUnratedMovies(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<MovieFilters>,
) {
  return useQuery<MoviesWrapper>({
    queryKey: computed(() => movieKeys.watchedUnratedList(toValue(pageable), toValue(filters))),
    queryFn: () => libraryApi.getRecentlyWatchedUnratedMovies(toValue(pageable), toValue(filters)),
    enabled: computed(() => !!toValue(filters).profileId),
  })
}

export interface WatchedUnratedMutationVars {
  mediaItemId: string
  profileId: string
  mediaType: MediaType.MOVIE | MediaType.SERIES
}

/**
 * Mutation hook to dismiss an item from the "watched but not rated" list.
 */
export function useDismissWatchedUnrated() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, WatchedUnratedMutationVars>({
    mutationFn: ({ mediaItemId, profileId, mediaType }) =>
      mediaType === MediaType.MOVIE
        ? libraryApi.dismissWatchedUnratedMovie(mediaItemId, profileId)
        : libraryApi.dismissWatchedUnratedSeries(mediaItemId, profileId),
    onSettled: async (_data, _error, { mediaType }) => {
      const key =
        mediaType === MediaType.MOVIE ? movieKeys.watchedUnrated() : seriesKeys.watchedUnrated()
      await queryClient.invalidateQueries({ queryKey: key })
    },
  })
}

/**
 * Mutation hook to restore (undo dismiss) an item into the "watched but not rated" list.
 */
export function useRestoreWatchedUnrated() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, WatchedUnratedMutationVars>({
    mutationFn: ({ mediaItemId, profileId, mediaType }) =>
      mediaType === MediaType.MOVIE
        ? libraryApi.restoreWatchedUnratedMovie(mediaItemId, profileId)
        : libraryApi.restoreWatchedUnratedSeries(mediaItemId, profileId),
    onSettled: async (_data, _error, { mediaType }) => {
      const key =
        mediaType === MediaType.MOVIE ? movieKeys.watchedUnrated() : seriesKeys.watchedUnrated()
      await queryClient.invalidateQueries({ queryKey: key })
    },
  })
}

/**
 * Query hook to get a single movie by ID
 */
export function useMovie(
  id: MaybeRefOrGetter<string>,
  options?: MaybeRefOrGetter<libraryApi.GetMovieByIdOptions>,
) {
  return useQuery<MoviesWrapper>({
    queryKey: computed(() => {
      const opts = toValue(options) || {}
      return movieKeys.detail(toValue(id), opts.profileId)
    }),
    queryFn: () => libraryApi.getMovieById(toValue(id), toValue(options) || {}),
    enabled: computed(() => !!toValue(id)),
  })
}

/**
 * Query hook to get all series
 */
export function useSeries(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<SeriesFilters>,
) {
  return useQuery<SeriesWrapper>({
    queryKey: computed(() => seriesKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => libraryApi.getAllSeries(toValue(pageable), toValue(filters)),
  })
}

/**
 * Query hook to get recently watched unrated series
 */
export function useRecentlyWatchedUnratedSeries(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<SeriesFilters>,
) {
  return useQuery<SeriesWrapper>({
    queryKey: computed(() => seriesKeys.watchedUnratedList(toValue(pageable), toValue(filters))),
    queryFn: () => libraryApi.getRecentlyWatchedUnratedSeries(toValue(pageable), toValue(filters)),
    enabled: computed(() => !!toValue(filters).profileId),
  })
}

/**
 * Query hook to get a single series by ID
 */
export function useSeriesDetail(
  id: MaybeRefOrGetter<string>,
  options?: MaybeRefOrGetter<libraryApi.GetSeriesByIdOptions>,
) {
  return useQuery<SeriesWrapper>({
    queryKey: computed(() => {
      const opts = toValue(options) || {}
      return seriesKeys.detail(toValue(id), opts.profileId, opts.withEpisodes)
    }),
    queryFn: () => libraryApi.getSeriesById(toValue(id), toValue(options) || {}),
    enabled: computed(() => !!toValue(id)),
  })
}

/**
 * Query hook to get all genres
 */
export function useGenres(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<GenresFilters>,
) {
  return useQuery<GenresWrapper>({
    queryKey: computed(() => genreKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => libraryApi.getAllGenres(toValue(pageable), toValue(filters)),
  })
}

/**
 * Mutation hook to refresh all stale series metadata
 */
export function useRefreshAllStaleSeries() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, void>({
    mutationFn: () => libraryApi.refreshAllStaleSeries(),
    onSuccess: async () => {
      // Invalidate series queries to refetch updated data
      await queryClient.invalidateQueries({ queryKey: seriesKeys.all })
    },
  })
}

/**
 * Mutation hook to refresh a specific series by ID
 */
export function useRefreshSeries() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, string>({
    mutationFn: (id: string) => libraryApi.refreshSeriesById(id),
    onSuccess: async () => {
      // Invalidate series queries to refetch updated data
      await queryClient.invalidateQueries({ queryKey: seriesKeys.all })
    },
  })
}
