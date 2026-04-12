<script setup lang="ts">
  import Message from 'primevue/message'
  import ProgressSpinner from 'primevue/progressspinner'
  import SearchResultCard from './SearchResultCard.vue'
  import type { SearchResultItem } from '@/queries/useSearch'

  interface Props {
    title: string
    results: SearchResultItem[]
    isLoading: boolean
    hasError: boolean
  }

  defineProps<Props>()
</script>

<template>
  <div class="search-column">
    <h2 class="search-column__title">{{ title }}</h2>

    <!-- Loading State -->
    <div v-if="isLoading" class="search-column__loading">
      <ProgressSpinner style="width: 50px; height: 50px" strokeWidth="4" animationDuration="1s" />
    </div>

    <!-- Error State -->
    <div v-else-if="hasError" class="search-column__error">
      <Message severity="error" :closable="false">
        Failed to load {{ title.toLowerCase() }}
      </Message>
    </div>

    <!-- Empty State -->
    <div v-else-if="results.length === 0" class="search-column__empty">
      <Message severity="secondary" :closable="false"> No {{ title.toLowerCase() }} found </Message>
    </div>

    <!-- Results Grid -->
    <div v-else class="search-column__grid">
      <SearchResultCard
        v-for="result in results"
        :key="`${result.source}-${result.id}`"
        :result="result"
      />
    </div>
  </div>
</template>

<style scoped>
  .search-column {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    height: 100%;
  }

  .search-column__title {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
    padding: 0 0.5rem;
    color: var(--p-text-color);
  }

  .search-column__loading,
  .search-column__error,
  .search-column__empty {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 2rem 1rem;
  }

  .search-column__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 1.5rem;
    padding: 0 0.5rem 1rem;
  }

  @media (max-width: 768px) {
    .search-column__grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }
  }
</style>
