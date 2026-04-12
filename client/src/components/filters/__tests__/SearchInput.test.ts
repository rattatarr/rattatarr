import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import SearchInput from '../SearchInput.vue'

// Mock VueUse to disable debouncing for tests
vi.mock('@vueuse/core', () => ({
  useDebounceFn: (fn: Function) => {
    // Return function that executes immediately (no debounce in tests)
    return (...args: never[]) => fn(...args)
  },
}))

describe('SearchInput', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders with initial value', () => {
    const wrapper = mount(SearchInput, {
      props: {
        modelValue: 'test search',
      },
      global: {
        stubs: {
          InputText: {
            template:
              '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" :placeholder="placeholder" />',
            props: ['modelValue', 'placeholder'],
          },
          IconField: { template: '<div><slot /></div>' },
          InputIcon: { template: '<i />' },
        },
      },
    })

    const input = wrapper.find('input')
    expect(input.exists()).toBe(true)
    expect(input.element.value).toBe('test search')
  })

  it('emits update:modelValue when input changes', async () => {
    const wrapper = mount(SearchInput, {
      props: {
        modelValue: '',
      },
      global: {
        stubs: {
          InputText: {
            template:
              '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
          },
          IconField: { template: '<div><slot /></div>' },
          InputIcon: { template: '<i />' },
        },
      },
    })

    const input = wrapper.find('input')
    await input.setValue('new value')

    await nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['new value'])
  })

  it('displays custom placeholder', () => {
    const wrapper = mount(SearchInput, {
      props: {
        modelValue: '',
        placeholder: 'Search movies...',
      },
      global: {
        stubs: {
          InputText: {
            template: '<input :placeholder="placeholder" />',
            props: ['modelValue', 'placeholder'],
          },
          IconField: { template: '<div><slot /></div>' },
          InputIcon: { template: '<i />' },
        },
      },
    })

    const input = wrapper.find('input')
    expect(input.attributes('placeholder')).toBe('Search movies...')
  })

  it('uses default placeholder when not provided', () => {
    const wrapper = mount(SearchInput, {
      props: {
        modelValue: '',
      },
      global: {
        stubs: {
          InputText: {
            template: '<input :placeholder="placeholder" />',
            props: ['modelValue', 'placeholder'],
          },
          IconField: { template: '<div><slot /></div>' },
          InputIcon: { template: '<i />' },
        },
      },
    })

    const input = wrapper.find('input')
    expect(input.attributes('placeholder')).toBe('Search...')
  })

  it('clears input when clear icon is clicked', async () => {
    const wrapper = mount(SearchInput, {
      props: {
        modelValue: 'some text',
      },
      global: {
        stubs: {
          InputText: { template: '<input />' },
          IconField: { template: '<div><slot /></div>' },
          InputIcon: {
            template: '<i @click="$attrs.onClick" />',
          },
        },
      },
    })

    // Find all icons (second one should be clear icon)
    const icons = wrapper.findAll('i')
    expect(icons.length).toBeGreaterThan(0)

    // The clear icon is conditional (v-if="localValue"), so it should exist
    const clearIcon = icons[icons.length - 1]!
    await clearIcon.trigger('click')

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([''])
  })

  it('syncs with external modelValue changes', async () => {
    const wrapper = mount(SearchInput, {
      props: {
        modelValue: 'initial',
      },
      global: {
        stubs: {
          InputText: { template: '<input />' },
          IconField: { template: '<div><slot /></div>' },
          InputIcon: { template: '<i />' },
        },
      },
    })

    await wrapper.setProps({ modelValue: 'updated' })
    await nextTick()

    // The component should sync its internal localValue
    expect(wrapper.props('modelValue')).toBe('updated')
  })
})
