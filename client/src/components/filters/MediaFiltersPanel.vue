<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { watchDebounced } from '@vueuse/core'
  import Card from 'primevue/card'
  import Select from 'primevue/select'
  import InputNumber from 'primevue/inputnumber'
  import SelectButton from 'primevue/selectbutton'
  import Button from 'primevue/button'
  import { SearchInput, GenresFilter, PeopleFilter } from '@/components/filters'
  import { useMediaFiltersContext } from '@/composables/useMediaFilters'
  import { Icon } from '@/utils/enums'
  import type { SortField, SortOrder, SortEntry } from '@/composables/useMediaFilters'
  import type { Person } from '@/types'

  interface Props {
    searchPlaceholder?: string
  }

  withDefaults(defineProps<Props>(), {
    searchPlaceholder: 'Search...',
  })

  const gridMode = defineModel<'default' | 'compact'>('gridMode', { default: 'default' })

  const gridModeOptions = [
    { label: 'Default', value: 'default' },
    { label: 'Compact', value: 'compact' },
  ]

  const filters = useMediaFiltersContext()

  const currentYear = new Date().getFullYear()

  // Draft filter state (only applied on button click, except search which is auto-applied)
  const draftTitleSearch = ref(filters.titleSearch.value)
  const draftSelectedGenres = ref<string[]>([...filters.selectedGenres.value])
  const draftSelectedPerson = ref<Person | null>(filters.selectedPerson.value)
  const draftReleasedAfter = ref(filters.releasedAfter.value)
  const draftReleasedBeforeInternal = ref(filters.releasedBefore.value)
  const draftUnrated = ref<boolean | null>(filters.unrated.value || null)
  const draftUnratedModel = computed({
    get: () => draftUnrated.value,
    set: (value: boolean | null) => {
      draftUnrated.value = value
    },
  })
  const draftSorts = ref<SortEntry[]>(filters.sorts.value.map((s) => ({ ...s })))
  // Typed computed used in template v-for so IDEs can infer entry/index types
  const draftSortsList = computed<SortEntry[]>(() => draftSorts.value)

  // Smart "To" year: when incrementing/decrementing from empty, jump to current year +/- 1
  const draftReleasedBefore = computed({
    get: () => draftReleasedBeforeInternal.value,
    set: (value) => {
      // If changing from undefined to a number, detect +/- button click
      if (draftReleasedBeforeInternal.value === undefined && value !== undefined) {
        if (value === 0 || value === 1) {
          // User clicked + (defaults to 0 or 1) → jump to currentYear + 1
          draftReleasedBeforeInternal.value = currentYear + 1
        } else if (value < 0) {
          // User clicked - (goes negative) → jump to currentYear - 1
          draftReleasedBeforeInternal.value = currentYear - 1
        } else {
          // User typed a value, use it as-is
          draftReleasedBeforeInternal.value = value
        }
      } else {
        // Normal update
        draftReleasedBeforeInternal.value = value
      }
    },
  })

  // Auto-apply search with debounce
  watchDebounced(
    draftTitleSearch,
    (newValue) => {
      filters.titleSearch.value = newValue
    },
    { debounce: 500 },
  )

  // Watch for external changes to filters (e.g., from reset)
  watch(
    () => [
      filters.titleSearch.value,
      filters.selectedGenres.value,
      filters.selectedPerson.value,
      filters.releasedAfter.value,
      filters.releasedBefore.value,
      filters.unrated.value,
      filters.sorts.value,
    ],
    () => {
      draftTitleSearch.value = filters.titleSearch.value
      draftSelectedGenres.value = [...filters.selectedGenres.value]
      draftSelectedPerson.value = filters.selectedPerson.value
      draftReleasedAfter.value = filters.releasedAfter.value
      draftReleasedBeforeInternal.value = filters.releasedBefore.value
      draftUnrated.value = filters.unrated.value || null
      draftSorts.value = filters.sorts.value.map((s) => ({ ...s }))
    },
  )

  function applyFilters() {
    // Note: titleSearch is auto-applied via debounced watcher
    filters.selectedGenres.value = [...draftSelectedGenres.value]
    filters.selectedPerson.value = draftSelectedPerson.value
    filters.releasedAfter.value = draftReleasedAfter.value
    filters.releasedBefore.value = draftReleasedBefore.value
    filters.unrated.value = draftUnrated.value ?? false
    filters.sorts.value = draftSorts.value.map((s) => ({ ...s }))
  }

  function resetFilters() {
    filters.reset()
    // Draft will sync via watcher
  }

  // Sort options
  const sortFieldOptions: { label: string; value: SortField }[] = [
    { label: 'Rating', value: 'ratings' },
    { label: 'Title', value: 'title' },
    { label: 'Year', value: 'productionYear' },
  ]

  const sortOrderOptions: { label: string; value: SortOrder }[] = [
    { label: 'Desc', value: 'desc' },
    { label: 'Asc', value: 'asc' },
  ]

  const MAX_SORTS = 3

  const unratedOptions = [
    { label: 'All', value: null },
    { label: 'Rated', value: false },
    { label: 'Unrated', value: true },
  ]

  type FieldOption = { label: string; value: SortField; disabled: boolean }

  // Fields already used in other sort rows (for disabling duplicates)
  function usedFieldsExcept(index: number): SortField[] {
    return draftSorts.value.filter((_, i) => i !== index).map((s) => s.field)
  }

  function fieldOptionsFor(index: number): FieldOption[] {
    const used = usedFieldsExcept(index)
    return sortFieldOptions.map((opt) => ({
      ...opt,
      disabled: used.includes(opt.value),
    }))
  }

  function isOptionDisabled(opt: FieldOption): boolean {
    return opt.disabled
  }

  function addSort() {
    if (draftSorts.value.length >= MAX_SORTS) return
    const usedFields = draftSorts.value.map((s) => s.field)
    const nextField = sortFieldOptions.find((o) => !usedFields.includes(o.value))
    if (!nextField) return
    draftSorts.value = [...draftSorts.value, { field: nextField.value, order: 'desc' }]
  }

  function removeSort(index: number) {
    if (draftSorts.value.length <= 1) return
    draftSorts.value = draftSorts.value.filter((_, i) => i !== index)
  }

  function moveSortUp(index: number) {
    if (index === 0) return
    const next = draftSorts.value.map((s) => ({ ...s }))
    const tmp = next[index - 1]!
    next[index - 1] = next[index]!
    next[index] = tmp
    draftSorts.value = next
  }

  function moveSortDown(index: number) {
    if (index >= draftSorts.value.length - 1) return
    const next = draftSorts.value.map((s) => ({ ...s }))
    const tmp = next[index + 1]!
    next[index + 1] = next[index]!
    next[index] = tmp
    draftSorts.value = next
  }
