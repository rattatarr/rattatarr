/**
 * Utilities for transforming TMDb data to internal format
 */

import type { DomainGenre, DomainCastMember, DomainCrewMember, DomainSeason } from '@/types'

const TMDB_IMAGE_BASE = 'https://image.tmdb.org/t/p'

export function getTMDbImageUrl(
  path: string | null | undefined,
  size: string = 'original',
): string | undefined {
  if (!path) return undefined
  return `${TMDB_IMAGE_BASE}/${size}${path}`
}

/**
 * Extract year from date string
 */
export function extractYear(dateString: string | null | undefined): number | undefined {
  if (!dateString) return undefined
  const year = new Date(dateString).getFullYear()
  return isNaN(year) ? undefined : year
}

export function transformInternalGenres(
  genres: Array<{ id?: string; name?: string }> | undefined,
): DomainGenre[] {
  if (!genres) return []
  return genres.filter((g) => g.name).map((g) => ({ name: g.name! }))
}

export function transformTMDbGenres(
  genres: Array<{ id?: number; name?: string }> | undefined,
): DomainGenre[] {
  if (!genres) return []
  return genres.filter((g) => g.name).map((g) => ({ name: g.name! }))
}

export function transformInternalCast(
  cast:
    | Array<{
        id?: string
        character?: string
        order?: number
        person?: {
          id?: string
          name?: string
          profilePathUrl?: string
        }
      }>
    | undefined,
): DomainCastMember[] {
  if (!cast) return []

  return cast
    .filter((c) => c.id && c.person?.name && c.character !== undefined)
    .map((c) => ({
      id: c.id!,
      character: c.character!,
      order: c.order ?? 0,
      person: {
        id: c.person!.id,
        name: c.person!.name!,
        profilePathUrl: c.person!.profilePathUrl,
      },
    }))
}

export function transformInternalCrew(
  crew:
    | Array<{
        id?: string
        job?: string
        department?: string
        person?: {
          id?: string
          name?: string
          profilePathUrl?: string
        }
      }>
    | undefined,
): DomainCrewMember[] {
  if (!crew) return []

  return crew
    .filter((c) => c.id && c.person?.name && c.job)
    .map((c) => ({
      id: c.id!,
      job: c.job!,
      department: c.department,
      person: {
        id: c.person!.id,
        name: c.person!.name!,
        profilePathUrl: c.person!.profilePathUrl,
      },
    }))
}

export function transformTMDbCast(
  cast:
    | Array<{
        id?: string | number
        name?: string
        character?: string
        order?: number
        profile_path?: string | null
      }>
    | undefined,
  limit: number = 10,
): DomainCastMember[] {
  if (!cast) return []

  return cast
    .filter((c) => c.id && c.name && c.character !== undefined && c.order !== undefined)
    .slice(0, limit)
    .map((c) => ({
      id: String(c.id!),
      character: c.character!,
      order: c.order!,
      person: {
        id: String(c.id!),
        name: c.name!,
        profilePathUrl: getTMDbImageUrl(c.profile_path, 'w185'),
      },
    }))
}

export function transformTMDbCrew(
  crew:
    | Array<{
        id?: string | number
        name?: string
        job?: string
        department?: string
        profile_path?: string | null
      }>
    | undefined,
  limit: number = 10,
): DomainCrewMember[] {
  if (!crew) return []

  return crew
    .filter((c) => c.id && c.name && c.job)
    .slice(0, limit)
    .map((c) => ({
      id: String(c.id!),
      job: c.job!,
      department: c.department,
      person: {
        id: String(c.id!),
        name: c.name!,
        profilePathUrl: getTMDbImageUrl(c.profile_path, 'w185'),
      },
    }))
}

export function transformTMDbSeasons(
  allSeasonDetails:
    | Record<
        string,
        {
          season_number?: number
          name?: string
          air_date?: string
          poster_path?: string | null
          episodes?: Array<{
            id?: number
            episode_number?: number
            name?: string
            runtime?: number
          }>
        }
      >
    | undefined,
): DomainSeason[] {
  if (!allSeasonDetails) return []

  return Object.values(allSeasonDetails)
    .filter((season) => {
      // Filter out seasons without season_number
      if (season.season_number === undefined) return false
      // Filter out "Specials" (season 0) if it has no episodes
      if (season.season_number === 0) {
        return season.episodes && season.episodes.length > 0
      }
      return true
    })
    .sort((a, b) => (a.season_number || 0) - (b.season_number || 0))
    .map((season) => ({
      seasonNumber: season.season_number!,
      title: season.name || `Season ${season.season_number}`,
      episodeCount: season.episodes?.length,
      airDate: season.air_date,
      posterUrl: getTMDbImageUrl(season.poster_path, 'w500'),
      episodes:
        season.episodes
          ?.filter((ep) => ep.episode_number !== undefined)
          .map((ep) => ({
            id: ep.id?.toString() || `${season.season_number}-${ep.episode_number}`,
            episode: ep.episode_number!,
            title: ep.name || `Episode ${ep.episode_number}`,
            runtimeMinutes: ep.runtime,
          })) || [],
      initiallyExpanded: season.season_number === 1,
    }))
}
