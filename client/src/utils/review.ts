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
