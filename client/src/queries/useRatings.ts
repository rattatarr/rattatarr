import { useMutation, useQueryClient } from '@tanstack/vue-query'
import * as ratingsApi from '@/api/ratings'
import { movieKeys, seriesKeys } from './queryKeys'
import type { RateRequest, GenericResponse } from '@/types'

/**
 * Mutation hook to rate a movie or series
 */
export function useRateMediaItem() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, RateRequest>({
    mutationFn: (request: RateRequest) => ratingsApi.rateMediaItem(request),
    onSuccess: async () => {
      // Invalidate library queries to show updated ratings
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: movieKeys.all }),
        queryClient.invalidateQueries({ queryKey: seriesKeys.all }),
      ])
    },
  })
}

/**
 * Mutation hook to import ratings from IMDb CSV file
 */
export function useImportIMDbRatings() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, { file: File; profileId: string }>({
    mutationFn: ({ file, profileId }) => ratingsApi.importIMDbRatings(file, profileId),
    onSuccess: async () => {
      // Invalidate all library queries as ratings have been imported
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: movieKeys.all }),
        queryClient.invalidateQueries({ queryKey: seriesKeys.all }),
      ])
    },
  })
}
