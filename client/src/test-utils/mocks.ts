/**
 * Mock Data and Utilities
 *
 * Provides mock data for testing.
 */

import type { Profile } from '@/types'

/**
 * Create a mock profile for testing
 */
export function createMockProfile(overrides: Partial<Profile> = {}): Profile {
  return {
    id: '1',
    name: 'Test Profile',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
    ...overrides,
  }
}

/**
 * Create multiple mock profiles
 */
export function createMockProfiles(count: number): Profile[] {
  return Array.from({ length: count }, (_, i) =>
    createMockProfile({
      id: String(i + 1),
      name: `Profile ${i + 1}`,
    }),
  )
}
