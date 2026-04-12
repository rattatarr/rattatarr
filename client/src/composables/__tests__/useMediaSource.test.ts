import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useMediaSource } from '../useMediaSource'
import { useRoute } from 'vue-router'
import { MediaSource } from '@/utils/enums'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
}))

describe('useMediaSource', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Query Parameter Detection (Priority 1)', () => {
    it('should detect TMDb source from query parameter', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'tmdb' },
      } as any)

      const id = ref('12345')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.TMDB,
        isTMDb: true,
        isInternal: false,
        isIMDb: false,
        id: '12345',
      })
    })

    it('should detect IMDb source from query parameter', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'imdb' },
      } as any)

      const id = ref('tt1234567')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.IMDB,
        isTMDb: false,
        isInternal: false,
        isIMDb: true,
        id: 'tt1234567',
      })
    })

    it('should detect internal source from query parameter', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'internal' },
      } as any)

      const id = ref('550e8400-e29b-41d4-a716-446655440000')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.INTERNAL,
        isTMDb: false,
        isInternal: true,
        isIMDb: false,
        id: '550e8400-e29b-41d4-a716-446655440000',
      })
    })

    it('should handle uppercase query parameter', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'TMDB' },
      } as any)

      const id = ref('12345')
      const result = useMediaSource(id)

      expect(result.value.type).toBe(MediaSource.TMDB)
      expect(result.value.isTMDb).toBe(true)
    })
  })

  describe('ID Format Detection (Priority 2)', () => {
    it('should detect TMDb from numeric ID format', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('550')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.TMDB,
        isTMDb: true,
        isInternal: false,
        isIMDb: false,
        id: '550',
      })
    })

    it('should detect TMDb from long numeric ID', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('12345678')
      const result = useMediaSource(id)

      expect(result.value.type).toBe(MediaSource.TMDB)
      expect(result.value.isTMDb).toBe(true)
    })

    it('should detect IMDb from tt-prefixed ID format', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('tt0468569')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.IMDB,
        isTMDb: false,
        isInternal: false,
        isIMDb: true,
        id: 'tt0468569',
      })
    })

    it('should detect internal from UUID format', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('550e8400-e29b-41d4-a716-446655440000')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.INTERNAL,
        isTMDb: false,
        isInternal: true,
        isIMDb: false,
        id: '550e8400-e29b-41d4-a716-446655440000',
      })
    })

    it('should detect internal from UUID with different casing', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('A1B2C3D4-E5F6-7890-ABCD-EF1234567890')
      const result = useMediaSource(id)

      expect(result.value.type).toBe(MediaSource.INTERNAL)
      expect(result.value.isInternal).toBe(true)
    })
  })

  describe('Default Behavior', () => {
    it('should default to internal for ambiguous ID format', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('some-weird-id-123')
      const result = useMediaSource(id)

      expect(result.value).toEqual({
        type: MediaSource.INTERNAL,
        isTMDb: false,
        isInternal: true,
        isIMDb: false,
        id: 'some-weird-id-123',
      })
    })

    it('should default to internal for empty ID', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('')
      const result = useMediaSource(id)

      expect(result.value.isInternal).toBe(true)
    })
  })

  describe('Reactivity', () => {
    it('should update when ID ref changes', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('550') // TMDb numeric
      const result = useMediaSource(id)

      expect(result.value.isTMDb).toBe(true)

      // Change to UUID
      id.value = '550e8400-e29b-41d4-a716-446655440000'

      expect(result.value.isInternal).toBe(true)
      expect(result.value.isTMDb).toBe(false)
    })

    it('should update when changing between different sources', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('tt0468569') // IMDb
      const result = useMediaSource(id)

      expect(result.value.isIMDb).toBe(true)

      // Change to TMDb
      id.value = '550'

      expect(result.value.isTMDb).toBe(true)
      expect(result.value.isIMDb).toBe(false)
    })
  })

  describe('Query Parameter Priority', () => {
    it('should prioritize query parameter over ID format', () => {
      // Query says tmdb, but ID looks like IMDb
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'tmdb' },
      } as any)

      const id = ref('tt0468569') // IMDb format
      const result = useMediaSource(id)

      // Should trust query parameter
      expect(result.value.type).toBe(MediaSource.TMDB)
      expect(result.value.isTMDb).toBe(true)
      expect(result.value.isIMDb).toBe(false)
    })

    it('should prioritize query parameter over UUID format', () => {
      // Query says tmdb, but ID is UUID
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'tmdb' },
      } as any)

      const id = ref('550e8400-e29b-41d4-a716-446655440000')
      const result = useMediaSource(id)

      // Should trust query parameter
      expect(result.value.type).toBe(MediaSource.TMDB)
      expect(result.value.isTMDb).toBe(true)
      expect(result.value.isInternal).toBe(false)
    })
  })

  describe('Edge Cases', () => {
    it('should handle ID that starts with tt but has non-numeric suffix', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('tt123abc') // Not valid IMDb format
      const result = useMediaSource(id)

      // Should default to internal (not matching IMDb pattern)
      expect(result.value.isInternal).toBe(true)
    })

    it('should handle numeric string with leading zeros', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: {},
      } as any)

      const id = ref('00550')
      const result = useMediaSource(id)

      // Still numeric, should be TMDb
      expect(result.value.isTMDb).toBe(true)
    })

    it('should preserve original ID value in result', () => {
      vi.mocked(useRoute).mockReturnValue({
        query: { source: 'tmdb' },
      } as any)

      const originalId = '12345-special'
      const id = ref(originalId)
      const result = useMediaSource(id)

      expect(result.value.id).toBe(originalId)
    })
  })
})
