<script setup lang="ts">
  import { ref } from 'vue'
  import { useMovies } from '@/queries'
  import { useQueryError } from '@/composables'
  import type { Pageable, MovieFilters } from '@/types'

  // Example query setup
  const pageable = ref<Pageable>({ page: 0, size: 20 })
  const filters = ref<MovieFilters>({ title: 'test' })

  const movies = useMovies(pageable, filters)

  // Option 1: Automatic toast on error
  useQueryError(movies.isError, movies.error, {
    message: 'Failed to load movies',
  })

  // Option 2: Custom error handling
  function handleCustomError() {
    const moviesWithCustomHandler = useMovies(pageable, filters)

    useQueryError(moviesWithCustomHandler.isError, moviesWithCustomHandler.error, {
      message: 'Failed to load movies',
      onError: (error) => {
        console.log('Custom error handling:', error)
        // You can do additional logging, analytics, etc.
      },
    })
  }

  // Option 3: No toast, just custom handling
  function handleNoToast() {
    const moviesNoToast = useMovies(pageable, filters)

    useQueryError(moviesNoToast.isError, moviesNoToast.error, {
      showToast: false,
      onError: (error) => {
        // Handle error without showing toast
        console.error('Silent error:', error)
      },
    })
  }

  // Option 4: Manual error handling (without composable)
  function manualErrorHandling() {
    const { isError, error } = useMovies(pageable, filters)

    // You can also manually check in template or watchers
    if (isError.value && error.value) {
      console.log('Manual error handling')
    }
  }
</script>

<template>
  <div class="query-error-example">
    <h1>Query Error Handling Examples</h1>

    <section>
      <h2>Movies Query</h2>

      <!-- Loading state -->
      <div v-if="movies.isLoading.value">Loading movies...</div>

      <!-- Error state (handled by useQueryError composable) -->
      <div v-else-if="movies.isError.value" class="error-message">
        <p>Failed to load movies</p>
        <button @click="movies.refetch()">Retry</button>
      </div>

      <!-- Success state -->
      <div v-else-if="movies.data.value">
        <p>Loaded {{ movies.data.value.movies?.length ?? 0 }} movies</p>
      </div>
    </section>

    <section>
      <h2>Different Error Handling Patterns</h2>
      <button @click="handleCustomError">Custom Error Handler</button>
      <button @click="handleNoToast">No Toast (Silent)</button>
      <button @click="manualErrorHandling">Manual Handling</button>
    </section>
  </div>
</template>

<style scoped>
  .query-error-example {
    padding: 2rem;
    max-width: 800px;
    margin: 0 auto;
  }

  section {
    margin-bottom: 2rem;
    padding: 1rem;
    border: 1px solid var(--color-border);
    border-radius: 8px;
  }

  .error-message {
    color: #ef4444;
    padding: 1rem;
    background: #fef2f2;
    border-radius: 4px;
  }

  button {
    margin: 0.5rem 0.5rem 0.5rem 0;
    padding: 0.5rem 1rem;
    background: var(--color-background-soft);
    border: 1px solid var(--color-border);
    border-radius: 4px;
    cursor: pointer;
  }

  button:hover {
    background: var(--color-background-mute);
  }
</style>
