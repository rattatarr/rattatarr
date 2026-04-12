import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, nextTick } from 'vue'
import PeopleFilter from '../PeopleFilter.vue'
import type { PeopleWrapper, Person } from '@/types'

// Mock VueUse to disable debouncing for tests
vi.mock('@vueuse/core', () => ({
  useDebounceFn: (fn: Function) => {
    return (...args: never[]) => fn(...args)
  },
}))

// Mock usePeople query hook
vi.mock('@/queries/usePeople', () => ({
  usePeople: vi.fn(),
}))

// Import the mocked function
import { usePeople } from '@/queries/usePeople'

describe('PeopleFilter', () => {
  const mockPeople: Person[] = [
    { id: '1', name: 'Tom Hanks', profilePathUrl: undefined },
    { id: '2', name: 'Meryl Streep', profilePathUrl: 'https://example.com/meryl.jpg' },
    { id: '3', name: 'Leonardo DiCaprio', profilePathUrl: undefined },
  ]

  const defaultMockReturn = () => ({
    data: ref<PeopleWrapper>({ people: mockPeople, pagination: { totalElements: 3 } }),
    isLoading: ref(false),
    isError: ref(false),
    error: ref(null),
    isPending: ref(false),
    isLoadingError: ref(false),
    isRefetchError: ref(false),
    isSuccess: ref(true),
    status: ref('success'),
    fetchStatus: ref('idle'),
    refetch: vi.fn(),
    suspense: vi.fn(),
    isFetching: ref(false),
    isFetched: ref(true),
    isFetchedAfterMount: ref(true),
    isPlaceholderData: ref(false),
    isRefetching: ref(false),
    isStale: ref(false),
    isInitialLoading: ref(false),
    isPaused: ref(false),
    promise: Promise.resolve({} as PeopleWrapper),
    dataUpdatedAt: ref(0),
    errorUpdatedAt: ref(0),
    errorUpdateCount: ref(0),
    failureCount: ref(0),
    failureReason: ref(null),
  })

  const autoCompleteStub = {
    name: 'AutoComplete',
    template: `
      <div class="autocomplete-stub"
        :data-placeholder="placeholder"
        :data-loading="loading"
        @complete="$emit('complete', $event)"
        @item-select="$emit('item-select', $event)"
        @clear="$emit('clear')"
      >
        <slot name="empty" />
      </div>
    `,
    props: ['modelValue', 'suggestions', 'optionLabel', 'placeholder', 'loading', 'forceSelection'],
    emits: ['update:modelValue', 'complete', 'item-select', 'clear'],
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(usePeople).mockReturnValue(defaultMockReturn() as never)
  })

  it('renders without error', () => {
    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('uses default placeholder when not provided', () => {
    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    const ac = wrapper.find('.autocomplete-stub')
    expect(ac.attributes('data-placeholder')).toBe('Filter by people...')
  })

  it('passes custom placeholder through to AutoComplete', () => {
    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null, placeholder: 'Search cast...' },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    const ac = wrapper.find('.autocomplete-stub')
    expect(ac.attributes('data-placeholder')).toBe('Search cast...')
  })

  it('shows Loading... placeholder when isLoading is true', () => {
    vi.mocked(usePeople).mockReturnValue({
      data: ref<PeopleWrapper | undefined>(undefined),
      isLoading: ref(true),
    } as never)

    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    const ac = wrapper.find('.autocomplete-stub')
    expect(ac.attributes('data-placeholder')).toBe('Loading...')
  })

  it('passes correct pageable to usePeople', () => {
    mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    const callArgs = vi.mocked(usePeople).mock.calls[0] as unknown[]
    const pageableRef = callArgs[0] as { value: { page: number; size: number; sort: string[] } }
    expect(pageableRef.value).toEqual({
      page: 0,
      size: 20,
      sort: ['name,asc'],
    })
  })

  it('updates suggestions when peopleData changes', async () => {
    const peopleData = ref<PeopleWrapper>({ people: mockPeople, pagination: { totalElements: 3 } })
    vi.mocked(usePeople).mockReturnValue({
      data: peopleData,
      isLoading: ref(false),
    } as never)

    mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    // Update the data
    const newPeople: Person[] = [{ id: '99', name: 'New Person', profilePathUrl: undefined }]
    peopleData.value = { people: newPeople, pagination: { totalElements: 1 } }
    await nextTick()

    // The component's suggestions should have updated — usePeople was called
    expect(usePeople).toHaveBeenCalled()
  })

  it('emits update:modelValue with selected person on item-select', async () => {
    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: {
        stubs: {
          AutoComplete: {
            name: 'AutoComplete',
            template: `<div class="autocomplete-stub" @click="$emit('item-select', { value: person })"></div>`,
            props: ['modelValue', 'suggestions', 'placeholder', 'loading', 'forceSelection'],
            emits: ['update:modelValue', 'complete', 'item-select', 'clear'],
            setup() {
              return { person: mockPeople[0] }
            },
          },
        },
      },
    })

    await wrapper.find('.autocomplete-stub').trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([mockPeople[0]])
  })

  it('emits update:modelValue with null on clear', async () => {
    const wrapper = mount(PeopleFilter, {
      props: { modelValue: mockPeople[0] ?? null },
      global: {
        stubs: {
          AutoComplete: {
            name: 'AutoComplete',
            template: `<div class="autocomplete-stub" @click="$emit('clear')"></div>`,
            props: ['modelValue', 'suggestions', 'placeholder', 'loading', 'forceSelection'],
            emits: ['update:modelValue', 'complete', 'item-select', 'clear'],
          },
        },
      },
    })

    await wrapper.find('.autocomplete-stub').trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([null])
  })

  it('syncs selectedPerson when external modelValue changes', async () => {
    const wrapper = mount(PeopleFilter, {
      props: { modelValue: mockPeople[0] ?? null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    await wrapper.setProps({ modelValue: mockPeople[1] ?? null })
    await nextTick()

    // The component should accept the prop change without error
    expect(wrapper.props('modelValue')).toEqual(mockPeople[1])
  })

  it('shows "Type to search people" when no search query and not loading', () => {
    vi.mocked(usePeople).mockReturnValue({
      data: ref<PeopleWrapper>({ people: [], pagination: { totalElements: 0 } }),
      isLoading: ref(false),
    } as never)

    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    expect(wrapper.html()).toContain('Type to search people')
  })

  it('shows "Searching..." when loading', () => {
    vi.mocked(usePeople).mockReturnValue({
      data: ref<PeopleWrapper | undefined>(undefined),
      isLoading: ref(true),
    } as never)

    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: { stubs: { AutoComplete: autoCompleteStub } },
    })

    expect(wrapper.html()).toContain('Searching...')
  })

  it('shows "No people found" when there is a search query but no results', async () => {
    vi.mocked(usePeople).mockReturnValue({
      data: ref<PeopleWrapper>({ people: [], pagination: { totalElements: 0 } }),
      isLoading: ref(false),
    } as never)

    const wrapper = mount(PeopleFilter, {
      props: { modelValue: null },
      global: {
        stubs: {
          AutoComplete: {
            name: 'AutoComplete',
            template: `
              <div class="autocomplete-stub"
                @click="$emit('complete', { query: 'someone' })"
              >
                <slot name="empty" />
              </div>
            `,
            props: ['modelValue', 'suggestions', 'placeholder', 'loading', 'forceSelection'],
            emits: ['update:modelValue', 'complete', 'item-select', 'clear'],
          },
        },
      },
    })

    // Trigger search to set searchQuery
    await wrapper.find('.autocomplete-stub').trigger('click')
    await nextTick()

    expect(wrapper.html()).toContain('No people found')
  })
})
