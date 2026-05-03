import { describe, it, expect, vi, beforeEach } from 'vitest'
import { importSonarrSeries, testSonarrConnection } from '@/api/sonarr'
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
  id: 'job-sonarr-1',
  type: 'SONARR_IMPORT',
  status: 'PENDING',
  createdAt: '2026-04-26T10:00:00Z',
}

const mockConnectionOk: GenericResponse = {
  status: '200 OK',
  message: 'Connected',
}

describe('sonarr API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('importSonarrSeries', () => {
    it('calls POST /api/v1/sonarr/import with instance=DEFAULT', async () => {
      mockPOST.mockResolvedValue({ data: mockJob })
      mockHandleResponse.mockReturnValue(mockJob)

      await importSonarrSeries()

      expect(mockPOST).toHaveBeenCalledWith('/api/v1/sonarr/import', {
        params: { query: { instance: 'DEFAULT' } },
      })
    })

    it('returns the job from handleResponse', async () => {
      mockPOST.mockResolvedValue({ data: mockJob })
      mockHandleResponse.mockReturnValue(mockJob)

      const result = await importSonarrSeries()

      expect(result).toEqual(mockJob)
    })

    it('propagates errors from handleResponse', async () => {
      mockPOST.mockResolvedValue({ error: { status: 503 } })
      mockHandleResponse.mockRejectedValue(new Error('Sonarr unavailable'))

      await expect(importSonarrSeries()).rejects.toThrow('Sonarr unavailable')
    })
  })

  describe('testSonarrConnection', () => {
    it('calls GET /api/v1/sonarr/test with instance=DEFAULT', async () => {
      mockGET.mockResolvedValue({ data: mockConnectionOk })
      mockHandleResponse.mockReturnValue(mockConnectionOk)

      await testSonarrConnection()

      expect(mockGET).toHaveBeenCalledWith('/api/v1/sonarr/test', {
        params: { query: { instance: 'DEFAULT' } },
      })
    })

    it('returns the response from handleResponse', async () => {
      mockGET.mockResolvedValue({ data: mockConnectionOk })
      mockHandleResponse.mockReturnValue(mockConnectionOk)

      const result = await testSonarrConnection()

      expect(result).toEqual(mockConnectionOk)
    })

    it('propagates errors from handleResponse', async () => {
      mockGET.mockResolvedValue({ error: { status: 401 } })
      mockHandleResponse.mockRejectedValue(new Error('Unauthorized'))

      await expect(testSonarrConnection()).rejects.toThrow('Unauthorized')
    })
  })
})
