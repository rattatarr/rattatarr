import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getLogs } from '@/api/logs'
import type { LogsResponseWrapper, Pageable } from '@/types'

// Mock the API client and handleResponse
const mockGET = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    GET: (...args: never[]) => mockGET(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

// Mock buildLogsQueryParams
vi.mock('@/api/queryParams', () => ({
  buildLogsQueryParams: (pageable: Pageable, filters?: any) => ({
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
    level: filters?.level,
    logger: filters?.logger,
    startDate: filters?.startDate,
    endDate: filters?.endDate,
    requestId: filters?.requestId,
  }),
}))

describe('logs API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getLogs', () => {
    const mockResponse: LogsResponseWrapper = {
      logs: [
        {
          timestamp: 1707912000000,
          level: 'INFO',
          logger: 'com.rattatarr.rattatarr',
          message: 'Test log message',
          mdc: { requestId: '123' },
          serviceName: 'rattatarr',
        },
      ],
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

    it('calls API with pageable only', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }

      await getLogs(pageable)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            level: undefined,
            logger: undefined,
            startDate: undefined,
            endDate: undefined,
          },
        },
      })
      expect(mockHandleResponse).toHaveBeenCalled()
    })

    it('calls API with level filter', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }
      const filters = { level: 'ERROR' }

      await getLogs(pageable, filters)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            level: 'ERROR',
            logger: undefined,
            startDate: undefined,
            endDate: undefined,
          },
        },
      })
    })

    it('calls API with logger filter', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }
      const filters = { logger: 'com.rattatarr' }

      await getLogs(pageable, filters)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            level: undefined,
            logger: 'com.rattatarr',
            startDate: undefined,
            endDate: undefined,
          },
        },
      })
    })

    it('calls API with requestId filter', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }
      const filters = { requestId: 'ab12cd34' }

      await getLogs(pageable, filters)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            level: undefined,
            logger: undefined,
            startDate: undefined,
            endDate: undefined,
            requestId: 'ab12cd34',
          },
        },
      })
    })

    it('calls API with date range filters', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }
      const filters = {
        startDate: '2024-02-14T10:00:00',
        endDate: '2024-02-14T15:00:00',
      }

      await getLogs(pageable, filters)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            level: undefined,
            logger: undefined,
            startDate: '2024-02-14T10:00:00',
            endDate: '2024-02-14T15:00:00',
          },
        },
      })
    })

    it('calls API with all filters', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 1, size: 50 }
      const filters = {
        level: 'WARN',
        logger: 'com.rattatarr',
        startDate: '2024-02-14T10:00:00',
        endDate: '2024-02-14T15:00:00',
      }

      await getLogs(pageable, filters)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 1,
            size: 50,
            sort: undefined,
            level: 'WARN',
            logger: 'com.rattatarr',
            startDate: '2024-02-14T10:00:00',
            endDate: '2024-02-14T15:00:00',
          },
        },
      })
    })

    it('includes all filter values in flat structure', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }
      const filters = {
        level: 'INFO',
        logger: undefined,
        startDate: undefined,
        endDate: undefined,
      }

      await getLogs(pageable, filters)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/logs', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
            level: 'INFO',
            logger: undefined,
            startDate: undefined,
            endDate: undefined,
          },
        },
      })
    })

    it('returns paginated logs response', async () => {
      mockGET.mockResolvedValue({ data: mockResponse })
      mockHandleResponse.mockReturnValue(mockResponse)

      const pageable: Pageable = { page: 0, size: 20 }
      const result = await getLogs(pageable)

      expect(result).toEqual(mockResponse)
      expect(result.logs).toBeDefined()
      expect(result.pagination).toBeDefined()
    })

    it('handles API errors through handleResponse', async () => {
      const mockError = new Error('API Error')
      mockGET.mockResolvedValue({ error: { status: 500 } })
      mockHandleResponse.mockRejectedValue(mockError)

      const pageable: Pageable = { page: 0, size: 20 }

      await expect(getLogs(pageable)).rejects.toThrow('API Error')
    })
  })
})
