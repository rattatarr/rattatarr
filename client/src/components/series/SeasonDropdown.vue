<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import Button from 'primevue/button'
  import { Icon } from '@/utils/enums'
  import type { Episode, Review } from '@/types'
  import RatingDialog from '@/components/common/RatingDialog.vue'
  import ReviewDialog from '@/components/common/ReviewDialog.vue'
  import type { ReviewSubmitPayload } from '@/components/common/ReviewDialog.vue'
  import ReviewDisplay from '@/components/common/ReviewDisplay.vue'
  import { useRateMediaItem, useSetReview, useDeleteReview } from '@/queries/useRatings'
  import { useProfileStore } from '@/stores/profileStore'
  import { useReviewDraftStore } from '@/stores/reviewDraftStore'
  import { useToast } from '@/composables/useToast'

  interface Props {
    id?: string
    title?: string
    episodeCount?: number
    airDate?: string
    posterUrl?: string
    episodes?: Episode[]
    initiallyExpanded?: boolean
    myRating?: number
    review?: Review
    showRating?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    initiallyExpanded: false,
    showRating: false,
  })

  const emit = defineEmits<{
    ratingUpdated: [rating: number]
    reviewUpdated: []
  }>()

  const profileStore = useProfileStore()
  const { success, error: showError } = useToast()
  const ratingMutation = useRateMediaItem()
  const setReviewMutation = useSetReview()
  const deleteReviewMutation = useDeleteReview()
  const reviewDraftStore = useReviewDraftStore()

  const isExpanded = ref(props.initiallyExpanded)
  const showRatingDialog = ref(false)
  const showReviewDialog = ref(false)
  // When rating was triggered from the review dialog, reopen it afterwards
  const reopenReviewAfterRating = ref(false)

  const hasRating = computed(() => !!props.myRating)

  // Reset the reopen intent whenever the rating dialog closes (e.g. cancel)
  watch(showRatingDialog, (visible) => {
    if (!visible) reopenReviewAfterRating.value = false
  })

  const toggleExpanded = () => {
    isExpanded.value = !isExpanded.value
  }

  const openRatingDialog = (event: Event) => {
    event.stopPropagation()
    showRatingDialog.value = true
  }

  const openReviewDialog = (event: Event) => {
    event.stopPropagation()
    if (!profileStore.selectedProfileId) {
      showError('No profile selected', 'Please select a profile to review media')
      return
    }
    showReviewDialog.value = true
  }

  const handleReviewRate = () => {
    reopenReviewAfterRating.value = true
    showReviewDialog.value = false
    showRatingDialog.value = true
  }

  const formatRating = (rating: number): string => rating.toFixed(1)

  const formatDate = (date: string): string => {
    try {
      return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
      })
    } catch {
      return date
    }
  }

  const handleRatingSubmit = async (rating: number) => {
    if (!profileStore.selectedProfileId) {
      showError('No profile selected', 'Please select a profile to rate media')
      showRatingDialog.value = false
      return
    }
    if (!props.id) {
      showError('Invalid season', 'This season has no ID')
      showRatingDialog.value = false
      return
    }
    try {
      await ratingMutation.mutateAsync({
        profileId: profileStore.selectedProfileId,
        entityId: props.id,
        ratingMediaType: 'MEDIA_SEASON',
        rating,
      })
      success('Rating saved', `Rated "${props.title}" ${formatRating(rating)}/10.0`)
      emit('ratingUpdated', rating)
      const reopenReview = reopenReviewAfterRating.value
      showRatingDialog.value = false
      if (reopenReview) showReviewDialog.value = true
    } catch (error) {
      showError(error, 'Failed to save rating')
      showRatingDialog.value = false
    }
  }

  const handleReviewSubmit = async (payload: ReviewSubmitPayload) => {
    if (!profileStore.selectedProfileId || !props.id) {
      showError('Cannot save review', 'Missing profile or season')
      return
    }
    try {
      await setReviewMutation.mutateAsync({
        profileId: profileStore.selectedProfileId,
        entityId: props.id,
        ratingMediaType: 'MEDIA_SEASON',
        ...payload,
      })
      reviewDraftStore.clearDraft(profileStore.selectedProfileId, props.id)
      success('Review saved', `Your review for "${props.title}" has been saved`)
      emit('reviewUpdated')
      showReviewDialog.value = false
    } catch (error) {
      showError(error, 'Failed to save review')
    }
  }

  const handleReviewDelete = async () => {
    if (!profileStore.selectedProfileId || !props.id) return
    try {
      await deleteReviewMutation.mutateAsync({
        profileId: profileStore.selectedProfileId,
        entityId: props.id,
        ratingMediaType: 'MEDIA_SEASON',
      })
      reviewDraftStore.clearDraft(profileStore.selectedProfileId, props.id)
      success('Review deleted', `Your review for "${props.title}" has been removed`)
      emit('reviewUpdated')
      showReviewDialog.value = false
    } catch (error) {
      showError(error, 'Failed to delete review')
    }
  }
</script>

