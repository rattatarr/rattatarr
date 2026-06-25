<script setup lang="ts">
  import { computed } from 'vue'
  import { isReviewHtmlEmpty } from '@/utils/review'
  import type { Review } from '@/types'

  interface Props {
    review: Review
  }

  const props = defineProps<Props>()

  const STRUCTURED_FIELDS = [
    { key: 'reviewStory', label: 'Story' },
    { key: 'reviewPerformances', label: 'Performances' },
    { key: 'reviewDirection', label: 'Direction' },
    { key: 'reviewVisuals', label: 'Visuals' },
    { key: 'reviewSound', label: 'Sound' },
    { key: 'reviewVerdict', label: 'Verdict' },
  ] as const

  const isFreeText = computed(() => props.review.reviewType === 'FREE_TEXT')

  const hasFreeText = computed(() => !isReviewHtmlEmpty(props.review.reviewText))

  const structuredSections = computed(() =>
    STRUCTURED_FIELDS.map((field) => ({
      label: field.label,
      value: props.review[field.key] ?? '',
    })).filter((section) => !isReviewHtmlEmpty(section.value)),
  )
</script>

<!--
  Review HTML is produced by the constrained Quill editor (no images) and
  sanitized server-side by ReviewHtmlSanitizer before persistence, so the value
  rendered here (always fetched from the backend) is trusted markup.
-->
<template>
  <div class="review-display">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-if="isFreeText && hasFreeText" class="review-prose" v-html="review.reviewText" />

    <div v-else-if="!isFreeText" class="review-structured">
      <div v-for="section in structuredSections" :key="section.label" class="review-section">
        <h4 class="review-section-label">{{ section.label }}</h4>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <div class="review-prose" v-html="section.value" />
      </div>
    </div>
  </div>
</template>

<style scoped>
  .review-display {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .review-structured {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .review-section {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .review-section-label {
    margin: 0;
    font-size: 1.15rem;
    font-weight: 700;
    color: var(--primary-color);
  }

  .review-prose {
    line-height: 1.7;
    color: var(--p-text-color);
    overflow-wrap: break-word;
    word-break: break-word;
  }

  .review-prose :deep(p) {
    margin: 0 0 0.75rem 0;
  }

  .review-prose :deep(p:last-child) {
    margin-bottom: 0;
  }

  /* Global reset forces font-weight:normal; restore bold/heading weights */
  .review-prose :deep(strong),
  .review-prose :deep(b) {
    font-weight: 700;
  }

  .review-prose :deep(h1),
  .review-prose :deep(h2),
  .review-prose :deep(h3) {
    margin: 1rem 0 0.5rem 0;
    line-height: 1.3;
    font-weight: 600;
  }

  .review-prose :deep(ul),
  .review-prose :deep(ol) {
    margin: 0 0 0.75rem 0;
    padding-left: 1.5rem;
  }

  .review-prose :deep(blockquote) {
    margin: 0 0 0.75rem 0;
    padding-left: 1rem;
    border-left: 3px solid var(--surface-border);
    color: var(--p-text-secondary-color);
  }

  .review-prose :deep(a) {
    color: var(--primary-color);
    text-decoration: underline;
  }

  .review-prose :deep(pre) {
    padding: 0.75rem;
    border-radius: var(--border-radius);
    background: var(--surface-ground);
    overflow-x: auto;
  }
</style>
