import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { mount } from '@vue/test-utils'
import DashboardView from '../DashboardView.vue'

const mockUseProfileStore = vi.fn()
const mockUseProfileStatistics = vi.fn()
const mockUseRecentlyWatchedUnratedMovies = vi.fn()
const mockUseRecentlyWatchedUnratedSeries = vi.fn()

vi.mock('@/stores', () => ({
  useProfileStore: () => mockUseProfileStore(),
}))

vi.mock('@/queries', () => ({
  useProfileStatistics: (...args: never[]) => mockUseProfileStatistics(...args),
}))

vi.mock('@/queries/useLibrary', () => ({
  useRecentlyWatchedUnratedMovies: (...args: never[]) =>
    mockUseRecentlyWatchedUnratedMovies(...args),
  useRecentlyWatchedUnratedSeries: (...args: never[]) =>
    mockUseRecentlyWatchedUnratedSeries(...args),
}))

vi.mock('@/composables/useRoutePreload', () => ({
  useRoutePreload: () => ({
    preloadMainRoutes: vi.fn(),
  }),
}))

describe('DashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    mockUseProfileStore.mockReturnValue({
      hasSelectedProfile: true,
      selectedProfileId: 'profile-1',
    })

    mockUseProfileStatistics.mockReturnValue({
      data: ref({
        statistics: {
          overallStats: { totalRatings: 12 },
          ratingHeatmap: [],
          uniqueMediaPlayedHeatmap: [],
        },
      }),
      isLoading: ref(false),
      isError: ref(false),
    })

    mockUseRecentlyWatchedUnratedMovies.mockReturnValue({
      data: ref({
        movies: [{ id: 'movie-1', title: 'Movie 1', metadata: {} }],
      }),
    })

    mockUseRecentlyWatchedUnratedSeries.mockReturnValue({
      data: ref({
        series: [{ id: 'series-1', title: 'Series 1', metadata: {} }],
      }),
    })
  })

  it('renders watched-unrated section under overall stats', () => {
    const wrapper = mount(DashboardView, {
      global: {
        stubs: {
          Card: {
            template:
              '<section class="card"><header><slot name="title" /></header><div><slot name="content" /></div></section>',
          },
          Message: { template: '<div><slot /></div>' },
          SelectButton: true,
          MediaItemCard: { template: '<div class="media-item-card" />' },
          OverallStatsCard: { template: '<div class="overall-stats">overall stats</div>' },
          RatingDistributionChart: true,
          MediaTypeBreakdown: true,
          TopGenres: true,
          FavoritePeople: true,
          DecadePreferences: true,
          RecentTrends: true,
          DayOfWeekActivity: true,
          RatingHeatmap: true,
          GenreOverTimeChart: true,
        },
      },
    })

    const html = wrapper.html()
    const overallIdx = html.indexOf('overall-stats')
    const watchedIdx = html.indexOf('Watched but Not Rated')

    expect(overallIdx).toBeGreaterThan(-1)
    expect(watchedIdx).toBeGreaterThan(-1)
    expect(overallIdx).toBeLessThan(watchedIdx)
  })

  it('passes jellyfin statistics data to dashboard cards', () => {
    mockUseProfileStatistics.mockReturnValue({
      data: ref({
        statistics: {
          overallStats: { totalRatings: 12 },
          runtimeStats: { totalRuntime: 120, averageRuntime: 60 },
          jellyfinRuntimeStats: { totalRuntime: 240, averageRuntime: 80 },
          mediaTypeBreakdown: [{ mediaType: 'MOVIE', count: 1, totalCount: 1, percentage: 100 }],
          jellyfinMediaTypeBreakdown: [
            { mediaType: 'MOVIE', count: 2, totalCount: 2, percentage: 100 },
          ],
          topGenresByCount: [{ genreName: 'Drama', count: 1 }],
          topGenresByScore: [{ genreName: 'Drama', averageRating: 8.5 }],
          jellyfinTopGenresByCount: [{ genreName: 'Action', count: 2 }],
          genreOverTime: [{ year: 2024, genres: [{ genreName: 'Drama', count: 1 }] }],
          jellyfinGenreOverTime: [{ year: 2024, genres: [{ genreName: 'Action', count: 2 }] }],
          dayOfWeekActivity: [{ dayOfWeek: 'MONDAY', count: 1 }],
          jellyfinDayOfWeekActivity: [{ dayOfWeek: 'MONDAY', count: 2 }],
          decadePreferences: [{ decade: 1990, count: 1, averageRating: 7.5 }],
          jellyfinDecadePreferences: [{ decade: 2000, count: 2, averageRating: 0 }],
          recentTrends: [{ period: '30_DAYS', count: 1, averageRating: 7.5 }],
          jellyfinRecentTrends: [{ period: '30_DAYS', count: 2, averageRating: 0 }],
          ratingHeatmap: [],
          uniqueMediaPlayedHeatmap: [],
        },
      }),
      isLoading: ref(false),
      isError: ref(false),
    })

    const wrapper = mount(DashboardView, {
      global: {
        stubs: {
          Card: {
            template:
              '<section class="card"><header><slot name="title" /></header><div><slot name="content" /></div></section>',
          },
          Message: { template: '<div><slot /></div>' },
          SelectButton: true,
          MediaItemCard: true,
          OverallStatsCard: {
            props: ['jellyfinRuntimeStats'],
            template: '<div class="overall-stats" :data-jellyfin="!!jellyfinRuntimeStats" />',
          },
          RatingDistributionChart: true,
          MediaTypeBreakdown: {
            props: ['jellyfinBreakdown'],
            template:
              '<div class="media-type-breakdown" :data-jellyfin="(jellyfinBreakdown?.length ?? 0) > 0" />',
          },
          TopGenres: {
            props: ['jellyfinByCount'],
            template:
              '<div class="top-genres" :data-jellyfin="(jellyfinByCount?.length ?? 0) > 0" />',
          },
          FavoritePeople: true,
          DecadePreferences: {
            props: ['jellyfinDecades'],
            template:
              '<div class="decade-preferences" :data-jellyfin="(jellyfinDecades?.length ?? 0) > 0" />',
          },
          RecentTrends: {
            props: ['jellyfinTrends'],
            template:
              '<div class="recent-trends" :data-jellyfin="(jellyfinTrends?.length ?? 0) > 0" />',
          },
          DayOfWeekActivity: {
            props: ['jellyfinActivity'],
            template:
              '<div class="day-of-week-activity" :data-jellyfin="(jellyfinActivity?.length ?? 0) > 0" />',
          },
          RatingHeatmap: true,
          GenreOverTimeChart: {
            props: ['jellyfinData'],
            template:
              '<div class="genre-over-time" :data-jellyfin="(jellyfinData?.length ?? 0) > 0" />',
          },
        },
      },
    })

    expect(wrapper.find('.overall-stats').attributes('data-jellyfin')).toBe('true')
    expect(wrapper.find('.media-type-breakdown').attributes('data-jellyfin')).toBe('true')
    expect(wrapper.find('.top-genres').attributes('data-jellyfin')).toBe('true')
    expect(wrapper.find('.decade-preferences').attributes('data-jellyfin')).toBe('true')
    expect(wrapper.find('.recent-trends').attributes('data-jellyfin')).toBe('true')
    expect(wrapper.find('.day-of-week-activity').attributes('data-jellyfin')).toBe('true')
    expect(wrapper.find('.genre-over-time').attributes('data-jellyfin')).toBe('true')
  })
})
