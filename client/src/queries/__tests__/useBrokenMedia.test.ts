import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import {
  useBrokenMovies,
  useBrokenSeries,
  useResolveBrokenMediaItem,
} from '@/queries/useBrokenMedia'

// Mock broken media API
const mockGetBrokenMovies = vi.fn()
const mockGetBrokenSeries = vi.fn()
const mockResolveItem = vi.fn()

vi.mock('@/api/brokenMedia', () => ({
  getBrokenMovies: (...args: never[]) => mockGetBrokenMovies(...args),
  getBrokenSeries: (...args: never[]) => mockGetBrokenSeries(...args),
  resolveItem: (...args: never[]) => mockResolveItem(...args),
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
  movieKeys: {
    all: ['movies'],
    broken: () => ['movies', 'broken'],
    brokenList: (pageable: never) => ['movies', 'broken', { pageable }],
  },
  seriesKeys: {
    all: ['series'],
    broken: () => ['series', 'broken'],
    brokenList: (pageable: never) => ['series', 'broken', { pageable }],
  },
}))

describe('useBrokenMovies', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 0, size: 50 })

    useBrokenMovies(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['movies', 'broken', { pageable: { page: 0, size: 50 } }])
  })

  it('creates query with correct key for different pageable values', () => {
    const pageable = ref({ page: 3, size: 100 })

    useBrokenMovies(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['movies', 'broken', { pageable: { page: 3, size: 100 } }])
  })

  it('calls API with correct pageable', async () => {
    mockGetBrokenMovies.mockResolvedValue({ movies: [] })

    const pageable = { page: 2, size: 30 }
    useBrokenMovies(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetBrokenMovies).toHaveBeenCalledWith(pageable)
  })

  it('passes plain pageable object (non-ref) correctly', async () => {
    mockGetBrokenMovies.mockResolvedValue({ movies: [] })

    const pageable = { page: 0, size: 20 }
    useBrokenMovies(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetBrokenMovies).toHaveBeenCalledWith(pageable)
  })
})

describe('useBrokenSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 0, size: 100 })

    useBrokenSeries(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['series', 'broken', { pageable: { page: 0, size: 100 } }])
  })

  it('creates query with correct key for different pageable values', () => {
    const pageable = ref({ page: 1, size: 25 })

    useBrokenSeries(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['series', 'broken', { pageable: { page: 1, size: 25 } }])
  })

  it('calls API with correct pageable', async () => {
    mockGetBrokenSeries.mockResolvedValue({ movies: [] })

    const pageable = { page: 1, size: 50 }
    useBrokenSeries(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetBrokenSeries).toHaveBeenCalledWith(pageable)
  })

  it('passes plain pageable object (non-ref) correctly', async () => {
    mockGetBrokenSeries.mockResolvedValue({ movies: [] })

    const pageable = { page: 0, size: 20 }
    useBrokenSeries(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetBrokenSeries).toHaveBeenCalledWith(pageable)
  })
})

describe('useResolveBrokenMediaItem', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn() })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockResolveItem.mockResolvedValue({ id: 'item-1', resolved: true })

    useResolveBrokenMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn({ id: 'item-1', request: { mediaItemId: 'media-42' } })

    expect(mockResolveItem).toHaveBeenCalledWith('item-1', { mediaItemId: 'media-42' })
  })

  it('passes id and request to resolveItem', async () => {
    mockResolveItem.mockResolvedValue({ id: 'item-2', resolved: true })

    useResolveBrokenMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn({ id: 'item-2', request: { mediaItemId: 'media-99' } })

    expect(mockResolveItem).toHaveBeenCalledWith('item-2', { mediaItemId: 'media-99' })
  })

  it('invalidates movie broken queries on success', async () => {
    useResolveBrokenMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['movies', 'broken'],
    })
  })

  it('invalidates series broken queries on success', async () => {
    useResolveBrokenMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['series', 'broken'],
    })
  })

  it('invalidates both movie and series broken queries on success', async () => {
    useResolveBrokenMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledTimes(2)
  })
})
