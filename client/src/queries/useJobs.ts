import { useQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import * as jobsApi from '@/api/jobs'
import { jobKeys } from './queryKeys'
import type { BackgroundJob, BackgroundJobsWrapper, JobsFilters, Pageable } from '@/types'

export function useJobs(
  pageable: MaybeRefOrGetter<Pageable>,
  filters?: MaybeRefOrGetter<JobsFilters | undefined>,
) {
  return useQuery<BackgroundJobsWrapper>({
    queryKey: computed(() => jobKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => jobsApi.getJobs(toValue(pageable), toValue(filters)),
    placeholderData: (prev) => prev,
  })
}

export function useJob(id: MaybeRefOrGetter<string>) {
  return useQuery<BackgroundJob>({
    queryKey: computed(() => jobKeys.detail(toValue(id))),
    queryFn: () => jobsApi.getJob(toValue(id)),
  })
}
