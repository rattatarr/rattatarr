<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import ProgressSpinner from 'primevue/progressspinner'
  import Message from 'primevue/message'
  import Button from 'primevue/button'
  import { Icon, MediaSource, MediaType } from '@/utils/enums'
  import MediaHero from '@/components/media/MediaHero.vue'
  import MediaPosterCard from '@/components/media/MediaPosterCard.vue'
  import MediaMetadata from '@/components/media/MediaMetadata.vue'
  import CrewSection from '@/components/media/CrewSection.vue'
  import SeasonDropdown from '@/components/series/SeasonDropdown.vue'
  import RatingDialog from '@/components/common/RatingDialog.vue'
  import { useMediaSource } from '@/composables/useMediaSource'
  import { useUnifiedSeriesDetail } from '@/composables/useUnifiedSeriesDetail'
  import { useImportTMDbData } from '@/queries/useTMDb'
  import { useRefreshSeries } from '@/queries/useLibrary'
  import { useProfileStore } from '@/stores/profileStore'
  import { useRateMediaItem } from '@/queries/useRatings'
  import { useToast } from '@/composables/useToast'
  import jellyfinIcon from '@/assets/jellyfin-icon.svg'

  const route = useRoute()
  const router = useRouter()
  const profileStore = useProfileStore()
  const { success, error: showError } = useToast()

  const seriesId = computed(() => route.params.id as string)
  const selectedProfileId = computed(() => profileStore.selectedProfileId)

  // Detect source (internal vs TMDb)
  const mediaSource = useMediaSource(seriesId)

  // Fetch unified series data
  const { series, isLoading, error, refetch } = useUnifiedSeriesDetail(seriesId, mediaSource)

  // Import mutation with prefetch (for TMDb series)
  const importMutation = useImportTMDbData(selectedProfileId)

  const refreshMutation = useRefreshSeries()

  const handleRefresh = async () => {
    if (!series.value?.id) return
    try {
      await refreshMutation.mutateAsync(series.value.id)
      success('Series refreshed', `"${series.value.title}" has been updated`)
      await refetch()
    } catch (err) {
      showError(err, 'Failed to refresh series')
    }
  }

  // Seasons are already sorted and transformed in the unified composable
  const sortedSeasons = computed(() => series.value?.seasons ?? [])

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

    if (!series.value?.id) {
      showError('Invalid series', 'This series has no ID')
      closeRatingDialog()
      return
    }

    try {
      await ratingMutation.mutateAsync({
        profileId: selectedProfileId.value,
        entityId: series.value.id,
        ratingMediaType: 'MEDIA_ITEM',
        rating,
      })

      success('Rating saved', `Rated "${series.value.title}" ${formatRating(rating)}/10.0`)

      // Refetch series data to get updated rating
      await refetch()

      closeRatingDialog()
    } catch (error) {
      showError(error, 'Failed to save rating')
      closeRatingDialog()
    }
  }

  // Import handler (for TMDb series)
  const handleImport = async () => {
    if (!series.value?.id) return

    try {
      const internalId = await importMutation.mutateAsync({
        id: series.value.id,
        mediaType: 'SERIES',
      })

      if (internalId) {
        success('Series imported', `"${series.value.title}" has been imported to your library`)

        // Redirect to the newly imported series detail page
        await router.replace({ name: 'series-detail', params: { id: internalId } })
      }
    } catch (err) {
      showError(err, 'Failed to import series')
    }
  }
</script>

