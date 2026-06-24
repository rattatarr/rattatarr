<script setup lang="ts">
  import { ref, computed, shallowRef } from 'vue'
  import { useRouter } from 'vue-router'
  import { useProfileStore } from '@/stores/profileStore'
  import { useRateMediaItem } from '@/queries/useRatings'
  import { useToast } from '@/composables/useToast'
  import { useDataPreload, type CachedRatings } from '@/composables/useDataPreload'
  import MediaPosterCard from '@/components/media/MediaPosterCard.vue'
  import RatingDialog from '@/components/common/RatingDialog.vue'
  import SourceBadge from '@/components/common/SourceBadge.vue'
  import jellyfinIcon from '@/assets/jellyfin-icon.svg'
  import type { MediaItem } from '@/types'
  import { Icon, MediaSource, MediaType, type SearchResultSource } from '@/utils/enums'

  interface Props {
    item: MediaItem
    mediaType: MediaType.MOVIE | MediaType.SERIES
    /** Optional source hint for external items (tmdb, imdb, etc.) */
    source?: SearchResultSource
    /** Show a dismiss (X) button to remove the item from a list */
    dismissible?: boolean
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{
    ratingUpdated: [rating: number]
    dismiss: []
  }>()

  const router = useRouter()
  const profileStore = useProfileStore()
  const { success, error: showError } = useToast()
  const ratingMutation = useRateMediaItem()
  const { prefetchMediaItem, isDataCached, getCachedRatings } = useDataPreload()

  // State
  const showRatingDialog = ref(false)
  let prefetchTimeout: number | null = null
  const prefetchedRatings = shallowRef<CachedRatings | undefined>(undefined)

  // Computed
  const posterUrl = computed(() => props.item.metadata?.posterImageUrl)
  const backdropUrl = computed(() => props.item.metadata?.backdropImageUrl)

  // Use backdrop if available, otherwise fallback to poster
  const dialogBackdropUrl = computed(() => backdropUrl.value || posterUrl.value)

  const displayImdbRating = computed(
    () => prefetchedRatings.value?.imdbRating ?? props.item.metadata?.imdbRating ?? undefined,
  )
  const displayRtRating = computed(
    () =>
      prefetchedRatings.value?.rottenTomatoesRating ??
      props.item.metadata?.rottenTomatoesRating ??
      undefined,
  )

  // Only show rating for internal/Jellyfin sources when a profile is selected
  const showRating = computed(() => {
    // Check if source is internal
    const isInternal =
      !props.source ||
      props.source === MediaSource.INTERNAL ||
      props.source === MediaSource.JELLYFIN

    // Only show rating if internal AND profile is selected
    return isInternal && !!profileStore.selectedProfileId
  })

  // Show source badge for external sources (TMDb, IMDb, etc.)
  const isExternalSource = computed(
    () =>
      props.source &&
      props.source !== MediaSource.INTERNAL &&
      props.source !== MediaSource.JELLYFIN,
  )

  // Methods
  function formatRating(rating: number): string {
    return rating.toFixed(1)
  }

  function handleMouseEnter() {
    if (!props.item.id) return

    if (isDataCached(props.item.id, props.mediaType, props.source)) {
      prefetchedRatings.value = getCachedRatings(props.item.id, props.mediaType, props.source)
      return
    }

    prefetchTimeout = window.setTimeout(async () => {
      await prefetchMediaItem(props.item.id!, props.mediaType, props.source)
      prefetchedRatings.value = getCachedRatings(props.item.id!, props.mediaType, props.source)
    }, 150)
  }

  function handleMouseLeave() {
    // Cancel pending prefetch if user moves away quickly
    if (prefetchTimeout) {
      clearTimeout(prefetchTimeout)
      prefetchTimeout = null
    }
  }

  function openRatingDialog() {
    showRatingDialog.value = true
  }

  function handleDismiss(event: Event) {
    event.stopPropagation()
    emit('dismiss')
  }

  function navigateToDetail() {
    if (!props.item.id) return

    const routeName = props.mediaType === MediaType.MOVIE ? 'movie-detail' : 'series-detail'

    // Add source query param for external sources (tmdb, imdb, etc.)
    if (
      props.source &&
      props.source !== MediaSource.INTERNAL &&
      props.source !== MediaSource.JELLYFIN
    ) {
      router.push({
        name: routeName,
        params: { id: props.item.id },
        query: { source: props.source },
      })
    } else {
      // Internal/Jellyfin - no query param needed
      router.push({
        name: routeName,
        params: { id: props.item.id },
      })
    }
  }

  function closeRatingDialog() {
    showRatingDialog.value = false
  }

  async function submitRating(rating: number) {
    if (!profileStore.selectedProfileId) {
      showError('No profile selected', 'Please select a profile to rate media')
      closeRatingDialog()
      return
    }

    if (!props.item.id) {
      showError('Invalid media item', 'This media item has no ID')
      closeRatingDialog()
      return
    }

    try {
      await ratingMutation.mutateAsync({
        profileId: profileStore.selectedProfileId,
        entityId: props.item.id,
        ratingMediaType: 'MEDIA_ITEM',
        rating,
      })

      success('Rating saved', `Rated "${props.item.title}" ${formatRating(rating)}/10.0`)

      // Emit event so parent can update the data
      emit('ratingUpdated', rating)

      closeRatingDialog()
    } catch (error) {
      showError(error, 'Failed to save rating')
      closeRatingDialog()
    }
  }
</script>

<template>
  <div @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
    <!-- Use MediaPosterCard for presentation -->
    <MediaPosterCard
      :title="item.title || 'Untitled'"
      :poster-url="posterUrl"
      :my-rating="item.myRating"
      :show-rating="showRating"
      :show-jellyfin-badge="!!item.jellyfinId"
      :jellyfin-icon-src="jellyfinIcon"
      :imdb-rating="displayImdbRating"
      :rotten-tomatoes-rating="displayRtRating"
      @click="navigateToDetail"
      @click:rating="openRatingDialog"
    >
      <!-- Top-right overlay: source badge (external) and/or dismiss button -->
      <template v-if="isExternalSource || dismissible" #badge-top-right>
        <div class="card-top-right">
          <button
            v-if="dismissible"
            type="button"
            class="dismiss-button"
            aria-label="Dismiss from watched but not rated"
            title="Dismiss"
            @click="handleDismiss"
          >
            <i :class="Icon.TIMES" />
          </button>
          <div v-if="isExternalSource" class="source-badge-wrapper">
            <SourceBadge :source="source!" compact />
          </div>
        </div>
      </template>
    </MediaPosterCard>

    <!-- Rating Dialog -->
    <RatingDialog
      v-model:visible="showRatingDialog"
      :title="item.title"
      :my-rating="item.myRating"
      :backdrop-url="dialogBackdropUrl"
      :is-pending="ratingMutation.isPending.value"
      @submit="submitRating"
    />
  </div>
</template>

<style scoped>
  .card-top-right {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 10;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .source-badge-wrapper {
    display: flex;
  }

  .dismiss-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    padding: 0;
    border: none;
    border-radius: 6px;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    font-size: 13px;
    cursor: pointer;
    backdrop-filter: blur(8px);
    transition: all 0.2s ease;
  }

  .dismiss-button:hover {
    background: var(--p-red-500, #ef4444);
    transform: scale(1.08);
  }
</style>
