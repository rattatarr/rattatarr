import { apiClient, handleResponse } from './client'
import type { SettingsWrapper, CreateSettingsRequest, SettingWrapper } from '@/types'

export async function getAllSettings(): Promise<SettingsWrapper> {
  const response = await apiClient.GET('/api/v1/settings')
  return handleResponse<SettingsWrapper>(response)
}

export async function updateSettings(settings: CreateSettingsRequest): Promise<SettingsWrapper> {
  const response = await apiClient.PUT('/api/v1/settings', {
    body: settings,
  })
  return handleResponse<SettingsWrapper>(response)
}

export async function getSettingByKey(key: string): Promise<SettingWrapper> {
  const response = await apiClient.GET('/api/v1/settings/{key}', {
    params: { path: { key } },
  })
  return handleResponse<SettingWrapper>(response)
}
