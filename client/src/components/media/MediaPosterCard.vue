<script setup lang="ts">
  import { ref } from 'vue'
  import Skeleton from 'primevue/skeleton'
  import { Icon } from '@/utils/enums'

  interface Props {
    title: string
    subtitle?: string
    posterUrl?: string
    showJellyfinBadge?: boolean
    jellyfinIconSrc?: string
    myRating?: number
    showRating?: boolean
  }

  withDefaults(defineProps<Props>(), {
    showRating: false,
  })

  const emit = defineEmits<{
    click: []
    'click:rating': []
  }>()

  const imageError = ref(false)
  const imageLoading = ref(true)

  function onImageLoad() {
    imageLoading.value = false
  }

  function onImageError() {
    imageError.value = true
    imageLoading.value = false
  }

  const formatRating = (rating: number): string => {
    return rating.toFixed(1)
  }

  function handleClick() {
    emit('click')
  }

  function handleRatingClick(event: Event) {
    event.stopPropagation()
    emit('click:rating')
  }
</script>

<template>
  <div class="media-poster-card" @click="handleClick">
    <!-- Jellyfin Badge Slot -->
    <slot name="badge-top-right">
      <!-- Default Jellyfin Badge -->
      <div v-if="showJellyfinBadge && jellyfinIconSrc" class="jellyfin-badge">
        <img :src="jellyfinIconSrc" alt="Jellyfin" class="jellyfin-icon" />
      </div>
    </slot>

    <!-- Poster Image -->
    <div class="media-poster-card__poster">
      <!-- Skeleton Loader -->
      <Skeleton
        v-if="posterUrl && imageLoading && !imageError"
        width="100%"
        height="100%"
        class="poster-skeleton"
      />

      <!-- Actual Image -->
      <img
        v-if="posterUrl && !imageError"
        :src="posterUrl"
        :alt="title"
        class="poster-image"
        :class="{ 'image-loading': imageLoading }"
        @load="onImageLoad"
        @error="onImageError"
      />

      <!-- Placeholder (no URL or error) -->
      <div v-else class="poster-placeholder">
        <i :class="Icon.IMAGE" />
      </div>

      <!-- Rating Overlay (default) -->
      <div v-if="showRating && !$slots.overlay" class="rating-overlay" @click="handleRatingClick">
        <i :class="Icon.STAR" class="star-icon" />
        <span v-if="myRating" class="rating-value">{{ formatRating(myRating) }}</span>
        <span v-else class="rating-empty">Rate</span>
      </div>

      <!-- Overlay Slot (for custom overlays) -->
      <slot name="overlay" />
    </div>

    <!-- Title -->
    <div class="media-poster-card__title">{{ title }}</div>

    <!-- Subtitle (year, etc) -->
    <div v-if="subtitle" class="media-poster-card__subtitle">
      {{ subtitle }}
    </div>

    <!-- Additional Content Slot -->
    <slot name="footer" />
  </div>
</template>

<style scoped>
  .media-poster-card {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    background: var(--surface-card);
    box-shadow: var(--card-shadow);
    transition: all 0.3s ease;
    cursor: pointer;
  }

  .media-poster-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    transform: translateY(-4px);
  }

  /* Jellyfin Badge */
  .jellyfin-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 10;
    width: 32px;
    height: 32px;
    background: #aa5cc3;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    padding: 4px;
  }

  .jellyfin-icon {
    width: 100%;
    height: 100%;
    object-fit: contain;
    filter: brightness(0) invert(1);
  }

  /* Poster */
  .media-poster-card__poster {
    position: relative;
    width: 100%;
    aspect-ratio: 2 / 3;
    background: var(--surface-100);
    overflow: hidden;
  }

  .poster-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: opacity 0.3s ease;
  }

  .poster-image.image-loading {
    opacity: 0;
  }

  .poster-skeleton {
    position: absolute;
    inset: 0;
  }

  .poster-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: var(--surface-200);
    color: var(--p-text-color-secondary);
  }

  .poster-placeholder i {
    font-size: 3rem;
    opacity: 0.3;
  }

  /* Rating Overlay */
  .rating-overlay {
    position: absolute;
    top: 12px;
    left: 12px;
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: rgba(0, 0, 0, 0.75);
    backdrop-filter: blur(8px);
    border-radius: 20px;
    color: white;
    font-size: 14px;
    font-weight: 600;
    transition: all 0.2s ease;
    cursor: pointer;
    z-index: 5;
  }

  .rating-overlay:hover {
    background: rgba(0, 0, 0, 0.9);
    transform: scale(1.05);
  }

  .star-icon {
    color: #f5c518;
    font-size: 16px;
  }

  .rating-empty {
    font-size: 12px;
    opacity: 0.9;
  }

  /* Title */
  .media-poster-card__title {
    padding: 12px;
    font-size: 14px;
    font-weight: 600;
    line-height: 1.4;
    color: var(--p-text-color);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    max-height: calc(2 * 2em);
  }

  /* Subtitle */
  .media-poster-card__subtitle {
    padding: 0 12px 12px 12px;
    font-size: 13px;
    color: var(--p-text-secondary-color);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* Dark mode */
  :global(.app-dark) .media-poster-card {
    background: var(--surface-900);
  }

  :global(.app-dark) .poster-placeholder {
    background: var(--surface-800);
  }
</style>
