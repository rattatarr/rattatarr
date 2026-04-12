<script setup lang="ts">
  import { ref, watch } from 'vue'
  import AutoComplete from 'primevue/autocomplete'
  import { usePeople } from '@/queries/usePeople'
  import type { Pageable, Person } from '@/types'
  import { useDebounceFn } from '@vueuse/core'

  interface Props {
    modelValue: Person | null
    placeholder?: string
  }

  interface Emits {
    (e: 'update:modelValue', value: Person | null): void
  }

  const props = withDefaults(defineProps<Props>(), {
    placeholder: 'Filter by people...',
  })

  const emit = defineEmits<Emits>()

  const selectedPerson = ref<Person | null>(props.modelValue)
  const searchQuery = ref('')
  const suggestions = ref<Person[]>([])

  // Pageable for people (small page size for dropdown suggestions)
  const pageable = ref<Pageable>({
    page: 0,
    size: 20,
    sort: ['name,asc'],
  })

  // Filters for people search
  const peopleFilters = ref({ name: '' })

  // Fetch people based on current search query
  const { data: peopleData, isLoading } = usePeople(pageable, peopleFilters)

  // Update suggestions when data arrives
  watch(peopleData, (newData) => {
    suggestions.value = newData?.people ?? []
  })

  // Debounced search query update
  const debouncedSearch = useDebounceFn((value: string) => {
    peopleFilters.value = { name: value }
  }, 300)

  function onSearch(event: { query: string }) {
    searchQuery.value = event.query
    debouncedSearch(event.query)
  }

  function onSelect(event: { value: Person }) {
    emit('update:modelValue', event.value)
  }

  function onClear() {
    emit('update:modelValue', null)
  }

  // Sync with external changes (e.g. reset)
  watch(
    () => props.modelValue,
    (newValue) => {
      selectedPerson.value = newValue
    },
  )
</script>

<template>
  <div class="people-filter">
    <AutoComplete
      v-model="selectedPerson"
      :suggestions="suggestions"
      option-label="name"
      :placeholder="isLoading ? 'Loading...' : placeholder"
      :loading="isLoading"
      class="people-autocomplete"
      force-selection
      @complete="onSearch"
      @item-select="onSelect"
      @clear="onClear"
    >
      <template #option="{ option }">
        <div class="person-option">
          <img
            v-if="option.profilePathUrl"
            :src="option.profilePathUrl"
            :alt="option.name ?? ''"
            class="person-avatar"
          />
          <span v-else class="person-avatar person-avatar--placeholder">
            <i class="pi pi-user" />
          </span>
          <span class="person-name">{{ option.name }}</span>
        </div>
      </template>
      <template #empty>
        <span v-if="isLoading">Searching...</span>
        <span v-else-if="searchQuery">No people found</span>
        <span v-else>Type to search people</span>
      </template>
    </AutoComplete>
  </div>
</template>

<style scoped>
  .people-filter {
    width: 100%;
    max-width: 100%;
    min-width: 0;
  }

  .people-autocomplete {
    width: 100%;
    max-width: 100%;
  }

  .people-autocomplete :deep(.p-autocomplete-input) {
    width: 100%;
  }

  /* Dropdown item */
  .person-option {
    display: flex;
    align-items: center;
    gap: 0.6rem;
  }

  .person-avatar {
    width: 2rem;
    height: 2rem;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }

  .person-avatar--placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--surface-border);
    color: var(--text-color-secondary);
    font-size: 0.85rem;
  }

  .person-name {
    font-size: 0.9rem;
    color: var(--text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>
