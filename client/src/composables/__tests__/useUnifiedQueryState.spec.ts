import { describe, it, expect, vi } from 'vitest'
import { ref } from 'vue'
import { useUnifiedQueryState } from '../useUnifiedQueryState'
import type { MediaSourceInfo } from '../useMediaSource'

describe('useUnifiedQueryState', () => {
  // Mock MediaSourceInfo
  const createMockSource = (isInternal: boolean, isTMDb: boolean): MediaSourceInfo => ({
    type: (isInternal ? 'internal' : isTMDb ? 'tmdb' : 'imdb') as any,
    isInternal,
    isTMDb,
    isIMDb: false,
    id: isInternal ? 'uuid-123' : isTMDb ? '550' : 'tt0111161',
  })

  // Mock query results
  const createMockQuery = (
    isLoading = false,
    error: Error | null = null,
    data: any = undefined,
  ) => ({
    data: ref(data),
    isLoading: ref(isLoading),
    error: ref(error),
    refetch: vi.fn().mockResolvedValue(undefined),
  })

  describe('isLoading state', () => {
    it('returns true when internal query is loading and source is internal', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(true)
      const tmdbQuery = createMockQuery(false)

      const { isLoading } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(isLoading.value).toBe(true)
    })

    it('returns true when TMDb query is loading and source is TMDb', () => {
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery(false)
      const tmdbQuery = createMockQuery(true)

      const { isLoading } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(isLoading.value).toBe(true)
    })

    it('returns false when source is internal but internal query is not loading', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false)
      const tmdbQuery = createMockQuery(true) // TMDb loading doesn't matter

      const { isLoading } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(isLoading.value).toBe(false)
    })

    it('returns false when source is TMDb but TMDb query is not loading', () => {
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery(true) // Internal loading doesn't matter
      const tmdbQuery = createMockQuery(false)

      const { isLoading } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(isLoading.value).toBe(false)
    })

    it('returns false when both queries are not loading', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false)
      const tmdbQuery = createMockQuery(false)

      const { isLoading } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(isLoading.value).toBe(false)
    })

    it('reactively updates when source changes', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(true)
      const tmdbQuery = createMockQuery(false)

      const { isLoading } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(isLoading.value).toBe(true)

      // Change source to TMDb
      source.value = createMockSource(false, true)

      expect(isLoading.value).toBe(false)
    })
  })

  describe('error state', () => {
    it('returns internal error when source is internal', () => {
      const internalError = new Error('Internal API error')
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false, internalError)
      const tmdbQuery = createMockQuery(false, new Error('TMDb error'))

      const { error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(error.value).toBe(internalError)
    })

    it('returns TMDb error when source is TMDb', () => {
      const tmdbError = new Error('TMDb API error')
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery(false, new Error('Internal error'))
      const tmdbQuery = createMockQuery(false, tmdbError)

      const { error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(error.value).toBe(tmdbError)
    })

    it('returns null when internal source has no error', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false, null)
      const tmdbQuery = createMockQuery(false, new Error('TMDb error'))

      const { error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(error.value).toBeNull()
    })

    it('returns null when TMDb source has no error', () => {
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery(false, new Error('Internal error'))
      const tmdbQuery = createMockQuery(false, null)

      const { error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(error.value).toBeNull()
    })

    it('returns null when neither source is active', () => {
      const source = ref(createMockSource(false, false))
      const internalQuery = createMockQuery(false, new Error('Internal error'))
      const tmdbQuery = createMockQuery(false, new Error('TMDb error'))

      const { error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(error.value).toBeNull()
    })

    it('reactively updates when source changes', () => {
      const internalError = new Error('Internal error')
      const tmdbError = new Error('TMDb error')
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false, internalError)
      const tmdbQuery = createMockQuery(false, tmdbError)

      const { error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      expect(error.value).toBe(internalError)

      // Change source to TMDb
      source.value = createMockSource(false, true)

      expect(error.value).toBe(tmdbError)
    })
  })

  describe('refetch function', () => {
    it('calls internal refetch when source is internal', async () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery()
      const tmdbQuery = createMockQuery()

      const { refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      await refetch()

      expect(internalQuery.refetch).toHaveBeenCalledTimes(1)
      expect(tmdbQuery.refetch).not.toHaveBeenCalled()
    })

    it('calls TMDb refetch when source is TMDb', async () => {
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery()
      const tmdbQuery = createMockQuery()

      const { refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      await refetch()

      expect(tmdbQuery.refetch).toHaveBeenCalledTimes(1)
      expect(internalQuery.refetch).not.toHaveBeenCalled()
    })

    it('returns internal refetch promise', async () => {
      const mockData = { id: '123', title: 'Test' }
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery()
      internalQuery.refetch.mockResolvedValue(mockData)
      const tmdbQuery = createMockQuery()

      const { refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      const result = await refetch()

      expect(result).toBe(mockData)
    })

    it('returns TMDb refetch promise', async () => {
      const mockData = { id: 456, title: 'TMDb Movie' }
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery()
      const tmdbQuery = createMockQuery()
      tmdbQuery.refetch.mockResolvedValue(mockData)

      const { refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      const result = await refetch()

      expect(result).toBe(mockData)
    })

    it('returns undefined when neither source is active', async () => {
      const source = ref(createMockSource(false, false))
      const internalQuery = createMockQuery()
      const tmdbQuery = createMockQuery()

      const { refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      const result = await refetch()

      expect(result).toBeUndefined()
      expect(internalQuery.refetch).not.toHaveBeenCalled()
      expect(tmdbQuery.refetch).not.toHaveBeenCalled()
    })

    it('calls different refetch when source changes', async () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery()
      const tmdbQuery = createMockQuery()

      const { refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      // First call with internal source
      await refetch()
      expect(internalQuery.refetch).toHaveBeenCalledTimes(1)
      expect(tmdbQuery.refetch).not.toHaveBeenCalled()

      // Change source to TMDb
      source.value = createMockSource(false, true)

      // Second call with TMDb source
      await refetch()
      expect(internalQuery.refetch).toHaveBeenCalledTimes(1) // Still 1
      expect(tmdbQuery.refetch).toHaveBeenCalledTimes(1)
    })
  })

  describe('integration', () => {
    it('handles complete internal query lifecycle', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(true, null, { movies: [] })
      const tmdbQuery = createMockQuery(false)

      const { isLoading, error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      // Initially loading
      expect(isLoading.value).toBe(true)
      expect(error.value).toBeNull()

      // Update to loaded state
      internalQuery.isLoading.value = false
      internalQuery.data.value = { movies: [{ id: '1', title: 'Test' }] }

      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      // Simulate error
      internalQuery.error.value = new Error('Failed to load')

      expect(error.value).toBeInstanceOf(Error)
      expect(error.value?.message).toBe('Failed to load')
    })

    it('handles complete TMDb query lifecycle', () => {
      const source = ref(createMockSource(false, true))
      const internalQuery = createMockQuery(false)
      const tmdbQuery = createMockQuery(true, null, undefined)

      const { isLoading, error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      // Initially loading
      expect(isLoading.value).toBe(true)
      expect(error.value).toBeNull()

      // Update to loaded state
      tmdbQuery.isLoading.value = false
      tmdbQuery.data.value = { id: 550, title: 'Fight Club' }

      expect(isLoading.value).toBe(false)
      expect(error.value).toBeNull()

      // Simulate error
      tmdbQuery.error.value = new Error('TMDb rate limit')

      expect(error.value).toBeInstanceOf(Error)
      expect(error.value?.message).toBe('TMDb rate limit')
    })

    it('switches correctly between sources', () => {
      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false, new Error('Internal error'))
      const tmdbQuery = createMockQuery(true, new Error('TMDb error'))

      const { isLoading, error } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      // Internal source: not loading, has error
      expect(isLoading.value).toBe(false)
      expect(error.value?.message).toBe('Internal error')

      // Switch to TMDb source: loading, has error
      source.value = createMockSource(false, true)

      expect(isLoading.value).toBe(true)
      expect(error.value?.message).toBe('TMDb error')
    })
  })

  describe('type safety', () => {
    it('accepts different generic types for internal and TMDb queries', () => {
      interface InternalData {
        movies: Array<{ id: string; title: string }>
      }

      interface TMDbData {
        id: number
        title: string
        overview: string
      }

      const source = ref(createMockSource(true, false))
      const internalQuery = createMockQuery(false, null, { movies: [] } as InternalData)
      const tmdbQuery = createMockQuery(false, null, { id: 1, title: '', overview: '' } as TMDbData)

      const { isLoading, error, refetch } = useUnifiedQueryState(source, internalQuery, tmdbQuery)

      // Should compile without errors
      expect(isLoading.value).toBeDefined()
      expect(error.value).toBeNull()
      expect(refetch).toBeInstanceOf(Function)
    })
  })
})
