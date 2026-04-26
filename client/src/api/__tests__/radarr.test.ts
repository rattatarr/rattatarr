import { describe, it, expect, vi, beforeEach } from 'vitest'
import { importRadarrMovies, refreshRadarrRatings, testRadarrConnection } from '@/api/radarr'
import type { BackgroundJob, GenericResponse } from '@/types'

const mockPOST = vi.fn()
const mockGET = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    POST: (...args: never[]) => mockPOST(...args),
    GET: (...args: never[]) => mockGET(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

const mockJob: BackgroundJob = {
  id: 'job-radarr-1',
  type: 'RADARR_IMPORT',
  status: 'PENDING',
  createdAt: '2026-04-20T10:00:00Z',
}

const mockConnectionOk: GenericResponse = {
  status: '200 OK',
  message: 'Connected',
}

describe('radarr API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('importRadarrMovies', () => {
    it('calls POST /api/v1/radarr/import with no body', async () => {
      mockPOST.mockResolvedValue({ data: mockJob })
      mockHandleResponse.mockReturnValue(mockJob)

      await importRadarrMovies()

      expect(mockPOST).toHaveBeenCalledWith('/api/v1/radarr/import')
    })

    it('returns the job from handleResponse', async () => {
      mockPOST.mockResolvedValue({ data: mockJob })
      mockHandleResponse.mockReturnValue(mockJob)

      const result = await importRadarrMovies()

      expect(result).toEqual(mockJob)
    })

    it('propagates errors from handleResponse', async () => {
      mockPOST.mockResolvedValue({ error: { status: 503 } })
      mockHandleResponse.mockRejectedValue(new Error('Radarr unavailable'))

      await expect(importRadarrMovies()).rejects.toThrow('Radarr unavailable')
    })
  })

  describe('refreshRadarrRatings', () => {
    it('calls POST /api/v1/radarr/refresh-ratings', async () => {
      const refreshJob: BackgroundJob = { ...mockJob, type: 'RADARR_RATINGS_REFRESH' }
      mockPOST.mockResolvedValue({ data: refreshJob })
      mockHandleResponse.mockReturnValue(refreshJob)

      await refreshRadarrRatings()

      expect(mockPOST).toHaveBeenCalledWith('/api/v1/radarr/refresh-ratings')
    })

    it('returns the job from handleResponse', async () => {
      const refreshJob: BackgroundJob = { ...mockJob, type: 'RADARR_RATINGS_REFRESH' }
      mockPOST.mockResolvedValue({ data: refreshJob })
      mockHandleResponse.mockReturnValue(refreshJob)

      const result = await refreshRadarrRatings()

      expect(result).toEqual(refreshJob)
    })

    it('propagates errors from handleResponse', async () => {
      mockPOST.mockResolvedValue({ error: { status: 503 } })
      mockHandleResponse.mockRejectedValue(new Error('Radarr unavailable'))

      await expect(refreshRadarrRatings()).rejects.toThrow('Radarr unavailable')
    })
  })

  describe('testRadarrConnection', () => {
    it('calls GET /api/v1/radarr/test', async () => {
      mockGET.mockResolvedValue({ data: mockConnectionOk })
      mockHandleResponse.mockReturnValue(mockConnectionOk)

      await testRadarrConnection()

      expect(mockGET).toHaveBeenCalledWith('/api/v1/radarr/test')
    })

    it('returns the response from handleResponse', async () => {
      mockGET.mockResolvedValue({ data: mockConnectionOk })
      mockHandleResponse.mockReturnValue(mockConnectionOk)

      const result = await testRadarrConnection()

      expect(result).toEqual(mockConnectionOk)
    })

    it('propagates errors from handleResponse', async () => {
      mockGET.mockResolvedValue({ error: { status: 401 } })
      mockHandleResponse.mockRejectedValue(new Error('Unauthorized'))

      await expect(testRadarrConnection()).rejects.toThrow('Unauthorized')
    })
  })
})
