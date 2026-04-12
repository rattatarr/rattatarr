import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as settingsApi from '@/api/settings'
import { settingsKeys } from './queryKeys'
import type { CreateSettingsRequest, SettingsWrapper, SettingWrapper } from '@/types'

/**
 * Query hook to get all settings
 */
export function useSettings() {
  return useQuery<SettingsWrapper>({
    queryKey: settingsKeys.list(),
    queryFn: () => settingsApi.getAllSettings(),
  })
}

/**
 * Query hook to get a specific setting by key
 */
export function useSetting(key: MaybeRefOrGetter<string>) {
  return useQuery<SettingWrapper>({
    queryKey: computed(() => settingsKeys.detail(toValue(key))),
    queryFn: () => settingsApi.getSettingByKey(toValue(key)),
    enabled: computed(() => !!toValue(key)),
  })
}

/**
 * Mutation hook to update settings
 */
export function useUpdateSettings() {
  const queryClient = useQueryClient()

  return useMutation<SettingsWrapper, Error, CreateSettingsRequest>({
    mutationFn: (settings: CreateSettingsRequest) => settingsApi.updateSettings(settings),
    onSuccess: async () => {
      // Invalidate all settings queries to refetch
      await queryClient.invalidateQueries({ queryKey: settingsKeys.all })
    },
  })
}
