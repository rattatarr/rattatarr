import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useInfiniteWatchActivity } from '@/queries/useInfiniteWatchActivity'
import type { WatchActivityFilters, WatchActivityWrapper } from '@/types'

const mockGetWatchActivity = vi.fn()

vi.mock('@/api/watchActivity', () => ({
  getWatchActivity: (...args: never[]) => mockGetWatchActivity(...args),
}))

const mockUseInfiniteQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useInfiniteQuery: (options: never) => mockUseInfiniteQuery(options),
}))

vi.mock('../queryKeys', () => ({
  watchActivityKeys: {
    list: (pageable: never, filters: never) => ['watch-activity', 'list', pageable, filters],
  },
}))

describe('useInfiniteWatchActivity', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseInfiniteQuery.mockReturnValue({
      data: ref(null),
      hasNextPage: ref(false),
      fetchNextPage: vi.fn(),
    })
  })

  it('creates infinite query with correct query key', () => {
    const filters = ref<WatchActivityFilters>({ profileId: 'profile-1' })
    useInfiniteWatchActivity(filters, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'watch-activity',
      'list',
      { page: 0, size: 20 },
      { profileId: 'profile-1' },
    ])
  })

  it('calls API with correct pageable and filters in queryFn', async () => {
    const wrapper: WatchActivityWrapper = { events: [], pagination: {} }
    mockGetWatchActivity.mockResolvedValue(wrapper)

    const filters: WatchActivityFilters = {
      profileId: 'profile-1',
      startDate: '2024-01-01',
      endDate: '2024-01-31',
      mediaType: 'MOVIE',
    }
    useInfiniteWatchActivity(filters, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    await call.queryFn({ pageParam: 0 })

    expect(mockGetWatchActivity).toHaveBeenCalledWith(
      { page: 0, size: 20 },
      {
        profileId: 'profile-1',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        mediaType: 'MOVIE',
      },
    )
  })

  it('passes pageParam to API page field', async () => {
    mockGetWatchActivity.mockResolvedValue({ events: [], pagination: {} })

    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    await call.queryFn({ pageParam: 3 })

    expect(mockGetWatchActivity).toHaveBeenCalledWith({ page: 3, size: 20 }, expect.anything())
  })

  it('defaults pageParam to 0 when not provided', async () => {
    mockGetWatchActivity.mockResolvedValue({ events: [], pagination: {} })

    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    await call.queryFn({})

    expect(mockGetWatchActivity).toHaveBeenCalledWith({ page: 0, size: 20 }, expect.anything())
  })

  it('returns next page number when not on last page', () => {
    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      events: [],
      pagination: { currentPage: 2, isLast: false },
    })

    expect(nextPage).toBe(3)
  })

  it('returns undefined when on last page', () => {
    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      events: [],
      pagination: { currentPage: 5, isLast: true },
    })

    expect(nextPage).toBeUndefined()
  })

  it('returns undefined when pagination is missing', () => {
    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({ events: [], pagination: undefined })

    expect(nextPage).toBeUndefined()
  })

  it('returns undefined when currentPage is missing', () => {
    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    const nextPage = call.getNextPageParam({
      events: [],
      pagination: { currentPage: undefined, isLast: false },
    })

    expect(nextPage).toBeUndefined()
  })

  it('sets initialPageParam to 0', () => {
    useInfiniteWatchActivity({ profileId: 'p' }, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!

    expect(call.initialPageParam).toBe(0)
  })

  it('updates query key reactively when filters change', () => {
    const filters = ref<WatchActivityFilters>({ profileId: 'p1' })
    useInfiniteWatchActivity(filters, 20)

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!
    expect(call.queryKey.value[3]).toEqual({ profileId: 'p1' })

    filters.value = { profileId: 'p2', mediaType: 'SERIES' }
    expect(call.queryKey.value[3]).toEqual({ profileId: 'p2', mediaType: 'SERIES' })
  })

  it('uses default page size of 20', () => {
    useInfiniteWatchActivity({ profileId: 'p' })

    const call = mockUseInfiniteQuery.mock.calls[0]![0]!

    expect(call.queryKey.value[2].size).toBe(20)
  })
})
