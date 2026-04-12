import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as profilesApi from '@/api/profiles'
import { profileKeys } from './queryKeys'
import type { Pageable, ProfilesFilters, CreateProfileRequest } from '@/types'

/**
 * Query hook to get all profiles
 */
export function useProfiles(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<ProfilesFilters>,
) {
  return useQuery({
    queryKey: computed(() => profileKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => profilesApi.getAllProfiles(toValue(pageable), toValue(filters)),
  })
}

/**
 * Query hook to get a profile by ID
 */
export function useProfile(id: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => profileKeys.detail(toValue(id))),
    queryFn: () => profilesApi.getProfileById(toValue(id)),
    enabled: computed(() => !!toValue(id)),
  })
}

/**
 * Mutation hook to create a profile
 */
export function useCreateProfile() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateProfileRequest) => profilesApi.createProfile(request),
    onSuccess: async () => {
      // Invalidate all profile lists to refetch with new profile
      await queryClient.invalidateQueries({ queryKey: profileKeys.lists() })
    },
  })
}
