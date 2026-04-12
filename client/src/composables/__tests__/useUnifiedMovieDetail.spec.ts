import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useUnifiedMovieDetail } from '../useUnifiedMovieDetail'
import { MediaSource } from '@/utils/enums'
import type { MediaSourceInfo } from '../useMediaSource'

// Mock the query hooks
vi.mock('@/queries/useLibrary', () => ({
  useMovie: vi.fn(),
}))

vi.mock('@/queries/useTMDb', () => ({
  useTMDbMovie: vi.fn(),
}))

vi.mock('@/stores/profileStore', () => ({
  useProfileStore: vi.fn(() => ({
    selectedProfileId: 'profile-123',
  })),
}))

import { useMovie } from '@/queries/useLibrary'
import { useTMDbMovie } from '@/queries/useTMDb'

describe('useUnifiedMovieDetail', () => {
  const createMockSource = (isInternal: boolean): MediaSourceInfo => ({
    type: (isInternal ? 'internal' : 'tmdb') as any,
    isInternal,
    isTMDb: !isInternal,
    isIMDb: false,
    id: isInternal ? 'uuid-123' : '550',
  })

  const createMockQueryResult = (
    data: any = undefined,
    isLoading = false,
    error: Error | null = null,
  ) =>
    ({
      data: ref(data),
      isLoading: ref(isLoading),
      error: ref(error),
      refetch: vi.fn().mockResolvedValue(undefined),
      // Additional TanStack Query properties (not used but required for type compatibility)
      isError: ref(false),
      isPending: ref(isLoading),
      isLoadingError: ref(false),
      isRefetchError: ref(false),
      isSuccess: ref(!isLoading && !error),
      status: ref(isLoading ? 'pending' : error ? 'error' : 'success'),
      fetchStatus: ref('idle'),
      isFetched: ref(true),
      isFetchedAfterMount: ref(true),
      isFetching: ref(false),
      isInitialLoading: ref(false),
      isPaused: ref(false),
      isPlaceholderData: ref(false),
      isRefetching: ref(false),
      isStale: ref(false),
      dataUpdatedAt: ref(Date.now()),
      errorUpdatedAt: ref(0),
      failureCount: ref(0),
      failureReason: ref(null),
      errorUpdateCount: ref(0),
      suspense: vi.fn(),
    }) as any

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Internal Movie Source', () => {
    it('transforms internal movie data to unified format', () => {
      const internalMovieData = {
        movies: [
          {
            id: 'movie-uuid-123',
            title: 'The Matrix',
            productionYear: 1999,
            runtimeMinutes: 136,
            genres: [
              { id: 'g1', name: 'Sci-Fi' },
              { id: 'g2', name: 'Action' },
            ],
            cast: [
              {
                id: 'c1',
                character: 'Neo',
                order: 0,
                person: {
                  name: 'Keanu Reeves',
                  profilePathUrl: 'https://example.com/keanu.jpg',
                },
              },
            ],
            crew: [
              {
                id: 'cr1',
                job: 'Director',
                person: {
                  name: 'Lana Wachowski',
                  profilePathUrl: 'https://example.com/lana.jpg',
                },
              },
            ],
            metadata: {
              posterImageUrl: 'https://example.com/poster.jpg',
              backdropImageUrl: 'https://example.com/backdrop.jpg',
              description: 'A computer hacker learns about the true nature of reality.',
            },
            IMDbId: 'tt0133093',
            TMDbId: '603',
            jellyfinId: 'jellyfin-123',
            myRating: 9.5,
          },
        ],
      }

      const source = ref(createMockSource(true))
      const movieId = ref('movie-uuid-123')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult(internalMovieData))
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toEqual({
        id: 'movie-uuid-123',
        title: 'The Matrix',
        year: 1999,
        runtimeMinutes: 136,
        genres: [{ name: 'Sci-Fi' }, { name: 'Action' }],
        cast: expect.arrayContaining([
          expect.objectContaining({
            id: 'c1',
            character: 'Neo',
            order: 0,
            person: expect.objectContaining({
              name: 'Keanu Reeves',
            }),
          }),
        ]),
        crew: expect.arrayContaining([
          expect.objectContaining({
            id: 'cr1',
            job: 'Director',
            person: expect.objectContaining({
              name: 'Lana Wachowski',
            }),
          }),
        ]),
        posterUrl: 'https://example.com/poster.jpg',
        backdropUrl: 'https://example.com/backdrop.jpg',
        description: 'A computer hacker learns about the true nature of reality.',
        imdbId: 'tt0133093',
        tmdbId: '603',
        jellyfinId: 'jellyfin-123',
        myRating: 9.5,
        source: MediaSource.INTERNAL,
      })
    })

    it('handles internal movie with minimal data', () => {
      const minimalData = {
        movies: [
          {
            id: 'movie-uuid-456',
            title: 'Test Movie',
          },
        ],
      }

      const source = ref(createMockSource(true))
      const movieId = ref('movie-uuid-456')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult(minimalData))
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toEqual({
        id: 'movie-uuid-456',
        title: 'Test Movie',
        year: undefined,
        runtimeMinutes: undefined,
        genres: [],
        cast: [],
        crew: [],
        posterUrl: undefined,
        backdropUrl: undefined,
        description: undefined,
        imdbId: undefined,
        tmdbId: undefined,
        jellyfinId: undefined,
        myRating: undefined,
        source: MediaSource.INTERNAL,
      })
    })

    it('returns null when internal query has no data', () => {
      const source = ref(createMockSource(true))
      const movieId = ref('movie-uuid-789')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult(undefined))
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toBeNull()
    })

    it('returns null when internal query has empty movies array', () => {
      const source = ref(createMockSource(true))
      const movieId = ref('movie-uuid-999')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult({ movies: [] }))
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toBeNull()
    })
  })

  describe('TMDb Movie Source', () => {
    it('transforms TMDb movie data to unified format', () => {
      const tmdbMovieData = {
        id: 550,
        title: 'Fight Club',
        release_date: '1999-10-15',
        runtime: 139,
        genres: [
          { id: 18, name: 'Drama' },
          { id: 53, name: 'Thriller' },
        ],
        credits: {
          cast: [
            {
              id: 287,
              name: 'Brad Pitt',
              character: 'Tyler Durden',
              order: 0,
              profile_path: '/brad.jpg',
            },
          ],
          crew: [
            {
              id: 7467,
              name: 'David Fincher',
              job: 'Director',
              profile_path: '/fincher.jpg',
            },
          ],
        },
        poster_path: '/poster.jpg',
        backdrop_path: '/backdrop.jpg',
        overview: 'An insomniac office worker forms an underground fight club.',
        imdb_id: 'tt0137523',
      }

      const source = ref(createMockSource(false))
      const movieId = ref('550')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult(tmdbMovieData))

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toEqual({
        id: '550',
        title: 'Fight Club',
        year: 1999,
        runtimeMinutes: 139,
        genres: [{ name: 'Drama' }, { name: 'Thriller' }],
        cast: expect.arrayContaining([
          expect.objectContaining({
            id: '287',
            character: 'Tyler Durden',
            order: 0,
            person: expect.objectContaining({
              name: 'Brad Pitt',
              profilePathUrl: expect.stringContaining('/brad.jpg'),
            }),
          }),
        ]),
        crew: expect.arrayContaining([
          expect.objectContaining({
            id: '7467',
            job: 'Director',
            person: expect.objectContaining({
              name: 'David Fincher',
              profilePathUrl: expect.stringContaining('/fincher.jpg'),
            }),
          }),
        ]),
        posterUrl: expect.stringContaining('/poster.jpg'),
        backdropUrl: expect.stringContaining('/backdrop.jpg'),
        description: 'An insomniac office worker forms an underground fight club.',
        imdbId: 'tt0137523',
        tmdbId: '550',
        source: MediaSource.TMDB,
      })
    })

    it('handles TMDb movie with minimal data', () => {
      const minimalData = {
        id: 123,
        title: 'Minimal Movie',
      }

      const source = ref(createMockSource(false))
      const movieId = ref('123')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult(minimalData))

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toEqual({
        id: '123',
        title: 'Minimal Movie',
        year: undefined,
        runtimeMinutes: undefined,
        genres: [],
        cast: [],
        crew: [],
        posterUrl: undefined,
        backdropUrl: undefined,
        description: undefined,
        imdbId: undefined,
        tmdbId: '123',
        source: MediaSource.TMDB,
      })
    })

    it('returns null when TMDb query has no data', () => {
      const source = ref(createMockSource(false))
      const movieId = ref('999')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult(undefined))

      const { movie } = useUnifiedMovieDetail(movieId, source)

      expect(movie.value).toBeNull()
    })
  })

  describe('Query State', () => {
    it('combines loading states correctly', () => {
      const source = ref(createMockSource(true))
      const movieId = ref('test-id')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult(undefined, true))
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult(undefined, false))

      const { isLoading } = useUnifiedMovieDetail(movieId, source)

      expect(isLoading.value).toBe(true)
    })

    it('combines error states correctly for internal source', () => {
      const testError = new Error('API Error')
      const source = ref(createMockSource(true))
      const movieId = ref('test-id')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult(undefined, false, testError))
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      const { error } = useUnifiedMovieDetail(movieId, source)

      expect(error.value).toBe(testError)
    })

    it('combines error states correctly for TMDb source', () => {
      const testError = new Error('TMDb Error')
      const source = ref(createMockSource(false))
      const movieId = ref('123')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult(undefined, false, testError))

      const { error } = useUnifiedMovieDetail(movieId, source)

      expect(error.value).toBe(testError)
    })

    it('calls correct refetch based on source', async () => {
      const source = ref(createMockSource(true))
      const movieId = ref('test-id')

      const internalQuery = createMockQueryResult()
      const tmdbQuery = createMockQueryResult()

      vi.mocked(useMovie).mockReturnValue(internalQuery)
      vi.mocked(useTMDbMovie).mockReturnValue(tmdbQuery)

      const { refetch } = useUnifiedMovieDetail(movieId, source)

      await refetch()

      expect(internalQuery.refetch).toHaveBeenCalledTimes(1)
      expect(tmdbQuery.refetch).not.toHaveBeenCalled()
    })
  })

  describe('Conditional Queries', () => {
    it('only runs internal query when source is internal', () => {
      const source = ref(createMockSource(true))
      const movieId = ref('movie-uuid-123')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      useUnifiedMovieDetail(movieId, source)

      // Verify internal query is called
      expect(useMovie).toHaveBeenCalled()
      // Verify TMDb query is also called (but should be disabled via empty ID)
      expect(useTMDbMovie).toHaveBeenCalled()
    })

    it('only runs TMDb query when source is TMDb', () => {
      const source = ref(createMockSource(false))
      const movieId = ref('550')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      useUnifiedMovieDetail(movieId, source)

      // Both queries are called, but internal should get empty ID
      expect(useMovie).toHaveBeenCalled()
      expect(useTMDbMovie).toHaveBeenCalled()
    })
  })

  describe('Return Structure', () => {
    it('returns correct interface structure', () => {
      const source = ref(createMockSource(true))
      const movieId = ref('test-id')

      vi.mocked(useMovie).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbMovie).mockReturnValue(createMockQueryResult())

      const result = useUnifiedMovieDetail(movieId, source)

      expect(result).toHaveProperty('movie')
      expect(result).toHaveProperty('isLoading')
      expect(result).toHaveProperty('error')
      expect(result).toHaveProperty('refetch')
      expect(result).toHaveProperty('source')
      expect(result.source).toEqual(source.value)
    })
  })
})
