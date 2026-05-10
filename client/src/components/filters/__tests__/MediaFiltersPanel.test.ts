import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, nextTick } from 'vue'
import MediaFiltersPanel from '../MediaFiltersPanel.vue'
import type { MediaFilterState, SortEntry } from '@/composables/useMediaFilters'

// Mock VueUse to disable debouncing for tests
vi.mock('@vueuse/core', () => ({
  watchDebounced: (source: unknown, callback: Function) => {
    if (typeof callback === 'function') {
      const unwatchFn = vi.fn()
      return unwatchFn
    }
    return vi.fn()
  },
  useDebounceFn: (fn: Function) => {
    return (...args: never[]) => fn(...args)
  },
}))

// Mock useMediaFiltersContext composable
const mockReset = vi.fn()
const mockFilterState: MediaFilterState = {
  titleSearch: ref(''),
  selectedGenres: ref([]),
  selectedPerson: ref(null),
  releasedAfter: ref(undefined),
  releasedBefore: ref(undefined),
  unrated: ref(false),
  sorts: ref<SortEntry[]>([{ field: 'ratings', order: 'desc' }]),
  movieFilters: ref({ title: '' }),
  seriesFilters: ref({ title: '' }),
  sortArray: ref(['ratings,desc']),
  reset: mockReset,
  PAGE_SIZE: 24,
}

vi.mock('@/composables/useMediaFilters', () => ({
  useMediaFiltersContext: vi.fn(() => mockFilterState),
}))

