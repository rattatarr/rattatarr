<script setup lang="ts">
  import { computed, watch, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import { useDebounceFn, useMediaQuery, useSessionStorage } from '@vueuse/core'
  import Message from 'primevue/message'
  import Accordion from 'primevue/accordion'
  import AccordionPanel from 'primevue/accordionpanel'
  import AccordionHeader from 'primevue/accordionheader'
  import AccordionContent from 'primevue/accordioncontent'
  import SearchColumn from '@/components/search/SearchColumn.vue'
  import { useSearch, useSearchById } from '@/queries/useSearch'
  import { useProfileStore } from '@/stores/profileStore'

  const route = useRoute()
  const profileStore = useProfileStore()

  const isMobile = useMediaQuery('(max-width: 768px)')

  const activeAccordionPanels = useSessionStorage<string[]>('search-active-panels', ['0', '1'])

  // Get search query from URL parameter
  const searchQuery = computed(() => (route.query.q as string) || '')

  // Debounced query for API calls
  const debouncedQuery = ref<string>('')

  // Debounce function (300ms)
  const updateDebouncedQuery = useDebounceFn((query: string) => {
    debouncedQuery.value = query
  }, 300)

  // Watch searchQuery and update debounced version
  watch(
    searchQuery,
    (newQuery) => {
      updateDebouncedQuery(newQuery)
    },
    { immediate: true },
  )

  // Detect if query is numeric (TMDB ID)
  const isNumericQuery = computed(() => /^\d+$/.test(debouncedQuery.value.trim()))

  // Use appropriate search hook based on query type (using debounced query)
  const textSearchResults = useSearch(debouncedQuery, () => profileStore.selectedProfileId)
  const idSearchResults = useSearchById(debouncedQuery)

  // Select results based on query type
  const results = computed(() => {
    return isNumericQuery.value ? idSearchResults.value : textSearchResults.value
  })
</script>

<template>
  <div class="search-view">
    <!-- No Query State -->
    <div v-if="!searchQuery" class="search-view__no-query">
      <Message severity="secondary" :closable="false">
        Enter a search query to find movies and series
      </Message>
    </div>

    <!-- Results Grid -->
    <div v-else class="search-view__results">
      <!-- Mobile: Collapsible Accordion -->
      <template v-if="isMobile">
        <Accordion v-model:value="activeAccordionPanels" multiple class="search-view__accordion">
          <!-- Movies Panel -->
          <AccordionPanel value="0">
            <AccordionHeader>Movies</AccordionHeader>
            <AccordionContent>
              <SearchColumn
                title="Movies"
                :results="results.movies"
                :is-loading="results.isLoading"
                :has-error="results.hasError"
              />
            </AccordionContent>
          </AccordionPanel>

          <!-- Series Panel -->
          <AccordionPanel value="1">
            <AccordionHeader>Series</AccordionHeader>
            <AccordionContent>
              <SearchColumn
                title="Series"
                :results="results.series"
                :is-loading="results.isLoading"
                :has-error="results.hasError"
              />
            </AccordionContent>
          </AccordionPanel>
        </Accordion>
      </template>

      <!-- Desktop: Side-by-side Columns -->
      <template v-else>
        <SearchColumn
          title="Movies"
          :results="results.movies"
          :is-loading="results.isLoading"
          :has-error="results.hasError"
        />

        <!-- Divider -->
        <div class="search-view__divider" />

        <SearchColumn
          title="Series"
          :results="results.series"
          :is-loading="results.isLoading"
          :has-error="results.hasError"
        />
      </template>
    </div>
  </div>
</template>

<style scoped>
  .search-view {
    height: calc(100vh - 140px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .search-view__no-query {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }

  .search-view__results {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    gap: 0;
    height: 100%;
    overflow: hidden;
    padding: 1rem 2rem;
  }

  .search-view__results > *:nth-child(1) {
    height: 100%;
    overflow-y: auto;
    padding-right: 1.5rem;
  }

  .search-view__results > *:nth-child(2) {
    width: 1px;
    height: 100%;
    background: var(--surface-border);
    margin: 0 1.5rem;
    flex-shrink: 0;
  }

  .search-view__results > *:nth-child(3) {
    height: 100%;
    overflow-y: auto;
    padding-left: 0;
    padding-right: 0.5rem;
  }

  /* Custom scrollbar styling */
  .search-view__results > *::-webkit-scrollbar {
    width: 8px;
  }

  .search-view__results > *::-webkit-scrollbar-track {
    background: var(--surface-100);
    border-radius: 4px;
  }

  .search-view__results > *::-webkit-scrollbar-thumb {
    background: var(--surface-400);
    border-radius: 4px;
  }

  .search-view__results > *::-webkit-scrollbar-thumb:hover {
    background: var(--surface-500);
  }

  /* Mobile: Stack columns vertically */
  @media (max-width: 768px) {
    .search-view {
      height: auto;
      min-height: calc(100vh - 140px);
    }

    .search-view__results {
      grid-template-columns: 1fr;
      gap: 1.5rem;
      height: auto;
      overflow: visible;
      padding: 1rem;
      display: flex;
      flex-direction: column;
    }

    .search-view__results > *:nth-child(1),
    .search-view__results > *:nth-child(3) {
      height: auto !important;
      overflow-y: visible !important;
      padding: 0 !important;
    }

    .search-view__divider {
      display: none;
    }

    /* Accordion styling for mobile */
    .search-view__accordion {
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    /* Hide SearchColumn title when inside Accordion (AccordionHeader shows it) */
    .search-view__accordion :deep(.search-column__title) {
      display: none;
    }

    .search-view__accordion :deep(.search-column) {
      gap: 0.5rem;
    }

    .search-view__accordion :deep(.p-accordioncontent-content) {
      padding: 1rem;
    }

    .search-view__accordion :deep(.search-column__grid) {
      padding: 0;
    }
  }
</style>
