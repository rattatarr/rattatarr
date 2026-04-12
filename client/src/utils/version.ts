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
