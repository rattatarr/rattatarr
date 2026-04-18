import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import {
  useAgentSuggestions,
  useAgentPrompt,
  useAgentConversation,
  useSendAgentChat,
  useClearAgentConversation,
} from '@/queries'
import type { AgentConversationWrapper, AgentChatRequest } from '@/types'

// Mock API
const mockGetAgentSuggestions = vi.fn()
const mockGetAgentPrompt = vi.fn()
const mockGetAgentConversation = vi.fn()
const mockSendAgentChat = vi.fn()
const mockClearAgentConversation = vi.fn()

vi.mock('@/api/agent', () => ({
  getAgentSuggestions: () => mockGetAgentSuggestions(),
  getAgentPrompt: (...args: never[]) => mockGetAgentPrompt(...args),
  getAgentConversation: (...args: never[]) => mockGetAgentConversation(...args),
  sendAgentChat: (...args: never[]) => mockSendAgentChat(...args),
  clearAgentConversation: (...args: never[]) => mockClearAgentConversation(...args),
}))

// Mock TanStack Query
const mockUseQuery = vi.fn()
const mockUseMutation = vi.fn()
const mockCancelQueries = vi.fn()
const mockGetQueryData = vi.fn()
const mockSetQueryData = vi.fn()
const mockInvalidateQueries = vi.fn()
const mockUseQueryClient = vi.fn(() => ({
  cancelQueries: mockCancelQueries,
  getQueryData: mockGetQueryData,
  setQueryData: mockSetQueryData,
  invalidateQueries: mockInvalidateQueries,
}))

vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
  useMutation: (options: never) => mockUseMutation(options),
  useQueryClient: () => mockUseQueryClient(),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  agentKeys: {
    all: ['agent'],
    suggestions: () => ['agent', 'suggestions'],
    prompts: () => ['agent', 'prompt'],
    prompt: (profileId: string, agent?: string) => ['agent', 'prompt', profileId, { agent }],
    conversations: () => ['agent', 'conversation'],
    conversation: (profileId: string, agent?: string) => [
      'agent',
      'conversation',
      profileId,
      { agent },
    ],
  },
}))

describe('useAgentSuggestions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with suggestions key', () => {
    useAgentSuggestions()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey).toEqual(['agent', 'suggestions'])
  })

  it('calls getAgentSuggestions as queryFn', async () => {
    mockGetAgentSuggestions.mockResolvedValue({ suggestions: [] })

    useAgentSuggestions()

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAgentSuggestions).toHaveBeenCalled()
  })

  it('sets staleTime to 5 minutes', () => {
    useAgentSuggestions()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.staleTime).toBe(5 * 60 * 1000)
  })
})

describe('useAgentPrompt', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with profile-scoped prompt key', () => {
    useAgentPrompt(ref('profile-1'))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual(['agent', 'prompt', 'profile-1', { agent: undefined }])
  })

  it('includes agent in key when provided', () => {
    useAgentPrompt(ref('profile-1'), ref('ollama'))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual(['agent', 'prompt', 'profile-1', { agent: 'ollama' }])
  })

  it('is disabled when profileId is null', () => {
    useAgentPrompt(ref(null))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(false)
  })

  it('is enabled when profileId is set', () => {
    useAgentPrompt(ref('profile-1'))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(true)
  })

  it('calls getAgentPrompt with profileId', async () => {
    mockGetAgentPrompt.mockResolvedValue({ prompt: 'You are...' })

    useAgentPrompt('profile-1')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAgentPrompt).toHaveBeenCalledWith('profile-1', undefined)
  })
})

describe('useAgentConversation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with profile-scoped conversation key', () => {
    useAgentConversation(ref('profile-1'))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual([
      'agent',
      'conversation',
      'profile-1',
      { agent: undefined },
    ])
  })

  it('includes agent in key when provided', () => {
    useAgentConversation(ref('profile-1'), ref('ollama'))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual(['agent', 'conversation', 'profile-1', { agent: 'ollama' }])
  })

  it('is disabled when profileId is null', () => {
    useAgentConversation(ref(null))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(false)
  })

  it('is enabled when profileId is set', () => {
    useAgentConversation(ref('profile-1'))

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(true)
  })

  it('calls getAgentConversation with profileId', async () => {
    mockGetAgentConversation.mockResolvedValue({ conversation: [] })

    useAgentConversation('profile-1')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAgentConversation).toHaveBeenCalledWith('profile-1', undefined)
  })
})

