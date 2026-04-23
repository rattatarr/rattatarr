import { useMutation } from '@tanstack/vue-query'
import { importRadarrMovies, refreshRadarrRatings } from '@/api/radarr'
import type { BackgroundJob } from '@/types'

export function useImportRadarrMovies() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: importRadarrMovies,
  })
}

export function useRefreshRadarrRatings() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: refreshRadarrRatings,
  })
}
