import { apiClient, handleResponse } from './client'
import type {
  CreateProfileRequest,
  Pageable,
  ProfilesFilters,
  ProfileWrapper,
  ProfilesWrapper,
} from '@/types'
import { buildProfilesQueryParams } from './queryParams'

export async function getAllProfiles(
  pageable: Pageable,
  filters: ProfilesFilters,
): Promise<ProfilesWrapper> {
  const queryParams = buildProfilesQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/profiles', {
    params: { query: queryParams as any },
  })
  return handleResponse<ProfilesWrapper>(response)
}

export async function getProfileById(id: string): Promise<ProfileWrapper> {
  const response = await apiClient.GET('/api/v1/profiles/{id}', {
    params: { path: { id } },
  })
  return handleResponse<ProfileWrapper>(response)
}

/**
 * Create a new profile
 */
export async function createProfile(request: CreateProfileRequest): Promise<ProfileWrapper> {
  const response = await apiClient.POST('/api/v1/profiles', {
    body: request,
  })
  return handleResponse<ProfileWrapper>(response)
}
