import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useProfileStatistics } from '@/queries/useStatistics'
import type { ProfileStatisticsRequest } from '@/types'

// Mock statistics API
const mockGetProfileStatistics = vi.fn()

vi.mock('@/api/statistics', () => ({
  getProfileStatistics: (...args: never[]) => mockGetProfileStatistics(...args),
}))

// Mock TanStack Query
const mockUseQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  statisticsKeys: {
    all: ['statistics'],
    detail: (profileId: string) => ['statistics', profileId],
  },
}))

describe('useProfileStatistics', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const request: ProfileStatisticsRequest = { profileId: 'profile-1' }

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['statistics', 'profile-1'])
  })

  it('calls API with correct request', async () => {
    const request: ProfileStatisticsRequest = { profileId: 'profile-1' }
    mockGetProfileStatistics.mockResolvedValue({ statistics: {} })

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetProfileStatistics).toHaveBeenCalledWith(request)
  })

  it('is disabled when profileId is empty string', () => {
    const request: ProfileStatisticsRequest = { profileId: '' }

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.enabled.value).toBe(false)
  })

  it('is enabled when profileId is provided', () => {
    const request: ProfileStatisticsRequest = { profileId: 'profile-42' }

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.enabled.value).toBe(true)
  })

  it('query key updates reactively when profileId changes', () => {
    const request = ref<ProfileStatisticsRequest>({ profileId: 'profile-a' })

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual(['statistics', 'profile-a'])

    request.value = { profileId: 'profile-b' }

    expect(call.queryKey.value).toEqual(['statistics', 'profile-b'])
  })

  it('becomes disabled reactively when profileId becomes empty', () => {
    const request = ref<ProfileStatisticsRequest>({ profileId: 'profile-1' })

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(true)

    request.value = { profileId: '' }

    expect(call.enabled.value).toBe(false)
  })

  it('becomes enabled reactively when profileId is set', () => {
    const request = ref<ProfileStatisticsRequest>({ profileId: '' })

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(false)

    request.value = { profileId: 'profile-99' }

    expect(call.enabled.value).toBe(true)
  })

  it('passes reactive request value to queryFn', async () => {
    const request = ref<ProfileStatisticsRequest>({ profileId: 'profile-1' })
    mockGetProfileStatistics.mockResolvedValue({ statistics: {} })

    useProfileStatistics(request)

    const call = mockUseQuery.mock.calls[0]![0]!

    request.value = { profileId: 'profile-2' }
    await call.queryFn()

    expect(mockGetProfileStatistics).toHaveBeenCalledWith({ profileId: 'profile-2' })
  })
})
