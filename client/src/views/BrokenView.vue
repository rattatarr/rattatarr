<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Button from 'primevue/button'
  import Column from 'primevue/column'
  import DataTable from 'primevue/datatable'
  import Dialog from 'primevue/dialog'
  import InputText from 'primevue/inputtext'
  import ProgressSpinner from 'primevue/progressspinner'
  import BrokenMediaTable from '@/components/broken/BrokenMediaTable.vue'
  import {
    useBrokenMovies,
    useBrokenSeries,
    useResolveBrokenMediaItem,
    useMovies,
    useSeries,
  } from '@/queries'
  import { useToast } from '@/composables/useToast'
  import type { BrokenMediaItem, Movie, Show, Pageable, MovieFilters, SeriesFilters } from '@/types'
  import { Icon, MediaType } from '@/utils/enums'

  type ActiveTab = MediaType.MOVIE | MediaType.SERIES

  const toast = useToast()

  // Tab state
  const activeTab = ref<ActiveTab>(MediaType.MOVIE)

  // Pagination
  const pageable = ref<Pageable>({ page: 0, size: 50 })

  // Fetch broken items
  const brokenMoviesQuery = useBrokenMovies(pageable)
  const brokenSeriesQuery = useBrokenSeries(pageable)

  const currentItems = computed((): BrokenMediaItem[] => {
    if (activeTab.value === MediaType.MOVIE) {
      return brokenMoviesQuery.data.value?.movies ?? []
    }
    return brokenSeriesQuery.data.value?.movies ?? []
  })

  const currentLoading = computed(() => {
    return activeTab.value === MediaType.MOVIE
      ? brokenMoviesQuery.isLoading.value
      : brokenSeriesQuery.isLoading.value
  })

  // Resolve dialog state
  const resolveDialogVisible = ref(false)
  const resolveTarget = ref<BrokenMediaItem | null>(null)
  const searchQuery = ref('')
  const selectedMediaItem = ref<Movie | Show | null>(null)

  // Search local library
  const searchPageable = ref<Pageable>({ page: 0, size: 20 })
  const movieSearchFilters = computed<MovieFilters>(() => ({
    title: searchQuery.value,
    posterSize: 'w92',
  }))
  const seriesSearchFilters = computed<SeriesFilters>(() => ({
    title: searchQuery.value,
    posterSize: 'w92',
  }))

  const movieSearchQuery = useMovies(searchPageable, movieSearchFilters)
  const seriesSearchQuery = useSeries(searchPageable, seriesSearchFilters)

  const searchResults = computed<Array<Movie | Show>>(() => {
    if (activeTab.value === MediaType.MOVIE) {
      return (movieSearchQuery.data.value?.movies ?? []) as Movie[]
    }
    return (seriesSearchQuery.data.value?.series ?? []) as Show[]
  })

  const searchLoading = computed(() =>
    activeTab.value === MediaType.MOVIE
      ? movieSearchQuery.isLoading.value
      : seriesSearchQuery.isLoading.value,
  )

  // Resolve mutation
  const resolveMutation = useResolveBrokenMediaItem()

  function openResolveDialog(item: BrokenMediaItem) {
    resolveTarget.value = item
    searchQuery.value = item.title ?? ''
    selectedMediaItem.value = null
    resolveDialogVisible.value = true
  }

  function closeResolveDialog() {
    resolveDialogVisible.value = false
    resolveTarget.value = null
    selectedMediaItem.value = null
    searchQuery.value = ''
  }

  async function confirmResolve() {
    if (!resolveTarget.value?.id || !selectedMediaItem.value?.id) return

    try {
      await resolveMutation.mutateAsync({
        id: resolveTarget.value.id,
        request: { mediaItemId: selectedMediaItem.value.id },
      })
      toast.success('Media item resolved successfully')
      closeResolveDialog()
    } catch (err) {
      toast.error(err)
    }
  }
</script>

<template>
  <div class="broken-view">
    <!-- Tab buttons -->
    <div class="tab-bar">
      <Button
        label="Movies"
        :icon="Icon.FILM"
        :outlined="activeTab !== MediaType.MOVIE"
        @click="activeTab = MediaType.MOVIE"
      />
      <Button
        label="Series"
        :icon="Icon.TH_LARGE"
        :outlined="activeTab !== MediaType.SERIES"
        @click="activeTab = MediaType.SERIES"
      />
    </div>

    <!-- Table -->
    <div class="broken-table-container">
      <BrokenMediaTable
        :items="currentItems"
        :loading="currentLoading"
        @resolve="openResolveDialog"
      />
    </div>

    <!-- Resolve Dialog -->
    <Dialog
      v-model:visible="resolveDialogVisible"
      :header="`Resolve: ${resolveTarget?.title ?? ''}`"
      :style="{ width: '600px' }"
      modal
      @hide="closeResolveDialog"
    >
      <div class="resolve-dialog-content">
        <p class="resolve-hint">
          Search for the correct
          {{ activeTab === MediaType.MOVIE ? 'movie' : MediaType.SERIES }} in the local library to
          link this item to.
        </p>

        <InputText
          v-model="searchQuery"
          placeholder="Search by title..."
          class="search-input"
          fluid
        />

        <div v-if="searchResults.length > 0 || searchLoading" class="search-results">
          <ProgressSpinner v-if="searchLoading" style="width: 32px; height: 32px" />
          <DataTable
            v-else
            :value="searchResults"
            v-model:selection="selectedMediaItem"
            selection-mode="single"
            :meta-key-selection="false"
            class="results-table"
          >
            <Column style="width: 4rem; padding: 0.25rem">
              <template #body="{ data }">
                <img
                  v-if="data.metadata?.posterImageUrl"
                  :src="data.metadata.posterImageUrl"
                  :alt="data.title"
                  class="result-poster"
                />
                <div v-else class="result-poster result-poster--placeholder">
                  <i :class="Icon.IMAGE" />
                </div>
              </template>
            </Column>
            <Column field="title" header="Title" />
            <Column field="productionYear" header="Year" style="width: 6rem" />
          </DataTable>
        </div>

        <div v-else-if="searchQuery" class="no-results">
          No results found for "{{ searchQuery }}"
        </div>
      </div>

      <template #footer>
        <Button label="Cancel" :icon="Icon.TIMES" outlined @click="closeResolveDialog" />
        <Button
          label="Confirm"
          :icon="Icon.CHECK"
          :disabled="!selectedMediaItem"
          :loading="resolveMutation.isPending.value"
          @click="confirmResolve"
        />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
  .broken-view {
    padding-bottom: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .broken-header {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .broken-title {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .broken-icon {
    font-size: 1.25rem;
    color: var(--p-text-muted-color);
  }

  .broken-subtitle {
    margin: 0;
    color: var(--p-text-muted-color);
  }

  .tab-bar {
    display: flex;
    gap: 0.5rem;
  }

  .broken-table-container {
    flex: 1;
    overflow: auto;
  }

  .resolve-dialog-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .resolve-hint {
    margin: 0;
    color: var(--p-text-muted-color);
    font-size: 0.9rem;
  }

  .search-input {
    width: 100%;
  }

  .search-results {
    max-height: 320px;
    overflow-y: auto;
  }

  .no-results {
    color: var(--p-text-muted-color);
    font-style: italic;
    text-align: center;
    padding: 1rem;
  }

  .result-poster {
    width: 3rem;
    height: 4.5rem;
    object-fit: cover;
    border-radius: 4px;
    display: block;
  }

  .result-poster--placeholder {
    background: var(--p-surface-200);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--p-text-muted-color);
  }
</style>
