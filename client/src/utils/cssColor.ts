/**
 * Resolves PrimeVue CSS custom properties to their actual color values so that
 * Chart.js can use them directly on a <canvas> element (canvas cannot resolve
 * CSS variables itself).
 *
 * Call once per component — values are stable for the lifetime of the page
 * since the app is always in dark mode (.app-dark is set unconditionally).
 */
export function useCssColor(...vars: string[]): Record<string, string> {
  const style = getComputedStyle(document.documentElement)
  const result: Record<string, string> = {}
  for (const v of vars) {
    // Strip "var(" wrapper if caller passes the full token
    const prop = v.startsWith('var(') ? v.slice(4, -1).trim() : v
    result[v] = style.getPropertyValue(prop).trim()
  }
  return result
}
