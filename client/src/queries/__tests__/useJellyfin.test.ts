import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  useJellyfinTest,
  useSyncJellyfinMedia,
  useSyncJellyfinProfiles,
  usePollJellyfinActivity,
} from '@/queries'

// Mock API
const mockTestJellyfinConnection = vi.fn()
const mockSyncJellyfinMedia = vi.fn()
const mockSyncJellyfinProfiles = vi.fn()
const mockPollJellyfinActivity = vi.fn()

vi.mock('@/api/jellyfin', () => ({
  testJellyfinConnection: () => mockTestJellyfinConnection(),
  syncJellyfinMedia: () => mockSyncJellyfinMedia(),
  syncJellyfinProfiles: () => mockSyncJellyfinProfiles(),
  pollJellyfinActivity: () => mockPollJellyfinActivity(),
}))

// Mock TanStack Query
const mockUseQuery = vi.fn()
const mockUseMutation = vi.fn()
const mockInvalidateQueries = vi.fn()
const mockUseQueryClient = vi.fn(() => ({
  invalidateQueries: mockInvalidateQueries,
}))

vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
  useMutation: (options: never) => mockUseMutation(options),
  useQueryClient: () => mockUseQueryClient(),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  jellyfinKeys: {
    test: () => ['jellyfin', 'test'],
  },
  movieKeys: {
    all: ['movies'],
  },
  seriesKeys: {
    all: ['series'],
  },
  profileKeys: {
    all: ['profiles'],
  },
  jobKeys: {
    all: ['jobs'],
  },
  watchActivityKeys: {
    all: ['watch-activity'],
  },
}))

describe('useJellyfinTest', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: { value: null },
      isLoading: { value: false },
      refetch: vi.fn(),
    })
  })

  it('creates query with correct query key', () => {
    useJellyfinTest()

    expect(mockUseQuery).toHaveBeenCalledWith(
      expect.objectContaining({
        queryKey: ['jellyfin', 'test'],
      }),
    )
  })

  it('calls API in queryFn', async () => {
    mockTestJellyfinConnection.mockResolvedValue({ success: true })

    useJellyfinTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockTestJellyfinConnection).toHaveBeenCalled()
  })

  it('is disabled by default', () => {
    useJellyfinTest()

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.enabled).toBe(false)
  })

  it('does not retry failed connections', () => {
    useJellyfinTest()

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.retry).toBe(false)
  })
})

describe('useSyncJellyfinMedia', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockSyncJellyfinMedia.mockResolvedValue({ success: true, message: 'Synced' })

    useSyncJellyfinMedia()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn()

    expect(mockSyncJellyfinMedia).toHaveBeenCalled()
  })

  it('invalidates movie and series queries on success', async () => {
    useSyncJellyfinMedia()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledTimes(3)
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['movies'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['series'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['jobs'] })
  })

  it('handles successful sync response', async () => {
    const response = { success: true, message: '150 items synced' }
    mockSyncJellyfinMedia.mockResolvedValue(response)

    useSyncJellyfinMedia()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn()

    expect(result).toEqual(response)
  })
})

describe('useSyncJellyfinProfiles', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockSyncJellyfinProfiles.mockResolvedValue({ profiles: [] })

    useSyncJellyfinProfiles()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn()

    expect(mockSyncJellyfinProfiles).toHaveBeenCalled()
  })

  it('invalidates profile queries on success', async () => {
    useSyncJellyfinProfiles()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['profiles'],
    })
  })

  it('handles successful profile sync response', async () => {
    const response = {
      profiles: [
        { id: 'p1', name: 'User1' },
        { id: 'p2', name: 'User2' },
      ],
    }
    mockSyncJellyfinProfiles.mockResolvedValue(response)

    useSyncJellyfinProfiles()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn()

    expect(result).toEqual(response)
  })
})

describe('usePollJellyfinActivity', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
      isPending: { value: false },
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    const jobResponse = { id: 'job-1', type: 'JELLYFIN_ACTIVITY_POLL', status: 'PENDING' }
    mockPollJellyfinActivity.mockResolvedValue(jobResponse)

    usePollJellyfinActivity()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn()

    expect(mockPollJellyfinActivity).toHaveBeenCalled()
    expect(result).toEqual(jobResponse)
  })

  it('invalidates job queries on success', async () => {
    usePollJellyfinActivity()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['jobs'] })
  })

  it('does not invalidate movie or series queries on success', async () => {
    usePollJellyfinActivity()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).not.toHaveBeenCalledWith({ queryKey: ['movies'] })
    expect(mockInvalidateQueries).not.toHaveBeenCalledWith({ queryKey: ['series'] })
  })
})
