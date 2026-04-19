<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import Card from 'primevue/card'
  import { ScrollToTop } from '@/components/common'
  import ActivityFilters from '@/components/activity/ActivityFilters.vue'
  import WatchEventCard from '@/components/activity/WatchEventCard.vue'
  import { useProfileStore } from '@/stores'
  import { useInfiniteWatchActivity } from '@/queries/useInfiniteWatchActivity'
  import { useSentinelInfiniteScroll } from '@/composables/useSentinelInfiniteScroll'
  import { useScrollToTop } from '@/composables/useScrollToTop'
  import { Icon } from '@/utils/enums'
  import type { WatchEventResponse } from '@/types'

  const profileStore = useProfileStore()
  const route = useRoute()
  const router = useRouter()

  const startDateObj = ref<Date | undefined>(undefined)
  const endDateObj = ref<Date | undefined>(undefined)
  const mediaTypeFilter = ref<'MOVIE' | 'SERIES' | undefined>(undefined)

  function toIsoDate(d: Date | undefined): string | undefined {
    if (!d) return undefined
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }

  function fromIsoDate(s: string): Date {
    const [y, m, d] = s.split('-').map(Number)
    return new Date(y!, m! - 1, d!)
  }

  onMounted(() => {
    if (typeof route.query.startDate === 'string')
      startDateObj.value = fromIsoDate(route.query.startDate)
    if (typeof route.query.endDate === 'string') endDateObj.value = fromIsoDate(route.query.endDate)
    if (route.query.mediaType === 'MOVIE' || route.query.mediaType === 'SERIES') {
      mediaTypeFilter.value = route.query.mediaType
    }
  })

  watch([startDateObj, endDateObj, mediaTypeFilter], () => {
    router.replace({
      query: {
        startDate: toIsoDate(startDateObj.value) ?? undefined,
        endDate: toIsoDate(endDateObj.value) ?? undefined,
        mediaType: mediaTypeFilter.value ?? undefined,
      },
    })
  })

  const filters = computed(() => ({
    profileId: profileStore.selectedProfileId ?? undefined,
    startDate: toIsoDate(startDateObj.value),
    endDate: toIsoDate(endDateObj.value),
    mediaType: mediaTypeFilter.value,
  }))

  const hasActiveFilters = computed(
    () => !!startDateObj.value || !!endDateObj.value || !!mediaTypeFilter.value,
  )

  const { data, isLoading, isFetchingNextPage, hasNextPage, fetchNextPage, error } =
    useInfiniteWatchActivity(filters, 20)

  const allEvents = computed(() => data.value?.pages.flatMap((page) => page.events ?? []) ?? [])

  const { sentinel } = useSentinelInfiniteScroll(
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  )

  const pageCount = computed(() => data.value?.pages?.length ?? 0)
  const { showButton: showScrollToTop, scrollToTop } = useScrollToTop(pageCount)

  function navigateToMedia(event: WatchEventResponse) {
    if (event.movie?.id) router.push({ name: 'movie-detail', params: { id: event.movie.id } })
    else if (event.show?.id) router.push({ name: 'series-detail', params: { id: event.show.id } })
  }
</script>

<template>
  <div class="activity-view">
    <Card v-if="!profileStore.hasSelectedProfile" severity="warn">
      <template #title>
        <i :class="Icon.EXCLAMATION_TRIANGLE" />
        No Profile Selected
      </template>
      <template #content>
        <p>Please select a profile from the header to view watch activity.</p>
      </template>
    </Card>

    <div v-else class="activity-content">
      <ActivityFilters
        v-model:start-date="startDateObj"
        v-model:end-date="endDateObj"
        v-model:media-type="mediaTypeFilter"
      />

      <div v-if="isLoading" class="loading-container">
        <i :class="Icon.SPINNER" class="pi-spin" />
        <p>Loading activity...</p>
      </div>

      <Card v-else-if="error" severity="error">
        <template #title>
          <i :class="Icon.TIMES_CIRCLE" />
          Error Loading Activity
        </template>
        <template #content
          ><p>{{ error.message }}</p></template
        >
      </Card>

      <Card v-else-if="allEvents.length === 0">
        <template #title>
          <i :class="Icon.INFO_CIRCLE" />
          No Activity Found
        </template>
        <template #content>
          <p v-if="hasActiveFilters">No watch events match the current filters.</p>
          <p v-else>No watch activity recorded yet.</p>
        </template>
      </Card>

      <div v-else class="activity-grid">
        <WatchEventCard
          v-for="event in allEvents"
          :key="event.id"
          :event="event"
          @click="navigateToMedia(event)"
        />
      </div>

      <div v-if="allEvents.length > 0" ref="sentinel" class="scroll-sentinel" />

      <div v-if="isFetchingNextPage" class="loading-more">
        <i :class="Icon.SPINNER" class="pi-spin" />
        <p>Loading more...</p>
      </div>

      <div v-if="!isLoading && !hasNextPage && allEvents.length > 0" class="end-message">
        <p>No more activity to load</p>
      </div>
    </div>

    <ScrollToTop :show="showScrollToTop" :on-scroll="scrollToTop" />
  </div>
</template>

<style scoped>
  .activity-view {
    padding-bottom: 2rem;
  }

  .activity-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .activity-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }

  .loading-container,
  .loading-more {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--p-text-secondary-color);
    padding: 1rem 0;
  }

  .end-message {
    text-align: center;
    color: var(--p-text-secondary-color);
    padding: 1rem 0;
    font-size: 0.875rem;
  }

  .scroll-sentinel {
    height: 1px;
    width: 100%;
  }
</style>
