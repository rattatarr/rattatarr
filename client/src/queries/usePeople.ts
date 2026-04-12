import { useQuery } from '@tanstack/vue-query'
import type { MaybeRefOrGetter } from 'vue'
import { toValue, computed } from 'vue'
import { getAllPeople, getPersonById } from '@/api/people'
import { peopleKeys } from './queryKeys'
import type { Pageable, PeopleFilters, PeopleWrapper, Person } from '@/types'

/**
 * Query hook to get all people
 */
export function usePeople(
  pageable: MaybeRefOrGetter<Pageable>,
  filters: MaybeRefOrGetter<PeopleFilters>,
) {
  return useQuery<PeopleWrapper>({
    queryKey: computed(() => peopleKeys.list(toValue(pageable), toValue(filters))),
    queryFn: () => getAllPeople(toValue(pageable), toValue(filters)),
  })
}

/**
 * Query hook to get a single person by ID
 */
export function usePerson(id: MaybeRefOrGetter<string | undefined>) {
  return useQuery<Person>({
    queryKey: computed(() => peopleKeys.detail(toValue(id) ?? '')),
    queryFn: () => getPersonById(toValue(id)!),
    enabled: computed(() => !!toValue(id)),
  })
}
