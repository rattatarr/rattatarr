<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import Card from 'primevue/card'
  import Select from 'primevue/select'
  import Message from 'primevue/message'
  import ProgressSpinner from 'primevue/progressspinner'
  import { useProfileStore } from '@/stores'
  import { useAvailableRewindYears, useYearRewind } from '@/queries'
  import RewindHighlights from '@/components/rewind/RewindHighlights.vue'
  import RatingDistributionChart from '@/components/dashboard/RatingDistributionChart.vue'
  import TopGenres from '@/components/dashboard/TopGenres.vue'
  import FavoritePeople from '@/components/dashboard/FavoritePeople.vue'
  import DecadePreferences from '@/components/dashboard/DecadePreferences.vue'
  import DayOfWeekActivity from '@/components/dashboard/DayOfWeekActivity.vue'

  const profileStore = useProfileStore()
  const profileId = computed(() => profileStore.selectedProfileId ?? '')

  const { data: yearsData, isLoading: yearsLoading } = useAvailableRewindYears(profileId)

  const availableYears = computed(() => yearsData.value?.years ?? [])

  const selectedYear = ref<number | null>(null)

  watch(
    availableYears,
    (years) => {
      if (years.length > 0 && selectedYear.value === null) {
        selectedYear.value = years[0] ?? null
      }
    },
    { immediate: true },
  )

  const rewindRequest = computed(() => ({
    profileId: profileId.value,
    year: selectedYear.value ?? 0,
  }))

  const { data: rewindData, isLoading: rewindLoading, isError } = useYearRewind(rewindRequest)

  const rewind = computed(() => rewindData.value?.rewind)

  const isLoading = computed(() => yearsLoading.value || rewindLoading.value)

  const hasActors = computed(
    () =>
      (rewind.value?.actorsByCount?.length ?? 0) > 0 ||
      (rewind.value?.actorsByScore?.length ?? 0) > 0,
  )

  const hasDirectors = computed(
    () =>
      (rewind.value?.directorsByCount?.length ?? 0) > 0 ||
      (rewind.value?.directorsByScore?.length ?? 0) > 0,
  )

  const hasProducers = computed(
    () =>
      (rewind.value?.producersByCount?.length ?? 0) > 0 ||
      (rewind.value?.producersByScore?.length ?? 0) > 0,
  )

  const hasGenres = computed(
    () =>
      (rewind.value?.topGenresByCount?.length ?? 0) > 0 ||
      (rewind.value?.topGenresByScore?.length ?? 0) > 0 ||
      (rewind.value?.jellyfinTopGenresByCount?.length ?? 0) > 0,
  )

  const hasDistribution = computed(() => {
    const byInt = rewind.value?.ratingDistributionByInteger ?? []
    if (byInt.length > 0 && byInt.some((d) => (d.count ?? 0) > 0)) return true
    const dist = rewind.value?.ratingDistribution ?? []
    return dist.length > 0 && dist.some((d) => (d.count ?? 0) > 0)
  })

  const hasDayOfWeekActivity = computed(
    () =>
      (rewind.value?.dayOfWeekActivity?.length ?? 0) > 0 ||
      (rewind.value?.jellyfinDayOfWeekActivity?.length ?? 0) > 0,
  )

  const hasDecadePreferences = computed(
    () =>
      (rewind.value?.decadePreferences?.length ?? 0) > 0 ||
      (rewind.value?.jellyfinDecadePreferences?.length ?? 0) > 0,
  )
</script>