<template>
  <div class="season-dropdown">
    <div
      class="season-header"
      :style="{ backgroundImage: posterUrl ? `url(${posterUrl})` : undefined }"
      @click="toggleExpanded"
    >
      <div class="season-overlay">
        <div class="season-info">
          <h3 class="season-title">{{ title }}</h3>
          <span v-if="episodeCount" class="episode-count">{{ episodeCount }} episodes</span>
          <span v-if="airDate" class="air-date">{{ formatDate(airDate) }}</span>
        </div>
        <div class="season-actions">
          <div v-if="showRating && id" class="season-rating" @click="openRatingDialog">
            <span v-if="myRating" class="rating-badge">
              <i class="pi pi-star-fill" />
              {{ formatRating(myRating) }}
            </span>
            <Button
              :icon="myRating ? Icon.STAR_FILL : Icon.STAR"
              text
              rounded
              size="small"
              :severity="myRating ? 'warn' : 'secondary'"
              :aria-label="myRating ? 'Update season rating' : 'Rate this season'"
              @click="openRatingDialog"
            />
          </div>
          <Button
            v-if="showRating && id"
            :icon="Icon.COMMENTS"
            text
            rounded
            size="small"
            :severity="review ? 'info' : 'secondary'"
            :aria-label="review ? 'Edit season review' : 'Review this season'"
            @click="openReviewDialog"
          />
          <Button
            :icon="isExpanded ? Icon.CHEVRON_UP : Icon.CHEVRON_DOWN"
            text
            rounded
            severity="secondary"
          />
        </div>
      </div>
    </div>

    <Transition name="expand">
      <div v-if="isExpanded" class="episodes-list">
        <div v-if="review" class="season-review">
          <h4 class="season-review-title">
            <i :class="Icon.COMMENTS" />
            My Review
          </h4>
          <ReviewDisplay :review="review" />
        </div>
        <div v-for="episode in episodes" :key="episode.id" class="episode-item">
          <div class="episode-number">{{ episode.episode }}</div>
          <div class="episode-details">
            <div class="episode-title">{{ episode.title }}</div>
            <div v-if="episode.runtimeMinutes" class="episode-runtime">
              {{ episode.runtimeMinutes }} min
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <RatingDialog
      v-if="showRating && id"
      v-model:visible="showRatingDialog"
      :title="title"
      :my-rating="myRating"
      :backdrop-url="posterUrl"
      :is-pending="ratingMutation.isPending.value"
      @submit="handleRatingSubmit"
    />

    <ReviewDialog
      v-if="showRating && id"
      v-model:visible="showReviewDialog"
      :title="title"
      :review="review"
      :backdrop-url="posterUrl"
      :profile-id="profileStore.selectedProfileId ?? undefined"
      :entity-id="id"
      :has-rating="hasRating"
      :is-pending="setReviewMutation.isPending.value || deleteReviewMutation.isPending.value"
      @submit="handleReviewSubmit"
      @delete="handleReviewDelete"
      @rate="handleReviewRate"
    />
  </div>
</template>

<style scoped>
  .season-dropdown {
    border: 1px solid var(--surface-border);
    border-radius: var(--border-radius);
    overflow: hidden;
    background: var(--surface-card);
  }

  .season-header {
    position: relative;
    min-height: 100px;
    cursor: pointer;
    transition: transform 0.2s;
    overflow: hidden;
  }

  /* Blurred backdrop layer */
  .season-header::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: inherit;
    background-size: cover;
    background-position: center 25%;
    filter: blur(8px);
    transform: scale(1.1); /* Prevent blur edge artifacts */
    z-index: 0;
  }

  .season-header:hover {
    transform: translateY(-2px);
  }

  .season-overlay {
    position: relative;
    inset: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1.5rem;
    gap: 1rem;
    min-height: 100px;
    z-index: 1;
  }

  .season-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    color: white;
  }

  .season-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  }

  .episode-count,
  .air-date {
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.9);
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  }

  .season-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-shrink: 0;
  }

  .season-rating {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    cursor: pointer;
  }

  .rating-badge {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.25rem 0.5rem;
    background: rgba(255, 193, 7, 0.25);
    border: 1px solid rgba(255, 193, 7, 0.5);
    border-radius: 999px;
    color: #ffc107;
    font-size: 0.8rem;
    font-weight: 600;
    white-space: nowrap;
  }

  .rating-badge .pi {
    font-size: 0.75rem;
  }

  .episodes-list {
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    background: var(--surface-ground);
  }

  .season-review {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    padding: 1rem;
    border-radius: var(--border-radius);
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
  }

  .season-review-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 0;
    font-size: 1rem;
    font-weight: 600;
    color: var(--p-text-color);
  }

  .season-review-title i {
    color: var(--primary-color);
  }

  .episode-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.875rem 1rem;
    border-radius: var(--border-radius);
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    transition: all 0.2s ease;
  }

  .episode-item:hover {
    background-color: var(--surface-hover);
    box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12);
    transform: translateX(4px);
    border-color: var(--primary-color);
  }

  .episode-number {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: var(--primary-color);
    color: white;
    font-weight: 600;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .episode-details {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    min-width: 0;
    flex: 1;
  }

  .episode-title {
    font-weight: 500;
    color: var(--p-text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .episode-runtime {
    font-size: 0.875rem;
    color: var(--p-text-color-secondary);
  }

  /* Expand transition */
  .expand-enter-active,
  .expand-leave-active {
    transition: all 0.3s ease;
    max-height: 2000px;
  }

  .expand-enter-from,
  .expand-leave-to {
    max-height: 0;
    opacity: 0;
  }

  @media (max-width: 768px) {
    .season-overlay {
      padding: 1rem;
    }

    .season-title {
      font-size: 1.125rem;
    }
  }
</style>
