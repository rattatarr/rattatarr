import { useInfiniteQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as libraryApi from '@/api/library'
import { movieKeys, seriesKeys } from './queryKeys'
import type { MovieFilters, SeriesFilters, MoviesWrapper, SeriesWrapper } from '@/types'

/**
 * Infinite query hook for movies with automatic pagination
 */
export function useInfiniteMovies(
  filters: MaybeRefOrGetter<MovieFilters>,
  pageSize: MaybeRefOrGetter<number> = 10,
  sortArray: MaybeRefOrGetter<string[]> = [],
) {
  return useInfiniteQuery({
    queryKey: computed(() =>
      movieKeys.list(
        { page: 0, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      ),
    ),
    queryFn: async ({ pageParam = 0 }) => {
      return libraryApi.getAllMovies(
        { page: pageParam, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      )
    },
    getNextPageParam: (lastPage: MoviesWrapper) => {
      const pagination = lastPage.pagination
      if (!pagination || pagination.isLast || pagination.currentPage === undefined) {
        return undefined // No more pages
      }
      return pagination.currentPage + 1
    },
    initialPageParam: 0,
  })
}

/**
 * Infinite query hook for "watched but not rated" movies.
 * Disabled until `enabled` is true (e.g. user toggles "View all").
 */
export function useInfiniteWatchedUnratedMovies(
  filters: MaybeRefOrGetter<MovieFilters>,
  pageSize: MaybeRefOrGetter<number> = 24,
  sortArray: MaybeRefOrGetter<string[]> = [],
  enabled: MaybeRefOrGetter<boolean> = true,
) {
  return useInfiniteQuery({
    queryKey: computed(() =>
      movieKeys.watchedUnratedList(
        { page: 0, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      ),
    ),
    queryFn: async ({ pageParam = 0 }) => {
      return libraryApi.getRecentlyWatchedUnratedMovies(
        { page: pageParam, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      )
    },
    getNextPageParam: (lastPage: MoviesWrapper) => {
      const pagination = lastPage.pagination
      if (!pagination || pagination.isLast || pagination.currentPage === undefined) {
        return undefined // No more pages
      }
      return pagination.currentPage + 1
    },
    initialPageParam: 0,
    enabled: computed(() => toValue(enabled) && !!toValue(filters).profileId),
  })
}

/**
 * Infinite query hook for series with automatic pagination
 */
export function useInfiniteSeries(
  filters: MaybeRefOrGetter<SeriesFilters>,
  pageSize: MaybeRefOrGetter<number> = 10,
  sortArray: MaybeRefOrGetter<string[]> = [],
) {
  return useInfiniteQuery({
    queryKey: computed(() =>
      seriesKeys.list(
        { page: 0, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      ),
    ),
    queryFn: async ({ pageParam = 0 }) => {
      return libraryApi.getAllSeries(
        { page: pageParam, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      )
    },
    getNextPageParam: (lastPage: SeriesWrapper) => {
      const pagination = lastPage.pagination
      if (!pagination || pagination.isLast || pagination.currentPage === undefined) {
        return undefined // No more pages
      }
      return pagination.currentPage + 1
    },
    initialPageParam: 0,
  })
}

/**
 * Infinite query hook for "watched but not rated" series.
 * Disabled until `enabled` is true (e.g. user toggles "View all").
 */
export function useInfiniteWatchedUnratedSeries(
  filters: MaybeRefOrGetter<SeriesFilters>,
  pageSize: MaybeRefOrGetter<number> = 24,
  sortArray: MaybeRefOrGetter<string[]> = [],
  enabled: MaybeRefOrGetter<boolean> = true,
) {
  return useInfiniteQuery({
    queryKey: computed(() =>
      seriesKeys.watchedUnratedList(
        { page: 0, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      ),
    ),
    queryFn: async ({ pageParam = 0 }) => {
      return libraryApi.getRecentlyWatchedUnratedSeries(
        { page: pageParam, size: toValue(pageSize), sort: toValue(sortArray) },
        toValue(filters),
      )
    },
    getNextPageParam: (lastPage: SeriesWrapper) => {
      const pagination = lastPage.pagination
      if (!pagination || pagination.isLast || pagination.currentPage === undefined) {
        return undefined // No more pages
      }
      return pagination.currentPage + 1
    },
    initialPageParam: 0,
    enabled: computed(() => toValue(enabled) && !!toValue(filters).profileId),
  })
}
