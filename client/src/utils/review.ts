import type {
  ReviewFields,
  ReviewSubmitPayload,
  ReviewType,
  StructuredReviewFieldKey,
} from '@/types'

/** Options for the free-text / structured toggle. */
export const REVIEW_TYPE_OPTIONS: { label: string; value: ReviewType }[] = [
  { label: 'Free text', value: 'FREE_TEXT' },
  { label: 'Structured', value: 'STRUCTURED' },
]

/** Human-readable labels for review types (used in confirmation copy). */
export const REVIEW_TYPE_LABEL: Record<ReviewType, string> = {
  FREE_TEXT: 'free-text',
  STRUCTURED: 'structured',
}

export interface StructuredReviewField {
  key: StructuredReviewFieldKey
  label: string
  placeholder: string
}

/** Ordered structured-review categories — one stepper step each. */
export const STRUCTURED_REVIEW_FIELDS: StructuredReviewField[] = [
  { key: 'reviewStory', label: 'Story', placeholder: 'Plot, pacing, writing…' },
  { key: 'reviewPerformances', label: 'Performances', placeholder: 'Acting, casting, chemistry…' },
  { key: 'reviewDirection', label: 'Direction', placeholder: 'Direction, editing, tone…' },
  { key: 'reviewVisuals', label: 'Visuals', placeholder: 'Cinematography, effects, design…' },
  { key: 'reviewSound', label: 'Sound', placeholder: 'Score, sound design, mixing…' },
  { key: 'reviewVerdict', label: 'Verdict', placeholder: 'Overall take, recommendation…' },
]

/** A blank set of review fields. */
export function createEmptyReviewFields(): ReviewFields {
  return {
    reviewText: '',
    reviewStory: '',
    reviewPerformances: '',
    reviewDirection: '',
    reviewVisuals: '',
    reviewSound: '',
    reviewVerdict: '',
  }
}

/**
 * Quill emits an empty document as markup like `<p><br></p>`. Strip tags and
 * non-breaking spaces to decide whether the user actually wrote anything.
 */
export function isReviewHtmlEmpty(html: string | undefined | null): boolean {
  if (!html) return true
  const text = html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim()
  return text.length === 0
}

/** True when every review field is empty. */
export function isReviewFormEmpty(fields: ReviewFields): boolean {
  return (
    isReviewHtmlEmpty(fields.reviewText) &&
    STRUCTURED_REVIEW_FIELDS.every((field) => isReviewHtmlEmpty(fields[field.key]))
  )
}

/** True when the given review type has enough content to be saved. */
export function canSubmitReview(reviewType: ReviewType, fields: ReviewFields): boolean {
  if (reviewType === 'FREE_TEXT') {
    return !isReviewHtmlEmpty(fields.reviewText)
  }
  return STRUCTURED_REVIEW_FIELDS.some((field) => !isReviewHtmlEmpty(fields[field.key]))
}

/** Build the API payload for the active review type, dropping empty fields. */
export function buildReviewPayload(
  reviewType: ReviewType,
  fields: ReviewFields,
): ReviewSubmitPayload {
  if (reviewType === 'FREE_TEXT') {
    return { reviewType: 'FREE_TEXT', reviewText: fields.reviewText }
  }
  const payload: ReviewSubmitPayload = { reviewType: 'STRUCTURED' }
  for (const field of STRUCTURED_REVIEW_FIELDS) {
    if (!isReviewHtmlEmpty(fields[field.key])) payload[field.key] = fields[field.key]
  }
  return payload
}
