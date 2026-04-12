import { computed, toValue, type MaybeRefOrGetter } from 'vue'
import { useScroll } from '@vueuse/core'

/**
 * Composable for scroll-to-top button functionality
 *
 * Shows button when scrolled down AND multiple pages are loaded.
 *
 * @param pageCount - Number of pages loaded (for infinite scroll views)
 * @returns showButton (computed), scrollToTop (function)
 */
export function useScrollToTop(pageCount: MaybeRefOrGetter<number>) {
  // Find the scroll container (.app-layout__main)
  const mainElement =
    (document.querySelector('.app-layout__main') as HTMLElement) ?? document.documentElement

  const { y: scrollY } = useScroll(mainElement, {
    behavior: 'smooth',
  })

  // Show button when: scrolled down >300px AND loaded multiple pages
  const showButton = computed(() => {
    return scrollY.value > 300 && toValue(pageCount) > 1
  })

  /**
   * Scrolls the container to the top with smooth animation
   */
  function scrollToTop() {
    mainElement.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return {
    showButton,
    scrollToTop,
  }
}
