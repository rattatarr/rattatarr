import { useQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as statisticsApi from '@/api/statistics'
import { statisticsKeys } from './queryKeys'
import type { ProfileStatisticsRequest } from '@/types'

/**
 * Query hook to get profile statistics
 */
export function useProfileStatistics(request: MaybeRefOrGetter<ProfileStatisticsRequest>) {
  return useQuery({
    queryKey: computed(() => statisticsKeys.detail(toValue(request).profileId)),
    queryFn: () => statisticsApi.getProfileStatistics(toValue(request)),
    enabled: computed(() => !!toValue(request).profileId),
  })
}
