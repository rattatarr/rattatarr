/**
 * API Client Modules
 *
 * Type-safe API clients generated from backend OpenAPI schema.
 * All functions use openapi-fetch for automatic type inference.
 *
 * Usage in components:
 * - Import from domain-specific modules (settings, profiles, etc.)
 * - Wrap with TanStack Query hooks in src/queries/ for caching
 * - Never call these directly from components (use query hooks instead)
 */

export * from './client'
export * as settings from './settings'
export * as profiles from './profiles'
export * as ratings from './ratings'
export * as library from './library'
export * as people from './people'
export * as tmdb from './tmdb'
export * as jellyfin from './jellyfin'
export * as logs from './logs'
export * as brokenMedia from './brokenMedia'
export * as statistics from './statistics'
export * as agent from './agent'
export * as jobs from './jobs'
export * as watchActivity from './watchActivity'
export * as radarr from './radarr'
export * as sonarr from './sonarr'
export * as github from './github'
