import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useLogs } from '@/queries'
import type { LogsResponseWrapper, Pageable } from '@/types'

// Mock API
const mockGetLogs = vi.fn()
vi.mock('@/api/logs', () => ({
  getLogs: (...args: never[]) => mockGetLogs(...args),
}))

// Mock TanStack Query
const mockUseQuery = vi.fn()
vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  logKeys: {
    list: (pageable: Pageable, filters?: { level?: string; logger?: string }) => [
      'logs',
      'list',
      { pageable, filters },
    ],
  },
}))

describe('useLogs', () => {
  const defaultPageable: Pageable = { page: 0, size: 20 }

  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: ref<LogsResponseWrapper>({
        logs: [],
        pagination: {
          currentPage: 0,
          pageSize: 20,
          totalElements: 0,
          totalPages: 0,
          isFirst: true,
          isLast: true,
          hasNext: false,
          hasPrevious: false,
        },
      }),
      isLoading: ref(false),
      isError: ref(false),
    })
  })

  it('creates query with correct query key', () => {
    useLogs(defaultPageable)

    expect(mockUseQuery).toHaveBeenCalledWith(
      expect.objectContaining({
        queryKey: expect.anything(),
      }),
    )
  })

  it('creates query with pageable', () => {
    const pageable = ref({ page: 1, size: 50 })
    useLogs(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'logs',
      'list',
      { pageable: { page: 1, size: 50 }, filters: undefined },
    ])
  })

  it('creates query with level filter', () => {
    const filters = ref({ level: 'ERROR' })
    useLogs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'logs',
      'list',
      { pageable: defaultPageable, filters: { level: 'ERROR' } },
    ])
  })

  it('creates query with logger filter', () => {
    const filters = ref({ logger: 'com.rattatarr' })
    useLogs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'logs',
      'list',
      { pageable: defaultPageable, filters: { logger: 'com.rattatarr' } },
    ])
  })

  it('creates query with multiple filters', () => {
    const filters = ref({
      level: 'WARN',
      logger: 'com.rattatarr',
      startDate: '2024-02-14T10:00:00',
      endDate: '2024-02-14T15:00:00',
    })
    useLogs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'logs',
      'list',
      {
        pageable: defaultPageable,
        filters: {
          level: 'WARN',
          logger: 'com.rattatarr',
          startDate: '2024-02-14T10:00:00',
          endDate: '2024-02-14T15:00:00',
        },
      },
    ])
  })

  it('calls API with correct parameters in queryFn', async () => {
    mockGetLogs.mockResolvedValue({
      logs: [],
      pagination: {},
    })

    const filters = { level: 'ERROR', logger: 'TestLogger' }
    useLogs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetLogs).toHaveBeenCalledWith(defaultPageable, filters)
  })

  it('does not enable polling (historical logs only)', () => {
    useLogs(defaultPageable)

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.refetchInterval).toBeUndefined()
  })

  it('keeps previous data while refetching', () => {
    useLogs(defaultPageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const previousData: LogsResponseWrapper = {
      logs: [{ message: 'old log', timestamp: 123456 }],
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

    const placeholder = call.placeholderData(previousData)

    expect(placeholder).toBe(previousData)
  })

  it('updates query key when pageable changes', () => {
    const pageable = ref({ page: 0, size: 20 })
    useLogs(pageable)

    const call = mockUseQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    pageable.value = { page: 1, size: 20 }
    const key2 = call.queryKey.value

    expect(key1).toEqual(['logs', 'list', { pageable: { page: 0, size: 20 }, filters: undefined }])
    expect(key2).toEqual(['logs', 'list', { pageable: { page: 1, size: 20 }, filters: undefined }])
  })

  it('updates query key when filters change', () => {
    const filters = ref({ level: 'INFO' })
    useLogs(defaultPageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    filters.value = { level: 'ERROR' }
    const key2 = call.queryKey.value

    expect(key1).toEqual([
      'logs',
      'list',
      { pageable: defaultPageable, filters: { level: 'INFO' } },
    ])
    expect(key2).toEqual([
      'logs',
      'list',
      { pageable: defaultPageable, filters: { level: 'ERROR' } },
    ])
  })
})
