import { describe, it, expect, vi, beforeEach } from 'vitest'
import { importRadarrMovies } from '@/api/radarr'
import type { BackgroundJob } from '@/types'

const mockPOST = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  apiClient: {
    POST: (...args: never[]) => mockPOST(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

const mockJob: BackgroundJob = {
  id: 'job-radarr-1',
  type: 'RADARR_IMPORT',
  status: 'PENDING',
  createdAt: '2026-04-20T10:00:00Z',
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
})
