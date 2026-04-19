import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getWatchActivity } from '@/api/watchActivity'
import type { Pageable, WatchActivityFilters, WatchActivityWrapper } from '@/types'

const mockGET = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    GET: (...args: never[]) => mockGET(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

describe('getWatchActivity', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('calls watch-activity endpoint with flat query params', async () => {
    const wrapper: WatchActivityWrapper = { events: [], pagination: {} }
    mockGET.mockResolvedValue({ data: wrapper })
    mockHandleResponse.mockReturnValue(wrapper)

    const pageable: Pageable = { page: 0, size: 20 }
    const filters: WatchActivityFilters = { profileId: 'profile-1' }

    await getWatchActivity(pageable, filters)

    expect(mockGET).toHaveBeenCalledWith('/api/v1/watch-activity', {
      params: {
        query: {
          page: 0,
          size: 20,
          sort: undefined,
          profileId: 'profile-1',
          startDate: undefined,
          endDate: undefined,
          mediaType: undefined,
        },
      },
    })
  })

  it('passes date range filters', async () => {
    const wrapper: WatchActivityWrapper = { events: [] }
    mockGET.mockResolvedValue({ data: wrapper })
    mockHandleResponse.mockReturnValue(wrapper)

    const pageable: Pageable = { page: 0, size: 20 }
    const filters: WatchActivityFilters = {
      profileId: 'profile-1',
      startDate: '2024-01-01',
      endDate: '2024-01-31',
    }

    await getWatchActivity(pageable, filters)

    expect(mockGET).toHaveBeenCalledWith('/api/v1/watch-activity', {
      params: {
        query: expect.objectContaining({
          startDate: '2024-01-01',
          endDate: '2024-01-31',
        }),
      },
    })
  })

  it('passes mediaType filter', async () => {
    const wrapper: WatchActivityWrapper = { events: [] }
    mockGET.mockResolvedValue({ data: wrapper })
    mockHandleResponse.mockReturnValue(wrapper)

    const pageable: Pageable = { page: 0, size: 20 }
    const filters: WatchActivityFilters = { profileId: 'profile-1', mediaType: 'MOVIE' }

    await getWatchActivity(pageable, filters)

    expect(mockGET).toHaveBeenCalledWith('/api/v1/watch-activity', {
      params: {
        query: expect.objectContaining({ mediaType: 'MOVIE' }),
      },
    })
  })

  it('passes pagination params', async () => {
    const wrapper: WatchActivityWrapper = { events: [] }
    mockGET.mockResolvedValue({ data: wrapper })
    mockHandleResponse.mockReturnValue(wrapper)

    await getWatchActivity({ page: 3, size: 10 }, {})

    expect(mockGET).toHaveBeenCalledWith('/api/v1/watch-activity', {
      params: {
        query: expect.objectContaining({ page: 3, size: 10 }),
      },
    })
  })

  it('returns data from handleResponse', async () => {
    const wrapper: WatchActivityWrapper = {
      events: [{ id: 'evt-1', eventType: 'COMPLETE' }],
      pagination: { currentPage: 0, totalPages: 1, isLast: true },
    }
    mockGET.mockResolvedValue({ data: wrapper })
    mockHandleResponse.mockReturnValue(wrapper)

    const result = await getWatchActivity({ page: 0, size: 20 }, {})

    expect(result).toBe(wrapper)
  })
})
