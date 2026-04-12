import { ref } from 'vue'
import { useIntersectionObserver } from '@vueuse/core'
import type { Ref } from 'vue'

/**
 * Composable for infinite scroll using sentinel element + intersection observer
 *
 * Creates an invisible sentinel element that triggers loading when it becomes visible.
 * More reliable than scroll-based detection as it only triggers on actual visibility.
 *
 * @param fetchNextPage - Function to fetch the next page
 * @param hasNextPage - Ref indicating if more pages exist
 * @param isFetchingNextPage - Ref indicating if currently fetching
 * @param isLoading - Ref indicating if initial load is in progress
 * @param options - Configuration options
 * @returns Object with sentinel ref to attach to template element
 *
 * @example
 * ```vue
 * <script setup>
 * const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useInfiniteMovies(...)
 * const { sentinel } = useSentinelInfiniteScroll(fetchNextPage, hasNextPage, isFetchingNextPage, isLoading)
 * </script>
 *
 * <template>
 *   <div class="items-grid">
 *     <ItemCard v-for="item in items" :key="item.id" ... />
 *   </div>
 *   <!-- Invisible sentinel element triggers loading when visible -->
 *   <div v-if="items.length > 0" ref="sentinel" class="scroll-sentinel" />
 * </template>
 *
 * <style>
 * .scroll-sentinel {
 *   height: 1px;
 *   width: 100%;
 * }
 * </style>
 * ```
 */
export function useSentinelInfiniteScroll(
  fetchNextPage: () => void,
  hasNextPage: Ref<boolean | undefined>,
  isFetchingNextPage: Ref<boolean>,
  isLoading: Ref<boolean>,
  options: {
    rootMargin?: string
  } = {},
) {
  const { rootMargin = '400px' } = options

  // Sentinel element ref
  const sentinel = ref<HTMLElement | null>(null)

  // Watch sentinel visibility
  useIntersectionObserver(
    sentinel,
    (entries) => {
      const isIntersecting = entries[0]?.isIntersecting

      // Only trigger if all conditions are met
      if (isIntersecting && hasNextPage.value && !isFetchingNextPage.value && !isLoading.value) {
        fetchNextPage()
      }
    },
    {
      rootMargin,
    },
  )

  return {
    sentinel,
  }
}
