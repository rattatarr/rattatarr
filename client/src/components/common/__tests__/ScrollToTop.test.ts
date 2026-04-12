import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ScrollToTop from '../ScrollToTop.vue'

// Mock PrimeVue Button
vi.mock('primevue/button', () => ({
  default: {
    name: 'Button',
    template: '<button @click="$attrs.onClick"><slot /></button>',
  },
}))

describe('ScrollToTop', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Mock window.scrollTo
    window.scrollTo = vi.fn()
    document.documentElement.scrollTo = vi.fn()
    document.body.scrollTo = vi.fn()
  })

  it('renders button when show is true', () => {
    const wrapper = mount(ScrollToTop, {
      props: { show: true },
    })

    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('does not render button when show is false', () => {
    const wrapper = mount(ScrollToTop, {
      props: { show: false },
    })

    expect(wrapper.find('button').exists()).toBe(false)
  })

  it('calls default scroll behavior when clicked without custom onScroll', async () => {
    const wrapper = mount(ScrollToTop, {
      props: { show: true },
    })

    await wrapper.find('button').trigger('click')

    expect(window.scrollTo).toHaveBeenCalledWith({
      top: 0,
      behavior: 'smooth',
    })
  })

  it('calls custom onScroll when provided', async () => {
    const customScroll = vi.fn()
    const wrapper = mount(ScrollToTop, {
      props: {
        show: true,
        onScroll: customScroll,
      },
    })

    await wrapper.find('button').trigger('click')

    expect(customScroll).toHaveBeenCalled()
    expect(window.scrollTo).not.toHaveBeenCalled()
  })

  it('has correct aria-label for accessibility', () => {
    const wrapper = mount(ScrollToTop, {
      props: { show: true },
    })

    expect(wrapper.find('button').attributes('aria-label')).toBe('Scroll to top')
  })

  it('updates visibility when show prop changes', async () => {
    const wrapper = mount(ScrollToTop, {
      props: { show: false },
    })

    expect(wrapper.find('button').exists()).toBe(false)

    await wrapper.setProps({ show: true })

    expect(wrapper.find('button').exists()).toBe(true)
  })
})
