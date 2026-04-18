import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useJobs, useJob } from '@/queries/useJobs'
import type { BackgroundJob, BackgroundJobsWrapper, Pageable } from '@/types'

const mockGetJobs = vi.fn()
const mockGetJob = vi.fn()
vi.mock('@/api/jobs', () => ({
  getJobs: (...args: never[]) => mockGetJobs(...args),
  getJob: (...args: never[]) => mockGetJob(...args),
}))

const mockUseQuery = vi.fn()
vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
}))

vi.mock('../queryKeys', () => ({
  jobKeys: {
    list: (pageable: Pageable, filters?: unknown) => ['jobs', 'list', { pageable, filters }],
    detail: (id: string) => ['jobs', 'detail', id],
  },
}))

const mockWrapper: BackgroundJobsWrapper = {
  jobs: [{ id: 'abc', type: 'JELLYFIN_SYNC', status: 'COMPLETED' }],
  pagination: {
    currentPage: 0,
    pageSize: 20,
    totalElements: 1,
    totalPages: 1,
    isFirst: true,
    isLast: true,
    hasNext: false,
    hasPrevious: false,
  },
}

describe('useJobs', () => {
  const defaultPageable: Pageable = { page: 0, size: 20 }

  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: ref(mockWrapper),
      isLoading: ref(false),
      error: ref(null),
    })
  })

  it('builds query key from pageable and filters', () => {
    const filters = ref({ type: 'JELLYFIN_SYNC' as const })
    useJobs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual([
      'jobs',
      'list',
      { pageable: defaultPageable, filters: { type: 'JELLYFIN_SYNC' } },
    ])
  })

  it('calls getJobs in queryFn', async () => {
    mockGetJobs.mockResolvedValue(mockWrapper)
    const filters = { status: 'FAILED' as const }
    useJobs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetJobs).toHaveBeenCalledWith(defaultPageable, filters)
  })

  it('keeps previous data as placeholder', () => {
    useJobs(defaultPageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const prev = { jobs: [], pagination: {} }
    expect(call.placeholderData(prev)).toBe(prev)
  })

  it('updates query key when pageable changes', () => {
    const pageable = ref<Pageable>({ page: 0, size: 20 })
    useJobs(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    pageable.value = { page: 1, size: 20 }

    expect(call.queryKey.value).toEqual([
      'jobs',
      'list',
      { pageable: { page: 1, size: 20 }, filters: undefined },
    ])
  })
})

describe('useJob', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref<BackgroundJob>({ id: 'abc' }), isLoading: ref(false) })
  })

  it('builds query key from job id', () => {
    useJob('abc-123')

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.queryKey.value).toEqual(['jobs', 'detail', 'abc-123'])
  })

  it('calls getJob in queryFn', async () => {
    mockGetJob.mockResolvedValue({ id: 'abc-123' })
    useJob('abc-123')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetJob).toHaveBeenCalledWith('abc-123')
  })

  it('updates query key when id ref changes', () => {
    const id = ref('abc-123')
    useJob(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    id.value = 'def-456'

    expect(call.queryKey.value).toEqual(['jobs', 'detail', 'def-456'])
  })
})
