import { useQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as statisticsApi from '@/api/statistics'
import { statisticsKeys, rewindKeys } from './queryKeys'
import type { ProfileStatisticsRequest, YearRewindRequest } from '@/types'

export function useProfileStatistics(request: MaybeRefOrGetter<ProfileStatisticsRequest>) {
  return useQuery({
    queryKey: computed(() => statisticsKeys.detail(toValue(request).profileId)),
    queryFn: () => statisticsApi.getProfileStatistics(toValue(request)),
    enabled: computed(() => !!toValue(request).profileId),
  })
}

export function useAvailableRewindYears(profileId: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => rewindKeys.availableYears(toValue(profileId))),
    queryFn: () => statisticsApi.getAvailableRewindYears(toValue(profileId)),
    enabled: computed(() => !!toValue(profileId)),
  })
}

export function useYearRewind(request: MaybeRefOrGetter<YearRewindRequest>) {
  return useQuery({
    queryKey: computed(() => rewindKeys.detail(toValue(request).profileId, toValue(request).year)),
    queryFn: () => statisticsApi.getYearRewind(toValue(request)),
    enabled: computed(() => !!toValue(request).profileId && !!toValue(request).year),
  })
}
