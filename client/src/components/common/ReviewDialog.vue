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
  import { useConfirm } from 'primevue/useconfirm'
  import ReviewRichText from '@/components/common/ReviewRichText.vue'
  import { useReviewDraftStore } from '@/stores/reviewDraftStore'
  import { Icon, ButtonSeverity } from '@/utils/enums.ts'
  import {
    REVIEW_TYPE_OPTIONS,
    REVIEW_TYPE_LABEL,
    STRUCTURED_REVIEW_FIELDS,
    buildReviewPayload,
    canSubmitReview,
    createEmptyReviewFields,
    isReviewFormEmpty,
  } from '@/utils/review'
  import type { Review, ReviewFields, ReviewSubmitPayload, ReviewType } from '@/types'

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
  const confirm = useConfirm()

  const reviewType = ref<ReviewType>('FREE_TEXT')
  const activeStep = ref(1)
  // Guards the persist-watch from firing while we load review/draft into the form
  const isHydrating = ref(false)
  // True when an unsaved cached draft is currently shown
  const draftActive = ref(false)
  const form = reactive<ReviewFields>(createEmptyReviewFields())

  const hasExistingReview = computed(() => !!props.review)
  const header = computed(() => `Review: ${props.title || 'Media'}`)
  const canSubmit = computed(() => canSubmitReview(reviewType.value, form))

  const dialogStyle = computed(() => {
    const isMobile = window.innerWidth <= 768
    if (isMobile) {
      return { width: '100vw', maxWidth: '100vw', margin: '0' }
    }
    return { width: '800px', maxWidth: '90vw' }
  })

  const applyFields = (source: Partial<ReviewFields>) => {
    Object.assign(form, createEmptyReviewFields(), {
      reviewText: source.reviewText ?? '',
      reviewStory: source.reviewStory ?? '',
      reviewPerformances: source.reviewPerformances ?? '',
      reviewDirection: source.reviewDirection ?? '',
      reviewVisuals: source.reviewVisuals ?? '',
      reviewSound: source.reviewSound ?? '',
      reviewVerdict: source.reviewVerdict ?? '',
    })
  }

  // Populate from existing review (then overlay any cached draft) when the dialog opens
  watch(
    () => props.visible,
    (isVisible) => {
      if (!isVisible) return
      isHydrating.value = true
      activeStep.value = 1

      const existing = props.review
      reviewType.value = existing?.reviewType ?? 'FREE_TEXT'
      applyFields(existing ?? {})

      const draft =
        props.profileId && props.entityId
          ? draftStore.getDraft(props.profileId, props.entityId)
          : undefined
      if (draft) {
        reviewType.value = draft.reviewType
        applyFields(draft)
      }
      draftActive.value = !!draft

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

      if (isReviewFormEmpty(form)) {
        draftStore.clearDraft(props.profileId, props.entityId)
        draftActive.value = false
      } else {
        draftStore.setDraft(props.profileId, props.entityId, {
          ...form,
          reviewType: reviewType.value,
        })
        draftActive.value = true
      }
    },
    { deep: true },
  )

  const handleVisibilityChange = (value: boolean) => {
    emit('update:visible', value)
  }

  const handleRate = () => {
    emit('rate')
  }

  const handleSubmit = () => {
    if (!canSubmit.value || !props.hasRating) return

    const payload = buildReviewPayload(reviewType.value, form)
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
          <Step v-for="(field, i) in STRUCTURED_REVIEW_FIELDS" :key="field.key" :value="i + 1">
            {{ field.label }}
          </Step>
        </StepList>
        <StepPanels>
          <StepPanel
            v-for="(field, i) in STRUCTURED_REVIEW_FIELDS"
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
                  v-if="i < STRUCTURED_REVIEW_FIELDS.length - 1"
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
