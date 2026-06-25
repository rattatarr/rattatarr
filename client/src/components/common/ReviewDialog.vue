<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import Dialog from 'primevue/dialog'
  import Button from 'primevue/button'
  import Message from 'primevue/message'
  import SelectButton from 'primevue/selectbutton'
  import Stepper from 'primevue/stepper'
  import StepList from 'primevue/steplist'
  import Step from 'primevue/step'
  import StepPanels from 'primevue/steppanels'
  import StepPanel from 'primevue/steppanel'
  import ReviewRichText from '@/components/common/ReviewRichText.vue'
  import { useConfirm } from 'primevue/useconfirm'
  import { useReviewDraftStore, type ReviewDraftFields } from '@/stores/reviewDraftStore'
  import { Icon, ButtonSeverity } from '@/utils/enums.ts'
  import { isReviewHtmlEmpty } from '@/utils/review'
  import type { Review, ReviewRequest, ReviewType } from '@/types'

  /**
   * Payload emitted on submit. The parent attaches profileId / entityId /
   * ratingMediaType before calling the API.
   */
  export type ReviewSubmitPayload = Omit<
    ReviewRequest,
    'profileId' | 'entityId' | 'ratingMediaType'
  >

  interface Props {
    visible: boolean
    title?: string
    review?: Review
    backdropUrl?: string
    isPending?: boolean
    /** Used to key the session draft cache. */
    profileId?: string
    entityId?: string
    /** Reviews require a rating first — gates saving. */
    hasRating?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    isPending: false,
    hasRating: false,
  })

  const emit = defineEmits<{
    'update:visible': [value: boolean]
    submit: [payload: ReviewSubmitPayload]
    delete: []
    rate: []
  }>()

  const draftStore = useReviewDraftStore()

  const REVIEW_TYPE_OPTIONS: { label: string; value: ReviewType }[] = [
    { label: 'Free text', value: 'FREE_TEXT' },
    { label: 'Structured', value: 'STRUCTURED' },
  ]

  const REVIEW_TYPE_LABEL: Record<ReviewType, string> = {
    FREE_TEXT: 'free-text',
    STRUCTURED: 'structured',
  }

  const confirm = useConfirm()

  const STRUCTURED_FIELDS = [
    { key: 'reviewStory', label: 'Story', placeholder: 'Plot, pacing, writing…' },
    {
      key: 'reviewPerformances',
      label: 'Performances',
      placeholder: 'Acting, casting, chemistry…',
    },
    { key: 'reviewDirection', label: 'Direction', placeholder: 'Direction, editing, tone…' },
    { key: 'reviewVisuals', label: 'Visuals', placeholder: 'Cinematography, effects, design…' },
    { key: 'reviewSound', label: 'Sound', placeholder: 'Score, sound design, mixing…' },
    { key: 'reviewVerdict', label: 'Verdict', placeholder: 'Overall take, recommendation…' },
  ] as const

  const reviewType = ref<ReviewType>('FREE_TEXT')
  const activeStep = ref(1)
  // Guards the persist-watch from firing while we load review/draft into the form
  const isHydrating = ref(false)
  // True when an unsaved cached draft is currently shown
  const draftActive = ref(false)

  const form = reactive({
    reviewText: '',
    reviewStory: '',
    reviewPerformances: '',
    reviewDirection: '',
    reviewVisuals: '',
    reviewSound: '',
    reviewVerdict: '',
  })

  const hasExistingReview = computed(() => !!props.review)

  const header = computed(() => `Review: ${props.title || 'Media'}`)

  const dialogStyle = computed(() => {
    const isMobile = window.innerWidth <= 768
    if (isMobile) {
      return { width: '100vw', maxWidth: '100vw', margin: '0' }
    }
    return { width: '800px', maxWidth: '90vw' }
  })

  const resetForm = () => {
    form.reviewText = ''
    form.reviewStory = ''
    form.reviewPerformances = ''
    form.reviewDirection = ''
    form.reviewVisuals = ''
    form.reviewSound = ''
    form.reviewVerdict = ''
  }

  const applyFields = (source: Partial<ReviewDraftFields>) => {
    form.reviewText = source.reviewText ?? ''
    form.reviewStory = source.reviewStory ?? ''
    form.reviewPerformances = source.reviewPerformances ?? ''
    form.reviewDirection = source.reviewDirection ?? ''
    form.reviewVisuals = source.reviewVisuals ?? ''
    form.reviewSound = source.reviewSound ?? ''
    form.reviewVerdict = source.reviewVerdict ?? ''
  }

  const snapshotFields = (): ReviewDraftFields => ({
    reviewType: reviewType.value,
    reviewText: form.reviewText,
    reviewStory: form.reviewStory,
    reviewPerformances: form.reviewPerformances,
    reviewDirection: form.reviewDirection,
    reviewVisuals: form.reviewVisuals,
    reviewSound: form.reviewSound,
    reviewVerdict: form.reviewVerdict,
  })

  const isFormEmpty = () =>
    isReviewHtmlEmpty(form.reviewText) &&
    STRUCTURED_FIELDS.every((field) => isReviewHtmlEmpty(form[field.key]))

  // Populate from existing review (then overlay any cached draft) when the dialog opens
  watch(
    () => props.visible,
    (isVisible) => {
      if (!isVisible) return
      isHydrating.value = true
      resetForm()
      activeStep.value = 1

      const existing = props.review
      reviewType.value = existing?.reviewType ?? 'FREE_TEXT'
      if (existing) applyFields(existing)

      const draft =
        props.profileId && props.entityId
          ? draftStore.getDraft(props.profileId, props.entityId)
          : undefined
      if (draft) {
        reviewType.value = draft.reviewType
        applyFields(draft)
        draftActive.value = true
      } else {
        draftActive.value = false
      }

      void nextTick(() => {
        isHydrating.value = false
      })
    },
  )

  // Persist edits to the in-memory session draft
  watch(
    [() => reviewType.value, form],
    () => {
      if (!props.visible || isHydrating.value) return
      if (!props.profileId || !props.entityId) return

      if (isFormEmpty()) {
        draftStore.clearDraft(props.profileId, props.entityId)
        draftActive.value = false
      } else {
        draftStore.setDraft(props.profileId, props.entityId, snapshotFields())
        draftActive.value = true
      }
    },
    { deep: true },
  )

  const canSubmit = computed(() => {
    if (reviewType.value === 'FREE_TEXT') {
      return !isReviewHtmlEmpty(form.reviewText)
    }
    return STRUCTURED_FIELDS.some((field) => !isReviewHtmlEmpty(form[field.key]))
  })

  const handleVisibilityChange = (value: boolean) => {
    emit('update:visible', value)
  }

  const buildPayload = (): ReviewSubmitPayload => {
    if (reviewType.value === 'FREE_TEXT') {
      return { reviewType: 'FREE_TEXT', reviewText: form.reviewText }
    }
    const payload: ReviewSubmitPayload = { reviewType: 'STRUCTURED' }
    for (const field of STRUCTURED_FIELDS) {
      if (!isReviewHtmlEmpty(form[field.key])) payload[field.key] = form[field.key]
    }
    return payload
  }

  const handleRate = () => {
    emit('rate')
  }

  const handleSubmit = () => {
    if (!canSubmit.value || !props.hasRating) return

    const payload = buildPayload()
    const existingType = props.review?.reviewType

    // Review stores exactly one type. Switching type discards the saved one.
    if (existingType && existingType !== reviewType.value) {
      confirm.require({
        header: 'Change review type?',
        message: `This will replace your existing ${REVIEW_TYPE_LABEL[existingType]} review with a ${REVIEW_TYPE_LABEL[reviewType.value]} one. The previous content will be lost.`,
        icon: 'pi pi-exclamation-triangle',
        acceptLabel: 'Replace',
        rejectLabel: 'Cancel',
        acceptProps: { severity: 'danger' },
        rejectProps: { severity: 'secondary', outlined: true },
        accept: () => emit('submit', payload),
      })
      return
    }

    emit('submit', payload)
  }

  const handleDelete = () => {
    emit('delete')
  }

  const handleCancel = () => {
    emit('update:visible', false)
  }
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    :header="header"
    :style="dialogStyle"
    :dismissableMask="true"
    class="review-dialog"
    @update:visible="handleVisibilityChange"
  >
    <div
      v-if="backdropUrl"
      class="dialog-backdrop"
      :style="{ backgroundImage: `url(${backdropUrl})` }"
    />

    <div class="review-dialog-content">
      <!-- Must rate before a review can be saved -->
      <Message v-if="!hasRating" severity="warn" :closable="false" class="review-banner">
        <div class="banner-row">
          <span>
            Rate this title before you can save a review. Your text is kept on this device for now,
            so you won't lose it.
          </span>
          <Button
            label="Rate now"
            :icon="Icon.STAR"
            size="small"
            :severity="ButtonSeverity.WARNING"
            @click="handleRate"
          />
        </div>
      </Message>

      <!-- Unsaved cached draft notice -->
      <Message v-else-if="draftActive" severity="info" :closable="false" class="review-banner">
        Showing a locally cached draft — not saved yet.
      </Message>

      <SelectButton
        v-model="reviewType"
        :options="REVIEW_TYPE_OPTIONS"
        option-label="label"
        option-value="value"
        :allow-empty="false"
        aria-label="Review type"
      />

      <!-- Free text -->
      <div v-if="reviewType === 'FREE_TEXT'" class="review-field">
        <ReviewRichText
          v-model="form.reviewText"
          placeholder="Write your review…"
          editor-style="height: 260px"
        />
      </div>

      <!-- Structured: one step per category -->
      <Stepper v-else v-model:value="activeStep" class="review-stepper">
        <StepList>
          <Step v-for="(field, i) in STRUCTURED_FIELDS" :key="field.key" :value="i + 1">
            {{ field.label }}
          </Step>
        </StepList>
        <StepPanels>
          <StepPanel
            v-for="(field, i) in STRUCTURED_FIELDS"
            :key="field.key"
            v-slot="{ activateCallback }"
            :value="i + 1"
          >
            <div class="step-body">
              <p class="step-hint">{{ field.placeholder }}</p>
              <ReviewRichText v-model="form[field.key]" :placeholder="field.label" />
              <div class="step-nav">
                <Button
                  v-if="i > 0"
                  label="Back"
                  :icon="Icon.ARROW_LEFT"
                  :severity="ButtonSeverity.SECONDARY"
                  text
                  @click="activateCallback(i)"
                />
                <Button
                  v-if="i < STRUCTURED_FIELDS.length - 1"
                  label="Next"
                  :icon="Icon.ARROW_RIGHT"
                  icon-pos="right"
                  :severity="ButtonSeverity.SECONDARY"
                  @click="activateCallback(i + 2)"
                />
              </div>
            </div>
          </StepPanel>
        </StepPanels>
      </Stepper>
    </div>

    <template #footer>
      <div class="review-dialog-footer">
        <Button
          v-if="hasExistingReview"
          label="Delete"
          :icon="Icon.TRASH"
          :severity="ButtonSeverity.DANGER"
          text
          :disabled="isPending"
          @click="handleDelete"
        />
        <div class="footer-actions">
          <Button
            label="Cancel"
            :icon="Icon.TIMES"
            :severity="ButtonSeverity.SECONDARY"
            text
            @click="handleCancel"
          />
          <Button
            :label="hasExistingReview ? 'Update Review' : 'Save Review'"
            :icon="Icon.CHECK"
            :severity="ButtonSeverity.PRIMARY"
            :loading="isPending"
            :disabled="!canSubmit || !hasRating"
            @click="handleSubmit"
          />
        </div>
      </div>
    </template>
  </Dialog>
