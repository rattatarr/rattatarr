import { computed, type Ref, type ComputedRef } from 'vue'
import { useSeriesDetail } from '@/queries/useLibrary'
import { useTMDbSeries } from '@/queries/useTMDb'
import { useProfileStore } from '@/stores/profileStore'
import { MediaSource } from '@/utils/enums'
import type { MediaSourceInfo } from './useMediaSource'
import type { UnifiedSeries, InternalSeries, ExternalSeries, DomainSeason } from '@/types'
import {
  extractYear,
  transformInternalGenres,
  transformInternalCast,
  transformInternalCrew,
  transformTMDbGenres,
  transformTMDbCast,
  transformTMDbCrew,
  transformTMDbSeasons,
  getTMDbImageUrl,
} from '@/utils/mediaTransformers'
import { useUnifiedQueryState } from './useUnifiedQueryState'

interface UnifiedSeriesDetailResult {
  series: ComputedRef<UnifiedSeries | null>
  isLoading: ComputedRef<boolean>
  error: ComputedRef<Error | null>
  refetch: () => Promise<any>
  source: MediaSourceInfo
}

/**
 * Composable for fetching series details from either internal API or TMDb
 *
 * Automatically detects source and fetches from appropriate API,
 * then transforms data to unified format.
 *
 * @param seriesId - Series ID (internal UUID or TMDb numeric ID)
 * @param source - Media source information
 * @returns Unified series data and query state
 */
export function useUnifiedSeriesDetail(
  seriesId: Ref<string>,
  source: Ref<MediaSourceInfo>,
): UnifiedSeriesDetailResult {
  const profileStore = useProfileStore()

  // Conditional query keys to prevent both queries from running
  const internalSeriesId = computed(() => (source.value.isInternal ? seriesId.value : ''))
  const tmdbSeriesId = computed(() => (source.value.isTMDb ? seriesId.value : ''))

  // Query internal series (only runs if source is internal)
  const internalQuery = useSeriesDetail(
    internalSeriesId,
    computed(() => ({
      profileId: profileStore.selectedProfileId ?? undefined,
      withEpisodes: true,
      posterSize: 'w500',
      backdropSize: 'original',
      profileSize: 'w185',
    })),
  )

  // Query TMDb series (only runs if source is TMDb)
  const tmdbQuery = useTMDbSeries(tmdbSeriesId)

  // Transform internal seasons to match unified format
  const transformInternalSeasons = (seasons: any[] | undefined): DomainSeason[] => {
    if (!seasons) return []

    return [...seasons]
      .filter((season) => {
        // Filter out "Specials" (season 0) if it has no episodes
        if (season.season === 0) {
          return season.episodes && season.episodes.length > 0
        }
        return true
      })
      .sort((a, b) => (a.season || 0) - (b.season || 0))
      .map((season) => ({
        seasonNumber: season.season ?? 0,
        title: season.title ?? `Season ${season.season}`,
        episodeCount: season.metadata?.episodeCount,
        airDate: season.metadata?.airDate,
        posterUrl: season.metadata?.posterImageUrl,
        episodes:
          season.episodes?.map((ep: any) => ({
            id: ep.id ?? '',
            episode: ep.episode ?? 0,
            title: ep.title ?? `Episode ${ep.episode}`,
            runtimeMinutes: ep.runtimeMinutes,
          })) || [],
        initiallyExpanded: season.season === 1,
      }))
  }

  // Transform and unify data
  const series = computed<UnifiedSeries | null>(() => {
    if (source.value.isInternal && internalQuery.data.value?.series?.[0]) {
      const internalSeries = internalQuery.data.value.series[0]
      return {
        id: internalSeries.id ?? '',
        title: internalSeries.title ?? 'Untitled',
        year: internalSeries.productionYear,
        runtimeMinutes: internalSeries.runtimeMinutes,
        genres: transformInternalGenres(internalSeries.genres),
        cast: transformInternalCast(internalSeries.cast),
        crew: transformInternalCrew(internalSeries.crew),
        posterUrl: internalSeries.metadata?.posterImageUrl,
        backdropUrl: internalSeries.metadata?.backdropImageUrl,
        description: internalSeries.metadata?.description,
        imdbId: internalSeries.IMDbId,
        tmdbId: internalSeries.TMDbId,
        jellyfinId: internalSeries.jellyfinId,
        myRating: internalSeries.myRating,
        seasons: transformInternalSeasons(internalSeries.seasons),
        source: MediaSource.INTERNAL,
      } satisfies InternalSeries
    }

    if (source.value.isTMDb && tmdbQuery.data.value) {
      const tmdbSeries = tmdbQuery.data.value
      return {
        id: tmdbSeries.id?.toString() ?? '',
        title: tmdbSeries.name ?? 'Untitled',
        year: extractYear(tmdbSeries.first_air_date),
        runtimeMinutes: tmdbSeries.episode_run_time?.[0],
        genres: transformTMDbGenres(tmdbSeries.genres),
        cast: transformTMDbCast(tmdbSeries.credits?.cast),
        crew: transformTMDbCrew(tmdbSeries.credits?.crew),
        posterUrl: getTMDbImageUrl(tmdbSeries.poster_path, 'w500'),
        backdropUrl: getTMDbImageUrl(tmdbSeries.backdrop_path, 'original'),
        description: tmdbSeries.overview,
        tmdbId: tmdbSeries.id?.toString(),
        seasons: transformTMDbSeasons(tmdbSeries.allSeasonDetails),
        source: MediaSource.TMDB,
      } satisfies ExternalSeries
    }

    return null
  })

  const { isLoading, error, refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

  return {
    series,
    isLoading,
    error,
    refetch,
    source: source.value,
  }
}
