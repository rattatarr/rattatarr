import { computed, type Ref } from 'vue'
import { useRoute } from 'vue-router'
import { MediaSource, type MediaSourceType } from '@/utils/enums'

export interface MediaSourceInfo {
  type: MediaSourceType
  isTMDb: boolean
  isInternal: boolean
  isIMDb: boolean
  id: string
}

// ID format patterns for different sources
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
const NUMERIC_ID_REGEX = /^\d+$/ // TMDb IDs are purely numeric
const IMDB_ID_REGEX = /^tt\d+$/ // IMDb IDs start with 'tt' followed by numbers

/**
 * Detect media source from ID format
 * Extensible for future sources
 */
function detectSourceFromIdFormat(id: string): MediaSourceType | null {
  if (UUID_REGEX.test(id)) return MediaSource.INTERNAL
  if (IMDB_ID_REGEX.test(id)) return MediaSource.IMDB
  if (NUMERIC_ID_REGEX.test(id)) return MediaSource.TMDB
  return null
}

/**
 * Composable for detecting media source (internal vs external)
 *
 * Detection strategy (priority order):
 * 1. Check query parameter (?source=tmdb, ?source=imdb, etc.)
 * 2. Check ID format (UUID = internal, numeric = TMDb, tt##### = IMDb)
 * 3. Default to internal if ambiguous
 *
 * @param id - Media ID from route params (reactive)
 * @returns Media source information
 */
export function useMediaSource(id: Ref<string>): Ref<MediaSourceInfo> {
  const route = useRoute()

  return computed<MediaSourceInfo>(() => {
    const idValue = id.value

    // Priority 1: Check query parameter
    const querySource = route.query.source as string | undefined
    if (querySource) {
      const sourceType = querySource.toLowerCase() as MediaSourceType
      return {
        type: sourceType,
        isTMDb: sourceType === MediaSource.TMDB,
        isInternal: sourceType === MediaSource.INTERNAL,
        isIMDb: sourceType === MediaSource.IMDB,
        id: idValue,
      }
    }

    // Priority 2: Detect from ID format
    const detectedSource = detectSourceFromIdFormat(idValue)
    if (detectedSource) {
      return {
        type: detectedSource,
        isTMDb: detectedSource === MediaSource.TMDB,
        isInternal: detectedSource === MediaSource.INTERNAL,
        isIMDb: detectedSource === MediaSource.IMDB,
        id: idValue,
      }
    }

    // Default: Assume internal
    return {
      type: MediaSource.INTERNAL,
      isTMDb: false,
      isInternal: true,
      isIMDb: false,
      id: idValue,
    }
  })
}
