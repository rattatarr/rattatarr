/**
 * Vue Test Utils Helpers
 *
 * Provides utilities for testing Vue components with proper setup.
 */

import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'

/**
 * Create a fresh QueryClient for testing
 *
 * Disables retries and caching for predictable tests.
 */
export function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Disable retries in tests
        gcTime: 0, // Disable caching
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  })
}

/**
 * Mount a Vue component with test-friendly defaults
 *
 * Automatically provides:
 * - Fresh Pinia instance
 * - Fresh QueryClient
 * - Global stubs for common components
 */

export function mountWithPlugins(component: any, options: any = {}) {
  // Create fresh Pinia instance
  const pinia = createPinia()
  setActivePinia(pinia)

  // Create fresh QueryClient
  const queryClient = createTestQueryClient()

  // Merge with user options
  const mergedOptions = {
    global: {
      plugins: [pinia, [VueQueryPlugin, { queryClient }], ...(options.global?.plugins || [])],
      stubs: {
        // Stub PrimeVue components by default
        Button: true,
        InputText: true,
        InputNumber: true,
        Dropdown: true,
        MultiSelect: true,
        Dialog: true,
        Card: true,
        Menu: true,
        // Allow overrides
        ...(options.global?.stubs || {}),
      },
      ...(options.global || {}),
    },
    ...(options || {}),
  }

  return mount(component, mergedOptions)
}

/**
 * Wait for async updates to complete
 *
 * Useful for waiting on TanStack Query updates, nextTick, etc.
 */
export async function flushPromises(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0))
}
