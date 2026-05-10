import { ref, computed, provide, inject, type InjectionKey, type Ref } from 'vue'
import type { MovieFilters, SeriesFilters, Person } from '@/types'
import { useProfileStore } from '@/stores'

/**
 * Sort field options for movies and series
 */
export type SortField = 'title' | 'ratings' | 'productionYear' | 'lastWatched'

/**
 * Sort order options
 */
export type SortOrder = 'asc' | 'desc'

/**
 * A single sort criterion (field + direction)
 */
export interface SortEntry {
  field: SortField
  order: SortOrder
}

/**
 * Media filter state
 */
export interface MediaFilterState {
  // Search
  titleSearch: Ref<string>

  // Genres
  selectedGenres: Ref<string[]>

  // People (single person filter)
  selectedPerson: Ref<Person | null>

  // Year range
  releasedAfter: Ref<number | undefined>
  releasedBefore: Ref<number | undefined>

  // Unrated filter
  unrated: Ref<boolean>

  // Sorting (ordered priority list, up to 3 entries)
  sorts: Ref<SortEntry[]>

  // Computed filters
  movieFilters: Ref<MovieFilters>
  seriesFilters: Ref<SeriesFilters>
  sortArray: Ref<string[]>

  // Actions
  reset: () => void

  // Constants
  PAGE_SIZE: number
}

const MEDIA_FILTERS_KEY: InjectionKey<MediaFilterState> = Symbol('media-filters')

const PAGE_SIZE: number = 24

/**
 * Create media filter state for movies
 */
export function useMediaFilters() {
  const profileStore = useProfileStore()
  const titleSearch = ref('')
  const selectedGenres = ref<string[]>([])
  const selectedPerson = ref<Person | null>(null)
  const releasedAfter = ref<number | undefined>()
  const releasedBefore = ref<number | undefined>()
  const unrated = ref(false)
  const sorts = ref<SortEntry[]>([{ field: 'ratings', order: 'desc' }])

  // Build sort array from priority list
  const sortArray = computed(() => sorts.value.map((s) => `${s.field},${s.order}`))

  const movieFilters = computed<MovieFilters>(() => ({
    title: titleSearch.value || '',
    genres: selectedGenres.value.length > 0 ? selectedGenres.value : undefined,
    releasedAfter: releasedAfter.value,
    releasedBefore: releasedBefore.value,
    profileId: profileStore.selectedProfileId ?? undefined,
    unrated: unrated.value || undefined,
    personId: selectedPerson.value?.id ?? undefined,
  }))

  const seriesFilters = computed<SeriesFilters>(() => ({
    title: titleSearch.value || '',
    genres: selectedGenres.value.length > 0 ? selectedGenres.value : undefined,
    releasedAfter: releasedAfter.value,
    releasedBefore: releasedBefore.value,
    profileId: profileStore.selectedProfileId ?? undefined,
    unrated: unrated.value || undefined,
    personId: selectedPerson.value?.id ?? undefined,
  }))

  function reset() {
    titleSearch.value = ''
    selectedGenres.value = []
    selectedPerson.value = null
    releasedAfter.value = undefined
    releasedBefore.value = undefined
    unrated.value = false
    sorts.value = [{ field: 'ratings', order: 'desc' }]
  }

  const state: MediaFilterState = {
    titleSearch,
    selectedGenres,
    selectedPerson,
    releasedAfter,
    releasedBefore,
    unrated,
    sorts,
    movieFilters,
    seriesFilters,
    sortArray,
    reset,
    PAGE_SIZE,
  }

  provide(MEDIA_FILTERS_KEY, state)

  return state
}

/**
 * Inject media filter state
 */
export function useMediaFiltersContext(): MediaFilterState {
  const state = inject(MEDIA_FILTERS_KEY)
  if (!state) {
    throw new Error('useMediaFiltersContext must be used within a provider')
  }
  return state
}
