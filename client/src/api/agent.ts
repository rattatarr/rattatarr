import { apiClient, handleResponse } from './client'
import type {
  AgentChatRequest,
  AgentChatResponse,
  AgentSuggestionsResponse,
  AgentPromptResponse,
  AgentConversationWrapper,
} from '@/types'

export async function getAgentSuggestions(): Promise<AgentSuggestionsResponse> {
  const response = await apiClient.GET('/api/v1/agent/suggestions')
  return handleResponse<AgentSuggestionsResponse>(response)
}

export async function getAgentPrompt(
  profileId: string,
  agent?: string,
): Promise<AgentPromptResponse> {
  const response = await apiClient.GET('/api/v1/agent/prompt/{profileId}', {
    params: { path: { profileId }, query: { agent } },
  })
  return handleResponse<AgentPromptResponse>(response)
}

export async function getAgentConversation(
  profileId: string,
  agent?: string,
): Promise<AgentConversationWrapper> {
  const response = await apiClient.GET('/api/v1/agent/conversation/{profileId}', {
    params: { path: { profileId }, query: { agent } },
  })
  return handleResponse<AgentConversationWrapper>(response)
}

export async function sendAgentChat(
  request: AgentChatRequest,
  agent?: string,
): Promise<AgentChatResponse> {
  const response = await apiClient.POST('/api/v1/agent/chat', {
    params: { query: { agent } },
    body: request,
  })
  return handleResponse<AgentChatResponse>(response)
}

export async function clearAgentConversation(profileId: string, agent?: string): Promise<void> {
  await apiClient.DELETE('/api/v1/agent/conversation/{profileId}', {
    params: { path: { profileId }, query: { agent } },
  })
}
