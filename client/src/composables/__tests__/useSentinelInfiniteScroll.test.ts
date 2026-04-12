import { describe, it, expect, vi } from 'vitest'
import { ref } from 'vue'
import { useSentinelInfiniteScroll } from '../useSentinelInfiniteScroll'

describe('useSentinelInfiniteScroll', () => {
  it('should return a sentinel ref', () => {
    const fetchNextPage = vi.fn()
    const hasNextPage = ref(true)
    const isFetchingNextPage = ref(false)
    const isLoading = ref(false)

    const { sentinel } = useSentinelInfiniteScroll(
      fetchNextPage,
      hasNextPage,
      isFetchingNextPage,
      isLoading,
    )

    expect(sentinel).toBeDefined()
    expect(sentinel.value).toBeNull()
  })

  it('should accept optional rootMargin parameter', () => {
    const fetchNextPage = vi.fn()
    const hasNextPage = ref(true)
    const isFetchingNextPage = ref(false)
    const isLoading = ref(false)

    const { sentinel } = useSentinelInfiniteScroll(
      fetchNextPage,
      hasNextPage,
      isFetchingNextPage,
      isLoading,
      { rootMargin: '500px' },
    )

    expect(sentinel).toBeDefined()
  })

  it('should not call fetchNextPage immediately', () => {
    const fetchNextPage = vi.fn()
    const hasNextPage = ref(true)
    const isFetchingNextPage = ref(false)
    const isLoading = ref(false)

    useSentinelInfiniteScroll(fetchNextPage, hasNextPage, isFetchingNextPage, isLoading)

    // Should not call until intersection observer triggers
    expect(fetchNextPage).not.toHaveBeenCalled()
  })

  it('should work with different guard states', () => {
    const fetchNextPage = vi.fn()

    // Test with hasNextPage = false
    const result1 = useSentinelInfiniteScroll(fetchNextPage, ref(false), ref(false), ref(false))
    expect(result1.sentinel).toBeDefined()

    // Test with isFetchingNextPage = true
    const result2 = useSentinelInfiniteScroll(fetchNextPage, ref(true), ref(true), ref(false))
    expect(result2.sentinel).toBeDefined()

    // Test with isLoading = true
    const result3 = useSentinelInfiniteScroll(fetchNextPage, ref(true), ref(false), ref(true))
    expect(result3.sentinel).toBeDefined()

    // All should set up observer but not call fetchNextPage
    expect(fetchNextPage).not.toHaveBeenCalled()
  })
})
