import { describe, it, expect, vi, beforeEach } from 'vitest'
import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'
import { provideSettingsNavigation, useSettingsNavigation } from '@/composables'

describe('useSettingsNavigation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('throws error when used without provider', () => {
    const TestComponent = defineComponent({
      setup() {
        useSettingsNavigation()
        return () => h('div')
      },
    })

    expect(() => mount(TestComponent)).toThrow(
      'useSettingsNavigation must be used within a component wrapped by provideSettingsNavigation',
    )
  })

  it('provides and injects context correctly', () => {
    const ProviderComponent = defineComponent({
      setup() {
        provideSettingsNavigation()
        return () => h('div')
      },
    })

    const ConsumerComponent = defineComponent({
      setup() {
        const context = useSettingsNavigation()
        expect(context).toBeDefined()
        expect(context.registerField).toBeInstanceOf(Function)
        expect(context.scrollToSetting).toBeInstanceOf(Function)
        expect(context.highlightedKey).toBeDefined()
        return () => h('div')
      },
    })

    const wrapper = mount(ProviderComponent, {
      slots: {
        default: () => h(ConsumerComponent),
      },
    })

    expect(wrapper).toBeDefined()
  })

  it('registers and unregisters field elements', () => {
    const ProviderComponent = defineComponent({
      setup() {
        const context = provideSettingsNavigation()

        const element1 = document.createElement('div')
        const element2 = document.createElement('div')

        // Register elements
        context.registerField('setting1', element1)
        context.registerField('setting2', element2)

        // Unregister by passing null
        context.registerField('setting1', null)

        return () => h('div')
      },
    })

    mount(ProviderComponent)
  })

  it('scrolls to registered setting field', () => {
    const mockElement = document.createElement('div')
    const scrollIntoViewMock = vi.fn()
    mockElement.scrollIntoView = scrollIntoViewMock

    const ProviderComponent = defineComponent({
      setup() {
        const context = provideSettingsNavigation()

        // Register element
        context.registerField('testSetting', mockElement)

        // Scroll to it
        context.scrollToSetting('testSetting')

        expect(scrollIntoViewMock).toHaveBeenCalledWith({
          behavior: 'smooth',
          block: 'center',
        })

        return () => h('div')
      },
    })

    mount(ProviderComponent)
  })

  it('highlights field when scrolling to it', () => {
    const mockElement = document.createElement('div')
    mockElement.scrollIntoView = vi.fn()

    const ProviderComponent = defineComponent({
      setup() {
        const context = provideSettingsNavigation()

        context.registerField('testSetting', mockElement)

        // Initially no highlight
        expect(context.highlightedKey.value).toBeNull()

        // Scroll to setting
        context.scrollToSetting('testSetting')

        // Should be highlighted
        expect(context.highlightedKey.value).toBe('testSetting')

        return () => h('div')
      },
    })

    mount(ProviderComponent)
  })

  it('removes highlight after 2 seconds', () => {
    const mockElement = document.createElement('div')
    mockElement.scrollIntoView = vi.fn()

    const ProviderComponent = defineComponent({
      setup() {
        const context = provideSettingsNavigation()

        context.registerField('testSetting', mockElement)
        context.scrollToSetting('testSetting')

        expect(context.highlightedKey.value).toBe('testSetting')

        // Fast-forward 2 seconds
        vi.advanceTimersByTime(2000)

        expect(context.highlightedKey.value).toBeNull()

        return () => h('div')
      },
    })

    mount(ProviderComponent)
  })

  it('warns when trying to scroll to unregistered field', () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})

    const ProviderComponent = defineComponent({
      setup() {
        const context = provideSettingsNavigation()

        // Try to scroll to non-existent field
        context.scrollToSetting('nonExistentSetting')

        expect(consoleWarnSpy).toHaveBeenCalledWith(
          'Setting field with key "nonExistentSetting" not found',
        )

        return () => h('div')
      },
    })

    mount(ProviderComponent)

    consoleWarnSpy.mockRestore()
  })

  it('handles multiple registered fields', () => {
    const element1 = document.createElement('div')
    const element2 = document.createElement('div')
    const element3 = document.createElement('div')

    element1.scrollIntoView = vi.fn()
    element2.scrollIntoView = vi.fn()
    element3.scrollIntoView = vi.fn()

    const ProviderComponent = defineComponent({
      setup() {
        const context = provideSettingsNavigation()

        context.registerField('field1', element1)
        context.registerField('field2', element2)
        context.registerField('field3', element3)

        // Scroll to field2
        context.scrollToSetting('field2')

        expect(element1.scrollIntoView).not.toHaveBeenCalled()
        expect(element2.scrollIntoView).toHaveBeenCalled()
        expect(element3.scrollIntoView).not.toHaveBeenCalled()

        return () => h('div')
      },
    })

    mount(ProviderComponent)
  })
})
