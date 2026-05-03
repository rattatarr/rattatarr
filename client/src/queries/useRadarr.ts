import { useQuery, useMutation } from '@tanstack/vue-query'
import { importRadarrMovies, refreshRadarrRatings, testRadarrConnection } from '@/api/radarr'
import { radarrKeys } from './queryKeys'
import type { BackgroundJob, GenericResponse } from '@/types'

export function useRadarrTest() {
  return useQuery<GenericResponse>({
    queryKey: radarrKeys.test(),
    queryFn: () => testRadarrConnection('DEFAULT'),
    enabled: false,
    retry: false,
  })
}

export function useRadarrAnimeTest() {
  return useQuery<GenericResponse>({
    queryKey: radarrKeys.animeTest(),
    queryFn: () => testRadarrConnection('ANIME'),
    enabled: false,
    retry: false,
  })
}

export function useImportRadarrMovies() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: () => importRadarrMovies('DEFAULT'),
  })
}

export function useImportRadarrAnimeMovies() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: () => importRadarrMovies('ANIME'),
  })
}

export function useRefreshRadarrRatings() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: () => refreshRadarrRatings('DEFAULT'),
  })
}

export function useRefreshRadarrAnimeRatings() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: () => refreshRadarrRatings('ANIME'),
  })
}
