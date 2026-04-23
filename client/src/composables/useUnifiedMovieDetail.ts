import { computed, type Ref, type ComputedRef } from 'vue'
import { useMovie } from '@/queries/useLibrary'
import { useTMDbMovie } from '@/queries/useTMDb'
import { useProfileStore } from '@/stores/profileStore'
import { MediaSource } from '@/utils/enums'
import type { MediaSourceInfo } from './useMediaSource'
import type { UnifiedMovie, InternalMovie, ExternalMovie } from '@/types'
import {
  extractYear,
  transformInternalGenres,
  transformInternalCast,
  transformInternalCrew,
  transformTMDbGenres,
  transformTMDbCast,
  transformTMDbCrew,
  getTMDbImageUrl,
} from '@/utils/mediaTransformers'
import { useUnifiedQueryState } from './useUnifiedQueryState'

interface UnifiedMovieDetailResult {
  movie: ComputedRef<UnifiedMovie | null>
  isLoading: ComputedRef<boolean>
  error: ComputedRef<Error | null>
  refetch: () => Promise<any>
  source: MediaSourceInfo
}

/**
 * Composable for fetching movie details from either internal API or TMDb
 *
 * Automatically detects source and fetches from appropriate API,
 * then transforms data to unified format.
 *
 * @param movieId - Movie ID (internal UUID or TMDb numeric ID)
 * @param source - Media source information
 * @returns Unified movie data and query state
 */
export function useUnifiedMovieDetail(
  movieId: Ref<string>,
  source: Ref<MediaSourceInfo>,
): UnifiedMovieDetailResult {
  const profileStore = useProfileStore()

  // Conditional query keys to prevent both queries from running
  const internalMovieId = computed(() => (source.value.isInternal ? movieId.value : ''))
  const tmdbMovieId = computed(() => (source.value.isTMDb ? movieId.value : ''))

  // Query internal movie (only runs if source is internal)
  const internalQuery = useMovie(
    internalMovieId,
    computed(() => ({
      profileId: profileStore.selectedProfileId ?? undefined,
      posterSize: 'w500',
      backdropSize: 'original',
      profileSize: 'w185',
    })),
  )

  // Query TMDb movie (only runs if source is TMDb)
  const tmdbQuery = useTMDbMovie(tmdbMovieId)

  // Transform and unify data
  const movie = computed<UnifiedMovie | null>(() => {
    if (source.value.isInternal && internalQuery.data.value?.movies?.[0]) {
      const internalMovie = internalQuery.data.value.movies[0]
      return {
        id: internalMovie.id ?? '',
        title: internalMovie.title ?? 'Untitled',
        year: internalMovie.productionYear,
        runtimeMinutes: internalMovie.runtimeMinutes,
        genres: transformInternalGenres(internalMovie.genres),
        cast: transformInternalCast(internalMovie.cast),
        crew: transformInternalCrew(internalMovie.crew),
        posterUrl: internalMovie.metadata?.posterImageUrl,
        backdropUrl: internalMovie.metadata?.backdropImageUrl,
        description: internalMovie.metadata?.description,
        imdbId: internalMovie.IMDbId,
        tmdbId: internalMovie.TMDbId,
        imdbRating: internalMovie.metadata?.imdbRating ?? undefined,
        rottenTomatoesRating: internalMovie.metadata?.rottenTomatoesRating ?? undefined,
        jellyfinId: internalMovie.jellyfinId,
        myRating: internalMovie.myRating,
        source: MediaSource.INTERNAL,
      } satisfies InternalMovie
    }

    if (source.value.isTMDb && tmdbQuery.data.value) {
      const tmdbMovie = tmdbQuery.data.value
      return {
        id: tmdbMovie.id?.toString() ?? '',
        title: tmdbMovie.title ?? 'Untitled',
        year: extractYear(tmdbMovie.release_date),
        runtimeMinutes: tmdbMovie.runtime,
        genres: transformTMDbGenres(tmdbMovie.genres),
        cast: transformTMDbCast(tmdbMovie.credits?.cast),
        crew: transformTMDbCrew(tmdbMovie.credits?.crew),
        posterUrl: getTMDbImageUrl(tmdbMovie.poster_path, 'w500'),
        backdropUrl: getTMDbImageUrl(tmdbMovie.backdrop_path, 'original'),
        description: tmdbMovie.overview,
        imdbId: tmdbMovie.imdb_id,
        tmdbId: tmdbMovie.id?.toString(),
        source: MediaSource.TMDB,
      } satisfies ExternalMovie
    }

    return null
  })

  const { isLoading, error, refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

  return {
    movie,
    isLoading,
    error,
    refetch,
    source: source.value,
  }
}
