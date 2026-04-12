<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import ProgressSpinner from 'primevue/progressspinner'
  import Message from 'primevue/message'
  import Button from 'primevue/button'
  import { Icon, MediaSource, MediaType } from '@/utils/enums'
  import MediaHero from '@/components/media/MediaHero.vue'
  import MediaPosterCard from '@/components/media/MediaPosterCard.vue'
  import MediaMetadata from '@/components/media/MediaMetadata.vue'
  import CrewSection from '@/components/media/CrewSection.vue'
  import RatingDialog from '@/components/common/RatingDialog.vue'
  import { useMediaSource } from '@/composables/useMediaSource'
  import { useUnifiedMovieDetail } from '@/composables/useUnifiedMovieDetail'
  import { useImportTMDbData } from '@/queries/useTMDb'
  import { useProfileStore } from '@/stores/profileStore'
  import { useRateMediaItem } from '@/queries/useRatings'
  import { useToast } from '@/composables/useToast'
  import jellyfinIcon from '@/assets/jellyfin-icon.svg'

  const route = useRoute()
  const router = useRouter()
  const profileStore = useProfileStore()
  const { success, error: showError } = useToast()

  const movieId = computed(() => route.params.id as string)
  const selectedProfileId = computed(() => profileStore.selectedProfileId)

  // Detect source (internal vs TMDb)
  const mediaSource = useMediaSource(movieId)

  // Fetch unified movie data
  const { movie, isLoading, error, refetch } = useUnifiedMovieDetail(movieId, mediaSource)

  // Import mutation with prefetch (for TMDb movies)
  const importMutation = useImportTMDbData(selectedProfileId)

  // Rating dialog state
  const showRatingDialog = ref(false)
  const ratingMutation = useRateMediaItem()

  const openRatingDialog = () => {
    if (!selectedProfileId.value) {
      showError('No profile selected', 'Please select a profile to rate media')
      return
    }
    showRatingDialog.value = true
  }

  const closeRatingDialog = () => {
    showRatingDialog.value = false
  }

  const formatRating = (rating: number): string => {
    return rating.toFixed(1)
  }

  const handleRatingSubmit = async (rating: number) => {
    if (!selectedProfileId.value) {
      showError('No profile selected', 'Please select a profile to rate media')
      closeRatingDialog()
      return
    }

    if (!movie.value?.id) {
      showError('Invalid movie', 'This movie has no ID')
      closeRatingDialog()
      return
    }

    try {
      await ratingMutation.mutateAsync({
        profileId: selectedProfileId.value,
        entityId: movie.value.id,
        ratingMediaType: 'MEDIA_ITEM',
        rating,
      })

      success('Rating saved', `Rated "${movie.value.title}" ${formatRating(rating)}/10.0`)

      // Refetch movie data to get updated rating
      await refetch()

      closeRatingDialog()
    } catch (error) {
      showError(error, 'Failed to save rating')
      closeRatingDialog()
    }
  }

  // Import handler (for TMDb movies)
  const handleImport = async () => {
    if (!movie.value?.id) return

    try {
      const internalId = await importMutation.mutateAsync({
        id: movie.value.id,
        mediaType: 'MOVIE',
      })

      if (internalId) {
        success('Movie imported', `"${movie.value.title}" has been imported to your library`)

        // Redirect to the newly imported movie's detail page
        await router.replace({ name: 'movie-detail', params: { id: internalId } })
      }
    } catch (err) {
      showError(err, 'Failed to import movie')
    }
  }
</script>

