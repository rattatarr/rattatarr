import { apiClient, handleResponse } from './client'
import type { GenericResponse, ProfilesWrapper } from '@/types'

export async function syncJellyfinMedia(): Promise<GenericResponse> {
  const response = await apiClient.PUT('/api/v1/jellyfin/sync-media')
  return handleResponse<GenericResponse>(response)
}

export async function syncJellyfinProfiles(): Promise<ProfilesWrapper> {
  const response = await apiClient.PUT('/api/v1/jellyfin/sync-profiles')
  return handleResponse<ProfilesWrapper>(response)
}

export async function testJellyfinConnection(): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/jellyfin/test')
  return handleResponse<GenericResponse>(response)
}
