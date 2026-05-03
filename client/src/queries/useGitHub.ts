import { useQuery } from '@tanstack/vue-query'
import { getLatestRelease } from '@/api/github'
import { githubKeys } from './queryKeys'

export function useLatestRelease() {
  return useQuery({
    queryKey: githubKeys.latestRelease(),
    queryFn: getLatestRelease,
    staleTime: 60 * 60 * 1000,
    retry: 1,
  })
}