<template>
  <div class="movie-detail-view">
    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <Message severity="error" :closable="false">
        Failed to load movie details: {{ error.message }}
      </Message>
      <Button label="Go Back" :icon="Icon.ARROW_LEFT" @click="router.back()" />
    </div>

    <!-- Movie Content -->
    <div v-else-if="movie" class="movie-content">
      <!-- Hero Section -->
      <MediaHero
        :title="movie.title"
        :backdrop-url="movie.backdropUrl"
        :year="movie.year"
        :runtime="movie.runtimeMinutes"
        :genres="movie.genres"
        :media-type="MediaType.MOVIE"
        :my-rating="movie.source === MediaSource.INTERNAL ? movie.myRating : undefined"
        :show-rating="movie.source === MediaSource.INTERNAL && !!selectedProfileId"
        @click:rating="openRatingDialog"
      />

      <!-- Mobile Import Button (TMDb only) -->
      <div v-if="movie.source === MediaSource.TMDB" class="mobile-import-section">
        <Button
          label="Import to Library"
          :icon="Icon.DOWNLOAD"
          severity="success"
          :loading="importMutation.isPending.value"
          @click="handleImport"
          fluid
        />
      </div>

      <!-- Main Content -->
      <div class="main-content">
        <div class="content-grid">
          <!-- Left: Poster -->
          <aside class="poster-column">
            <MediaPosterCard
              :title="movie.title"
              :poster-url="movie.posterUrl"
              :my-rating="movie.source === MediaSource.INTERNAL ? movie.myRating : undefined"
              :show-rating="movie.source === MediaSource.INTERNAL && !!selectedProfileId"
              @click:rating="openRatingDialog"
            />

            <!-- Jellyfin Badge (Internal) -->
            <div
              v-if="movie.source === MediaSource.INTERNAL && movie.jellyfinId"
              class="jellyfin-badge"
            >
              <img :src="jellyfinIcon" alt="Jellyfin" class="jellyfin-icon" />
              <span>Synced from Jellyfin</span>
            </div>

            <!-- External Badge (TMDb) -->
            <div v-if="movie.source === MediaSource.TMDB" class="tmdb-badge">
              <span>External</span>
            </div>

            <!-- Import Button (TMDb) -->
            <Button
              v-if="movie.source === MediaSource.TMDB"
              label="Import to Library"
              :icon="Icon.DOWNLOAD"
              severity="success"
              :loading="importMutation.isPending.value"
              @click="handleImport"
            />
          </aside>

          <!-- Right: Metadata -->
          <main class="metadata-column">
            <MediaMetadata
              :title="movie.title"
              :description="movie.description"
              :cast="movie.cast"
              :imdb-id="movie.imdbId"
              :tmdb-id="movie.tmdbId"
              :media-type="MediaType.MOVIE"
            >
              <template #crew>
                <div v-if="movie.crew && movie.crew.length > 0" class="metadata-section">
                  <CrewSection :crew="movie.crew" />
                </div>
              </template>
            </MediaMetadata>
          </main>
        </div>

        <!-- Back Button -->
        <div class="actions">
          <Button label="Back" :icon="Icon.ARROW_LEFT" outlined @click="router.back()" />
        </div>
      </div>
    </div>

    <!-- No Data -->
    <div v-else class="error-container">
      <Message severity="warn" :closable="false"> Movie not found </Message>
      <Button label="Go Back" :icon="Icon.ARROW_LEFT" @click="router.back()" />
    </div>

    <!-- Rating Dialog (Internal only) -->
    <RatingDialog
      v-if="movie && movie.source === MediaSource.INTERNAL"
      v-model:visible="showRatingDialog"
      :title="movie.title"
      :my-rating="movie.source === MediaSource.INTERNAL ? movie.myRating : undefined"
      :backdrop-url="movie.backdropUrl"
      :is-pending="ratingMutation.isPending.value"
      @submit="handleRatingSubmit"
    />
  </div>
</template>

<style scoped>
  .movie-detail-view {
    min-height: 100vh;
  }

  .loading-container,
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2rem;
    min-height: 60vh;
    padding: 2rem;
  }

  .movie-content {
    display: flex;
    flex-direction: column;
  }

  .mobile-import-section {
    display: none;
  }

  /* Show mobile import button on mobile */
  @media (max-width: 768px) {
    .mobile-import-section {
      display: block;
      padding: 1rem;
      background: var(--surface-ground);
      border-bottom: 1px solid var(--surface-border);
    }
  }

  .main-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 3rem 2rem;
    display: flex;
    flex-direction: column;
    gap: 3rem;
    width: 100%;
  }

  .content-grid {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 3rem;
    align-items: start;
  }

  .poster-column {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    align-self: flex-start;
    position: sticky;
    top: 2rem;
  }

  /* Hide poster on mobile */
  @media (max-width: 768px) {
    .poster-column {
      display: none;
    }
  }

  .jellyfin-badge {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    background: #aa5cc3;
    color: white;
    border-radius: var(--border-radius);
    font-size: 0.875rem;
    font-weight: 500;
    justify-content: center;
  }

  .jellyfin-icon {
    width: 20px;
    height: 20px;
    filter: brightness(0) invert(1);
  }

  .tmdb-badge {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    background: var(--p-surface-800);
    color: white;
    border-radius: var(--border-radius);
    font-size: 0.875rem;
    font-weight: 500;
    justify-content: center;
  }

  .metadata-column {
    min-width: 0;
  }

  .actions {
    display: flex;
    justify-content: flex-start;
    padding-top: 2rem;
    border-top: 1px solid var(--surface-border);
  }

  @media (max-width: 968px) {
    .content-grid {
      grid-template-columns: 200px 1fr;
      gap: 2rem;
    }

    .poster-column {
      position: static;
    }
  }

  @media (max-width: 768px) {
    .main-content {
      padding: 2rem 1rem;
      gap: 2rem;
    }

    .content-grid {
      grid-template-columns: 1fr;
      gap: 2rem;
    }

    .metadata-column {
      width: 100%;
    }
  }
</style>
