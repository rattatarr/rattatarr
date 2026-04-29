import { nextTick } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { onActivated } from 'vue'

const scrollPositions = new Map<string, number>()

function getScrollContainer(): HTMLElement | null {
  return document.querySelector('.app-layout__main')
}

export function useScrollRestoration() {
  const route = useRoute()

  onBeforeRouteLeave(() => {
    const container = getScrollContainer()
    if (container) {
      scrollPositions.set(route.path, container.scrollTop)
    }
  })

  onActivated(() => {
    const saved = scrollPositions.get(route.path)
    if (saved !== undefined) {
      nextTick(() => {
        requestAnimationFrame(() => {
          const container = getScrollContainer()
          if (container) container.scrollTop = saved
        })
      })
    }
  })
}