<template>
  <div class="rewind-view">
    <!-- No profile -->
    <Card v-if="!profileStore.hasSelectedProfile" class="welcome-card">
      <template #title>Year Rewind</template>
      <template #content>
        <p>Please select a profile to view your year rewind.</p>
      </template>
    </Card>

    <template v-else>
      <!-- Header: year picker -->
      <div class="rewind-header">
        <h1 class="rewind-title">Year Rewind</h1>
        <Select
          v-if="availableYears.length > 0"
          v-model="selectedYear"
          :options="availableYears"
          placeholder="Select year"
          class="year-select"
        />
        <span v-else-if="!yearsLoading" class="no-years">No rating data yet</span>
      </div>

      <!-- Loading -->
      <div v-if="isLoading" class="loading-state">
        <ProgressSpinner />
      </div>

      <!-- Error -->
      <Message v-else-if="isError" severity="error" :closable="false">
        Failed to load rewind data. Please try again.
      </Message>

      <!-- No years -->
      <Message
        v-else-if="!isLoading && availableYears.length === 0"
        severity="info"
        :closable="false"
      >
        No rating data found. Start rating movies and series to see your Year Rewind.
      </Message>

      <!-- Rewind content -->
      <template v-else-if="rewind && selectedYear">
        <!-- Highlights -->
        <Card v-if="rewind.highlights">
          <template #title>
            <span class="highlights-title">{{ selectedYear }} Highlights</span>
          </template>
          <template #content>
            <RewindHighlights :highlights="rewind.highlights" />
          </template>
        </Card>

        <!-- Rating distribution + genres -->
        <div v-if="hasDistribution || hasGenres" class="rewind-row two-col">
          <RatingDistributionChart
            v-if="hasDistribution"
            :distribution="rewind.ratingDistribution ?? []"
            :distribution-by-integer="rewind.ratingDistributionByInteger ?? []"
            class="stretch"
          />
          <TopGenres
            v-if="hasGenres"
            :by-count="rewind.topGenresByCount ?? []"
            :by-score="rewind.topGenresByScore ?? []"
            :jellyfin-by-count="rewind.jellyfinTopGenresByCount ?? []"
            class="stretch"
          />
        </div>

        <!-- Actors + directors + producers -->
        <div v-if="hasActors || hasDirectors || hasProducers" class="rewind-row three-col">
          <FavoritePeople
            v-if="hasActors"
            title="Actors"
            :by-count="rewind.actorsByCount ?? []"
            :by-score="rewind.actorsByScore ?? []"
            class="stretch"
          />
          <FavoritePeople
            v-if="hasDirectors"
            title="Directors"
            :by-count="rewind.directorsByCount ?? []"
            :by-score="rewind.directorsByScore ?? []"
            class="stretch"
          />
          <FavoritePeople
            v-if="hasProducers"
            title="Producers"
            :by-count="rewind.producersByCount ?? []"
            :by-score="rewind.producersByScore ?? []"
            class="stretch"
          />
        </div>

        <!-- Decade preferences -->
        <div v-if="hasDecadePreferences" class="rewind-row full-width">
          <DecadePreferences
            :decades="rewind.decadePreferences ?? []"
            :jellyfin-decades="rewind.jellyfinDecadePreferences ?? []"
            class="stretch"
          />
        </div>

        <!-- Day of week activity -->
        <div v-if="hasDayOfWeekActivity" class="rewind-row full-width">
          <DayOfWeekActivity
            :activity="rewind.dayOfWeekActivity ?? []"
            :jellyfin-activity="rewind.jellyfinDayOfWeekActivity ?? []"
            class="stretch"
          />
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
  .rewind-view {
    padding-bottom: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .rewind-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .rewind-title {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 700;
  }

  .highlights-title {
    display: block;
    text-align: center;
    color: var(--p-primary-color);
    text-shadow:
      0 0 12px color-mix(in srgb, var(--p-primary-color) 60%, transparent),
      0 0 24px color-mix(in srgb, var(--p-primary-color) 30%, transparent);
  }

  .year-select {
    min-width: 140px;
  }

  .no-years {
    font-size: 0.875rem;
    color: var(--p-text-muted-color);
  }

  .loading-state {
    display: flex;
    justify-content: center;
    padding: 3rem;
  }

  .rewind-row {
    display: flex;
    gap: 1.25rem;
    width: 100%;
  }

  .full-width {
    flex-direction: column;
  }

  .two-col > * {
    flex: 1 1 0;
    min-width: 0;
  }

  .three-col > * {
    flex: 1 1 0;
    min-width: 0;
  }

  .stretch {
    width: 100%;
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
