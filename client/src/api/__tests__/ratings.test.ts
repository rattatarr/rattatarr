import { describe, it, expect, vi, beforeEach } from 'vitest'
import { APIError } from '@/api/client'
import { exportRatingsCsv } from '@/api/ratings'

const mockFetch = vi.fn()

describe('ratings API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('fetch', mockFetch)
  })

  it('calls export endpoint with text/csv accept header', async () => {
    mockFetch.mockResolvedValue(
      new Response('title,rating\nMovie,8.5', {
        status: 200,
        headers: {
          'Content-Disposition': 'attachment; filename="profile-ratings.csv"',
          'Content-Type': 'text/csv',
        },
      }),
    )

    await exportRatingsCsv('profile-123')

    expect(mockFetch).toHaveBeenCalledWith('/api/v1/ratings/export?profileId=profile-123', {
      method: 'GET',
      headers: {
        Accept: 'text/csv',
      },
    })
  })

  it('returns csv data and filename from content-disposition header', async () => {
    const csvData = 'title,rating\nMovie,9.0'
    mockFetch.mockResolvedValue(
      new Response(csvData, {
        status: 200,
        headers: {
          'Content-Disposition': 'attachment; filename="my-ratings.csv"',
          'Content-Type': 'text/csv',
        },
      }),
    )

    const result = await exportRatingsCsv('profile-abc')

    expect(result).toEqual({
      csvData,
      fileName: 'my-ratings.csv',
    })
  })

  it('uses fallback filename when header missing', async () => {
    const csvData = 'title,rating\nMovie,7.5'
    mockFetch.mockResolvedValue(
      new Response(csvData, {
        status: 200,
        headers: {
          'Content-Type': 'text/csv',
        },
      }),
    )

    const result = await exportRatingsCsv('profile-fallback')

    expect(result.fileName).toBe('profile-ratings.csv')
  })

  it('throws APIError with text body for non-json error response', async () => {
    mockFetch.mockResolvedValue(new Response('Export failed', { status: 500, statusText: 'Error' }))

    try {
      await exportRatingsCsv('profile-error')
      expect.fail('Expected exportRatingsCsv to throw')
    } catch (error) {
      expect(error).toBeInstanceOf(APIError)
      expect(error).toMatchObject({
        message: 'Export failed',
        status: 500,
      })
    }
  })
})
