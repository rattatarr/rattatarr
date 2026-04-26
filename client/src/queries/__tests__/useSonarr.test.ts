import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useImportSonarrSeries, useSonarrTest } from '@/queries/useSonarr'
import type { BackgroundJob, GenericResponse } from '@/types'

const mockImportSonarrSeries = vi.fn()
const mockTestSonarrConnection = vi.fn()

vi.mock('@/api/sonarr', () => ({
  importSonarrSeries: () => mockImportSonarrSeries(),
  testSonarrConnection: () => mockTestSonarrConnection(),
}))

const mockUseMutation = vi.fn()
const mockUseQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useMutation: (options: never) => mockUseMutation(options),
  useQuery: (options: never) => mockUseQuery(options),
}))

vi.mock('@/queries/queryKeys', () => ({
  sonarrKeys: {
    all: ['sonarr'],
    test: () => ['sonarr', 'test'],
  },
}))

const mockJob: BackgroundJob = {
  id: 'job-sonarr-1',
  type: 'SONARR_IMPORT',
  status: 'PENDING',
  createdAt: '2026-04-26T10:00:00Z',
}

const mockConnectionOk: GenericResponse = {
  status: '200 OK',
  message: 'Connected',
}

describe('useSonarrTest', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: { value: mockConnectionOk }, error: { value: null } })
  })

  it('creates query with sonarr test key', () => {
    useSonarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey).toEqual(['sonarr', 'test'])
  })

  it('is disabled by default', () => {
    useSonarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled).toBe(false)
  })

  it('does not retry on failure', () => {
    useSonarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.retry).toBe(false)
  })

  it('queryFn calls testSonarrConnection', async () => {
    mockTestSonarrConnection.mockResolvedValue(mockConnectionOk)

    useSonarrTest()

    const call = mockUseQuery.mock.calls[0]![0]!
    const result = await call.queryFn()

    expect(mockTestSonarrConnection).toHaveBeenCalled()
    expect(result).toEqual(mockConnectionOk)
  })
})

describe('useImportSonarrSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn(), mutateAsync: vi.fn() })
  })

  it('creates mutation with importSonarrSeries as mutationFn', async () => {
    mockImportSonarrSeries.mockResolvedValue(mockJob)

    useImportSonarrSeries()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn()

    expect(mockImportSonarrSeries).toHaveBeenCalled()
    expect(result).toEqual(mockJob)
  })

  it('propagates errors from the API', async () => {
    mockImportSonarrSeries.mockRejectedValue(new Error('Sonarr unavailable'))

    useImportSonarrSeries()

    const call = mockUseMutation.mock.calls[0]![0]!

    await expect(call.mutationFn()).rejects.toThrow('Sonarr unavailable')
  })
})