</script>

<template>
  <Card class="filters-panel">
    <template #content>
      <div class="filters-container">
        <!-- Row 1: Search, Genres, and People -->
        <div class="filter-row row-1">
          <div class="filter-group">
            <label class="filter-label">Search</label>
            <SearchInput v-model="draftTitleSearch" :placeholder="searchPlaceholder" />
          </div>

          <div class="filter-group">
            <label class="filter-label">Genres</label>
            <GenresFilter v-model="draftSelectedGenres" placeholder="Filter by genres..." />
          </div>

          <div class="filter-group">
            <label class="filter-label">People</label>
            <PeopleFilter v-model="draftSelectedPerson" placeholder="Filter by people..." />
          </div>
        </div>

        <!-- Row 2: Year Range, Sort, Unrated, Actions -->
        <div class="filter-row row-2">
          <div class="filter-group">
            <label class="filter-label">Release Year</label>
            <div class="year-range">
              <InputNumber
                v-model="draftReleasedAfter"
                placeholder="From"
                :min="1900"
                :use-grouping="false"
                show-buttons
                button-layout="horizontal"
                :step="1"
                class="year-input"
              />
              <span class="year-separator">to</span>
              <InputNumber
                v-model="draftReleasedBefore"
                placeholder="To"
                :use-grouping="false"
                show-buttons
                button-layout="horizontal"
                :step="1"
                class="year-input"
              />
            </div>
          </div>

          <div class="filter-group sort-group">
            <label class="filter-label">Sort By</label>
            <div class="sort-list">
              <div
                v-for="(entry, index) in draftSortsList"
                :key="index"
                class="sort-row"
                :data-testid="`sort-row-${index}`"
              >
                <!-- Reorder buttons -->
                <div class="sort-reorder">
                  <button
                    class="reorder-btn"
                    :disabled="index === 0"
                    :aria-label="`Move sort ${index + 1} up`"
                    @click="moveSortUp(index)"
                  >
                    <i :class="Icon.CHEVRON_UP" />
                  </button>
                  <button
                    class="reorder-btn"
                    :disabled="index >= draftSorts.length - 1"
                    :aria-label="`Move sort ${index + 1} down`"
                    @click="moveSortDown(index)"
                  >
                    <i :class="Icon.CHEVRON_DOWN" />
                  </button>
                </div>

                <!-- Field selector -->
                <Select
                  v-model="entry.field"
                  :options="fieldOptionsFor(index)"
                  option-label="label"
                  option-value="value"
                  :option-disabled="isOptionDisabled"
                  class="sort-field-select"
                  :aria-label="`Sort field ${index + 1}`"
                />

                <!-- Order selector -->
                <Select
                  v-model="entry.order"
                  :options="sortOrderOptions"
                  option-label="label"
                  option-value="value"
                  class="sort-order-select"
                  :aria-label="`Sort order ${index + 1}`"
                />

                <!-- Remove button (disabled when only 1 entry) -->
                <button
                  class="sort-remove-btn"
                  :disabled="draftSorts.length <= 1"
                  :aria-label="`Remove sort ${index + 1}`"
                  @click="removeSort(index)"
                >
                  <i :class="Icon.TIMES" />
                </button>
              </div>

              <!-- Add sort criteria -->
              <button v-if="draftSorts.length < MAX_SORTS" class="add-sort-btn" @click="addSort">
                <i :class="Icon.PLUS" />
                Add sort criteria
              </button>
            </div>
          </div>

          <div class="filter-group unrated-group">
            <label class="filter-label">Ratings</label>
            <SelectButton
              v-model="draftUnratedModel"
              :options="unratedOptions"
              option-label="label"
              option-value="value"
              :allow-empty="false"
              class="unrated-select"
            />
          </div>

          <div class="filter-group grid-mode-group">
            <label class="filter-label">View</label>
            <SelectButton
              v-model="gridMode"
              :options="gridModeOptions"
              option-label="label"
              option-value="value"
              :allow-empty="false"
              class="grid-mode-select"
            />
          </div>

          <div class="filter-group filter-actions">
            <label class="filter-label" style="visibility: hidden">Actions</label>
            <div class="action-buttons">
              <Button label="Apply" :icon="Icon.CHECK" severity="primary" @click="applyFilters" />
              <Button
                label="Reset"
                :icon="Icon.UNDO"
                severity="secondary"
                outlined
                @click="resetFilters"
              />
            </div>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
  .filters-panel {
    background: var(--surface-card);
    width: 100%;
    max-width: 100%;
    overflow: hidden;
  }

  .filters-container {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    width: 100%;
    max-width: 100%;
  }

  .filter-row {
    display: grid;
    gap: 1.5rem;
    align-items: end;
    width: 100%;
    max-width: 100%;
  }

  /* Row 1: Search, Genres, and People (3 equal columns) */
  .row-1 {
    grid-template-columns: 1fr 1fr 1fr;
  }

  /* Row 2: Year, Sort list, Unrated, View, Actions */
  .row-2 {
    grid-template-columns: minmax(280px, 1.5fr) minmax(320px, 2fr) minmax(210px, auto) auto minmax(
        180px,
        1fr
      );
    align-items: start;
  }

  .filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    min-width: 0;
    max-width: 100%;
  }

  .filter-label {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-color);
  }

  /* Year Range */
  .year-range {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    width: 100%;
    max-width: 100%;
  }

  .year-input {
    flex: 1 1 0;
    min-width: 0;
    max-width: calc(50% - 1.5rem);
  }

  .year-input :deep(.p-inputnumber) {
    width: 100%;
  }

  .year-input :deep(.p-inputnumber-input) {
    width: 100%;
    min-width: 0;
  }

  .year-separator {
    color: var(--text-color-secondary);
    font-size: 0.9rem;
    flex-shrink: 0;
    white-space: nowrap;
  }

  /* Sort list */
  .sort-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .sort-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .sort-reorder {
    display: flex;
    flex-direction: column;
    gap: 1px;
    flex-shrink: 0;
  }

  .reorder-btn,
  .sort-remove-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: 1px solid var(--surface-border);
    border-radius: var(--border-radius);
    color: var(--text-color-secondary);
    cursor: pointer;
    padding: 0.2rem 0.35rem;
    line-height: 1;
    transition:
      color 0.2s,
      border-color 0.2s;
  }

  .reorder-btn {
    font-size: 0.65rem;
    padding: 0.15rem 0.3rem;
  }

  .reorder-btn:disabled,
  .sort-remove-btn:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  .reorder-btn:not(:disabled):hover,
  .sort-remove-btn:not(:disabled):hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
  }

  .sort-remove-btn {
    font-size: 0.75rem;
    padding: 0.3rem 0.45rem;
    flex-shrink: 0;
  }

  .sort-field-select {
    flex: 1 1 0;
    min-width: 0;
  }

  .sort-order-select {
    flex: 0 0 5.5rem;
    width: 5.5rem;
  }

  .add-sort-btn {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    background: transparent;
    border: 1px dashed var(--surface-border);
    border-radius: var(--border-radius);
    color: var(--text-color-secondary);
    cursor: pointer;
    font-size: 0.85rem;
    padding: 0.4rem 0.75rem;
    transition:
      color 0.2s,
      border-color 0.2s;
    width: 100%;
  }

  .add-sort-btn:hover {
    color: var(--primary-color);
    border-color: var(--primary-color);
  }

  /* Unrated select button */
  .unrated-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .unrated-select :deep(.p-togglebutton-content) {
    padding: 0.4rem 0.75rem;
    justify-content: center;
  }

  /* Grid mode */
  .grid-mode-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .grid-mode-select :deep(.p-togglebutton-content) {
    padding: 0.4rem 0.75rem;
    justify-content: center;
  }

  /* Actions */
  .filter-actions {
    display: flex;
    flex-direction: column;
  }

  .action-buttons {
    display: flex;
    gap: 0.5rem;
  }

  /* Responsive */
  @media (max-width: 1400px) {
    .row-1 {
      grid-template-columns: 1fr 1fr;
    }

    .row-2 {
      grid-template-columns: minmax(280px, 1.5fr) minmax(280px, 2fr) minmax(140px, auto) auto;
    }

    .filter-actions {
      grid-column: 1 / -1;
    }

    .action-buttons {
      justify-content: flex-end;
    }
  }

  @media (max-width: 1024px) {
    .row-1 {
      grid-template-columns: 1fr 1fr;
    }

    .row-2 {
      grid-template-columns: 1fr 1fr;
    }

    .unrated-group,
    .grid-mode-group {
      grid-column: 1 / -1;
    }

    .filter-actions {
      grid-column: 1 / -1;
    }
  }

  @media (max-width: 768px) {
    .filters-panel {
      margin: 0;
    }

    .filters-container {
      gap: 1rem;
    }

    .filter-row {
      gap: 1rem;
    }

    .row-1,
    .row-2 {
      grid-template-columns: 1fr;
    }

    .year-range {
      gap: 0.5rem;
    }

    /* On mobile, give each year input equal space with enough room for text */
    .year-input {
      flex: 1 1 0;
      min-width: 0;
      max-width: calc(50% - 1rem);
    }

    .year-separator {
      font-size: 0.85rem;
      padding: 0 0.25rem;
    }

    .action-buttons {
      flex-direction: column;
      gap: 0.5rem;
    }

    .action-buttons button {
      width: 100%;
    }

    .unrated-select :deep(.p-selectbutton),
    .grid-mode-select :deep(.p-selectbutton) {
      display: flex;
      width: 100%;
    }

    .unrated-select :deep(.p-togglebutton),
    .grid-mode-select :deep(.p-togglebutton) {
      flex: 1;
    }
  }

  /* Stack year inputs vertically on small phones */
  @media (max-width: 480px) {
    .year-range {
      flex-direction: column;
      align-items: stretch;
      gap: 0.5rem;
    }

    .year-input {
      max-width: 100%;
    }

    .year-separator {
      text-align: center;
      padding: 0.25rem 0;
      font-size: 0.8rem;
    }
  }
</style>
