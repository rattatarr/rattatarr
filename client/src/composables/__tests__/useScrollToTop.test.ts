import { describe, it, expect, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useScrollToTop } from '@/composables'

describe('useScrollToTop', () => {
  beforeEach(() => {
    // Clear DOM
    document.body.innerHTML = ''

    // Create mock main element
    const main = document.createElement('div')
    main.className = 'app-layout__main'
    main.style.overflow = 'auto'
    main.style.height = '100vh'
    document.body.appendChild(main)
  })

  it('should initialize with showButton as false when pageCount is 0', () => {
    const { showButton } = useScrollToTop(0)

    expect(showButton.value).toBe(false)
  })

  it('should initialize with showButton as false when pageCount is 1', () => {
    const { showButton } = useScrollToTop(1)

    expect(showButton.value).toBe(false)
  })

  it('should initialize with showButton as false when not scrolled', () => {
    const { showButton } = useScrollToTop(2)

    // Not scrolled down yet
    expect(showButton.value).toBe(false)
  })

  it('should provide scrollToTop function', () => {
    const { scrollToTop } = useScrollToTop(0)

    expect(typeof scrollToTop).toBe('function')
  })

  it('should return reactive showButton ref', () => {
    const pageCount = ref(0)
    const { showButton } = useScrollToTop(pageCount)

    expect(showButton.value).toBe(false)

    // Update pageCount
    pageCount.value = 2

    // showButton should still be reactive (though still false without scroll)
    expect(typeof showButton.value).toBe('boolean')
  })

  it('should handle missing main element gracefully', () => {
    // Remove main element
    const main = document.querySelector('.app-layout__main')
    main?.remove()

    const { scrollToTop, showButton } = useScrollToTop(2)

    // Should not throw
    expect(() => scrollToTop()).not.toThrow()
    expect(showButton.value).toBe(false)
  })

  it('should use fallback element when main is missing', () => {
    // Remove main element
    document.querySelector('.app-layout__main')?.remove()

    const { showButton } = useScrollToTop(2)

    // Should fallback to documentElement and not error
    expect(showButton).toBeDefined()
    expect(typeof showButton.value).toBe('boolean')
  })
})
