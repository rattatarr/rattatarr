import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useImportRadarrMovies, useRefreshRadarrRatings, useRadarrTest } from '@/queries/useRadarr'
import type { BackgroundJob, GenericResponse } from '@/types'

const mockImportRadarrMovies = vi.fn()
const mockRefreshRadarrRatings = vi.fn()
const mockTestRadarrConnection = vi.fn()

vi.mock('@/api/radarr', () => ({
  importRadarrMovies: () => mockImportRadarrMovies(),
  refreshRadarrRatings: () => mockRefreshRadarrRatings(),
  testRadarrConnection: () => mockTestRadarrConnection(),
}))

const mockUseMutation = vi.fn()
const mockUseQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useMutation: (options: never) => mockUseMutation(options),
  useQuery: (options: never) => mockUseQuery(options),
}))

vi.mock('@/queries/queryKeys', () => ({
  radarrKeys: {
    all: ['radarr'],
    test: () => ['radarr', 'test'],
  },
}))

const mockJob: BackgroundJob = {
  id: 'job-radarr-1',
  type: 'RADARR_IMPORT',
  status: 'PENDING',
  createdAt: '2026-04-20T10:00:00Z',
}

const mockConnectionOk: GenericResponse = {
  status: '200 OK',
  message: 'Connected',
}

describe('useRadarrTest', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: { value: mockConnectionOk }, error: { value: null } })
  })

  it('creates query with radarr test key', () => {
    useRadarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey).toEqual(['radarr', 'test'])
  })

  it('is disabled by default', () => {
    useRadarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled).toBe(false)
  })

  it('does not retry on failure', () => {
    useRadarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.retry).toBe(false)
  })

  it('queryFn calls testRadarrConnection', async () => {
    mockTestRadarrConnection.mockResolvedValue(mockConnectionOk)

    useRadarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    const result = await call.queryFn()

    expect(mockTestRadarrConnection).toHaveBeenCalled()
    expect(result).toEqual(mockConnectionOk)
  })
})

describe('useImportRadarrMovies', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn(), mutateAsync: vi.fn() })
  })

  it('creates mutation with importRadarrMovies as mutationFn', async () => {
    mockImportRadarrMovies.mockResolvedValue(mockJob)

    useImportRadarrMovies()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn()

    expect(mockImportRadarrMovies).toHaveBeenCalled()
    expect(result).toEqual(mockJob)
  })

  it('propagates errors from the API', async () => {
    mockImportRadarrMovies.mockRejectedValue(new Error('Radarr unavailable'))

    useImportRadarrMovies()

    const call = mockUseMutation.mock.calls[0]![0]!

    await expect(call.mutationFn()).rejects.toThrow('Radarr unavailable')
  })
})

describe('useRefreshRadarrRatings', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn(), mutateAsync: vi.fn() })
  })

  it('creates mutation with refreshRadarrRatings as mutationFn', async () => {
    const refreshJob: BackgroundJob = { ...mockJob, type: 'RADARR_RATINGS_REFRESH' }
    mockRefreshRadarrRatings.mockResolvedValue(refreshJob)

    useRefreshRadarrRatings()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn()

    expect(mockRefreshRadarrRatings).toHaveBeenCalled()
    expect(result).toEqual(refreshJob)
  })

  it('propagates errors from the API', async () => {
    mockRefreshRadarrRatings.mockRejectedValue(new Error('Radarr unavailable'))

    useRefreshRadarrRatings()

    const call = mockUseMutation.mock.calls[0]![0]!

    await expect(call.mutationFn()).rejects.toThrow('Radarr unavailable')
  })
})
