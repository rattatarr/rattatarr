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
