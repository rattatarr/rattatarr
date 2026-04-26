import { useQuery, useMutation } from '@tanstack/vue-query'
import { importRadarrMovies, refreshRadarrRatings, testRadarrConnection } from '@/api/radarr'
import { radarrKeys } from './queryKeys'
import type { BackgroundJob, GenericResponse } from '@/types'

export function useRadarrTest() {
  return useQuery<GenericResponse>({
    queryKey: radarrKeys.test(),
    queryFn: testRadarrConnection,
    enabled: false,
    retry: false,
  })
}

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
