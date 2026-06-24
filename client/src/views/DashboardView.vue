<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import Card from 'primevue/card'
  import Message from 'primevue/message'
  import SelectButton from 'primevue/selectbutton'
  import MediaItemCard from '@/components/common/MediaItemCard.vue'
  import { useProfileStore } from '@/stores'
  import { useProfileStatistics } from '@/queries'
  import {
    useRecentlyWatchedUnratedMovies,
    useRecentlyWatchedUnratedSeries,
    useDismissWatchedUnrated,
    useRestoreWatchedUnrated,
  } from '@/queries/useLibrary'
  import {
    useInfiniteWatchedUnratedMovies,
    useInfiniteWatchedUnratedSeries,
  } from '@/queries/useInfiniteLibrary'
  import { useSentinelInfiniteScroll } from '@/composables/useSentinelInfiniteScroll'
  import { Icon } from '@/utils/enums'
  import ToggleButton from 'primevue/togglebutton'
  import { useToast } from '@/composables/useToast'
  import { useConfirm } from 'primevue/useconfirm'
  import { toast } from 'vue-sonner'
  import type { MediaItem } from '@/types'
  import { useRoutePreload } from '@/composables/useRoutePreload'
  import OverallStatsCard from '@/components/dashboard/OverallStatsCard.vue'
  import RatingDistributionChart from '@/components/dashboard/RatingDistributionChart.vue'
  import MediaTypeBreakdown from '@/components/dashboard/MediaTypeBreakdown.vue'
  import TopGenres from '@/components/dashboard/TopGenres.vue'
  import FavoritePeople from '@/components/dashboard/FavoritePeople.vue'
  import DecadePreferences from '@/components/dashboard/DecadePreferences.vue'
  import RecentTrends from '@/components/dashboard/RecentTrends.vue'
  import DayOfWeekActivity from '@/components/dashboard/DayOfWeekActivity.vue'
  import RatingHeatmap from '@/components/dashboard/RatingHeatmap.vue'
  import GenreOverTimeChart from '@/components/dashboard/GenreOverTimeChart.vue'
  import { MediaType } from '@/utils/enums'

  const router = useRouter()

  const profileStore = useProfileStore()
  const { preloadMainRoutes } = useRoutePreload()

  onMounted(() => {
    setTimeout(() => {
      preloadMainRoutes()
    }, 1000)
  })

  const statisticsRequest = computed(() => ({
    profileId: profileStore.selectedProfileId ?? '',
  }))

  const { data: statisticsData, isLoading, isError } = useProfileStatistics(statisticsRequest)

  const watchedUnratedPageable = computed(() => ({
    page: 0,
    size: 12,
    sort: ['productionYear,desc'],
  }))

  const watchedUnratedMovieFilters = computed(() => ({
    title: '',
    profileId: profileStore.selectedProfileId ?? undefined,
  }))

  const watchedUnratedSeriesFilters = computed(() => ({
    title: '',
    profileId: profileStore.selectedProfileId ?? undefined,
  }))

  const { data: watchedUnratedMoviesData } = useRecentlyWatchedUnratedMovies(
    watchedUnratedPageable,
    watchedUnratedMovieFilters,
  )
  const { data: watchedUnratedSeriesData } = useRecentlyWatchedUnratedSeries(
    watchedUnratedPageable,
    watchedUnratedSeriesFilters,
  )

  const statistics = computed(() => statisticsData.value?.statistics)
  const watchedUnratedMovies = computed(
    () => watchedUnratedMoviesData.value?.movies?.slice(0, 12) ?? [],
  )
  const watchedUnratedSeries = computed(
    () => watchedUnratedSeriesData.value?.series?.slice(0, 12) ?? [],
  )
  const hasWatchedUnrated = computed(
    () => watchedUnratedMovies.value.length > 0 || watchedUnratedSeries.value.length > 0,
  )

  // "View all" toggles per column → switch from the 12-item preview to a paginated
  // infinite list. Infinite queries stay disabled until their toggle is on.
  const WATCHED_UNRATED_SORT = ['productionYear,desc']
  const viewAllMovies = ref(false)
  const viewAllSeries = ref(false)

  const infiniteMovies = useInfiniteWatchedUnratedMovies(
    watchedUnratedMovieFilters,
    24,
    WATCHED_UNRATED_SORT,
    viewAllMovies,
  )
  const infiniteSeries = useInfiniteWatchedUnratedSeries(
    watchedUnratedSeriesFilters,
    24,
    WATCHED_UNRATED_SORT,
    viewAllSeries,
  )

  const allWatchedUnratedMovies = computed(
    () => infiniteMovies.data.value?.pages.flatMap((page) => page.movies ?? []) ?? [],
  )
  const allWatchedUnratedSeries = computed(
    () => infiniteSeries.data.value?.pages.flatMap((page) => page.series ?? []) ?? [],
  )

  // While "view all" is on but the first infinite page is still loading, keep the
  // 12-item preview visible to avoid an empty-grid flicker. The full list shares the
  // same sort, so the first items are identical and the swap is seamless.
  const displayedMovies = computed(() => {
    if (!viewAllMovies.value) return watchedUnratedMovies.value
    return allWatchedUnratedMovies.value.length
      ? allWatchedUnratedMovies.value
      : watchedUnratedMovies.value
  })
  const displayedSeries = computed(() => {
    if (!viewAllSeries.value) return watchedUnratedSeries.value
    return allWatchedUnratedSeries.value.length
      ? allWatchedUnratedSeries.value
      : watchedUnratedSeries.value
  })

  const { sentinel: moviesSentinel } = useSentinelInfiniteScroll(
    infiniteMovies.fetchNextPage,
    infiniteMovies.hasNextPage,
    infiniteMovies.isFetchingNextPage,
    infiniteMovies.isLoading,
  )
  const { sentinel: seriesSentinel } = useSentinelInfiniteScroll(
    infiniteSeries.fetchNextPage,
    infiniteSeries.hasNextPage,
    infiniteSeries.isFetchingNextPage,
    infiniteSeries.isLoading,
  )

  const { error: showError } = useToast()
  const confirm = useConfirm()
  const dismissMutation = useDismissWatchedUnrated()
  const restoreMutation = useRestoreWatchedUnrated()

  function handleDismissWatchedUnrated(
    item: MediaItem,
    mediaType: MediaType.MOVIE | MediaType.SERIES,
  ) {
    const profileId = profileStore.selectedProfileId
    if (!profileId || !item.id) return

    confirm.require({
      header: 'Remove from Watched but Not Rated',
      message: `Remove "${item.title}" from this list? It will re-appear if you watch it again later.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Remove',
      rejectLabel: 'Cancel',
      acceptProps: { severity: 'danger' },
      rejectProps: { severity: 'secondary', outlined: true },
      accept: () => dismissWatchedUnrated(item, mediaType, profileId),
    })
  }

  async function dismissWatchedUnrated(
    item: MediaItem,
    mediaType: MediaType.MOVIE | MediaType.SERIES,
    profileId: string,
  ) {
    const mediaItemId = item.id!

    try {
      await dismissMutation.mutateAsync({ mediaItemId, profileId, mediaType })
      toast.success(`Removed "${item.title}"`, {
        description: 'No longer shown in Watched but Not Rated.',
        duration: 6000,
        closeButton: true,
        action: {
          label: 'Undo',
          onClick: () => {
            restoreMutation.mutate({ mediaItemId, profileId, mediaType })
          },
        },
      })
    } catch (err) {
      showError(err, 'Failed to dismiss item')
    }
  }

  const HEATMAP_MODE_OPTIONS = [
    { label: 'Activity', value: 'activity' as const },
    { label: 'Rating', value: 'rating' as const },
  ]
  const heatmapMode = ref<'activity' | 'rating'>('activity')
  const selectedHeatmap = computed(() => {
    if (heatmapMode.value === 'activity') {
      return statistics.value?.uniqueMediaPlayedHeatmap ?? []
    }

    return statistics.value?.ratingHeatmap ?? []
  })
  const hasAnyHeatmap = computed(
    () =>
      (statistics.value?.uniqueMediaPlayedHeatmap?.length ?? 0) > 0 ||
      (statistics.value?.ratingHeatmap?.length ?? 0) > 0,
  )

  const hasActors = computed(
    () =>
      (statistics.value?.actorsByCount?.length ?? 0) > 0 ||
      (statistics.value?.actorsByScore?.length ?? 0) > 0,
  )
  const hasDirectors = computed(
    () =>
      (statistics.value?.directorsByCount?.length ?? 0) > 0 ||
      (statistics.value?.directorsByScore?.length ?? 0) > 0,
  )
  const hasProducers = computed(
    () =>
      (statistics.value?.producersByCount?.length ?? 0) > 0 ||
      (statistics.value?.producersByScore?.length ?? 0) > 0,
  )
  const hasGenres = computed(
    () =>
      (statistics.value?.topGenresByCount?.length ?? 0) > 0 ||
      (statistics.value?.topGenresByScore?.length ?? 0) > 0 ||
      (statistics.value?.jellyfinTopGenresByCount?.length ?? 0) > 0,
  )

  const hasGenreOverTime = computed(
    () =>
      (statistics.value?.genreOverTime?.length ?? 0) > 0 ||
      (statistics.value?.jellyfinGenreOverTime?.length ?? 0) > 0,
  )

  const hasMediaTypeBreakdown = computed(
    () =>
      (statistics.value?.mediaTypeBreakdown?.length ?? 0) > 0 ||
      (statistics.value?.jellyfinMediaTypeBreakdown?.length ?? 0) > 0,
  )

  const hasDayOfWeekActivity = computed(
    () =>
      (statistics.value?.dayOfWeekActivity?.length ?? 0) > 0 ||
      (statistics.value?.jellyfinDayOfWeekActivity?.length ?? 0) > 0,
  )
  const hasDistribution = computed(
    () =>
      (statistics.value?.ratingDistributionByInteger?.length ?? 0) > 0 ||
      (statistics.value?.ratingDistribution?.length ?? 0) > 0,
  )

  const hasDecadePreferences = computed(
    () =>
      (statistics.value?.decadePreferences?.length ?? 0) > 0 ||
      (statistics.value?.jellyfinDecadePreferences?.length ?? 0) > 0,
  )

  const hasRecentTrends = computed(
    () =>
      (statistics.value?.recentTrends?.length ?? 0) > 0 ||
      (statistics.value?.jellyfinRecentTrends?.length ?? 0) > 0,
  )

  function onHeatmapDayClick(date: string) {
    router.push({ name: 'activity', query: { startDate: date, endDate: date } })
  }

  function onHeatmapMonthClick(startDate: string, endDate: string) {
    router.push({ name: 'activity', query: { startDate, endDate } })
  }
</script>

<template>
  <div class="dashboard-view">
    <!-- No profile selected -->
    <Card v-if="!profileStore.hasSelectedProfile" class="welcome-card">
      <template #title>Welcome!</template>
      <template #content>
        <p>Please select a profile to see your rating statistics and insights.</p>
      </template>
    </Card>

    <!-- Loading -->
    <div v-else-if="isLoading" class="loading-placeholder">
      <div v-for="n in 6" :key="n" class="skeleton-card" />
    </div>

    <!-- Error -->
    <Message v-else-if="isError" severity="error" :closable="false">
      Failed to load statistics. Please try again.
    </Message>

    <!-- Statistics dashboard -->
    <template v-else-if="statistics">
      <!-- Row 1: Overall stats (full width) -->
      <OverallStatsCard
        v-if="statistics.overallStats"
        :overall-stats="statistics.overallStats"
        :runtime-stats="statistics.runtimeStats ?? undefined"
        :jellyfin-runtime-stats="statistics.jellyfinRuntimeStats ?? undefined"
        :rating-consistency="statistics.ratingConsistency ?? undefined"
      />

      <!-- Row 2: Watched but Unrated (full width), might be empty -> not displayed  -->
      <div v-if="hasWatchedUnrated" class="dashboard-row full-width">
        <Card class="stretch">
          <template #title>Watched but Not Rated</template>
          <template #content>
            <div class="watched-unrated-layout">
              <div v-if="displayedMovies.length" class="watched-unrated-column">
                <div class="watched-unrated-heading">
                  <h3 class="section-heading">Movies</h3>
                  <ToggleButton
                    v-model="viewAllMovies"
                    on-label="Show less"
                    off-label="View all"
                    :on-icon="Icon.CHEVRON_UP"
                    :off-icon="Icon.CHEVRON_DOWN"
                    size="small"
                  />
                </div>
                <div class="watched-unrated-grid">
                  <MediaItemCard
                    v-for="movie in displayedMovies"
                    :key="`movie-${movie.id}`"
                    :item="movie"
                    :media-type="MediaType.MOVIE"
                    dismissible
                    @dismiss="handleDismissWatchedUnrated(movie, MediaType.MOVIE)"
                  />
                </div>
                <template v-if="viewAllMovies">
                  <div ref="moviesSentinel" class="scroll-sentinel" />
                  <div v-if="infiniteMovies.isFetchingNextPage.value" class="watched-unrated-more">
                    <i :class="Icon.SPINNER" class="pi-spin" /> Loading more movies...
                  </div>
                </template>
              </div>

              <div v-if="displayedSeries.length" class="watched-unrated-column">
                <div class="watched-unrated-heading">
                  <h3 class="section-heading">Series</h3>
                  <ToggleButton
                    v-model="viewAllSeries"
                    on-label="Show less"
                    off-label="View all"
                    :on-icon="Icon.CHEVRON_UP"
                    :off-icon="Icon.CHEVRON_DOWN"
                    size="small"
                  />
                </div>
                <div class="watched-unrated-grid">
                  <MediaItemCard
                    v-for="show in displayedSeries"
                    :key="`series-${show.id}`"
                    :item="show"
                    :media-type="MediaType.SERIES"
                    dismissible
                    @dismiss="handleDismissWatchedUnrated(show, MediaType.SERIES)"
                  />
                </div>
                <template v-if="viewAllSeries">
                  <div ref="seriesSentinel" class="scroll-sentinel" />
                  <div v-if="infiniteSeries.isFetchingNextPage.value" class="watched-unrated-more">
                    <i :class="Icon.SPINNER" class="pi-spin" /> Loading more series...
                  </div>
                </template>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- Row 3: Heatmap (full width) -->
      <div v-if="hasAnyHeatmap" class="dashboard-row full-width">
        <div class="heatmap-header-left">
          <SelectButton
            v-model="heatmapMode"
            :options="HEATMAP_MODE_OPTIONS"
            option-label="label"
            option-value="value"
            :allow-empty="false"
          />
        </div>
        <RatingHeatmap
          v-if="selectedHeatmap.length"
          :heatmap="selectedHeatmap"
          :mode="heatmapMode"
          class="stretch"
          @day-click="onHeatmapDayClick"
          @month-click="onHeatmapMonthClick"
        />
        <Message v-else severity="info" :closable="false">
          No {{ heatmapMode }} heatmap data available.
        </Message>
      </div>

      <!-- Row 4: Decade Preferences (full width) -->
      <div v-if="hasDecadePreferences" class="dashboard-row full-width">
        <DecadePreferences
          :decades="statistics.decadePreferences ?? []"
          :jellyfin-decades="statistics.jellyfinDecadePreferences ?? []"
          class="stretch"
        />
      </div>

      <!-- Row 5: Actors / Directors / Producers (3 columns) -->
      <div v-if="hasActors || hasDirectors || hasProducers" class="dashboard-row three-col">
        <FavoritePeople
          v-if="hasActors"
          title="Actors"
          :by-count="statistics.actorsByCount ?? []"
          :by-score="statistics.actorsByScore ?? []"
          class="stretch"
        />
        <FavoritePeople
          v-if="hasDirectors"
          title="Directors"
          :by-count="statistics.directorsByCount ?? []"
          :by-score="statistics.directorsByScore ?? []"
          class="stretch"
        />
        <FavoritePeople
          v-if="hasProducers"
          title="Producers"
          :by-count="statistics.producersByCount ?? []"
          :by-score="statistics.producersByScore ?? []"
          class="stretch"
        />
      </div>

      <!-- Row 6: Rating Distribution + Top Genres (same height, two columns) -->
      <div v-if="hasDistribution || hasGenres" class="dashboard-row two-col">
        <RatingDistributionChart
          v-if="hasDistribution"
          :distribution="statistics.ratingDistribution ?? []"
          :distribution-by-integer="statistics.ratingDistributionByInteger ?? []"
          class="stretch"
        />
        <TopGenres
          v-if="hasGenres"
          :by-count="statistics.topGenresByCount ?? []"
          :by-score="statistics.topGenresByScore ?? []"
          :jellyfin-by-count="statistics.jellyfinTopGenresByCount ?? []"
          class="stretch"
        />
      </div>

      <!-- Row 7: Genre Over Time (full width) -->
      <div v-if="hasGenreOverTime" class="dashboard-row full-width">
        <GenreOverTimeChart
          :data="statistics.genreOverTime ?? []"
          :jellyfin-data="statistics.jellyfinGenreOverTime ?? []"
          class="stretch"
        />
      </div>

      <!-- Row 8: Media Type Breakdown (full width) -->
      <div v-if="hasMediaTypeBreakdown" class="dashboard-row full-width">
        <MediaTypeBreakdown
          :breakdown="statistics.mediaTypeBreakdown ?? []"
          :jellyfin-breakdown="statistics.jellyfinMediaTypeBreakdown ?? []"
          class="stretch"
        />
      </div>

      <!-- Row 9: Recent Trends + Day of Week (two columns) -->
      <div v-if="hasRecentTrends || hasDayOfWeekActivity" class="dashboard-row two-col">
        <RecentTrends
          v-if="hasRecentTrends"
          :trends="statistics.recentTrends ?? []"
          :jellyfin-trends="statistics.jellyfinRecentTrends ?? []"
          class="stretch"
        />
        <DayOfWeekActivity
          v-if="hasDayOfWeekActivity"
          :activity="statistics.dayOfWeekActivity ?? []"
          :jellyfin-activity="statistics.jellyfinDayOfWeekActivity ?? []"
          class="stretch"
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
  .dashboard-view {
    padding-bottom: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .welcome-card {
    margin-bottom: 0;
  }

  /* Rows */
  .dashboard-row {
    display: flex;
    gap: 1.25rem;
    width: 100%;
  }

  .full-width {
    flex-direction: column;
  }

  .two-col {
    align-items: stretch;
  }

  .two-col > * {
    flex: 1 1 0;
    min-width: 0;
  }

  .three-col > * {
    flex: 1 1 0;
    min-width: 0;
  }

  /* Children that should fill their flex slot */
  .stretch {
    width: 100%;
  }

  .section-heading {
    margin: 0 0 0.75rem;
    font-size: 1rem;
    font-weight: 600;
  }

  .watched-unrated-layout {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .watched-unrated-column {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .watched-unrated-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .watched-unrated-heading .section-heading {
    margin: 0;
  }

  .scroll-sentinel {
    height: 1px;
    width: 100%;
  }

  .watched-unrated-more {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0;
    color: var(--p-text-secondary-color);
    font-size: 0.875rem;
  }

  .watched-unrated-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }

  .heatmap-header-left {
    display: flex;
    justify-content: flex-start;
  }

  /* Loading skeleton */
  .loading-placeholder {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.25rem;
  }

  .skeleton-card {
    height: 200px;
    border-radius: 8px;
    background: var(--p-surface-200);
    animation: pulse 1.5s ease-in-out infinite;
  }

  @keyframes pulse {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.4;
    }
  }

  /* Responsive */
  @media (max-width: 1100px) {
    .three-col {
      flex-wrap: wrap;
    }

    .three-col > * {
      flex: 1 1 calc(50% - 0.625rem);
    }
  }

  @media (max-width: 700px) {
    .two-col,
    .three-col {
      flex-direction: column;
    }

    .two-col > *,
    .three-col > * {
      flex: 1 1 100%;
    }

    .watched-unrated-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
</style>
