import { describe, it, expect, vi, afterEach } from 'vitest'
import { getAppVersion } from '@/utils'

describe('version', () => {
  describe('getAppVersion', () => {
    const originalEnv = import.meta.env.VITE_APP_VERSION

    afterEach(() => {
      // Restore original value
      if (originalEnv !== undefined) {
        vi.stubEnv('VITE_APP_VERSION', originalEnv)
      }
    })

    it('returns version from environment variable when set', () => {
      vi.stubEnv('VITE_APP_VERSION', 'v1.2.3')

      const version = getAppVersion()

      expect(version).toBe('v1.2.3')
    })

    it('returns default version when environment variable is not set', () => {
      vi.stubEnv('VITE_APP_VERSION', '')

      const version = getAppVersion()

      expect(version).toBe('v0.0.1')
    })

    it('handles git tag format', () => {
      vi.stubEnv('VITE_APP_VERSION', 'v2.5.0-beta')

      const version = getAppVersion()

      expect(version).toBe('v2.5.0-beta')
    })

    it('handles semver without v prefix', () => {
      vi.stubEnv('VITE_APP_VERSION', '3.0.0')

      const version = getAppVersion()

      expect(version).toBe('3.0.0')
    })

    it('handles development versions', () => {
      vi.stubEnv('VITE_APP_VERSION', 'v1.0.0-dev.123')

      const version = getAppVersion()

      expect(version).toBe('v1.0.0-dev.123')
    })
  })
})
