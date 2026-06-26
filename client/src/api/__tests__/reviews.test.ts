import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setReview, deleteReview } from '@/api/ratings'
import type { ReviewRequest, DeleteReviewRequest } from '@/types'

const mockPUT = vi.fn()
const mockDELETE = vi.fn()
const mockHandleResponse = vi.fn()

vi.mock('@/api/client', () => ({
  APIError: class APIError extends Error {},
  apiClient: {
    PUT: (...args: never[]) => mockPUT(...args),
    DELETE: (...args: never[]) => mockDELETE(...args),
  },
  handleResponse: (response: never) => mockHandleResponse(response),
}))

describe('reviews API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('upserts a review via PUT with the request body', async () => {
    const request: ReviewRequest = {
      profileId: 'p1',
      entityId: 'm1',
      ratingMediaType: 'MEDIA_ITEM',
      reviewType: 'FREE_TEXT',
      reviewText: 'Loved it',
    }
    mockPUT.mockResolvedValue({ data: { message: 'ok' } })
    mockHandleResponse.mockReturnValue({ message: 'ok' })

    const result = await setReview(request)

    expect(mockPUT).toHaveBeenCalledWith('/api/v1/ratings/review', { body: request })
    expect(result).toEqual({ message: 'ok' })
  })

  it('deletes a review via DELETE with the request body', async () => {
    const request: DeleteReviewRequest = {
      profileId: 'p1',
      entityId: 's1',
      ratingMediaType: 'MEDIA_SEASON',
    }
    mockDELETE.mockResolvedValue({ data: { message: 'deleted' } })
    mockHandleResponse.mockReturnValue({ message: 'deleted' })

    const result = await deleteReview(request)

    expect(mockDELETE).toHaveBeenCalledWith('/api/v1/ratings/review', { body: request })
    expect(result).toEqual({ message: 'deleted' })
  })
})
