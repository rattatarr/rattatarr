import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useQueryError } from '@/composables'
import { APIError } from '@/api/client'

// Mock useToast
const mockToastError = vi.fn()
vi.mock('../useToast', () => ({
  useToast: () => ({
    error: mockToastError,
  }),
}))

describe('useQueryError', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows toast when error occurs', async () => {
    const isError = ref(false)
    const error = ref<Error | null>(null)

    useQueryError(isError, error, {
      message: 'Failed to load data',
    })

    // Trigger error
    error.value = new Error('Network error')
    isError.value = true

    await vi.waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith(
        expect.any(Error),
        expect.objectContaining({
          fallbackMessage: 'Failed to load data',
        }),
      )
    })
  })

  it('does not show toast when showToast is false', async () => {
    const isError = ref(false)
    const error = ref<Error | null>(null)

    useQueryError(isError, error, {
      message: 'Failed to load data',
      showToast: false,
    })

    // Trigger error
    error.value = new Error('Network error')
    isError.value = true

    await new Promise((resolve) => setTimeout(resolve, 50))

    expect(mockToastError).not.toHaveBeenCalled()
  })

  it('calls custom onError callback when provided', async () => {
    const onErrorCallback = vi.fn()
    const isError = ref(false)
    const error = ref<Error | null>(null)

    useQueryError(isError, error, {
      message: 'Failed to load data',
      onError: onErrorCallback,
    })

    const testError = new Error('Network error')
    error.value = testError
    isError.value = true

    await vi.waitFor(() => {
      expect(onErrorCallback).toHaveBeenCalledWith(testError)
    })
  })

  it('uses default message when no custom message provided', async () => {
    const isError = ref(false)
    const error = ref<Error | null>(null)

    useQueryError(isError, error)

    error.value = new Error('Network error')
    isError.value = true

    await vi.waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith(
        expect.any(Error),
        expect.objectContaining({
          fallbackMessage: 'Failed to load data',
        }),
      )
    })
  })

  it('handles APIError correctly', async () => {
    const isError = ref(false)
    const error = ref<Error | null>(null)

    useQueryError(isError, error, {
      message: 'Failed to load movies',
    })

    const apiError = new APIError({
      status: 500,
      statusText: 'Internal Server Error',
      message: 'Database error',
      url: '/api/movies',
      body: { message: 'Database error' },
    })

    error.value = apiError
    isError.value = true

    await vi.waitFor(() => {
      expect(mockToastError).toHaveBeenCalledWith(
        apiError,
        expect.objectContaining({
          fallbackMessage: 'Failed to load movies',
        }),
      )
    })
  })

  it('triggers immediately on mount if error already exists', () => {
    const isError = ref(true)
    const error = ref<Error | null>(new Error('Existing error'))

    useQueryError(isError, error, {
      message: 'Failed to load',
    })

    expect(mockToastError).toHaveBeenCalledTimes(1)
  })

  it('does not trigger when error is cleared', async () => {
    const isError = ref(true)
    const error = ref<Error | null>(new Error('Initial error'))

    useQueryError(isError, error, {
      message: 'Failed to load',
    })

    expect(mockToastError).toHaveBeenCalledTimes(1)

    // Clear error
    isError.value = false
    error.value = null

    await new Promise((resolve) => setTimeout(resolve, 50))

    // Should still be called only once (from initial state)
    expect(mockToastError).toHaveBeenCalledTimes(1)
  })

  it('triggers again when error changes to a new error', async () => {
    const isError = ref(false)
    const error = ref<Error | null>(null)

    useQueryError(isError, error, {
      message: 'Failed to load',
    })

    // First error
    error.value = new Error('First error')
    isError.value = true

    await vi.waitFor(() => {
      expect(mockToastError).toHaveBeenCalledTimes(1)
    })

    // Second error
    error.value = new Error('Second error')

    await vi.waitFor(() => {
      expect(mockToastError).toHaveBeenCalledTimes(2)
    })
  })
})
