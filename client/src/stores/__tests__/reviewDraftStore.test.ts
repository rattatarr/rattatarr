import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useReviewDraftStore, type ReviewDraftFields } from '../reviewDraftStore'

const fields: ReviewDraftFields = {
  reviewType: 'FREE_TEXT',
  reviewText: '<p>draft</p>',
  reviewStory: '',
  reviewPerformances: '',
  reviewDirection: '',
  reviewVisuals: '',
  reviewSound: '',
  reviewVerdict: '',
}

describe('Review Draft Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('returns undefined when no draft exists', () => {
    const store = useReviewDraftStore()
    expect(store.getDraft('p1', 'e1')).toBeUndefined()
  })

  it('stores and retrieves a draft with a timestamp', () => {
    const store = useReviewDraftStore()
    store.setDraft('p1', 'e1', fields)

    const draft = store.getDraft('p1', 'e1')
    expect(draft).toMatchObject(fields)
    expect(typeof draft?.updatedAt).toBe('number')
  })

  it('keys drafts by profile and entity', () => {
    const store = useReviewDraftStore()
    store.setDraft('p1', 'e1', fields)

    expect(store.getDraft('p2', 'e1')).toBeUndefined()
    expect(store.getDraft('p1', 'e2')).toBeUndefined()
  })

  it('clears a draft', () => {
    const store = useReviewDraftStore()
    store.setDraft('p1', 'e1', fields)
    store.clearDraft('p1', 'e1')

    expect(store.getDraft('p1', 'e1')).toBeUndefined()
  })
})
