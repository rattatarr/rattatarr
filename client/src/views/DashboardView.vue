<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import Card from 'primevue/card'
  import Message from 'primevue/message'
  import SelectButton from 'primevue/selectbutton'
  import MediaItemCard from '@/components/common/MediaItemCard.vue'
  import { useProfileStore } from '@/stores'
  import { useProfileStatistics } from '@/queries'
  import {
    useRecentlyWatchedUnratedMovies,
    useRecentlyWatchedUnratedSeries,
  } from '@/queries/useLibrary'
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
              <div v-if="watchedUnratedMovies.length" class="watched-unrated-column">
                <h3 class="section-heading">Movies</h3>
                <div class="watched-unrated-grid">
                  <MediaItemCard
                    v-for="movie in watchedUnratedMovies"
                    :key="`movie-${movie.id}`"
                    :item="movie"
                    :media-type="MediaType.MOVIE"
                  />
                </div>
              </div>

              <div v-if="watchedUnratedSeries.length" class="watched-unrated-column">
                <h3 class="section-heading">Series</h3>
                <div class="watched-unrated-grid">
                  <MediaItemCard
                    v-for="show in watchedUnratedSeries"
                    :key="`series-${show.id}`"
                    :item="show"
                    :media-type="MediaType.SERIES"
                  />
                </div>
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
