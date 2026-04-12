import { describe, it, expect, vi, beforeEach } from 'vitest'
import { computed, ref } from 'vue'
import { useProfileManagement } from '@/composables'
import type { Profile } from '@/types'

// Mock data
const mockProfilesData = {
  profiles: [
    { id: '1', name: 'Profile 1' },
    { id: '2', name: 'Profile 2' },
  ] as Profile[],
}

// Mock functions
const mockSetProfile = vi.fn()
const mockGetSelectedProfile = vi.fn()

// Mock modules
vi.mock('@/queries', () => ({
  useProfiles: vi.fn(() => ({
    data: computed(() => mockProfilesData),
    isLoading: ref(false),
    isError: ref(false),
    error: ref(null),
  })),
}))

vi.mock('@/stores', () => ({
  useProfileStore: vi.fn(() => ({
    selectedProfileId: '1',
    setProfile: mockSetProfile,
    getSelectedProfile: mockGetSelectedProfile,
  })),
}))

vi.mock('../useQueryError', () => ({
  useQueryError: vi.fn(),
}))

describe('useProfileManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns profile list from query', () => {
    mockGetSelectedProfile.mockReturnValue({ id: '1', name: 'Profile 1' })

    const { profileList } = useProfileManagement()

    expect(profileList.value).toEqual([
      { id: '1', name: 'Profile 1' },
      { id: '2', name: 'Profile 2' },
    ])
  })

  it('calls getSelectedProfile when accessing selectedProfile', () => {
    mockGetSelectedProfile.mockReturnValue({ id: '1', name: 'Profile 1' })

    const { selectedProfile } = useProfileManagement()

    // Access the computed property to trigger the getter
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const _ = selectedProfile.value

    expect(mockGetSelectedProfile).toHaveBeenCalled()
  })

  it('updates store when selectedProfile is set', () => {
    mockGetSelectedProfile.mockReturnValue({ id: '1', name: 'Profile 1' })

    const { selectedProfile } = useProfileManagement()

    const newProfile = { id: '2', name: 'Profile 2' } as Profile
    selectedProfile.value = newProfile

    expect(mockSetProfile).toHaveBeenCalledWith('2')
  })

  it('clears profile when set to null', () => {
    mockGetSelectedProfile.mockReturnValue(null)

    const { selectedProfile } = useProfileManagement()

    selectedProfile.value = null

    expect(mockSetProfile).toHaveBeenCalledWith(null)
  })

  it('handles profile change event', () => {
    mockGetSelectedProfile.mockReturnValue({ id: '1', name: 'Profile 1' })

    const { onProfileChange } = useProfileManagement()

    const event = {
      value: { id: '2', name: 'Profile 2' } as Profile,
    } as any

    onProfileChange(event)

    expect(mockSetProfile).toHaveBeenCalledWith('2')
  })

  it('handles profile change event with null value', () => {
    mockGetSelectedProfile.mockReturnValue(null)

    const { onProfileChange } = useProfileManagement()

    const event = {
      value: null,
    } as any

    onProfileChange(event)

    expect(mockSetProfile).toHaveBeenCalledWith(null)
  })
})
