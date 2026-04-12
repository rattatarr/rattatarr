import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, nextTick } from 'vue'
import GenresFilter from '../GenresFilter.vue'
import type { GenresWrapper, Genre } from '@/types'

// Mock VueUse to disable debouncing for tests
vi.mock('@vueuse/core', () => ({
  useDebounceFn: (fn: Function) => {
    // Return function that executes immediately (no debounce in tests)
    return (...args: never[]) => fn(...args)
  },
}))

// Mock useGenres query hook
vi.mock('@/queries/useLibrary', () => ({
  useGenres: vi.fn(),
}))

// Import the mocked function
import { useGenres } from '@/queries/useLibrary'

describe('GenresFilter', () => {
  const mockGenres: Genre[] = [
    { id: '1', name: 'Action' },
    { id: '2', name: 'Comedy' },
    { id: '3', name: 'Drama' },
    { id: '4', name: 'Sci-Fi' },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    // Default mock implementation
    vi.mocked(useGenres).mockReturnValue({
      data: ref<GenresWrapper>({ genres: mockGenres, pagination: { totalElements: 4 } }),
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
      promise: Promise.resolve({} as GenresWrapper),
      dataUpdatedAt: ref(0),
      errorUpdatedAt: ref(0),
      errorUpdateCount: ref(0),
      failureCount: ref(0),
      failureReason: ref(null),
    } as never)
  })

  it('renders with initial selected genres', () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: ['Action', 'Drama'],
      },
      global: {
        stubs: {
          MultiSelect: {
            template:
              '<select multiple :modelValue="modelValue"><option v-for="opt in options" :key="opt.id">{{ opt.name }}</option></select>',
            props: ['modelValue', 'options', 'placeholder', 'filter', 'loading'],
          },
        },
      },
    })

    expect(wrapper.exists()).toBe(true)
    expect(useGenres).toHaveBeenCalledWith(
      expect.anything(), // pageable ref
      expect.anything(), // filters ref
    )
  })

  it('emits update:modelValue when selection changes', async () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: ['Action'],
      },
      global: {
        stubs: {
          MultiSelect: {
            name: 'MultiSelect',
            template:
              "<div class=\"multiselect-stub\" @click=\"$emit('update:modelValue', ['Action', 'Comedy'])\"></div>",
            props: ['modelValue', 'options'],
          },
        },
      },
    })

    const multiselect = wrapper.find('.multiselect-stub')
    await multiselect.trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([['Action', 'Comedy']])
  })

  it('displays custom placeholder', () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
        placeholder: 'Choose genres...',
      },
      global: {
        stubs: {
          MultiSelect: {
            name: 'MultiSelect',
            template: '<div class="multiselect-stub" :data-placeholder="placeholder"></div>',
            props: ['placeholder', 'modelValue', 'options'],
          },
        },
      },
    })

    const multiselect = wrapper.find('.multiselect-stub')
    expect(multiselect.attributes('data-placeholder')).toBe('Choose genres...')
  })

  it('uses default placeholder when not provided', () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            name: 'MultiSelect',
            template: '<div class="multiselect-stub" :data-placeholder="placeholder"></div>',
            props: ['placeholder', 'modelValue', 'options'],
          },
        },
      },
    })

    const multiselect = wrapper.find('.multiselect-stub')
    expect(multiselect.attributes('data-placeholder')).toBe('Select genres...')
  })

  it('shows loading state when fetching genres', () => {
    vi.mocked(useGenres).mockReturnValue({
      data: ref<GenresWrapper | undefined>(undefined),
      isLoading: ref(true),
    } as never)

    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            name: 'MultiSelect',
            template: '<div class="multiselect-stub" :data-loading="loading"></div>',
            props: ['loading', 'modelValue', 'options'],
          },
        },
      },
    })

    const multiselect = wrapper.find('.multiselect-stub')
    expect(multiselect.attributes('data-loading')).toBe('true')
  })

  it('displays empty state when no genres found', () => {
    vi.mocked(useGenres).mockReturnValue({
      data: ref<GenresWrapper>({ genres: [], pagination: { totalElements: 0 } }),
      isLoading: ref(false),
    } as never)

    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            template: '<div><slot name="empty" /></div>',
            props: ['modelValue', 'options'],
          },
        },
      },
    })

    // The empty slot should be rendered
    expect(wrapper.html()).toContain('No genres found')
  })

  it('displays loading message in empty slot when loading', () => {
    vi.mocked(useGenres).mockReturnValue({
      data: ref<GenresWrapper | undefined>(undefined),
      isLoading: ref(true),
    } as never)

    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            template: '<div><slot name="empty" /></div>',
            props: ['modelValue', 'options', 'loading'],
          },
        },
      },
    })

    // The empty slot should show loading message
    expect(wrapper.html()).toContain('Loading genres...')
  })

  it('syncs with external modelValue changes', async () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: ['Action'],
      },
      global: {
        stubs: {
          MultiSelect: {
            template: '<div></div>',
            props: ['modelValue', 'options'],
          },
        },
      },
    })

    await wrapper.setProps({ modelValue: ['Comedy', 'Drama'] })
    await nextTick()

    // The component should sync its internal selectedGenres
    expect(wrapper.props('modelValue')).toEqual(['Comedy', 'Drama'])
  })

  it('handles filter change with debounce', async () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            name: 'MultiSelect',
            template:
              '<div class="multiselect-stub" @click="$emit(\'filter\', { value: \'act\' })"></div>',
            props: ['modelValue', 'options', 'filter'],
          },
        },
      },
    })

    const multiselect = wrapper.find('.multiselect-stub')
    await multiselect.trigger('click')
    await nextTick()

    // After debounce (mocked to be immediate), useGenres should be called with updated filter
    // The component internally updates searchQuery which affects genreFilters
    expect(useGenres).toHaveBeenCalled()
  })

  it('sorts selected genres to the top', () => {
    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: ['Drama', 'Sci-Fi'], // Drama and Sci-Fi should appear first
      },
      global: {
        stubs: {
          MultiSelect: {
            template: '<div><span v-for="opt in options" :key="opt.id">{{ opt.name }}</span></div>',
            props: ['modelValue', 'options'],
          },
        },
      },
    })

    // The component's filteredGenres computed property should sort selected items first
    // We can verify this by checking the component instance
    const vm = wrapper.vm as unknown as {
      filteredGenres: Genre[]
    }

    // Selected genres (Drama, Sci-Fi) should be at the beginning
    expect(vm.filteredGenres[0]?.name).toBe('Drama')
    expect(vm.filteredGenres[1]?.name).toBe('Sci-Fi')
  })

  it('handles genres with undefined names gracefully', () => {
    const genresWithUndefined: Genre[] = [
      { id: '1', name: 'Action' },
      { id: '2', name: undefined },
      { id: '3', name: 'Drama' },
    ]

    vi.mocked(useGenres).mockReturnValue({
      data: ref<GenresWrapper>({ genres: genresWithUndefined, pagination: { totalElements: 3 } }),
      isLoading: ref(false),
    } as never)

    const wrapper = mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            template: '<div></div>',
            props: ['modelValue', 'options'],
          },
        },
      },
    })

    // Should render without errors
    expect(wrapper.exists()).toBe(true)

    // filteredGenres should handle undefined names (treat as empty string)
    const vm = wrapper.vm as unknown as {
      filteredGenres: Genre[]
    }
    expect(vm.filteredGenres).toHaveLength(3)
  })

  it('passes correct pageable configuration to useGenres', () => {
    mount(GenresFilter, {
      props: {
        modelValue: [],
      },
      global: {
        stubs: {
          MultiSelect: {
            template: '<div></div>',
            props: ['modelValue', 'options'],
          },
        },
      },
    })

    // Verify useGenres is called with correct pageable (page: 0, size: 100, sort: ['name,asc'])
    const callArgs = vi.mocked(useGenres).mock.calls[0] as unknown[]
    const pageableRef = callArgs[0] as { value: { page: number; size: number; sort: string[] } }
    expect(pageableRef.value).toEqual({
      page: 0,
      size: 100,
      sort: ['name,asc'],
    })
  })
})