<template>
  <div class="series-detail-view">
    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <ProgressSpinner />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <Message severity="error" :closable="false">
        Failed to load series details: {{ error.message }}
      </Message>
      <Button label="Go Back" :icon="Icon.ARROW_LEFT" @click="router.back()" />
    </div>

    <!-- Series Content -->
    <div v-else-if="series" class="series-content">
      <!-- Hero Section -->
      <MediaHero
        :title="series.title"
        :backdrop-url="series.backdropUrl"
        :year="series.year"
        :runtime="series.runtimeMinutes"
        :genres="series.genres"
        :media-type="MediaType.SERIES"
        :my-rating="series.source === MediaSource.INTERNAL ? series.myRating : undefined"
        :show-rating="series.source === MediaSource.INTERNAL && !!selectedProfileId"
        @click:rating="openRatingDialog"
      />

      <!-- Mobile Import Button (TMDb only) -->
      <div v-if="series.source === MediaSource.TMDB" class="mobile-import-section">
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
              :title="series.title"
              :poster-url="series.posterUrl"
              :my-rating="series.source === MediaSource.INTERNAL ? series.myRating : undefined"
              :show-rating="series.source === MediaSource.INTERNAL && !!selectedProfileId"
              @click:rating="openRatingDialog"
            />

            <!-- Jellyfin Badge (Internal) -->
            <div
              v-if="series.source === MediaSource.INTERNAL && series.jellyfinId"
              class="jellyfin-badge"
            >
              <img :src="jellyfinIcon" alt="Jellyfin" class="jellyfin-icon" />
              <span>Synced from Jellyfin</span>
            </div>

            <!-- External Badge (TMDb) -->
            <div v-if="series.source === MediaSource.TMDB" class="tmdb-badge">
              <span>External</span>
            </div>

            <!-- Import Button (TMDb) -->
            <Button
              v-if="series.source === MediaSource.TMDB"
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
              :title="series.title"
              :description="series.description"
              :cast="series.cast"
              :imdb-id="series.imdbId"
              :tmdb-id="series.tmdbId"
              :media-type="MediaType.SERIES"
            >
              <template #crew>
                <div v-if="series.crew && series.crew.length > 0" class="metadata-section">
                  <CrewSection :crew="series.crew" />
                </div>
              </template>
            </MediaMetadata>
          </main>
        </div>

        <!-- Seasons Section -->
        <div v-if="sortedSeasons.length > 0" class="seasons-section">
          <div class="seasons-header">
            <h2 class="section-title">
              <i :class="Icon.LIST" />
              Seasons
            </h2>
            <Button
              v-if="series.source === MediaSource.INTERNAL"
              :icon="Icon.REFRESH"
              :loading="refreshMutation.isPending.value"
              size="small"
              severity="secondary"
              text
              rounded
              aria-label="Refresh series"
              @click="handleRefresh"
            />
          </div>
          <p v-if="series.source === MediaSource.INTERNAL" class="seasons-hint">
            Series are often updated — you can enable auto-refresh in settings or manually refresh
            it.
          </p>
          <div class="seasons-list">
            <SeasonDropdown
              v-for="season in sortedSeasons"
              :key="season.seasonNumber"
              :title="season.title"
              :episode-count="season.episodeCount"
              :air-date="season.airDate"
              :poster-url="season.posterUrl"
              :episodes="season.episodes"
              :initially-expanded="season.initiallyExpanded"
            />
          </div>
        </div>

        <!-- Back Button -->
        <div class="actions">
          <Button label="Back" :icon="Icon.ARROW_LEFT" outlined @click="router.back()" />
        </div>
      </div>
    </div>

    <!-- No Data -->
    <div v-else class="error-container">
      <Message severity="warn" :closable="false"> Series not found </Message>
      <Button label="Go Back" :icon="Icon.ARROW_LEFT" @click="router.back()" />
    </div>

    <!-- Rating Dialog (Internal only) -->
    <RatingDialog
      v-if="series && series.source === MediaSource.INTERNAL"
      v-model:visible="showRatingDialog"
      :title="series.title"
      :my-rating="series.source === MediaSource.INTERNAL ? series.myRating : undefined"
      :backdrop-url="series.backdropUrl"
      :is-pending="ratingMutation.isPending.value"
      @submit="handleRatingSubmit"
    />
  </div>
</template>

<style scoped>
  .series-detail-view {
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

  .series-content {
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

  .seasons-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .seasons-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .seasons-hint {
    font-size: 0.8rem;
    color: var(--p-text-secondary-color);
    margin: -0.75rem 0 0 0;
    line-height: 1.5;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1.75rem;
    font-weight: 600;
    color: var(--p-text-color);
    margin: 0;
  }

  .section-title i {
    font-size: 1.5rem;
    color: var(--primary-color);
  }

  .seasons-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
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

    .section-title {
      font-size: 1.5rem;
    }
  }
</style>
