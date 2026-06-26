import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  useRateMediaItem,
  useImportIMDbRatings,
  useExportRatingsCsv,
  useSetReview,
  useDeleteReview,
} from '@/queries'

// Mock API
const mockRateMediaItem = vi.fn()
const mockImportIMDbRatings = vi.fn()
const mockExportRatingsCsv = vi.fn()
const mockSetReview = vi.fn()
const mockDeleteReview = vi.fn()
vi.mock('@/api/ratings', () => ({
  rateMediaItem: (...args: never[]) => mockRateMediaItem(...args),
  importIMDbRatings: (...args: never[]) => mockImportIMDbRatings(...args),
  exportRatingsCsv: (...args: never[]) => mockExportRatingsCsv(...args),
  setReview: (...args: never[]) => mockSetReview(...args),
  deleteReview: (...args: never[]) => mockDeleteReview(...args),
}))

// Mock TanStack Query
const mockUseMutation = vi.fn()
const mockInvalidateQueries = vi.fn()
const mockUseQueryClient = vi.fn(() => ({
  invalidateQueries: mockInvalidateQueries,
}))

vi.mock('@tanstack/vue-query', () => ({
  useMutation: (options: never) => mockUseMutation(options),
  useQueryClient: () => mockUseQueryClient(),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  movieKeys: {
    all: ['movies'],
  },
  seriesKeys: {
    all: ['series'],
  },
  jobKeys: {
    all: ['jobs'],
  },
}))

describe('useRateMediaItem', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockRateMediaItem.mockResolvedValue({ success: true })

    useRateMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request = { mediaItemId: '123', rating: 8.5, profileId: 'p1' }

    await call.mutationFn(request)

    expect(mockRateMediaItem).toHaveBeenCalledWith(request)
  })

  it('invalidates movie and series queries on success', async () => {
    useRateMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledTimes(2)
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['movies'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['series'] })
  })

  it('handles rating with decimal values', async () => {
    mockRateMediaItem.mockResolvedValue({ success: true })

    useRateMediaItem()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request = { mediaItemId: '456', rating: 7.5, profileId: 'p2' }

    await call.mutationFn(request)

    expect(mockRateMediaItem).toHaveBeenCalledWith(
      expect.objectContaining({
        rating: 7.5,
      }),
    )
  })
})

describe('useImportIMDbRatings', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    const file = new File(['content'], 'ratings.csv', { type: 'text/csv' })
    const profileId = 'profile-123'
    mockImportIMDbRatings.mockResolvedValue({ success: true })

    useImportIMDbRatings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn({ file, profileId })

    expect(mockImportIMDbRatings).toHaveBeenCalledWith(file, profileId)
  })

  it('invalidates movie and series queries on success', async () => {
    useImportIMDbRatings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledTimes(3)
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['movies'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['series'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['jobs'] })
  })

  it('accepts File object with profileId', async () => {
    const csvFile = new File(['test,data'], 'test.csv', { type: 'text/csv' })
    const profileId = 'f4b8ae54-8696-4b3e-bf12-6d72b47b65e8'
    mockImportIMDbRatings.mockResolvedValue({ message: 'Imported 10 ratings' })

    useImportIMDbRatings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn({ file: csvFile, profileId })

    expect(mockImportIMDbRatings).toHaveBeenCalledWith(csvFile, profileId)
    expect(csvFile.name).toBe('test.csv')
  })
})

describe('useSetReview', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockSetReview.mockResolvedValue({ success: true })

    useSetReview()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request = {
      profileId: 'p1',
      entityId: 'm1',
      ratingMediaType: 'MEDIA_ITEM',
      reviewType: 'FREE_TEXT',
      reviewText: 'Great movie',
    }

    await call.mutationFn(request)

    expect(mockSetReview).toHaveBeenCalledWith(request)
  })

  it('invalidates movie and series queries on success', async () => {
    useSetReview()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledTimes(2)
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['movies'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['series'] })
  })

  it('passes structured review fields through', async () => {
    mockSetReview.mockResolvedValue({ success: true })

    useSetReview()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request = {
      profileId: 'p1',
      entityId: 's1',
      ratingMediaType: 'MEDIA_SEASON',
      reviewType: 'STRUCTURED',
      reviewStory: 'Strong arc',
      reviewVerdict: 'Worth it',
    }

    await call.mutationFn(request)

    expect(mockSetReview).toHaveBeenCalledWith(
      expect.objectContaining({
        reviewType: 'STRUCTURED',
        ratingMediaType: 'MEDIA_SEASON',
        reviewStory: 'Strong arc',
      }),
    )
  })
})

describe('useDeleteReview', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    mockDeleteReview.mockResolvedValue({ success: true })

    useDeleteReview()

    const call = mockUseMutation.mock.calls[0]![0]!
    const request = { profileId: 'p1', entityId: 'm1', ratingMediaType: 'MEDIA_ITEM' }

    await call.mutationFn(request)

    expect(mockDeleteReview).toHaveBeenCalledWith(request)
  })

  it('invalidates movie and series queries on success', async () => {
    useDeleteReview()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledTimes(2)
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['movies'] })
    expect(mockInvalidateQueries).toHaveBeenCalledWith({ queryKey: ['series'] })
  })
})

describe('useExportRatingsCsv', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    const csv = { csvData: 'title,rating\nMovie,8.5', fileName: 'profile-ratings.csv' }
    const profileId = 'profile-123'
    mockExportRatingsCsv.mockResolvedValue(csv)

    useExportRatingsCsv()

    const call = mockUseMutation.mock.calls[0]![0]!
    const result = await call.mutationFn(profileId)

    expect(mockExportRatingsCsv).toHaveBeenCalledWith(profileId)
    expect(result).toEqual(csv)
  })
})
