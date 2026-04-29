<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue'
  import Card from 'primevue/card'
  import { useRoute } from 'vue-router'
  import { useProfileStore } from '@/stores'
  import { useInfiniteSeries } from '@/queries/useInfiniteLibrary'
  import MediaItemCard from '@/components/common/MediaItemCard.vue'
  import { MediaFiltersPanel } from '@/components/filters'
  import { Icon, MediaType } from '@/utils/enums'
  import { useMediaFilters } from '@/composables/useMediaFilters'
  import { useSentinelInfiniteScroll } from '@/composables/useSentinelInfiniteScroll'
  import { useScrollToTop } from '@/composables/useScrollToTop'
  import { useScrollRestoration } from '@/composables/useScrollRestoration'
  import { ScrollToTop } from '@/components/common'
  import { usePerson } from '@/queries/usePeople'
  import '@/styles/media-view.css'

  defineOptions({ name: 'SeriesView' })

  useScrollRestoration()

  const profileStore = useProfileStore()
  const route = useRoute()

  // Setup filters with provide/inject
  const filterState = useMediaFilters()

  const gridMode = ref<'default' | 'compact'>('default')

  // Fetch full person object when navigating with ?personId=uuid
  const personIdFromQuery = ref<string | undefined>(undefined)
  const { data: personData } = usePerson(personIdFromQuery)

  watch(personData, (person) => {
    if (person) filterState.selectedPerson.value = person
  })

  // Apply query params as initial filters (from person dialog or genre click navigation)
  onMounted(() => {
    const { personId, genre } = route.query
    if (personId && typeof personId === 'string') {
      personIdFromQuery.value = personId
    }
    if (genre && typeof genre === 'string') {
      filterState.selectedGenres.value = [genre]
    }
  })

  const hasActiveFilters = computed(() => {
    return (
      filterState.titleSearch.value !== '' ||
      filterState.selectedGenres.value.length > 0 ||
      filterState.releasedAfter.value !== undefined ||
      filterState.releasedBefore.value !== undefined
    )
  })

  // Infinite query for series
  const { data, isLoading, isFetchingNextPage, hasNextPage, fetchNextPage, error } =
    useInfiniteSeries(filterState.seriesFilters, filterState.PAGE_SIZE, filterState.sortArray)

  // Flatten all pages into a single array
  const allSeries = computed(() => {
    return data.value?.pages.flatMap((page) => page.series ?? []) ?? []
  })

  // Sentinel-based infinite scroll
  const { sentinel } = useSentinelInfiniteScroll(
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  )

  // Scroll-to-top button
  const pageCount = computed(() => data.value?.pages?.length ?? 0)
  const { showButton: showScrollToTop, scrollToTop } = useScrollToTop(pageCount)

  function handleRatingUpdate(seriesId: string, newRating: number) {
    // Find and update the series optimistically across all pages
    if (data.value) {
      for (const page of data.value.pages) {
        if (!page.series) continue
        const seriesIndex = page.series.findIndex((s) => s.id === seriesId)
        if (seriesIndex !== -1 && page.series[seriesIndex]) {
          page.series[seriesIndex] = {
            ...page.series[seriesIndex],
            myRating: newRating,
          }
          break
        }
      }
    }
    // Note: Query invalidation happens automatically via useRateMediaItem mutation
  }
</script>

<template>
  <div class="media-view">
    <Card v-if="!profileStore.hasSelectedProfile" severity="warn">
      <template #title>
        <i :class="Icon.EXCLAMATION_TRIANGLE" />
        No Profile Selected
      </template>
      <template #content>
        <p>Please select a profile from the header to view series.</p>
      </template>
    </Card>

    <div v-else class="media-content">
      <!-- Filters -->
      <MediaFiltersPanel v-model:grid-mode="gridMode" search-placeholder="Search series..." />

      <!-- Loading State -->
      <div v-if="isLoading" class="loading-container">
        <i :class="Icon.SPINNER" class="pi-spin" />
        <p>Loading series...</p>
      </div>

      <!-- Error State -->
      <Card v-else-if="error" severity="error">
        <template #title>
          <i :class="Icon.TIMES_CIRCLE" />
          Error Loading Series
        </template>
        <template #content>
          <p>{{ error.message }}</p>
        </template>
      </Card>

      <!-- Empty State -->
      <Card v-else-if="!allSeries || allSeries.length === 0">
        <template #title>
          <i :class="Icon.INFO_CIRCLE" />
          No Series Found
        </template>
        <template #content>
          <p v-if="hasActiveFilters">
            No series match your current filters. Try adjusting your search criteria.
          </p>
          <p v-else>Your series library is empty.</p>
        </template>
      </Card>

      <!-- Series Grid -->
      <div v-else :class="['media-grid', { compact: gridMode === 'compact' }]">
        <MediaItemCard
          v-for="show in allSeries"
          :key="show.id"
          :item="show"
          :media-type="MediaType.SERIES"
          @rating-updated="handleRatingUpdate(show.id!, $event)"
        />
      </div>

      <!-- Sentinel for infinite scroll - invisible trigger element -->
      <div v-if="allSeries.length > 0" ref="sentinel" class="scroll-sentinel" />

      <!-- Loading More Indicator -->
      <div v-if="isFetchingNextPage" class="loading-more">
        <i :class="Icon.SPINNER" class="pi-spin" />
        <p>Loading more series...</p>
      </div>

      <!-- End of Results -->
      <div v-if="!isLoading && !hasNextPage && allSeries.length > 0" class="end-message">
        <p>No more series to load</p>
      </div>
    </div>

    <!-- Scroll to Top Button -->
    <ScrollToTop :show="showScrollToTop" :on-scroll="scrollToTop" />
  </div>
</template>
