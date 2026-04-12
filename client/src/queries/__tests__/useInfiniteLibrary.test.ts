import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useInfiniteMovies, useInfiniteSeries } from '@/queries'
import type { MovieFilters } from '@/types'

// Mock API
const mockGetAllMovies = vi.fn()
const mockGetAllSeries = vi.fn()

vi.mock('@/api/library', () => ({
  getAllMovies: (...args: never[]) => mockGetAllMovies(...args),
  getAllSeries: (...args: never[]) => mockGetAllSeries(...args),
}))

// Mock TanStack Query
const mockUseInfiniteQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useInfiniteQuery: (options: never) => mockUseInfiniteQuery(options),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  movieKeys: {
    list: (pageable: never, filters: never) => ['movies', 'list', pageable, filters],
  },
  seriesKeys: {
    list: (pageable: never, filters: never) => ['series', 'list', pageable, filters],
  },
}))

describe('useInfiniteMovies', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseInfiniteQuery.mockReturnValue({
      data: ref(null),
      hasNextPage: ref(false),
      fetchNextPage: vi.fn(),
    })
  })

  it('creates infinite query with correct query key', () => {
    const filters = ref<MovieFilters>({ title: 'test' })
    useInfiniteMovies(filters, 24, [])

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['movies', 'list', { page: 0, size: 24, sort: [] }, { title: 'test' }])
  })

  it('calls API with page parameter in queryFn', async () => {
    mockGetAllMovies.mockResolvedValue({ movies: [], pagination: {} })

    const filters = { title: '', genres: ['Action'] }
    useInfiniteMovies(filters, 10, ['rating,desc'])

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    await call.queryFn({ pageParam: 0 })

    expect(mockGetAllMovies).toHaveBeenCalledWith(
      { page: 0, size: 10, sort: ['rating,desc'] },
      { title: '', genres: ['Action'] },
    )
  })

  it('uses default pageParam of 0', async () => {
    mockGetAllMovies.mockResolvedValue({ movies: [], pagination: {} })

    useInfiniteMovies({ title: '' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    await call.queryFn({})

    expect(mockGetAllMovies).toHaveBeenCalledWith(expect.objectContaining({ page: 0 }), {
      title: '',
    })
  })

  it('gets next page param when more pages exist', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      movies: [],
      pagination: { currentPage: 2, isLast: false },
    })

    expect(nextPage).toBe(3)
  })

  it('returns undefined when on last page', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      movies: [],
      pagination: { currentPage: 5, isLast: true },
    })

    expect(nextPage).toBeUndefined()
  })

  it('returns undefined when pagination is missing', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      movies: [],
      pagination: undefined,
    })

    expect(nextPage).toBeUndefined()
  })

  it('returns undefined when currentPage is missing', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      movies: [],
      pagination: { currentPage: undefined, isLast: false },
    })

    expect(nextPage).toBeUndefined()
  })

  it('sets initialPageParam to 0', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!

    expect(call.initialPageParam).toBe(0)
  })

  it('updates query key when filters change', () => {
    const filters = ref({ title: '', titleSearch: 'movie1' })
    useInfiniteMovies(filters)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    filters.value = { title: '', titleSearch: 'movie2' }
    const key2 = call.queryKey.value

    expect(key1[3]).toEqual({ title: '', titleSearch: 'movie1' })
    expect(key2[3]).toEqual({ title: '', titleSearch: 'movie2' })
  })

  it('updates query key when sort changes', () => {
    const sort = ref(['title,asc'])
    useInfiniteMovies({ title: '' }, 10, sort)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    sort.value = ['rating,desc']
    const key2 = call.queryKey.value

    expect(key1[2].sort).toEqual(['title,asc'])
    expect(key2[2].sort).toEqual(['rating,desc'])
  })

  it('uses default page size of 10', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey[2].size).toBe(10)
  })

  it('uses default empty sort array', () => {
    useInfiniteMovies({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey[2].sort).toEqual([])
  })
})

describe('useInfiniteSeries', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseInfiniteQuery.mockReturnValue({
      data: ref(null),
      hasNextPage: ref(false),
      fetchNextPage: vi.fn(),
    })
  })

  it('creates infinite query with correct query key', () => {
    const filters = ref({ title: '', titleSearch: 'series' })
    useInfiniteSeries(filters, 30, ['myRating,desc'])

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'series',
      'list',
      { page: 0, size: 30, sort: ['myRating,desc'] },
      { title: '', titleSearch: 'series' },
    ])
  })

  it('calls API with page parameter in queryFn', async () => {
    mockGetAllSeries.mockResolvedValue({ series: [], pagination: {} })

    const filters = { title: '', genres: ['Drama'] }
    useInfiniteSeries(filters, 15, [])

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    await call.queryFn({ pageParam: 1 })

    expect(mockGetAllSeries).toHaveBeenCalledWith(
      { page: 1, size: 15, sort: [] },
      { title: '', genres: ['Drama'] },
    )
  })

  it('gets next page param when more pages exist', () => {
    useInfiniteSeries({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      series: [],
      pagination: { currentPage: 0, isLast: false },
    })

    expect(nextPage).toBe(1)
  })

  it('returns undefined when on last page', () => {
    useInfiniteSeries({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      series: [],
      pagination: { currentPage: 10, isLast: true },
    })

    expect(nextPage).toBeUndefined()
  })

  it('sets initialPageParam to 0', () => {
    useInfiniteSeries({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!

    expect(call.initialPageParam).toBe(0)
  })

  it('uses default page size of 10', () => {
    useInfiniteSeries({ title: '' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey[2].size).toBe(10)
  })
})
