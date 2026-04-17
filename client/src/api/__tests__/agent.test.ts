import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getAgentSuggestions,
  getAgentPrompt,
  getAgentConversation,
  sendAgentChat,
  clearAgentConversation,
} from '@/api/agent'
import type {
  AgentChatRequest,
  AgentChatResponse,
  AgentSuggestionsResponse,
  AgentPromptResponse,
  AgentConversationWrapper,
} from '@/types'

const mockGET = vi.fn()
const mockPOST = vi.fn()
const mockDELETE = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    GET: (...args: never[]) => mockGET(...args),
    POST: (...args: never[]) => mockPOST(...args),
    DELETE: (...args: never[]) => mockDELETE(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

describe('getAgentSuggestions', () => {
  beforeEach(() => vi.clearAllMocks())

  it('calls suggestions endpoint and returns handleResponse result', async () => {
    const mockData: AgentSuggestionsResponse = { suggestions: ['What to watch?', 'Rate something'] }
    mockGET.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    const result = await getAgentSuggestions()

    expect(mockGET).toHaveBeenCalledWith('/api/v1/agent/suggestions')
    expect(result).toBe(mockData)
  })
})

describe('getAgentPrompt', () => {
  beforeEach(() => vi.clearAllMocks())

  it('calls prompt endpoint with profileId', async () => {
    const mockData: AgentPromptResponse = { prompt: 'You are a movie recommender...' }
    mockGET.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    await getAgentPrompt('profile-1')

    expect(mockGET).toHaveBeenCalledWith('/api/v1/agent/prompt/{profileId}', {
      params: { path: { profileId: 'profile-1' }, query: { agent: undefined } },
    })
  })

  it('passes optional agent parameter', async () => {
    const mockData: AgentPromptResponse = { prompt: 'Custom agent prompt...' }
    mockGET.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    await getAgentPrompt('profile-1', 'ollama')

    expect(mockGET).toHaveBeenCalledWith('/api/v1/agent/prompt/{profileId}', {
      params: { path: { profileId: 'profile-1' }, query: { agent: 'ollama' } },
    })
  })
})

describe('getAgentConversation', () => {
  beforeEach(() => vi.clearAllMocks())

  it('calls conversation endpoint with profileId', async () => {
    const mockData: AgentConversationWrapper = { conversation: [] }
    mockGET.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    await getAgentConversation('profile-1')

    expect(mockGET).toHaveBeenCalledWith('/api/v1/agent/conversation/{profileId}', {
      params: { path: { profileId: 'profile-1' }, query: { agent: undefined } },
    })
  })

  it('passes optional agent parameter', async () => {
    const mockData: AgentConversationWrapper = { conversation: [] }
    mockGET.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    await getAgentConversation('profile-2', 'ollama')

    expect(mockGET).toHaveBeenCalledWith('/api/v1/agent/conversation/{profileId}', {
      params: { path: { profileId: 'profile-2' }, query: { agent: 'ollama' } },
    })
  })
})

describe('sendAgentChat', () => {
  beforeEach(() => vi.clearAllMocks())

  it('POSTs to chat endpoint with request body', async () => {
    const mockData: AgentChatResponse = { reply: 'Try Inception!' }
    mockPOST.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Recommend a movie' }
    await sendAgentChat(request)

    expect(mockPOST).toHaveBeenCalledWith('/api/v1/agent/chat', {
      params: { query: { agent: undefined } },
      body: request,
    })
  })

  it('passes optional agent parameter', async () => {
    const mockData: AgentChatResponse = { reply: 'Try The Matrix!' }
    mockPOST.mockResolvedValue({ data: mockData })
    mockHandleResponse.mockReturnValue(mockData)

    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Recommend a movie' }
    await sendAgentChat(request, 'ollama')

    expect(mockPOST).toHaveBeenCalledWith('/api/v1/agent/chat', {
      params: { query: { agent: 'ollama' } },
      body: request,
    })
  })
})

describe('clearAgentConversation', () => {
  beforeEach(() => vi.clearAllMocks())

  it('DELETEs conversation for profileId', async () => {
    mockDELETE.mockResolvedValue({})

    await clearAgentConversation('profile-1')

    expect(mockDELETE).toHaveBeenCalledWith('/api/v1/agent/conversation/{profileId}', {
      params: { path: { profileId: 'profile-1' }, query: { agent: undefined } },
    })
  })

  it('passes optional agent parameter', async () => {
    mockDELETE.mockResolvedValue({})

    await clearAgentConversation('profile-1', 'ollama')

    expect(mockDELETE).toHaveBeenCalledWith('/api/v1/agent/conversation/{profileId}', {
      params: { path: { profileId: 'profile-1' }, query: { agent: 'ollama' } },
    })
  })
})
