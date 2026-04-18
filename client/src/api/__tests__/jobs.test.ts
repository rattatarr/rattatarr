import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getJobs, getJob } from '@/api/jobs'
import type { BackgroundJob, BackgroundJobsWrapper, Pageable } from '@/types'

const mockGET = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    GET: (...args: never[]) => mockGET(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

vi.mock('@/api/queryParams', () => ({
  buildJobsQueryParams: (pageable: Pageable, filters?: any) => ({
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
    type: filters?.type,
    status: filters?.status,
    startDate: filters?.startDate,
    endDate: filters?.endDate,
  }),
}))

const mockJob: BackgroundJob = {
  id: 'abc-123',
  type: 'JELLYFIN_SYNC',
  status: 'COMPLETED',
  message: 'Sync done',
  createdAt: '2026-04-18T12:00:00Z',
  updatedAt: '2026-04-18T12:05:00Z',
  startedAt: '2026-04-18T12:00:01Z',
  finishedAt: '2026-04-18T12:05:00Z',
}

const mockWrapper: BackgroundJobsWrapper = {
  jobs: [mockJob],
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

describe('jobs API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getJobs', () => {
    it('calls GET /api/v1/jobs with flat pageable params', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 0, size: 20 }
      await getJobs(pageable)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/jobs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            type: undefined,
            status: undefined,
            startDate: undefined,
            endDate: undefined,
          },
        },
      })
    })

    it('passes type filter', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      await getJobs({ page: 0, size: 20 }, { type: 'JELLYFIN_SYNC' })

      expect(mockGET).toHaveBeenCalledWith(
        '/api/v1/jobs',
        expect.objectContaining({
          params: expect.objectContaining({
            query: expect.objectContaining({ type: 'JELLYFIN_SYNC' }),
          }),
        }),
      )
    })

    it('passes status filter', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      await getJobs({ page: 0, size: 20 }, { status: 'FAILED' })

      expect(mockGET).toHaveBeenCalledWith(
        '/api/v1/jobs',
        expect.objectContaining({
          params: expect.objectContaining({
            query: expect.objectContaining({ status: 'FAILED' }),
          }),
        }),
      )
    })

    it('passes date range filters', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      await getJobs(
        { page: 0, size: 20 },
        { startDate: '2026-04-01T00:00:00', endDate: '2026-04-18T23:59:59' },
      )

      expect(mockGET).toHaveBeenCalledWith(
        '/api/v1/jobs',
        expect.objectContaining({
          params: expect.objectContaining({
            query: expect.objectContaining({
              startDate: '2026-04-01T00:00:00',
              endDate: '2026-04-18T23:59:59',
            }),
          }),
        }),
      )
    })

    it('returns the wrapper from handleResponse', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const result = await getJobs({ page: 0, size: 20 })

      expect(result).toEqual(mockWrapper)
    })

    it('propagates errors from handleResponse', async () => {
      mockGET.mockResolvedValue({ error: { status: 500 } })
      mockHandleResponse.mockRejectedValue(new Error('Server error'))

      await expect(getJobs({ page: 0, size: 20 })).rejects.toThrow('Server error')
    })
  })

  describe('getJob', () => {
    it('calls GET /api/v1/jobs/{id} with correct path param', async () => {
      mockGET.mockResolvedValue({ data: mockJob })
      mockHandleResponse.mockReturnValue(mockJob)

      await getJob('abc-123')

      expect(mockGET).toHaveBeenCalledWith('/api/v1/jobs/{id}', {
        params: { path: { id: 'abc-123' } },
      })
    })

    it('returns the job from handleResponse', async () => {
      mockGET.mockResolvedValue({ data: mockJob })
      mockHandleResponse.mockReturnValue(mockJob)

      const result = await getJob('abc-123')

      expect(result).toEqual(mockJob)
    })

    it('propagates errors from handleResponse', async () => {
      mockGET.mockResolvedValue({ error: { status: 404 } })
      mockHandleResponse.mockRejectedValue(new Error('Not found'))

      await expect(getJob('missing')).rejects.toThrow('Not found')
    })
  })
})
