import { useQuery, useMutation } from '@tanstack/vue-query'
import { importSonarrSeries, testSonarrConnection } from '@/api/sonarr'
import { sonarrKeys } from './queryKeys'
import type { BackgroundJob, GenericResponse } from '@/types'

export function useSonarrTest() {
  return useQuery<GenericResponse>({
    queryKey: sonarrKeys.test(),
    queryFn: testSonarrConnection,
    enabled: false,
    retry: false,
  })
}

export function useImportSonarrSeries() {
  return useMutation<BackgroundJob, Error>({
    mutationFn: importSonarrSeries,
  })
}
