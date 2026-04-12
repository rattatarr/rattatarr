import { apiClient, handleResponse } from './client'
import type { Pageable, PeopleFilters, PeopleWrapper, Person } from '@/types'
import { buildPeopleQueryParams } from './queryParams'

export async function getAllPeople(
  pageable: Pageable,
  filters: PeopleFilters,
): Promise<PeopleWrapper> {
  const queryParams = buildPeopleQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/library/people', {
    params: { query: queryParams as any },
  })
  return handleResponse<PeopleWrapper>(response)
}

export async function getPersonById(id: string, profileSize?: string): Promise<Person> {
  const response = await apiClient.GET('/api/v1/library/people/{id}', {
    params: {
      path: { id },
      query: profileSize ? { profileSize } : undefined,
    },
  })
  return handleResponse<Person>(response)
}
