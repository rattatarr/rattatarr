import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import {
  useMovies,
  useRecentlyWatchedUnratedMovies,
  useBrokenMovies,
  useSeries,
  useRecentlyWatchedUnratedSeries,
  useBrokenSeries,
  useGenres,
  useRefreshAllStaleSeries,
  useRefreshSeries,
} from '@/queries'

// Mock API
const mockGetAllMovies = vi.fn()
const mockGetRecentlyWatchedUnratedMovies = vi.fn()
const mockGetAllSeries = vi.fn()
const mockGetRecentlyWatchedUnratedSeries = vi.fn()
const mockGetAllGenres = vi.fn()
const mockRefreshAllStaleSeries = vi.fn()
const mockRefreshSeriesById = vi.fn()

vi.mock('@/api/library', () => ({
  getAllMovies: (...args: never[]) => mockGetAllMovies(...args),
  getRecentlyWatchedUnratedMovies: (...args: never[]) =>
    mockGetRecentlyWatchedUnratedMovies(...args),
  getAllSeries: (...args: never[]) => mockGetAllSeries(...args),
  getRecentlyWatchedUnratedSeries: (...args: never[]) =>
    mockGetRecentlyWatchedUnratedSeries(...args),
  getAllGenres: (...args: never[]) => mockGetAllGenres(...args),
  refreshAllStaleSeries: () => mockRefreshAllStaleSeries(),
  refreshSeriesById: (...args: never[]) => mockRefreshSeriesById(...args),
}))

// Mock broken media API (useBrokenMovies/useBrokenSeries live in useBrokenMedia.ts)
const mockGetBrokenMovies = vi.fn()
const mockGetBrokenSeries = vi.fn()

vi.mock('@/api/brokenMedia', () => ({
  getBrokenMovies: (...args: never[]) => mockGetBrokenMovies(...args),
  getBrokenSeries: (...args: never[]) => mockGetBrokenSeries(...args),
  resolveItem: vi.fn(),
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
    list: (pageable: never, filters: never) => ['movies', 'list', pageable, filters],
    watchedUnratedList: (pageable: never, filters: never) => [
      'movies',
      'watched-unrated',
      pageable,
      filters,
    ],
    brokenList: (pageable: never) => ['movies', 'broken', pageable],
  },
  seriesKeys: {
    all: ['series'],
    list: (pageable: never, filters: never) => ['series', 'list', pageable, filters],
    watchedUnratedList: (pageable: never, filters: never) => [
      'series',
      'watched-unrated',
      pageable,
      filters,
    ],
    brokenList: (pageable: never) => ['series', 'broken', pageable],
  },
  genreKeys: {
    list: (pageable: never, filters: never) => ['genres', 'list', pageable, filters],
  },
}))

describe('useMovies', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 0, size: 10 })
    const filters = ref({ title: '', titleSearch: 'test' })

    useMovies(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'movies',
      'list',
      { page: 0, size: 10 },
      { title: '', titleSearch: 'test' },
    ])
  })

  it('calls API with correct parameters', async () => {
    mockGetAllMovies.mockResolvedValue({ movies: [] })

    const pageable = { page: 1, size: 20 }
    const filters = { title: '', genres: ['Action'] }

    useMovies(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAllMovies).toHaveBeenCalledWith(pageable, filters)
  })
})

describe('useRecentlyWatchedUnratedMovies', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with watched-unrated movie key', () => {
    const pageable = ref({ page: 0, size: 10 })
    const filters = ref({ title: '', profileId: 'profile-1' })

    useRecentlyWatchedUnratedMovies(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual([
      'movies',
      'watched-unrated',
      { page: 0, size: 10 },
      { title: '', profileId: 'profile-1' },
    ])
  })

  it('calls watched-unrated movies API with pageable and filters', async () => {
    mockGetRecentlyWatchedUnratedMovies.mockResolvedValue({ movies: [] })

    const pageable = { page: 0, size: 10 }
    const filters = { title: '', profileId: 'profile-1' }

    useRecentlyWatchedUnratedMovies(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetRecentlyWatchedUnratedMovies).toHaveBeenCalledWith(pageable, filters)
  })

  it('is disabled when profileId is missing', () => {
    const pageable = { page: 0, size: 10 }
    const filters = { title: '' }

    useRecentlyWatchedUnratedMovies(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(false)
  })
})

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

    expect(queryKey).toEqual(['movies', 'broken', { page: 0, size: 50 }])
  })

  it('calls API with correct pageable', async () => {
    mockGetBrokenMovies.mockResolvedValue({ items: [] })

    const pageable = { page: 2, size: 30 }

    useBrokenMovies(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetBrokenMovies).toHaveBeenCalledWith(pageable)
  })
})

