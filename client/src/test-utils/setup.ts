/**
 * Vitest Setup File
 *
 * Runs before all tests to set up the testing environment.
 */

import { beforeEach } from 'vitest'

// Reset happy-dom before each test
beforeEach(() => {
  // Clear localStorage
  localStorage.clear()

  // Clear sessionStorage
  sessionStorage.clear()

  // Reset document body
  document.body.innerHTML = ''
})
