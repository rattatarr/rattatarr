import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import * as jellyfinApi from '@/api/jellyfin'
import { jellyfinKeys, jobKeys, movieKeys, seriesKeys, profileKeys } from './queryKeys'
import type { BackgroundJob, GenericResponse, ProfilesWrapper } from '@/types'

/**
 * Query hook to test Jellyfin connection
 */
export function useJellyfinTest() {
  return useQuery<GenericResponse>({
    queryKey: jellyfinKeys.test(),
    queryFn: () => jellyfinApi.testJellyfinConnection(),
    // Don't run automatically, let user trigger
    enabled: false,
    // Don't retry failed connection tests
    retry: false,
  })
}

/**
 * Mutation hook to sync media from Jellyfin
 */
export function useSyncJellyfinMedia() {
  const queryClient = useQueryClient()

  return useMutation<BackgroundJob, Error, void>({
    mutationFn: () => jellyfinApi.syncJellyfinMedia(),
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
 * Mutation hook to poll Jellyfin watch activity
 */
export function usePollJellyfinActivity() {
  const queryClient = useQueryClient()

  return useMutation<BackgroundJob, Error, void>({
    mutationFn: () => jellyfinApi.pollJellyfinActivity(),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: jobKeys.all })
    },
  })
}

/**
 * Mutation hook to sync profiles from Jellyfin
 */
export function useSyncJellyfinProfiles() {
  const queryClient = useQueryClient()

  return useMutation<ProfilesWrapper, Error, void>({
    mutationFn: () => jellyfinApi.syncJellyfinProfiles(),
    onSuccess: async () => {
      // Invalidate profile queries to show newly synced profiles
      await queryClient.invalidateQueries({ queryKey: profileKeys.all })
    },
  })
}
