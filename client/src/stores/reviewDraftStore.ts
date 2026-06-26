import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ReviewFields, ReviewType } from '@/types'

/**
 * Review Draft Store — UI state only.
 *
 * Holds in-progress review text the user has typed but not yet saved (e.g. while
 * they still need to rate the item first). Kept in memory only, so drafts survive
 * navigation within the session but are intentionally cleared on a page refresh.
 *
 * Keyed by `${profileId}:${entityId}` so drafts don't leak across profiles/items.
 */
export type ReviewDraftFields = ReviewFields & { reviewType: ReviewType }

export type ReviewDraft = ReviewDraftFields & { updatedAt: number }

export const useReviewDraftStore = defineStore('reviewDraft', () => {
  const drafts = ref<Record<string, ReviewDraft>>({})

  function draftKey(profileId: string, entityId: string): string {
    return `${profileId}:${entityId}`
  }

  function getDraft(profileId: string, entityId: string): ReviewDraft | undefined {
    return drafts.value[draftKey(profileId, entityId)]
  }

  function setDraft(profileId: string, entityId: string, fields: ReviewDraftFields) {
    drafts.value[draftKey(profileId, entityId)] = { ...fields, updatedAt: Date.now() }
  }

  function clearDraft(profileId: string, entityId: string) {
    delete drafts.value[draftKey(profileId, entityId)]
  }

  return { drafts, getDraft, setDraft, clearDraft }
})
