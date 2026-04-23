import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useImportRadarrMovies } from '@/queries/useRadarr'
import type { BackgroundJob } from '@/types'

const mockImportRadarrMovies = vi.fn()

vi.mock('@/api/radarr', () => ({
  importRadarrMovies: () => mockImportRadarrMovies(),
}))

const mockUseMutation = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useMutation: (options: never) => mockUseMutation(options),
}))

const mockJob: BackgroundJob = {
  id: 'job-radarr-1',
  type: 'RADARR_IMPORT',
  status: 'PENDING',
  createdAt: '2026-04-20T10:00:00Z',
}

describe('useImportRadarrMovies', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
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
