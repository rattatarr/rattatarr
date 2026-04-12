import { useRouter } from 'vue-router'

export function useRoutePreload() {
  const router = useRouter()

  const preloadRoute = async (routeName: string) => {
    try {
      console.log(`[Preload] Attempting to preload route: ${routeName}`)
      const route = router.resolve({ name: routeName })
      const matchedRoute = route.matched[0]

      if (!matchedRoute) {
        console.warn(`[Preload] Route not matched: ${routeName}`)
        return
      }

      if (!matchedRoute.components?.default) {
        console.warn(`[Preload] No default component for route: ${routeName}`)
        return
      }

      const component = matchedRoute.components.default as any
      console.log(`[Preload] Component type for ${routeName}:`, {
        type: typeof component,
        isFunction: typeof component === 'function',
        length: component.length,
      })

      if (typeof component === 'function' && component.length === 0) {
        // It's a lazy-loaded component (async component factory)
        console.log(`[Preload] Loading component for route: ${routeName}`)
        await component()
        console.log(`[Preload] Successfully preloaded route: ${routeName}`)
      } else {
        console.warn(`[Preload] Component is not lazy-loaded for route: ${routeName}`)
      }
    } catch (error) {
      console.error(`[Preload] Failed to preload route: ${routeName}`, error)
    }
  }

  const preloadRoutes = async (routeNames: string[]) => {
    await Promise.allSettled(routeNames.map((name) => preloadRoute(name)))
  }

  /**
   * Preload main navigation routes
   * Call this function when you want to preload, typically in onMounted or after a delay
   */
  const preloadMainRoutes = async () => {
    console.log('[Preload] Starting to preload main navigation routes')
    await preloadRoutes(['ratings', 'movies', 'series', 'settings'])
    console.log('[Preload] Finished preloading main navigation routes')
  }

  return {
    preloadRoute,
    preloadRoutes,
    preloadMainRoutes,
  }
}
