import { describe, it, expect, vi } from 'vitest'
import { useNavigation } from '@/composables'
import { useRouter } from 'vue-router'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: vi.fn(),
}))

describe('useNavigation', () => {
  it('should provide goHome function', () => {
    const mockPush = vi.fn()
    vi.mocked(useRouter).mockReturnValue({
      push: mockPush,
    } as any)

    const { goHome } = useNavigation()

    expect(typeof goHome).toBe('function')
  })

  it('should navigate to home using router.push', () => {
    const mockPush = vi.fn()
    vi.mocked(useRouter).mockReturnValue({
      push: mockPush,
    } as any)

    const { goHome } = useNavigation()

    goHome()

    expect(mockPush).toHaveBeenCalledWith('/')
  })

  it('should provide goToHowTo function', () => {
    const mockPush = vi.fn()
    vi.mocked(useRouter).mockReturnValue({
      push: mockPush,
    } as any)

    const { goToHowTo } = useNavigation()

    expect(typeof goToHowTo).toBe('function')
  })

  it('should navigate to how-to page', () => {
    const mockPush = vi.fn()
    vi.mocked(useRouter).mockReturnValue({
      push: mockPush,
    } as any)

    const { goToHowTo } = useNavigation()

    goToHowTo()

    expect(mockPush).toHaveBeenCalledWith({ name: 'howto' })
  })
})
