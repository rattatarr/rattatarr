import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getBrokenMovies, getBrokenSeries, resolveItem } from '@/api/brokenMedia'
import type { BrokenMediaItemWrapper, BrokenMediaItem, Pageable } from '@/types'
import type { ResolveBrokenMediaItemRequest } from '@/types'

// Mock the API client and handleResponse
const mockGET = vi.fn()
const mockPATCH = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    GET: (...args: never[]) => mockGET(...args),
    PATCH: (...args: never[]) => mockPATCH(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

// Mock buildPageableQueryParams
vi.mock('@/api/queryParams', () => ({
  buildPageableQueryParams: (pageable: Pageable) => ({
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort,
  }),
}))

describe('brokenMedia API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mockWrapper: BrokenMediaItemWrapper = {
    movies: [
      {
        id: 'item-1',
        title: 'Missing Movie',
        productionYear: 2020,
        missingFields: 'overview,posterImage',
        resolved: false,
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

  const mockItem: BrokenMediaItem = {
    id: 'item-1',
    title: 'Missing Movie',
    productionYear: 2020,
    missingFields: 'overview,posterImage',
    resolved: true,
  }

  describe('getBrokenMovies', () => {
    it('calls correct endpoint with pageable params', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 0, size: 20 }
      await getBrokenMovies(pageable)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/library/movies/broken', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
          },
        },
      })
    })

    it('calls correct endpoint with page and size', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 2, size: 50 }
      await getBrokenMovies(pageable)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/library/movies/broken', {
        params: {
          query: {
            page: 2,
            size: 50,
            sort: undefined,
          },
        },
      })
    })

    it('returns the broken media wrapper response', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 0, size: 20 }
      const result = await getBrokenMovies(pageable)

      expect(result).toEqual(mockWrapper)
      expect(mockHandleResponse).toHaveBeenCalled()
    })

    it('handles API errors through handleResponse', async () => {
      const mockError = new Error('API Error')
      mockGET.mockResolvedValue({ error: { status: 500 } })
      mockHandleResponse.mockRejectedValue(mockError)

      const pageable: Pageable = { page: 0, size: 20 }
      await expect(getBrokenMovies(pageable)).rejects.toThrow('API Error')
    })
  })

  describe('getBrokenSeries', () => {
    it('calls correct endpoint with pageable params', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 0, size: 20 }
      await getBrokenSeries(pageable)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/library/series/broken', {
        params: {
          query: {
            page: 0,
            size: 20,
            sort: undefined,
          },
        },
      })
    })

    it('calls correct endpoint with page and size', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 1, size: 100 }
      await getBrokenSeries(pageable)

      expect(mockGET).toHaveBeenCalledWith('/api/v1/library/series/broken', {
        params: {
          query: {
            page: 1,
            size: 100,
            sort: undefined,
          },
        },
      })
    })

    it('returns the broken media wrapper response', async () => {
      mockGET.mockResolvedValue({ data: mockWrapper })
      mockHandleResponse.mockReturnValue(mockWrapper)

      const pageable: Pageable = { page: 0, size: 20 }
      const result = await getBrokenSeries(pageable)

      expect(result).toEqual(mockWrapper)
      expect(mockHandleResponse).toHaveBeenCalled()
    })

    it('handles API errors through handleResponse', async () => {
      const mockError = new Error('Series API Error')
      mockGET.mockResolvedValue({ error: { status: 404 } })
      mockHandleResponse.mockRejectedValue(mockError)

      const pageable: Pageable = { page: 0, size: 20 }
      await expect(getBrokenSeries(pageable)).rejects.toThrow('Series API Error')
    })
  })

  describe('resolveItem', () => {
    const mockRequest: ResolveBrokenMediaItemRequest = {
      mediaItemId: 'media-123',
    }

    it('calls correct endpoint with id path param and request body', async () => {
      mockPATCH.mockResolvedValue({ data: mockItem })
      mockHandleResponse.mockReturnValue(mockItem)

      await resolveItem('item-1', mockRequest)

      expect(mockPATCH).toHaveBeenCalledWith('/api/v1/library/broken-media-items/{id}/resolve', {
        params: { path: { id: 'item-1' } },
        body: mockRequest,
      })
    })

    it('uses the provided id in the path', async () => {
      mockPATCH.mockResolvedValue({ data: mockItem })
      mockHandleResponse.mockReturnValue(mockItem)

      await resolveItem('different-id-456', mockRequest)

      expect(mockPATCH).toHaveBeenCalledWith(
        '/api/v1/library/broken-media-items/{id}/resolve',
        expect.objectContaining({
          params: { path: { id: 'different-id-456' } },
        }),
      )
    })

    it('sends the request body', async () => {
      mockPATCH.mockResolvedValue({ data: mockItem })
      mockHandleResponse.mockReturnValue(mockItem)

      const request: ResolveBrokenMediaItemRequest = { mediaItemId: 'linked-789' }
      await resolveItem('item-1', request)

      expect(mockPATCH).toHaveBeenCalledWith(
        '/api/v1/library/broken-media-items/{id}/resolve',
        expect.objectContaining({
          body: { mediaItemId: 'linked-789' },
        }),
      )
    })

    it('returns the resolved broken media item', async () => {
      mockPATCH.mockResolvedValue({ data: mockItem })
      mockHandleResponse.mockReturnValue(mockItem)

      const result = await resolveItem('item-1', mockRequest)

      expect(result).toEqual(mockItem)
      expect(mockHandleResponse).toHaveBeenCalled()
    })

    it('handles API errors through handleResponse', async () => {
      const mockError = new Error('Resolve Error')
      mockPATCH.mockResolvedValue({ error: { status: 400 } })
      mockHandleResponse.mockRejectedValue(mockError)

      await expect(resolveItem('item-1', mockRequest)).rejects.toThrow('Resolve Error')
    })
  })
})
