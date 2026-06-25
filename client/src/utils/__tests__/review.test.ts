import { describe, it, expect } from 'vitest'
import { isReviewHtmlEmpty } from '@/utils/review'

describe('isReviewHtmlEmpty', () => {
  it('treats nullish values as empty', () => {
    expect(isReviewHtmlEmpty(undefined)).toBe(true)
    expect(isReviewHtmlEmpty(null)).toBe(true)
    expect(isReviewHtmlEmpty('')).toBe(true)
  })

  it("treats Quill's empty document as empty", () => {
    expect(isReviewHtmlEmpty('<p><br></p>')).toBe(true)
    expect(isReviewHtmlEmpty('<p>&nbsp;</p>')).toBe(true)
    expect(isReviewHtmlEmpty('   ')).toBe(true)
  })

  it('treats markup with real text as non-empty', () => {
    expect(isReviewHtmlEmpty('<p>Great movie</p>')).toBe(false)
    expect(isReviewHtmlEmpty('<ul><li>point</li></ul>')).toBe(false)
  })
})
