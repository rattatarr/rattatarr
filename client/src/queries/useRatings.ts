import { useMutation, useQueryClient } from '@tanstack/vue-query'
import * as ratingsApi from '@/api/ratings'
import type { ExportRatingsCsvResponse } from '@/api/ratings'
import { jobKeys, movieKeys, seriesKeys } from './queryKeys'
import type {
  BackgroundJob,
  RateRequest,
  GenericResponse,
  ReviewRequest,
  DeleteReviewRequest,
} from '@/types'

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
 * Mutation hook to upsert a review for a movie, series, or season
 */
export function useSetReview() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, ReviewRequest>({
    mutationFn: (request: ReviewRequest) => ratingsApi.setReview(request),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: movieKeys.all }),
        queryClient.invalidateQueries({ queryKey: seriesKeys.all }),
      ])
    },
  })
}

/**
 * Mutation hook to delete a review for a movie, series, or season
 */
export function useDeleteReview() {
  const queryClient = useQueryClient()

  return useMutation<GenericResponse, Error, DeleteReviewRequest>({
    mutationFn: (request: DeleteReviewRequest) => ratingsApi.deleteReview(request),
    onSuccess: async () => {
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

  return useMutation<BackgroundJob, Error, { file: File; profileId: string }>({
    mutationFn: ({ file, profileId }) => ratingsApi.importIMDbRatings(file, profileId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: movieKeys.all }),
        queryClient.invalidateQueries({ queryKey: seriesKeys.all }),
        queryClient.invalidateQueries({ queryKey: jobKeys.all }),
      ])
    },
  })
}

/**
 * Mutation hook to export profile ratings as CSV
 */
export function useExportRatingsCsv() {
  return useMutation<ExportRatingsCsvResponse, Error, string>({
    mutationFn: (profileId: string) => ratingsApi.exportRatingsCsv(profileId),
  })
}
