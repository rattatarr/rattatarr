<script setup lang="ts">
  import { ref, watch } from 'vue'
  import Chip from 'primevue/chip'
  import Skeleton from 'primevue/skeleton'
  import { useRouter } from 'vue-router'
  import { Icon, MediaType } from '@/utils/enums'
  import type { Genre } from '@/types'

  interface Props {
    title?: string
    backdropUrl?: string
    year?: number
    runtime?: number
    genres?: Genre[]
    myRating?: number
    showRating?: boolean
    mediaType?: MediaType.MOVIE | MediaType.SERIES
  }

  const props = withDefaults(defineProps<Props>(), {
    showRating: false,
    mediaType: MediaType.MOVIE,
  })

  const emit = defineEmits<{
    'click:rating': []
  }>()

  const router = useRouter()

  // State
  const backdropLoading = ref(true)

  const formatRuntime = (minutes: number): string => {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    if (hours > 0) {
      return `${hours}h ${mins}m`
    }
    return `${mins}m`
  }

  const formatRating = (rating: number): string => {
    return rating.toFixed(1)
  }

  const handleRatingClick = () => {
    emit('click:rating')
  }

  const handleGenreClick = (genre: Genre) => {
    if (!genre.name) return
    const routeName = props.mediaType === MediaType.SERIES ? 'series' : 'movies'
    router.push({ name: routeName, query: { genre: genre.name } })
  }

  // Preload backdrop image
  const preloadBackdrop = (url: string) => {
    backdropLoading.value = true
    const img = new Image()
    img.onload = () => {
      backdropLoading.value = false
    }
    img.onerror = () => {
      backdropLoading.value = false
    }
    img.src = url
  }

  // Watch for backdrop URL changes
  watch(
    () => props.backdropUrl,
    (newUrl) => {
      if (newUrl) {
        preloadBackdrop(newUrl)
      } else {
        backdropLoading.value = false
      }
    },
    { immediate: true },
  )
</script>

<template>
  <div
    class="media-hero"
    :class="{ 'backdrop-loading': backdropLoading }"
    :style="{ backgroundImage: backdropUrl ? `url(${backdropUrl})` : undefined }"
  >
    <!-- Skeleton Loader -->
    <Skeleton v-if="backdropLoading" width="100%" height="100%" class="backdrop-skeleton" />

    <!-- Mobile Rating Button (top left) -->
    <div v-if="showRating" class="mobile-rating-overlay" @click.stop="handleRatingClick">
      <i :class="Icon.STAR" class="star-icon" />
      <span v-if="myRating" class="rating-value">{{ formatRating(myRating) }}</span>
      <span v-else class="rating-empty">Rate</span>
    </div>

    <div class="hero-overlay">
      <div class="hero-content">
        <h1 class="hero-title">{{ title }}</h1>
        <div class="hero-details">
          <span v-if="year" class="detail-item">{{ year }}</span>
          <span v-if="runtime" class="detail-item">{{ formatRuntime(runtime) }}</span>
          <div v-if="genres && genres.length > 0" class="genres">
            <Chip
              v-for="genre in genres.slice(0, 3)"
              :key="genre.id"
              :label="genre.name"
              class="genre-chip genre-chip-clickable"
              @click="handleGenreClick(genre)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .media-hero {
    position: relative;
    width: 100%;
    height: 400px;
    background-size: cover;
    background-position: center 25%;
    background-repeat: no-repeat;
    overflow: hidden;
    background-color: var(--surface-200);
    transition: opacity 0.3s ease;
  }

  .media-hero.backdrop-loading {
    opacity: 0.5;
  }

  .backdrop-skeleton {
    position: absolute;
    inset: 0;
    z-index: 0;
  }

  .hero-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.7) 100%);
    display: flex;
    align-items: flex-end;
    padding: 2rem;
  }

  .hero-content {
    max-width: 1200px;
    width: 100%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .hero-title {
    color: white;
    font-size: 3rem;
    font-weight: 700;
    margin: 0;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
    line-height: 1.2;
  }

  .hero-details {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .detail-item {
    color: rgba(255, 255, 255, 0.9);
    font-size: 1.125rem;
    font-weight: 500;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  }

  .detail-item:not(:last-child)::after {
    content: '•';
    margin-left: 1rem;
    color: rgba(255, 255, 255, 0.6);
  }

  .genres {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
  }

  .genre-chip {
    background-color: rgba(255, 255, 255, 0.2);
    color: white;
    font-size: 0.875rem;
    font-weight: 500;
    backdrop-filter: blur(10px);
  }

  .genre-chip-clickable {
    cursor: pointer;
    transition:
      background-color 0.2s,
      transform 0.1s;
  }

  .genre-chip-clickable:hover {
    background-color: rgba(255, 255, 255, 0.35);
    transform: scale(1.05);
  }

  /* Mobile Rating Overlay (hidden on desktop) */
  .mobile-rating-overlay {
    display: none;
  }

  @media (max-width: 768px) {
    /* Show rating button on mobile */
    .mobile-rating-overlay {
      display: flex;
      position: absolute;
      top: 1rem;
      left: 1rem;
      align-items: center;
      gap: 6px;
      padding: 8px 14px;
      background: rgba(0, 0, 0, 0.75);
      backdrop-filter: blur(8px);
      border-radius: 20px;
      color: white;
      font-size: 15px;
      font-weight: 600;
      transition: all 0.2s ease;
      cursor: pointer;
      z-index: 10;
    }

    .mobile-rating-overlay:active {
      background: rgba(0, 0, 0, 0.9);
      transform: scale(0.95);
    }

    .star-icon {
      color: #f5c518;
      font-size: 18px;
    }

    .rating-empty {
      font-size: 13px;
      opacity: 0.9;
    }
    .media-hero {
      height: 300px;
    }

    .hero-overlay {
      padding: 1.5rem;
    }

    .hero-title {
      font-size: 2rem;
    }

    .detail-item {
      font-size: 1rem;
    }
  }
</style>
