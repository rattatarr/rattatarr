import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useUnifiedSeriesDetail } from '../useUnifiedSeriesDetail'
import { MediaSource } from '@/utils/enums'
import type { MediaSourceInfo } from '../useMediaSource'

// Mock the query hooks
vi.mock('@/queries/useLibrary', () => ({
  useSeriesDetail: vi.fn(),
}))

vi.mock('@/queries/useTMDb', () => ({
  useTMDbSeries: vi.fn(),
}))

vi.mock('@/stores/profileStore', () => ({
  useProfileStore: vi.fn(() => ({
    selectedProfileId: 'profile-123',
  })),
}))

import { useSeriesDetail } from '@/queries/useLibrary'
import { useTMDbSeries } from '@/queries/useTMDb'

describe('useUnifiedSeriesDetail', () => {
  const createMockSource = (isInternal: boolean): MediaSourceInfo => ({
    type: (isInternal ? 'internal' : 'tmdb') as any,
    isInternal,
    isTMDb: !isInternal,
    isIMDb: false,
    id: isInternal ? 'uuid-456' : '1396',
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

  describe('Internal Series Source', () => {
    it('transforms internal series data to unified format', () => {
      const internalSeriesData = {
        series: [
          {
            id: 'series-uuid-123',
            title: 'Breaking Bad',
            productionYear: 2008,
            runtimeMinutes: 47,
            genres: [
              { id: 'g1', name: 'Crime' },
              { id: 'g2', name: 'Drama' },
            ],
            cast: [
              {
                id: 'c1',
                character: 'Walter White',
                order: 0,
                person: {
                  name: 'Bryan Cranston',
                  profilePathUrl: 'https://example.com/bryan.jpg',
                },
              },
            ],
            crew: [
              {
                id: 'cr1',
                job: 'Creator',
                person: {
                  name: 'Vince Gilligan',
                  profilePathUrl: 'https://example.com/vince.jpg',
                },
              },
            ],
            metadata: {
              posterImageUrl: 'https://example.com/poster.jpg',
              backdropImageUrl: 'https://example.com/backdrop.jpg',
              description: 'A chemistry teacher turned meth cook.',
            },
            IMDbId: 'tt0903747',
            TMDbId: '1396',
            jellyfinId: 'jellyfin-series-123',
            myRating: 9.8,
            seasons: [
              {
                season: 1,
                title: 'Season 1',
                metadata: {
                  episodeCount: 7,
                  airDate: '2008-01-20',
                  posterImageUrl: 'https://example.com/s1.jpg',
                },
                episodes: [
                  {
                    id: 'ep1',
                    episode: 1,
                    title: 'Pilot',
                    runtimeMinutes: 58,
                  },
                ],
              },
            ],
          },
        ],
      }

      const source = ref(createMockSource(true))
      const seriesId = ref('series-uuid-123')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(internalSeriesData))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      expect(series.value).toEqual({
        id: 'series-uuid-123',
        title: 'Breaking Bad',
        year: 2008,
        runtimeMinutes: 47,
        genres: [{ name: 'Crime' }, { name: 'Drama' }],
        cast: expect.arrayContaining([
          expect.objectContaining({
            id: 'c1',
            character: 'Walter White',
          }),
        ]),
        crew: expect.arrayContaining([
          expect.objectContaining({
            id: 'cr1',
            job: 'Creator',
          }),
        ]),
        posterUrl: 'https://example.com/poster.jpg',
        backdropUrl: 'https://example.com/backdrop.jpg',
        description: 'A chemistry teacher turned meth cook.',
        imdbId: 'tt0903747',
        tmdbId: '1396',
        jellyfinId: 'jellyfin-series-123',
        myRating: 9.8,
        seasons: expect.arrayContaining([
          expect.objectContaining({
            seasonNumber: 1,
            title: 'Season 1',
            episodeCount: 7,
            episodes: expect.arrayContaining([
              expect.objectContaining({
                id: 'ep1',
                episode: 1,
                title: 'Pilot',
                runtimeMinutes: 58,
              }),
            ]),
            initiallyExpanded: true, // Season 1 is initially expanded
          }),
        ]),
        source: MediaSource.INTERNAL,
      })
    })

    it('filters out empty specials (season 0)', () => {
      const seriesWithEmptySpecials = {
        series: [
          {
            id: 'series-uuid-456',
            title: 'Test Series',
            seasons: [
              {
                season: 0,
                title: 'Specials',
                episodes: [], // Empty specials should be filtered out
              },
              {
                season: 1,
                title: 'Season 1',
                episodes: [{ id: 'ep1', episode: 1, title: 'Episode 1' }],
              },
            ],
          },
        ],
      }

      const source = ref(createMockSource(true))
      const seriesId = ref('series-uuid-456')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(seriesWithEmptySpecials))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      // Should only have season 1
      expect(series.value?.seasons).toHaveLength(1)
      expect(series.value?.seasons[0]?.seasonNumber).toBe(1)
    })

    it('includes specials (season 0) when they have episodes', () => {
      const seriesWithSpecials = {
        series: [
          {
            id: 'series-uuid-789',
            title: 'Test Series',
            seasons: [
              {
                season: 0,
                title: 'Specials',
                episodes: [{ id: 'special1', episode: 1, title: 'Special Episode' }],
              },
              {
                season: 1,
                title: 'Season 1',
                episodes: [{ id: 'ep1', episode: 1, title: 'Episode 1' }],
              },
            ],
          },
        ],
      }

      const source = ref(createMockSource(true))
      const seriesId = ref('series-uuid-789')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(seriesWithSpecials))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      // Should have both specials and season 1
      expect(series.value?.seasons).toHaveLength(2)
      expect(series.value?.seasons[0]?.seasonNumber).toBe(0)
      expect(series.value?.seasons[1]?.seasonNumber).toBe(1)
    })

    it('sorts seasons by season number', () => {
      const unsortedSeasons = {
        series: [
          {
            id: 'series-uuid-999',
            title: 'Test Series',
            seasons: [
              { season: 3, title: 'Season 3', episodes: [] },
              { season: 1, title: 'Season 1', episodes: [] },
              { season: 2, title: 'Season 2', episodes: [] },
            ],
          },
        ],
      }

      const source = ref(createMockSource(true))
      const seriesId = ref('series-uuid-999')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(unsortedSeasons))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      expect(series.value?.seasons.map((s) => s.seasonNumber)).toEqual([1, 2, 3])
    })

    it('returns null when internal query has no data', () => {
      const source = ref(createMockSource(true))
      const seriesId = ref('series-uuid-none')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(undefined))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      expect(series.value).toBeNull()
    })
  })

  describe('TMDb Series Source', () => {
    it('transforms TMDb series data to unified format', () => {
      const tmdbSeriesData = {
        id: 1396,
        name: 'Breaking Bad',
        first_air_date: '2008-01-20',
        episode_run_time: [47],
        genres: [
          { id: 80, name: 'Crime' },
          { id: 18, name: 'Drama' },
        ],
        credits: {
          cast: [
            {
              id: 17419,
              name: 'Bryan Cranston',
              character: 'Walter White',
              order: 0,
              profile_path: '/bryan.jpg',
            },
          ],
          crew: [
            {
              id: 66633,
              name: 'Vince Gilligan',
              job: 'Creator',
              profile_path: '/vince.jpg',
            },
          ],
        },
        poster_path: '/poster.jpg',
        backdrop_path: '/backdrop.jpg',
        overview: 'A chemistry teacher turned meth cook.',
        allSeasonDetails: {
          '1': {
            season_number: 1,
            name: 'Season 1',
            air_date: '2008-01-20',
            poster_path: '/s1.jpg',
            episodes: [
              {
                id: 62085,
                episode_number: 1,
                name: 'Pilot',
                runtime: 58,
              },
            ],
          },
        },
      }

      const source = ref(createMockSource(false))
      const seriesId = ref('1396')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult(tmdbSeriesData))

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      expect(series.value).toEqual({
        id: '1396',
        title: 'Breaking Bad',
        year: 2008,
        runtimeMinutes: 47,
        genres: [{ name: 'Crime' }, { name: 'Drama' }],
        cast: expect.arrayContaining([
          expect.objectContaining({
            id: '17419',
            character: 'Walter White',
          }),
        ]),
        crew: expect.arrayContaining([
          expect.objectContaining({
            id: '66633',
            job: 'Creator',
          }),
        ]),
        posterUrl: expect.stringContaining('/poster.jpg'),
        backdropUrl: expect.stringContaining('/backdrop.jpg'),
        description: 'A chemistry teacher turned meth cook.',
        tmdbId: '1396',
        seasons: expect.arrayContaining([
          expect.objectContaining({
            seasonNumber: 1,
            title: 'Season 1',
            episodes: expect.arrayContaining([
              expect.objectContaining({
                id: expect.any(String),
                episode: 1,
                title: 'Pilot',
                runtimeMinutes: 58,
              }),
            ]),
            initiallyExpanded: true, // Season 1 is initially expanded
          }),
        ]),
        source: MediaSource.TMDB,
      })
    })

    it('handles TMDb series with minimal data', () => {
      const minimalData = {
        id: 123,
        name: 'Minimal Series',
      }

      const source = ref(createMockSource(false))
      const seriesId = ref('123')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult(minimalData))

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      expect(series.value).toEqual({
        id: '123',
        title: 'Minimal Series',
        year: undefined,
        runtimeMinutes: undefined,
        genres: [],
        cast: [],
        crew: [],
        posterUrl: undefined,
        backdropUrl: undefined,
        description: undefined,
        tmdbId: '123',
        seasons: [],
        source: MediaSource.TMDB,
      })
    })

    it('returns null when TMDb query has no data', () => {
      const source = ref(createMockSource(false))
      const seriesId = ref('999')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult(undefined))

      const { series } = useUnifiedSeriesDetail(seriesId, source)

      expect(series.value).toBeNull()
    })
  })

  describe('Query State', () => {
    it('combines loading states correctly', () => {
      const source = ref(createMockSource(true))
      const seriesId = ref('test-id')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(undefined, true))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult(undefined, false))

      const { isLoading } = useUnifiedSeriesDetail(seriesId, source)

      expect(isLoading.value).toBe(true)
    })

    it('combines error states correctly for internal source', () => {
      const testError = new Error('API Error')
      const source = ref(createMockSource(true))
      const seriesId = ref('test-id')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult(undefined, false, testError))
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const { error } = useUnifiedSeriesDetail(seriesId, source)

      expect(error.value).toBe(testError)
    })

    it('combines error states correctly for TMDb source', () => {
      const testError = new Error('TMDb Error')
      const source = ref(createMockSource(false))
      const seriesId = ref('123')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult(undefined, false, testError))

      const { error } = useUnifiedSeriesDetail(seriesId, source)

      expect(error.value).toBe(testError)
    })

    it('calls correct refetch based on source', async () => {
      const source = ref(createMockSource(true))
      const seriesId = ref('test-id')

      const internalQuery = createMockQueryResult()
      const tmdbQuery = createMockQueryResult()

      vi.mocked(useSeriesDetail).mockReturnValue(internalQuery)
      vi.mocked(useTMDbSeries).mockReturnValue(tmdbQuery)

      const { refetch } = useUnifiedSeriesDetail(seriesId, source)

      await refetch()

      expect(internalQuery.refetch).toHaveBeenCalledTimes(1)
      expect(tmdbQuery.refetch).not.toHaveBeenCalled()
    })
  })

  describe('Return Structure', () => {
    it('returns correct interface structure', () => {
      const source = ref(createMockSource(true))
      const seriesId = ref('test-id')

      vi.mocked(useSeriesDetail).mockReturnValue(createMockQueryResult())
      vi.mocked(useTMDbSeries).mockReturnValue(createMockQueryResult())

      const result = useUnifiedSeriesDetail(seriesId, source)

      expect(result).toHaveProperty('series')
      expect(result).toHaveProperty('isLoading')
      expect(result).toHaveProperty('error')
      expect(result).toHaveProperty('refetch')
      expect(result).toHaveProperty('source')
      expect(result.source).toEqual(source.value)
    })
  })
})
