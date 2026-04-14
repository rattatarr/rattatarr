import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getRecentlyWatchedUnratedMovies, getRecentlyWatchedUnratedSeries } from '@/api/library'
import type { MovieFilters, MoviesWrapper, Pageable, SeriesFilters, SeriesWrapper } from '@/types'

const mockGET = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    GET: (...args: never[]) => mockGET(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

vi.mock('@/api/queryParams', () => ({
  buildMoviesQueryParams: (pageable: Pageable, filters: MovieFilters) => ({
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
    title: filters.title || undefined,
    profileId: filters.profileId,
  }),
  buildSeriesQueryParams: (pageable: Pageable, filters: SeriesFilters) => ({
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
    title: filters.title || undefined,
    profileId: filters.profileId,
  }),
  buildGenresQueryParams: vi.fn(),
  buildPageableQueryParams: vi.fn(),
}))

describe('library API watched-unrated', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('calls watched-unrated movies endpoint with built query params', async () => {
    const mockWrapper: MoviesWrapper = { movies: [] }
    mockGET.mockResolvedValue({ data: mockWrapper })
    mockHandleResponse.mockReturnValue(mockWrapper)

    const pageable: Pageable = { page: 0, size: 10 }
    const filters: MovieFilters = { title: '', profileId: 'profile-1' }

    await getRecentlyWatchedUnratedMovies(pageable, filters)

    expect(mockGET).toHaveBeenCalledWith('/api/v1/library/movies/watched/unrated', {
      params: {
        query: {
          page: 0,
          size: 10,
          sort: undefined,
          title: undefined,
          profileId: 'profile-1',
        },
      },
    })
  })

  it('calls watched-unrated series endpoint with built query params', async () => {
    const mockWrapper: SeriesWrapper = { series: [] }
    mockGET.mockResolvedValue({ data: mockWrapper })
    mockHandleResponse.mockReturnValue(mockWrapper)

    const pageable: Pageable = { page: 0, size: 10 }
    const filters: SeriesFilters = { title: '', profileId: 'profile-1' }

    await getRecentlyWatchedUnratedSeries(pageable, filters)

    expect(mockGET).toHaveBeenCalledWith('/api/v1/library/series/watched/unrated', {
      params: {
        query: {
          page: 0,
          size: 10,
          sort: undefined,
          title: undefined,
          profileId: 'profile-1',
        },
      },
    })
  })
})
