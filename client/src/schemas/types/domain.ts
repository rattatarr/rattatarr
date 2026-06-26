import { MediaSource } from '@/utils/enums'
import type { Review } from './api.ts'

export interface DomainGenre {
  name: string
}

export interface DomainCastMember {
  id: string
  character: string
  order: number
  person: {
    id?: string
    name: string
    profilePathUrl?: string
  }
}

export interface DomainCrewMember {
  id: string
  job: string
  department?: string
  person: {
    id?: string
    name: string
    profilePathUrl?: string
  }
}

export interface DomainEpisode {
  id: string
  episode: number
  title: string
  runtimeMinutes?: number
}

export interface DomainSeason {
  id?: string
  seasonNumber: number
  title: string
  episodeCount?: number
  airDate?: string
  posterUrl?: string
  episodes: DomainEpisode[]
  initiallyExpanded: boolean
  myRating?: number
  review?: Review
}

/**
 * Base unified movie structure (common properties for all sources)
 */
export interface UnifiedMovieBase {
  id: string
  title: string
  year?: number
  runtimeMinutes?: number
  genres: DomainGenre[]
  cast: DomainCastMember[]
  crew: DomainCrewMember[]
  posterUrl?: string
  backdropUrl?: string
  description?: string
  imdbId?: string
  tmdbId?: string
  imdbRating?: number
  rottenTomatoesRating?: number
}

/**
 * Internal-only movie properties
 */
export interface InternalMovieProps {
  source: MediaSource.INTERNAL
  jellyfinId?: string
  myRating?: number
  review?: Review
}

/**
 * External (TMDb) movie properties
 */
export interface ExternalMovieProps {
  source: MediaSource.TMDB
}

/**
 * Internal movie: base properties + internal-specific properties
 */
export type InternalMovie = UnifiedMovieBase & InternalMovieProps

/**
 * External movie: base properties + external-specific properties
 */
export type ExternalMovie = UnifiedMovieBase & ExternalMovieProps

/**
 * Unified movie: can be either internal or external
 */
export type UnifiedMovie = InternalMovie | ExternalMovie

/**
 * Base unified series structure (common properties for all sources)
 */
export interface UnifiedSeriesBase {
  id: string
  title: string
  year?: number
  runtimeMinutes?: number
  genres: DomainGenre[]
  cast: DomainCastMember[]
  crew: DomainCrewMember[]
  posterUrl?: string
  backdropUrl?: string
  description?: string
  imdbId?: string
  tmdbId?: string
  imdbRating?: number
  rottenTomatoesRating?: number
  seasons: DomainSeason[]
}

/**
 * Internal-only series properties
 */
export interface InternalSeriesProps {
  source: MediaSource.INTERNAL
  jellyfinId?: string
  myRating?: number
  review?: Review
}

/**
 * External (TMDb) series properties
 */
export interface ExternalSeriesProps {
  source: MediaSource.TMDB
}

/**
 * Internal series: base properties + internal-specific properties
 */
export type InternalSeries = UnifiedSeriesBase & InternalSeriesProps

/**
 * External series: base properties + external-specific properties
 */
export type ExternalSeries = UnifiedSeriesBase & ExternalSeriesProps

/**
 * Unified series: can be either internal or external
 */
export type UnifiedSeries = InternalSeries | ExternalSeries
