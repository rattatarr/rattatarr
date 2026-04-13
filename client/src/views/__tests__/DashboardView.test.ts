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
})
