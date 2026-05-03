/**
 * Application version utilities
 *
 * The version is injected at build time by Vite from:
 * 1. Latest git tag (if available)
 * 2. package.json version (fallback)
 * 3. 'v0.0.1' (ultimate fallback)
 */

export function getAppVersion(): string {
  return import.meta.env.VITE_APP_VERSION || 'v0.0.1'
}

/** Normalizes version string to numeric tuple, stripping leading 'v' and pre-release suffixes. */
function parseVersion(version: string): number[] {
  return version
    .replace(/^v/, '')
    .split('-')[0]!
    .split('.')
    .map((n) => parseInt(n, 10) || 0)
}

export function isNewerVersion(currentVersion: string, latestVersion: string): boolean {
  const current = parseVersion(currentVersion)
  const latest = parseVersion(latestVersion)
  const len = Math.max(current.length, latest.length)
  for (let i = 0; i < len; i++) {
    const c = current[i] ?? 0
    const l = latest[i] ?? 0
    if (l > c) return true
    if (l < c) return false
  }
  return false
}
