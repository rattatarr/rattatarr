import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProfileStore } from '../profileStore'
import { createMockProfile } from '@/test-utils'

describe('Profile Store', () => {
  beforeEach(() => {
    // Create a fresh pinia instance before each test
    setActivePinia(createPinia())

    // Clear localStorage
    localStorage.clear()
  })

  describe('State', () => {
    it('should initialize with null selectedProfileId', () => {
      const store = useProfileStore()

      expect(store.selectedProfileId).toBeNull()
    })

    it('should update selectedProfileId when setProfile is called', () => {
      const store = useProfileStore()

      store.setProfile('profile-123')

      expect(store.selectedProfileId).toBe('profile-123')
    })
  })

  describe('Computed: hasSelectedProfile', () => {
    it('should return false when no profile is selected', () => {
      const store = useProfileStore()

      expect(store.hasSelectedProfile).toBe(false)
    })

    it('should return true when a profile is selected', () => {
      const store = useProfileStore()

      store.setProfile('profile-123')

      expect(store.hasSelectedProfile).toBe(true)
    })

    it('should return false when profile is set to null', () => {
      const store = useProfileStore()

      store.setProfile('profile-123')
      store.setProfile(null)

      expect(store.hasSelectedProfile).toBe(false)
    })
  })

  describe('Action: setProfile', () => {
    it('should set the selected profile ID', () => {
      const store = useProfileStore()

      store.setProfile('profile-789')

      expect(store.selectedProfileId).toBe('profile-789')
    })

    it('should allow changing profiles multiple times', () => {
      const store = useProfileStore()

      store.setProfile('profile-111')
      expect(store.selectedProfileId).toBe('profile-111')

      store.setProfile('profile-222')
      expect(store.selectedProfileId).toBe('profile-222')
    })

    it('should allow setting profile to null', () => {
      const store = useProfileStore()

      store.setProfile('profile-123')
      store.setProfile(null)

      expect(store.selectedProfileId).toBeNull()
    })
  })

  describe('Action: getSelectedProfile', () => {
    it('should return undefined when no profile is selected', () => {
      const store = useProfileStore()
      const profiles = [createMockProfile({ id: '1' }), createMockProfile({ id: '2' })]

      const result = store.getSelectedProfile(profiles)

      expect(result).toBeUndefined()
    })

    it('should return the selected profile from the list', () => {
      const store = useProfileStore()
      const profiles = [
        createMockProfile({ id: '1', name: 'Profile 1' }),
        createMockProfile({ id: '2', name: 'Profile 2' }),
        createMockProfile({ id: '3', name: 'Profile 3' }),
      ]

      store.setProfile('2')

      const result = store.getSelectedProfile(profiles)

      expect(result).toEqual({
        id: '2',
        name: 'Profile 2',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
      })
    })

    it('should return undefined when selected profile is not in the list', () => {
      const store = useProfileStore()
      const profiles = [createMockProfile({ id: '1' }), createMockProfile({ id: '2' })]

      store.setProfile('non-existent-id')

      const result = store.getSelectedProfile(profiles)

      expect(result).toBeUndefined()
    })

    it('should return undefined for empty profile list', () => {
      const store = useProfileStore()

      store.setProfile('1')

      const result = store.getSelectedProfile([])

      expect(result).toBeUndefined()
    })
  })

  describe('State Management', () => {
    it('should reactively update hasSelectedProfile', () => {
      const store = useProfileStore()

      expect(store.hasSelectedProfile).toBe(false)

      store.setProfile('profile-x')
      expect(store.hasSelectedProfile).toBe(true)

      store.setProfile(null)
      expect(store.hasSelectedProfile).toBe(false)
    })

    it('should handle rapid profile changes', () => {
      const store = useProfileStore()

      for (let i = 0; i < 10; i++) {
        store.setProfile(`profile-${i}`)
        expect(store.selectedProfileId).toBe(`profile-${i}`)
      }
    })
  })
})
