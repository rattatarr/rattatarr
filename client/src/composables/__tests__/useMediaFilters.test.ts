import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMediaFilters, useMediaFiltersContext } from '../useMediaFilters'
import { useProfileStore } from '@/stores'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'

describe('useMediaFilters', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('Initial State', () => {
    it('should initialize with default values', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      expect(filters.titleSearch.value).toBe('')
      expect(filters.selectedGenres.value).toEqual([])
      expect(filters.releasedAfter.value).toBeUndefined()
      expect(filters.releasedBefore.value).toBeUndefined()
      expect(filters.sorts.value).toEqual([{ field: 'ratings', order: 'desc' }])
      expect(filters.PAGE_SIZE).toBe(24)
    })
  })

  describe('Sort Array', () => {
    it('should generate sort array from default single entry', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      expect(filters.sortArray.value).toEqual(['ratings,desc'])
    })

    it('should update sort array when sorts changes', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.sorts.value = [{ field: 'title', order: 'desc' }]

      expect(filters.sortArray.value).toEqual(['title,desc'])
    })

    it('should support multiple sort entries', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.sorts.value = [
        { field: 'ratings', order: 'desc' },
        { field: 'productionYear', order: 'desc' },
      ]

      expect(filters.sortArray.value).toEqual(['ratings,desc', 'productionYear,desc'])
    })

    it('should support up to 3 sort entries', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.sorts.value = [
        { field: 'ratings', order: 'asc' },
        { field: 'productionYear', order: 'desc' },
        { field: 'title', order: 'asc' },
      ]

      expect(filters.sortArray.value).toEqual(['ratings,asc', 'productionYear,desc', 'title,asc'])
    })

    it('should support ascending sort order', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.sorts.value = [{ field: 'ratings', order: 'asc' }]

      expect(filters.sortArray.value).toEqual(['ratings,asc'])
    })
  })

  describe('Movie Filters', () => {
    it('should build movie filters with title', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.titleSearch.value = 'Inception'

      expect(filters.movieFilters.value).toMatchObject({
        title: 'Inception',
      })
    })

    it('should include genres when selected', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.selectedGenres.value = ['Action', 'Sci-Fi']

      expect(filters.movieFilters.value).toMatchObject({
        genres: ['Action', 'Sci-Fi'],
      })
    })

    it('should not include genres when empty', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      expect(filters.movieFilters.value.genres).toBeUndefined()
    })

    it('should include year range when set', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.releasedAfter.value = 2010
      filters.releasedBefore.value = 2020

      expect(filters.movieFilters.value).toMatchObject({
        releasedAfter: 2010,
        releasedBefore: 2020,
      })
    })

    it('should include profileId from store', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters
      const profileStore = useProfileStore()

      profileStore.setProfile('profile-123')

      expect(filters.movieFilters.value).toMatchObject({
        profileId: 'profile-123',
      })
    })

    it('should handle profileId as undefined when no profile selected', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      expect(filters.movieFilters.value.profileId).toBeUndefined()
    })
  })

  describe('Series Filters', () => {
    it('should build series filters similar to movie filters', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      filters.titleSearch.value = 'Breaking Bad'
      filters.selectedGenres.value = ['Drama', 'Crime']
      filters.releasedAfter.value = 2008

      expect(filters.seriesFilters.value).toMatchObject({
        title: 'Breaking Bad',
        genres: ['Drama', 'Crime'],
        releasedAfter: 2008,
      })
    })
  })

  describe('Reset Action', () => {
    it('should reset all filters to defaults', () => {
      const wrapper = mount(
        defineComponent({
          setup() {
            return { filters: useMediaFilters() }
          },
          render: () => h('div'),
        }),
      )

      const filters = wrapper.vm.filters

      // Set some values
      filters.titleSearch.value = 'Test'
      filters.selectedGenres.value = ['Action']
      filters.releasedAfter.value = 2000
      filters.releasedBefore.value = 2020
      filters.sorts.value = [
        { field: 'title', order: 'asc' },
        { field: 'productionYear', order: 'desc' },
      ]

      // Reset
      filters.reset()

      expect(filters.titleSearch.value).toBe('')
      expect(filters.selectedGenres.value).toEqual([])
      expect(filters.releasedAfter.value).toBeUndefined()
      expect(filters.releasedBefore.value).toBeUndefined()
      expect(filters.sorts.value).toEqual([{ field: 'ratings', order: 'desc' }])
    })
  })

  describe('Context Injection', () => {
    it('should provide and inject filter state', () => {
      const Parent = defineComponent({
        setup() {
          useMediaFilters()
          return () => h(Child)
        },
      })

      const Child = defineComponent({
        setup() {
          const filters = useMediaFiltersContext()
          return () => h('div', { id: 'sort' }, filters.sorts.value[0]?.field ?? '')
        },
      })

      const wrapper = mount(Parent)

      expect(wrapper.find('#sort').text()).toBe('ratings')
    })

    it('should throw error when context is not provided', () => {
      const Component = defineComponent({
        setup() {
          // This should throw because no provider exists
          useMediaFiltersContext()
          return () => h('div')
        },
      })

      expect(() => mount(Component)).toThrow(
        'useMediaFiltersContext must be used within a provider',
      )
    })
  })
})
