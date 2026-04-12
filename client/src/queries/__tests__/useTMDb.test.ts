import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import {
  useTMDbMovieSearch,
  useTMDbMovie,
  useTMDbSeriesSearch,
  useTMDbSeries,
  useTMDbSearch,
  useImportTMDbData,
  useBuildCredits,
} from '@/queries'
import { MediaType } from '@/utils'

// Mock API
const mockSearchMovies = vi.fn()
const mockGetMovieByTMDbId = vi.fn()
const mockSearchSeries = vi.fn()
const mockGetSeriesByTMDbId = vi.fn()
const mockSearchTMDb = vi.fn()
const mockImportTMDbData = vi.fn()
const mockBuildCredits = vi.fn()

vi.mock('@/api/tmdb', () => ({
  searchMovies: (...args: never[]) => mockSearchMovies(...args),
  getMovieByTMDbId: (...args: never[]) => mockGetMovieByTMDbId(...args),
  searchSeries: (...args: never[]) => mockSearchSeries(...args),
  getSeriesByTMDbId: (...args: never[]) => mockGetSeriesByTMDbId(...args),
  searchTMDb: (...args: never[]) => mockSearchTMDb(...args),
  importTMDbData: (...args: never[]) => mockImportTMDbData(...args),
  buildCredits: (...args: never[]) => mockBuildCredits(...args),
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
  tmdbKeys: {
    movieSearch: (filters: never) => ['tmdb', 'movies', 'search', filters],
    movieDetail: (id: string) => ['tmdb', 'movies', 'detail', id],
    seriesSearch: (filters: never) => ['tmdb', 'series', 'search', filters],
    seriesDetail: (id: string) => ['tmdb', 'series', 'detail', id],
    search: (filters: never) => ['tmdb', 'search', filters],
  },
  movieKeys: {
    all: ['movies'],
  },
  seriesKeys: {
    all: ['series'],
  },
}))

describe('useTMDbMovieSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const filters = ref({ query: 'inception' })

    useTMDbMovieSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['tmdb', 'movies', 'search', { query: 'inception' }])
  })

  it('calls API with correct filters', async () => {
    mockSearchMovies.mockResolvedValue({ results: [] })

    const filters = { query: 'matrix', year: 1999 }

    useTMDbMovieSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockSearchMovies).toHaveBeenCalledWith(filters)
  })

  it('is disabled when query is empty', () => {
    const filters = ref({ query: '' })

    useTMDbMovieSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })

  it('is enabled when query is provided', () => {
    const filters = ref({ query: 'avatar' })

    useTMDbMovieSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(true)
  })
})

describe('useTMDbMovie', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const id = ref('12345')

    useTMDbMovie(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['tmdb', 'movies', 'detail', '12345'])
  })

  it('calls API with correct ID', async () => {
    mockGetMovieByTMDbId.mockResolvedValue({ id: '550', title: 'Fight Club' })

    useTMDbMovie('550')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetMovieByTMDbId).toHaveBeenCalledWith('550')
  })

  it('is disabled when ID is empty', () => {
    const id = ref('')

    useTMDbMovie(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })

  it('is enabled when ID is provided', () => {
    const id = ref('680')

    useTMDbMovie(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(true)
  })
})

describe('useTMDbSeriesSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const filters = ref({ query: 'breaking bad' })

    useTMDbSeriesSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['tmdb', 'series', 'search', { query: 'breaking bad' }])
  })

  it('calls API with correct filters', async () => {
    mockSearchSeries.mockResolvedValue({ results: [] })

    const filters = { query: 'stranger things', year: 2016 }

    useTMDbSeriesSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockSearchSeries).toHaveBeenCalledWith(filters)
  })

  it('is disabled when query is empty', () => {
    const filters = ref({ query: '' })

    useTMDbSeriesSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })
})

describe('useTMDbSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const id = ref('67890')

    useTMDbSeries(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['tmdb', 'series', 'detail', '67890'])
  })

  it('calls API with correct ID', async () => {
    mockGetSeriesByTMDbId.mockResolvedValue({ id: '1396', name: 'Breaking Bad' })

    useTMDbSeries('1396')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetSeriesByTMDbId).toHaveBeenCalledWith('1396')
  })

  it('is disabled when ID is empty', () => {
    const id = ref('')

    useTMDbSeries(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })
})

describe('useTMDbSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const filters = ref({ query: 'star wars' })

    useTMDbSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['tmdb', 'search', { query: 'star wars' }])
  })

  it('calls API with correct filters', async () => {
    mockSearchTMDb.mockResolvedValue({ movies: [], series: [] })

    const filters = { query: 'game of thrones' }

    useTMDbSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockSearchTMDb).toHaveBeenCalledWith(filters)
  })

  it('is disabled when query is empty', () => {
    const filters = ref({ query: '' })

    useTMDbSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })

  it('is enabled when query is provided', () => {
    const filters = ref({ query: 'the office' })

    useTMDbSearch(filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(true)
  })
})

describe('useImportTMDbData', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    const request = { tmdbId: '550', mediaType: MediaType.MOVIE }
    mockImportTMDbData.mockResolvedValue({ success: true })

    useImportTMDbData(null)

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(request)

    expect(mockImportTMDbData).toHaveBeenCalledWith(request)
  })

  it('handles movie import request', async () => {
    const movieRequest = { tmdbId: '680', mediaType: MediaType.MOVIE }
    mockImportTMDbData.mockResolvedValue({ success: true })

    useImportTMDbData(null)

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(movieRequest)

    expect(mockImportTMDbData).toHaveBeenCalledWith(
      expect.objectContaining({
        mediaType: MediaType.MOVIE,
      }),
    )
  })

  it('handles series import request', async () => {
    const seriesRequest = { tmdbId: '1396', mediaType: MediaType.SERIES }
    mockImportTMDbData.mockResolvedValue({ success: true })

    useImportTMDbData(null)

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(seriesRequest)

    expect(mockImportTMDbData).toHaveBeenCalledWith(
      expect.objectContaining({
        mediaType: MediaType.SERIES,
      }),
    )
  })
})

describe('useBuildCredits', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({ mutate: vi.fn() })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockBuildCredits.mockResolvedValue({ success: true })

    useBuildCredits()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(false)

    expect(mockBuildCredits).toHaveBeenCalledWith(false)
  })

  it('handles force refresh parameter', async () => {
    mockBuildCredits.mockResolvedValue({ success: true })

    useBuildCredits()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(true)

    expect(mockBuildCredits).toHaveBeenCalledWith(true)
  })

  it('uses false as default for forceRefresh', async () => {
    mockBuildCredits.mockResolvedValue({ success: true })

    useBuildCredits()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(undefined)

    expect(mockBuildCredits).toHaveBeenCalledWith(false)
  })
})
