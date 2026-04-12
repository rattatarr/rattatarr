import { defineStore } from 'pinia'
import { computed } from 'vue'
import { useLocalStorage } from '@vueuse/core'
import type { Profile } from '@/types'

/**
 * Profile Store - Manages the currently selected profile
 *
 * IMPORTANT: This store only manages UI state (which profile is selected).
 * Actual profile data is managed by TanStack Query via useProfiles() hook.
 */
export const useProfileStore = defineStore('profile', () => {
  // State: Currently selected profile ID (automatically synced with localStorage)
  const selectedProfileId = useLocalStorage<string | null>('rattatarr:selectedProfileId', null)

  // Computed: Is a profile selected?
  const hasSelectedProfile = computed(() => selectedProfileId.value !== null)

  // Action: Set the active profile
  function setProfile(profileId: string | null) {
    selectedProfileId.value = profileId
  }

  // Action: Get selected profile data (requires profile list from query)
  function getSelectedProfile(profiles: Profile[]): Profile | undefined {
    if (!selectedProfileId.value) return undefined
    return profiles.find((p) => p.id === selectedProfileId.value)
  }

  return {
    // State
    selectedProfileId,

    // Computed
    hasSelectedProfile,

    // Actions
    setProfile,
    getSelectedProfile,
  }
})