describe('useSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 1, size: 15 })
    const filters = ref({ title: '', titleSearch: 'drama' })

    useSeries(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'series',
      'list',
      { page: 1, size: 15 },
      { title: '', titleSearch: 'drama' },
    ])
  })

  it('calls API with correct parameters', async () => {
    mockGetAllSeries.mockResolvedValue({ series: [] })

    const pageable = { page: 0, size: 25 }
    const filters = { title: '', genres: ['Sci-Fi'] }

    useSeries(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAllSeries).toHaveBeenCalledWith(pageable, filters)
  })
})

describe('useRecentlyWatchedUnratedSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with watched-unrated series key', () => {
    const pageable = ref({ page: 0, size: 10 })
    const filters = ref({ title: '', profileId: 'profile-1' })

    useRecentlyWatchedUnratedSeries(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual([
      'series',
      'watched-unrated',
      { page: 0, size: 10 },
      { title: '', profileId: 'profile-1' },
    ])
  })

  it('calls watched-unrated series API with pageable and filters', async () => {
    mockGetRecentlyWatchedUnratedSeries.mockResolvedValue({ series: [] })

    const pageable = { page: 0, size: 10 }
    const filters = { title: '', profileId: 'profile-1' }

    useRecentlyWatchedUnratedSeries(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetRecentlyWatchedUnratedSeries).toHaveBeenCalledWith(pageable, filters)
  })

  it('is disabled when profileId is missing', () => {
    const pageable = { page: 0, size: 10 }
    const filters = { title: '' }

    useRecentlyWatchedUnratedSeries(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(false)
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

    expect(queryKey).toEqual(['series', 'broken', { page: 0, size: 100 }])
  })

  it('calls API with correct pageable', async () => {
    mockGetBrokenSeries.mockResolvedValue({ items: [] })

    const pageable = { page: 1, size: 50 }

    useBrokenSeries(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetBrokenSeries).toHaveBeenCalledWith(pageable)
  })
})

describe('useGenres', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 0, size: 100 })
    const filters = ref({ name: 'Action' })

    useGenres(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['genres', 'list', { page: 0, size: 100 }, { name: 'Action' }])
  })

  it('calls API with correct parameters', async () => {
    mockGetAllGenres.mockResolvedValue({ genres: [] })

    const pageable = { page: 0, size: 200 }
    const filters = { name: '' }

    useGenres(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAllGenres).toHaveBeenCalledWith(pageable, filters)
  })
})

describe('useRefreshAllStaleSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn() })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockRefreshAllStaleSeries.mockResolvedValue({ success: true })

    useRefreshAllStaleSeries()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn()

    expect(mockRefreshAllStaleSeries).toHaveBeenCalled()
  })

  it('invalidates series queries on success', async () => {
    useRefreshAllStaleSeries()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['series'],
    })
  })
})

describe('useRefreshSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn() })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockRefreshSeriesById.mockResolvedValue({ success: true })

    useRefreshSeries()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn('series-123')

    expect(mockRefreshSeriesById).toHaveBeenCalledWith('series-123')
  })

  it('invalidates series queries on success', async () => {
    useRefreshSeries()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['series'],
    })
  })

  it('handles different series IDs', async () => {
    mockRefreshSeriesById.mockResolvedValue({ success: true })

    useRefreshSeries()

    const call = mockUseMutation.mock.calls[0]![0]!

    await call.mutationFn('s1')
    expect(mockRefreshSeriesById).toHaveBeenCalledWith('s1')

    await call.mutationFn('s2')
    expect(mockRefreshSeriesById).toHaveBeenCalledWith('s2')
  })
})
