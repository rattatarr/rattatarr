import { apiClient, handleResponse } from './client'
import type { BackgroundJob, GenericResponse, ProfilesWrapper } from '@/types'

export async function syncJellyfinMedia(): Promise<BackgroundJob> {
  const response = await apiClient.PUT('/api/v1/jellyfin/sync-media')
  return handleResponse<BackgroundJob>(response)
}

export async function syncJellyfinProfiles(): Promise<ProfilesWrapper> {
  const response = await apiClient.PUT('/api/v1/jellyfin/sync-profiles')
  return handleResponse<ProfilesWrapper>(response)
}

export async function pollJellyfinActivity(): Promise<BackgroundJob> {
  const response = await apiClient.POST('/api/v1/jellyfin/poll-activity')
  return handleResponse<BackgroundJob>(response)
}

export async function testJellyfinConnection(): Promise<GenericResponse> {
  const response = await apiClient.GET('/api/v1/jellyfin/test')
  return handleResponse<GenericResponse>(response)
}