describe('useSendAgentChat', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn() })
  })

  it('calls sendAgentChat as mutationFn', async () => {
    mockSendAgentChat.mockResolvedValue({ reply: 'Try Inception!' })

    useSendAgentChat()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Recommend a movie' }
    await call.mutationFn(request)

    expect(mockSendAgentChat).toHaveBeenCalledWith(request, undefined)
  })

  it('passes agent param to sendAgentChat', async () => {
    mockSendAgentChat.mockResolvedValue({ reply: 'Try The Matrix!' })

    useSendAgentChat(ref('ollama'))

    const call = mockUseMutation.mock.calls[0]![0]!
    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Recommend' }
    await call.mutationFn(request)

    expect(mockSendAgentChat).toHaveBeenCalledWith(request, 'ollama')
  })

  it('optimistically adds user message on mutate', async () => {
    const existing: AgentConversationWrapper = {
      conversation: [
        { id: 'msg-1', role: 'USER', content: 'Hello', sentAt: '2026-01-01T00:00:00Z' },
      ],
    }
    mockCancelQueries.mockResolvedValue(undefined)
    mockGetQueryData.mockReturnValue(existing)

    useSendAgentChat()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request: AgentChatRequest = { profileId: 'profile-1', message: 'New message' }
    const ctx = await call.onMutate(request)

    expect(mockCancelQueries).toHaveBeenCalledWith({
      queryKey: ['agent', 'conversation', 'profile-1', { agent: undefined }],
    })
    expect(ctx).toEqual({ previous: existing })

    const [, updater] = mockSetQueryData.mock.calls[0]!
    const updated = updater(existing)
    expect(updated.conversation).toHaveLength(2)
    expect(updated.conversation[1].role).toBe('USER')
    expect(updated.conversation[1].content).toBe('New message')
  })

  it('rolls back on error when context has previous data', async () => {
    const previous: AgentConversationWrapper = { conversation: [] }

    useSendAgentChat()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Msg' }
    await call.onError(new Error('fail'), request, { previous })

    expect(mockSetQueryData).toHaveBeenCalledWith(
      ['agent', 'conversation', 'profile-1', { agent: undefined }],
      previous,
    )
  })

  it('does not roll back when context has no previous data', async () => {
    useSendAgentChat()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Msg' }
    await call.onError(new Error('fail'), request, { previous: undefined })

    expect(mockSetQueryData).not.toHaveBeenCalled()
  })

  it('invalidates conversation on success', async () => {
    useSendAgentChat()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request: AgentChatRequest = { profileId: 'profile-1', message: 'Msg' }
    await call.onSuccess({ reply: 'ok' }, request, undefined)

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['agent', 'conversation', 'profile-1', { agent: undefined }],
    })
  })
})

describe('useClearAgentConversation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn() })
  })

  it('calls clearAgentConversation as mutationFn', async () => {
    mockClearAgentConversation.mockResolvedValue(undefined)

    useClearAgentConversation()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn('profile-1')

    expect(mockClearAgentConversation).toHaveBeenCalledWith('profile-1', undefined)
  })

  it('passes agent param to clearAgentConversation', async () => {
    mockClearAgentConversation.mockResolvedValue(undefined)

    useClearAgentConversation(ref('ollama'))

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn('profile-1')

    expect(mockClearAgentConversation).toHaveBeenCalledWith('profile-1', 'ollama')
  })

  it('invalidates conversation on success', async () => {
    useClearAgentConversation()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess(undefined, 'profile-1', undefined)

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['agent', 'conversation', 'profile-1', { agent: undefined }],
    })
  })
})
