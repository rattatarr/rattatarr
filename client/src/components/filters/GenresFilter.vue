<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import MultiSelect from 'primevue/multiselect'
  import { useGenres } from '@/queries/useLibrary'
  import type { Pageable, GenresFilters, Genre } from '@/types'
  import { useDebounceFn } from '@vueuse/core'

  interface Props {
    modelValue: string[]
    placeholder?: string
  }

  interface Emits {
    (e: 'update:modelValue', value: string[]): void
  }

  const props = withDefaults(defineProps<Props>(), {
    placeholder: 'Select genres...',
  })

  const emit = defineEmits<Emits>()

  const selectedGenres = ref<string[]>(props.modelValue)
  const searchQuery = ref('')

  // Pageable for genres (fetch all for dropdown)
  const pageable = ref<Pageable>({
    page: 0,
    size: 100,
    sort: ['name,asc'],
  })

  // Filters for genre search
  const genreFilters = computed<GenresFilters>(() => ({
    name: searchQuery.value,
  }))

  // Fetch genres
  const { data: genresData, isLoading } = useGenres(pageable, genreFilters)

  const filteredGenres = computed<Genre[]>(() => {
    const genres = genresData.value?.genres ?? []

    // Sort selected genres to the top for easy deselection
    return [...genres].sort((a, b) => {
      const aName = a.name ?? ''
      const bName = b.name ?? ''

      const aSelected = selectedGenres.value.includes(aName)
      const bSelected = selectedGenres.value.includes(bName)

      // Selected items first
      if (aSelected && !bSelected) return -1
      if (!aSelected && bSelected) return 1

      // Then alphabetically
      return aName.localeCompare(bName)
    })
  })

  // Debounced filter change
  const debouncedFilterChange = useDebounceFn((value: string) => {
    searchQuery.value = value
  }, 300)

  function onFilterChange(event: { value: string }) {
    debouncedFilterChange(event.value)
  }

  function onUpdate(value: string[]) {
    emit('update:modelValue', value)
  }

  // Sync with external changes
  watch(
    () => props.modelValue,
    (newValue) => {
      selectedGenres.value = newValue
    },
  )
</script>

<template>
  <div class="genres-filter">
    <MultiSelect
      v-model="selectedGenres"
      :options="filteredGenres"
      option-label="name"
      option-value="name"
      :placeholder="placeholder"
      :filter="true"
      :loading="isLoading"
      :max-selected-labels="3"
      selected-items-label="{0} genres selected"
      class="genres-select"
      @update:model-value="onUpdate"
      @filter="onFilterChange"
    >
      <template #empty>
        <span v-if="isLoading">Loading genres...</span>
        <span v-else>No genres found</span>
      </template>
    </MultiSelect>
  </div>
</template>

<style scoped>
  .genres-filter {
    width: 100%;
    max-width: 100%;
    min-width: 0;
  }

  .genres-select {
    width: 100%;
    max-width: 100%;
  }
</style>