</template>

<style scoped>
  .review-dialog :deep(.p-dialog-content) {
    position: relative;
    overflow: hidden;
  }

  .dialog-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    filter: blur(2px);
    opacity: 0.15;
    z-index: 0;
    pointer-events: none;
  }

  .review-dialog-content {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    padding: 1rem 0;
    z-index: 1;
    width: 100%;
  }

  .structured-fields {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .review-banner {
    width: 100%;
  }

  .banner-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    justify-content: space-between;
  }

  .banner-row span {
    flex: 1;
    min-width: 12rem;
  }

  .review-field {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    width: 100%;
  }

  .review-field-label {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--p-text-color);
  }

  .step-body {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    padding: 1rem 0.75rem 0.25rem;
  }

  .step-hint {
    margin: 0;
    font-size: 0.85rem;
    color: var(--p-text-secondary-color);
  }

  .step-nav {
    display: flex;
    justify-content: space-between;
    gap: 0.75rem;
  }

  .step-nav :deep(.p-button:only-child) {
    margin-left: auto;
  }

  .review-dialog-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    width: 100%;
  }

  .footer-actions {
    display: flex;
    gap: 0.75rem;
    margin-left: auto;
  }

  @media (max-width: 768px) {
    .review-dialog :deep(.p-dialog-header) {
      padding: 1rem;
      font-size: 1.125rem;
    }

    .review-dialog :deep(.p-dialog-content) {
      padding: 0.75rem;
    }
  }
</style>