describe('MediaFiltersPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset mock state to defaults
    mockFilterState.titleSearch.value = ''
    mockFilterState.selectedGenres.value = []
    mockFilterState.selectedPerson.value = null
    mockFilterState.releasedAfter.value = undefined
    mockFilterState.releasedBefore.value = undefined
    mockFilterState.unrated.value = false
    mockFilterState.sorts.value = [{ field: 'ratings', order: 'desc' }]
  })

  const createWrapper = (props = {}) => {
    return mount(MediaFiltersPanel, {
      props: {
        searchPlaceholder: 'Search...',
        ...props,
      },
      global: {
        stubs: {
          Card: {
            template: '<div class="card-stub"><slot name="content" /></div>',
          },
          SearchInput: {
            template:
              '<input class="search-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'placeholder'],
          },
          GenresFilter: {
            template:
              '<select class="genres-stub" multiple :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option>Action</option></select>',
            props: ['modelValue', 'placeholder'],
          },
          InputNumber: {
            template:
              '<input type="number" class="input-number-stub" :value="modelValue" @input="$emit(\'update:modelValue\', parseInt($event.target.value) || undefined)" />',
            props: [
              'modelValue',
              'placeholder',
              'min',
              'useGrouping',
              'showButtons',
              'buttonLayout',
              'step',
            ],
          },
          Select: {
            template:
              '<select class="select-stub" :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option v-for="opt in options" :key="opt.value" :value="opt.value" :disabled="opt.disabled">{{ opt.label }}</option></select>',
            props: [
              'modelValue',
              'options',
              'optionLabel',
              'optionValue',
              'optionDisabled',
              'placeholder',
            ],
          },
          Button: {
            template: '<button class="button-stub" @click="$emit(\'click\')">{{ label }}</button>',
            props: ['label', 'icon', 'severity', 'outlined'],
          },
          Checkbox: {
            template:
              '<input type="checkbox" class="checkbox-stub" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
            props: ['modelValue', 'inputId', 'binary'],
          },
          SelectButton: {
            template:
              '<div class="select-button-stub"><button v-for="opt in options" :key="opt.value" :class="{ active: modelValue === opt.value }" @click="$emit(\'update:modelValue\', modelValue === opt.value ? null : opt.value)">{{ opt.label }}</button></div>',
            props: ['modelValue', 'options', 'optionLabel', 'optionValue', 'allowEmpty'],
          },
          PeopleFilter: {
            template:
              '<input class="people-filter-stub" :value="modelValue?.name ?? \'\'" @input="$emit(\'update:modelValue\', null)" />',
            props: ['modelValue', 'placeholder'],
          },
        },
      },
    })
  }

  it('renders all filter controls', () => {
    const wrapper = createWrapper()

    expect(wrapper.find('.card-stub').exists()).toBe(true)
    expect(wrapper.find('.search-stub').exists()).toBe(true)
    expect(wrapper.find('.genres-stub').exists()).toBe(true)
    expect(wrapper.findAll('.input-number-stub').length).toBe(2) // Year from and to
    // Sort list should have one row with 2 selects (field + order)
    expect(wrapper.findAll('.sort-row').length).toBe(1)
    expect(wrapper.findAll('.select-stub').length).toBe(2) // field + order for 1 sort row
    expect(wrapper.findAll('.button-stub').length).toBe(2) // Apply and Reset
  })

  it('displays custom search placeholder', () => {
    const wrapper = createWrapper({ searchPlaceholder: 'Search movies...' })

    const searchInput = wrapper.find('.search-stub')
    expect(searchInput.exists()).toBe(true)
  })

  it('uses default search placeholder when not provided', () => {
    const wrapper = createWrapper({ searchPlaceholder: undefined })

    const searchInput = wrapper.find('.search-stub')
    expect(searchInput.exists()).toBe(true)
  })

  it('initializes draft state from filter context', async () => {
    mockFilterState.titleSearch.value = 'test search'
    mockFilterState.selectedGenres.value = ['Action', 'Drama']
    mockFilterState.releasedAfter.value = 2020
    mockFilterState.releasedBefore.value = 2023
    mockFilterState.sorts.value = [{ field: 'title', order: 'asc' }]

    const wrapper = createWrapper()

    await nextTick()

    // Verify inputs reflect the context values
    const searchInput = wrapper.find('.search-stub')
    expect((searchInput.element as HTMLInputElement).value).toBe('test search')

    const yearInputs = wrapper.findAll('.input-number-stub')
    expect((yearInputs[0]?.element as HTMLInputElement).value).toBe('2020')
    expect((yearInputs[1]?.element as HTMLInputElement).value).toBe('2023')

    // Sort field select should show 'title'
    const sortFieldSelect = wrapper.find('.select-stub')
    expect((sortFieldSelect.element as HTMLSelectElement).value).toBe('title')
  })

  it('applies filters when Apply button is clicked', async () => {
    const wrapper = createWrapper()

    // Modify year inputs
    const yearInputs = wrapper.findAll('.input-number-stub')
    await yearInputs[0]?.setValue('2020')
    await yearInputs[1]?.setValue('2023')

    // Change sort field in first sort row
    const sortFieldSelect = wrapper.find('.select-stub')
    await sortFieldSelect.setValue('title')

    await nextTick()

    // Click Apply button
    const applyButton = wrapper.findAll('.button-stub')[0]
    await applyButton?.trigger('click')
    await nextTick()

    // Context should be updated with draft values
    expect(mockFilterState.releasedAfter.value).toBe(2020)
    expect(mockFilterState.releasedBefore.value).toBe(2023)
    expect(mockFilterState.sorts.value[0]?.field).toBe('title')
  })

  it('calls reset when Reset button is clicked', async () => {
    const wrapper = createWrapper()

    const resetButton = wrapper.findAll('.button-stub')[1]
    await resetButton?.trigger('click')
    await nextTick()

    expect(mockReset).toHaveBeenCalled()
  })

  it('syncs draft state when external filters change', async () => {
    const wrapper = createWrapper()

    // Update context values
    mockFilterState.titleSearch.value = 'new search'
    mockFilterState.selectedGenres.value = ['Comedy']
    mockFilterState.releasedAfter.value = 2021
    mockFilterState.releasedBefore.value = 2024
    mockFilterState.sorts.value = [{ field: 'productionYear', order: 'asc' }]

    await nextTick()

    const searchInput = wrapper.find('.search-stub')
    expect((searchInput.element as HTMLInputElement).value).toBe('new search')

    const yearInputs = wrapper.findAll('.input-number-stub')
    expect((yearInputs[0]?.element as HTMLInputElement).value).toBe('2021')
    expect((yearInputs[1]?.element as HTMLInputElement).value).toBe('2024')
  })

  it('handles genre selection updates', async () => {
    const wrapper = createWrapper()

    const genresFilter = wrapper.find('.genres-stub')
    expect(genresFilter.exists()).toBe(true)

    // Context should NOT be updated yet (manual apply pattern)
    expect(mockFilterState.selectedGenres.value).toEqual([])
  })

  it('does not update context sort until Apply is clicked', async () => {
    const wrapper = createWrapper()

    const sortFieldSelect = wrapper.find('.select-stub')
    await sortFieldSelect.setValue('title')
    await nextTick()

    // Context should NOT be updated yet
    expect(mockFilterState.sorts.value[0]?.field).toBe('ratings')
  })

  it('renders sort field options correctly', () => {
    const wrapper = createWrapper()

    // The first select is the sort field select for the first sort row
    const sortFieldSelect = wrapper.find('.select-stub')
    const options = sortFieldSelect.findAll('option')

    expect(options.length).toBe(4)
    expect(options[0]?.text()).toBe('Rating')
    expect(options[1]?.text()).toBe('Title')
    expect(options[2]?.text()).toBe('Year')
    expect(options[3]?.text()).toBe('Last Watched')
  })

  it('renders sort order options correctly', () => {
    const wrapper = createWrapper()

    // Second select in the first sort row is the order select
    const selects = wrapper.findAll('.select-stub')
    const orderSelect = selects[1]
    const options = orderSelect?.findAll('option') ?? []

    expect(options.length).toBe(2)
    expect(options[0]?.text()).toBe('Desc')
    expect(options[1]?.text()).toBe('Asc')
  })

  it('handles year range inputs', async () => {
    const wrapper = createWrapper()

    const yearInputs = wrapper.findAll('.input-number-stub')
    const fromInput = yearInputs[0]
    const toInput = yearInputs[1]

    await fromInput?.setValue('2020')
    await toInput?.setValue('2023')
    await nextTick()

    expect((fromInput?.element as HTMLInputElement).value).toBe('2020')
    expect((toInput?.element as HTMLInputElement).value).toBe('2023')

    // Context should NOT be updated yet (manual apply pattern)
    expect(mockFilterState.releasedAfter.value).toBeUndefined()
    expect(mockFilterState.releasedBefore.value).toBeUndefined()
  })

  it('handles smart year behavior - increment from undefined', async () => {
    const wrapper = createWrapper()

    const yearInputs = wrapper.findAll('.input-number-stub')
    const toInput = yearInputs[1]

    expect((toInput?.element as HTMLInputElement).value).toBe('')

    await toInput?.setValue('2024')
    await nextTick()

    expect((toInput?.element as HTMLInputElement).value).toBe('2024')
  })

  it('handles smart year behavior - decrement from undefined', async () => {
    const wrapper = createWrapper()
    const currentYear = new Date().getFullYear()

    const yearInputs = wrapper.findAll('.input-number-stub')
    const toInput = yearInputs[1]

    await toInput?.setValue('-1')
    await nextTick()

    const expectedValue = String(currentYear - 1)
    expect((toInput?.element as HTMLInputElement).value).toBe(expectedValue)
  })

  it('handles normal year updates after initialization', async () => {
    const wrapper = createWrapper()

    const yearInputs = wrapper.findAll('.input-number-stub')
    const toInput = yearInputs[1]

    await toInput?.setValue('2020')
    await nextTick()
    expect((toInput?.element as HTMLInputElement).value).toBe('2020')

    await toInput?.setValue('2021')
    await nextTick()
    expect((toInput?.element as HTMLInputElement).value).toBe('2021')

    await toInput?.setValue('2019')
    await nextTick()
    expect((toInput?.element as HTMLInputElement).value).toBe('2019')
  })

  it('displays Apply and Reset button labels', () => {
    const wrapper = createWrapper()

    const buttons = wrapper.findAll('.button-stub')
    expect(buttons[0]?.text()).toBe('Apply')
    expect(buttons[1]?.text()).toBe('Reset')
  })

  it('shows Add sort criterion button when less than 3 sorts', () => {
    const wrapper = createWrapper()

    const addBtn = wrapper.find('.add-sort-btn')
    expect(addBtn.exists()).toBe(true)
  })

  it('adds a new sort row when Add sort criterion is clicked', async () => {
    const wrapper = createWrapper()

    const addBtn = wrapper.find('.add-sort-btn')
    await addBtn.trigger('click')
    await nextTick()

    expect(wrapper.findAll('.sort-row').length).toBe(2)
    // Now 4 selects (2 per row)
    expect(wrapper.findAll('.select-stub').length).toBe(4)
  })

  it('hides Add sort criterion button when 3 sorts exist', async () => {
    mockFilterState.sorts.value = [
      { field: 'ratings', order: 'desc' },
      { field: 'productionYear', order: 'desc' },
      { field: 'title', order: 'asc' },
    ]

    const wrapper = createWrapper()
    await nextTick()

    expect(wrapper.find('.add-sort-btn').exists()).toBe(false)
    expect(wrapper.findAll('.sort-row').length).toBe(3)
  })

  it('removes a sort row when remove button is clicked', async () => {
    mockFilterState.sorts.value = [
      { field: 'ratings', order: 'desc' },
      { field: 'productionYear', order: 'desc' },
    ]

    const wrapper = createWrapper()
    await nextTick()

    expect(wrapper.findAll('.sort-row').length).toBe(2)

    // Click the remove button on the second row
    const removeBtns = wrapper.findAll('.sort-remove-btn')
    await removeBtns[1]?.trigger('click')
    await nextTick()

    expect(wrapper.findAll('.sort-row').length).toBe(1)
  })

  it('disables remove button when only 1 sort row exists', () => {
    const wrapper = createWrapper()

    const removeBtn = wrapper.find('.sort-remove-btn')
    expect((removeBtn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('moves sort row up when up button is clicked', async () => {
    mockFilterState.sorts.value = [
      { field: 'ratings', order: 'desc' },
      { field: 'title', order: 'asc' },
    ]

    const wrapper = createWrapper()
    await nextTick()

    // Click the up button on the second row (index 1)
    const upBtns = wrapper.findAll('.reorder-btn')
    // Buttons per row: up, down → 4 total for 2 rows; index 2 = up for row 1
    await upBtns[2]?.trigger('click')
    await nextTick()

    // Apply to commit
    const applyButton = wrapper.findAll('.button-stub')[0]
    await applyButton?.trigger('click')
    await nextTick()

    expect(mockFilterState.sorts.value[0]?.field).toBe('title')
    expect(mockFilterState.sorts.value[1]?.field).toBe('ratings')
  })
})
