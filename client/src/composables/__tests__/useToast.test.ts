import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useToast } from '../useToast'
import { APIError } from '@/api/client'
import { toast } from 'vue-sonner'

// Mock vue-sonner
vi.mock('vue-sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
    info: vi.fn(),
    warning: vi.fn(),
    loading: vi.fn(() => 'toast-id'),
    promise: vi.fn(),
    dismiss: vi.fn(),
  },
}))

describe('useToast', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('error()', () => {
    it('handles APIError with user message and description', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const apiError = new APIError({
        status: 400,
        statusText: 'Bad Request',
        message: 'Invalid input',
        url: '/api/test',
        body: {
          message: 'Invalid input',
          description: 'Email format is incorrect',
        },
      })

      error(apiError)

      expect(toast.error).toHaveBeenCalledWith(
        'Invalid input',
        expect.objectContaining({
          description: 'Email format is incorrect',
          closeButton: true,
          duration: 5000,
        }),
      )
      expect(consoleSpy).toHaveBeenCalled()

      consoleSpy.mockRestore()
    })

    it('handles APIError with details instead of description', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const apiError = new APIError({
        status: 500,
        statusText: 'Internal Server Error',
        message: 'Server error',
        url: '/api/test',
        body: {
          message: 'Server error',
          details: 'Database connection failed',
        },
      })

      error(apiError)

      expect(toast.error).toHaveBeenCalledWith(
        'Server error',
        expect.objectContaining({
          description: 'Database connection failed',
        }),
      )

      consoleSpy.mockRestore()
    })

    it('handles APIError with statusText fallback', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const apiError = new APIError({
        status: 404,
        statusText: 'Not Found',
        message: 'Resource not found',
        url: '/api/test',
        body: {
          message: 'Resource not found',
        },
      })

      error(apiError)

      expect(toast.error).toHaveBeenCalledWith(
        'Resource not found',
        expect.objectContaining({
          description: 'Not Found',
        }),
      )

      consoleSpy.mockRestore()
    })

    it('handles standard Error objects', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const err = new Error('Something went wrong')

      error(err)

      expect(toast.error).toHaveBeenCalledWith(
        'Something went wrong',
        expect.objectContaining({
          closeButton: true,
          duration: 5000,
        }),
      )
      expect(consoleSpy).toHaveBeenCalledWith('Error:', err)

      consoleSpy.mockRestore()
    })

    it('handles string errors', () => {
      const { error } = useToast()

      error('Something failed')

      expect(toast.error).toHaveBeenCalledWith(
        'Something failed',
        expect.objectContaining({
          closeButton: true,
          duration: 5000,
        }),
      )
    })

    it('handles unknown error types with fallback message', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      error({ unknown: 'object' })

      expect(toast.error).toHaveBeenCalledWith(
        'An error occurred',
        expect.objectContaining({
          description: 'Please try again or contact support if the issue persists.',
        }),
      )
      expect(consoleSpy).toHaveBeenCalled()

      consoleSpy.mockRestore()
    })

    it('uses custom fallback message when provided as string', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      error({ unknown: 'object' }, 'Custom fallback')

      expect(toast.error).toHaveBeenCalledWith(
        'Custom fallback',
        expect.objectContaining({
          description: 'Please try again or contact support if the issue persists.',
        }),
      )

      consoleSpy.mockRestore()
    })

    it('dismisses loading toast when provided', () => {
      const { error } = useToast()
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      error(new Error('Failed'), {
        fallbackMessage: 'Error occurred',
        dismissLoadingToast: 'loading-123',
      })

      expect(toast.dismiss).toHaveBeenCalledWith('loading-123')

      consoleSpy.mockRestore()
    })
  })

  describe('success()', () => {
    it('displays success toast with message', () => {
      const { success } = useToast()

      success('Operation successful')

      expect(toast.success).toHaveBeenCalledWith(
        'Operation successful',
        expect.objectContaining({
          closeButton: true,
          duration: 4000,
          description: undefined,
        }),
      )
    })

    it('displays success toast with description as string', () => {
      const { success } = useToast()

      success('Saved', 'Your changes have been saved')

      expect(toast.success).toHaveBeenCalledWith(
        'Saved',
        expect.objectContaining({
          description: 'Your changes have been saved',
        }),
      )
    })

    it('displays success toast with description as object', () => {
      const { success } = useToast()

      success('Saved', { description: 'Your changes have been saved' })

      expect(toast.success).toHaveBeenCalledWith(
        'Saved',
        expect.objectContaining({
          description: 'Your changes have been saved',
        }),
      )
    })

    it('dismisses loading toast when provided', () => {
      const { success } = useToast()

      success('Complete', { dismissLoadingToast: 'loading-456' })

      expect(toast.dismiss).toHaveBeenCalledWith('loading-456')
    })
  })

  describe('info()', () => {
    it('displays info toast', () => {
      const { info } = useToast()

      info('Information', 'Details here')

      expect(toast.info).toHaveBeenCalledWith(
        'Information',
        expect.objectContaining({
          description: 'Details here',
          closeButton: true,
          duration: 4000,
        }),
      )
    })
  })

  describe('warning()', () => {
    it('displays warning toast', () => {
      const { warning } = useToast()

      warning('Warning', 'Be careful')

      expect(toast.warning).toHaveBeenCalledWith(
        'Warning',
        expect.objectContaining({
          description: 'Be careful',
          closeButton: true,
          duration: 4000,
        }),
      )
    })
  })

  describe('loading()', () => {
    it('displays loading toast and returns ID', () => {
      const { loading } = useToast()

      const toastId = loading('Loading...', 'Please wait')

      expect(toast.loading).toHaveBeenCalledWith('Loading...', {
        description: 'Please wait',
      })
      expect(toastId).toBe('toast-id')
    })
  })

  describe('promise()', () => {
    it('displays promise toast', () => {
      const { promise } = useToast()
      const mockPromise = Promise.resolve('data')

      promise(mockPromise, {
        loading: 'Loading...',
        success: 'Success!',
        error: 'Failed!',
      })

      expect(toast.promise).toHaveBeenCalledWith(mockPromise, {
        loading: 'Loading...',
        success: 'Success!',
        error: 'Failed!',
      })
    })
  })

  describe('dismiss()', () => {
    it('dismisses toast by ID', () => {
      const { dismiss } = useToast()

      dismiss('toast-789')

      expect(toast.dismiss).toHaveBeenCalledWith('toast-789')
    })

    it('dismisses all toasts when no ID provided', () => {
      const { dismiss } = useToast()

      dismiss()

      expect(toast.dismiss).toHaveBeenCalledWith(undefined)
    })
  })
})
