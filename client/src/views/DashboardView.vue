<script setup lang="ts">
  import { computed, onMounted } from 'vue'
  import Card from 'primevue/card'
  import Message from 'primevue/message'
  import { useProfileStore } from '@/stores'
  import { useProfileStatistics } from '@/queries'
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

  const statistics = computed(() => statisticsData.value?.statistics)

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
      (statistics.value?.topGenresByScore?.length ?? 0) > 0,
  )
  const hasDistribution = computed(
    () =>
      (statistics.value?.ratingDistributionByInteger?.length ?? 0) > 0 ||
      (statistics.value?.ratingDistribution?.length ?? 0) > 0,
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
        :rating-consistency="statistics.ratingConsistency ?? undefined"
      />

      <!-- Row 2: Heatmap (full width) -->
      <div v-if="statistics.ratingHeatmap?.length" class="dashboard-row full-width">
        <RatingHeatmap :heatmap="statistics.ratingHeatmap" class="stretch" />
      </div>

      <!-- Row 3: Decade Preferences (full width) -->
      <div v-if="statistics.decadePreferences?.length" class="dashboard-row full-width">
        <DecadePreferences :decades="statistics.decadePreferences" class="stretch" />
      </div>

      <!-- Row 4: Actors / Directors / Producers (3 columns) -->
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

      <!-- Row 5: Rating Distribution + Top Genres (same height, two columns) -->
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
          class="stretch"
        />
      </div>

      <!-- Row 6: Genre Over Time (full width) -->
      <div v-if="statistics.genreOverTime?.length" class="dashboard-row full-width">
        <GenreOverTimeChart :data="statistics.genreOverTime" class="stretch" />
      </div>

      <!-- Row 7: Media Type Breakdown (full width) -->
      <div v-if="statistics.mediaTypeBreakdown?.length" class="dashboard-row full-width">
        <MediaTypeBreakdown :breakdown="statistics.mediaTypeBreakdown" class="stretch" />
      </div>

      <!-- Row 8: Recent Trends + Day of Week (two columns) -->
      <div
        v-if="statistics.recentTrends?.length || statistics.dayOfWeekActivity?.length"
        class="dashboard-row two-col"
      >
        <RecentTrends
          v-if="statistics.recentTrends?.length"
          :trends="statistics.recentTrends"
          class="stretch"
        />
        <DayOfWeekActivity
          v-if="statistics.dayOfWeekActivity?.length"
          :activity="statistics.dayOfWeekActivity"
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
  }
</style>
