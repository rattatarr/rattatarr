import { toast, type ExternalToast } from 'vue-sonner'
import { APIError } from '@/api/client'

const DEFAULT_TOAST_OPTIONS: ExternalToast = {
  closeButton: true,
  duration: 4000,
}

const ERROR_TOAST_OPTIONS: ExternalToast = {
  ...DEFAULT_TOAST_OPTIONS,
  duration: 5000,
}

/**
 * Extract user-friendly description from APIError
 */
function getErrorDescription(err: APIError): string | undefined {
  // If backend provided a description, use it
  if (err.body && typeof err.body === 'object' && 'description' in err.body) {
    return String(err.body.description)
  }

  // If backend provided details, use them
  if (err.body && typeof err.body === 'object' && 'details' in err.body) {
    return String(err.body.details)
  }

  // Fallback to status text if available
  if (err.statusText && err.statusText !== 'Unknown') {
    return err.statusText
  }

  return undefined
}

/**
 * Toast notification composable with APIError handling
 *
 * Provides consistent toast notifications across the application,
 * with special handling for API errors from the backend.
 */
export function useToast() {
  /**
   * Display error toast with proper error extraction
   *
   * Handles:
   * - APIError instances (extracts user message and details)
   * - Standard Error objects
   * - String messages
   * - Unknown error types
   *
   * @param err - The error to display, can be of various types
   * @param options - Can be a string (fallback message) or object with fallbackMessage and dismissLoadingToast
   */
  function error(
    err: unknown,
    options?: string | { fallbackMessage?: string; dismissLoadingToast?: string | number },
  ): void {
    const opts = typeof options === 'string' ? { fallbackMessage: options } : options
    const fallbackMessage = opts?.fallbackMessage ?? 'An error occurred'

    if (opts?.dismissLoadingToast !== undefined) {
      toast.dismiss(opts.dismissLoadingToast)
    }

    if (err instanceof APIError) {
      // Extract user-friendly message from APIError
      const message = err.getUserMessage()
      const description = getErrorDescription(err)

      toast.error(message, {
        ...ERROR_TOAST_OPTIONS,
        description,
      })

      // Log full error for debugging
      console.error('APIError:', {
        status: err.status,
        statusText: err.statusText,
        url: err.url,
        body: err.body,
      })
    } else if (err instanceof Error) {
      // Standard JavaScript Error
      toast.error(err.message || fallbackMessage, ERROR_TOAST_OPTIONS)
      console.error('Error:', err)
    } else if (typeof err === 'string') {
      // String error message
      toast.error(err, ERROR_TOAST_OPTIONS)
    } else {
      // Unknown error type
      toast.error(fallbackMessage, {
        ...ERROR_TOAST_OPTIONS,
        description: 'Please try again or contact support if the issue persists.',
      })
      console.error('Unknown error:', err)
    }
  }

  /**
   * Display success toast
   *
   * @param message - Main message to display
   * @param options - Can be a string (description) or object with description and dismissLoadingToast
   */
  function success(
    message: string,
    options?: string | { description?: string; dismissLoadingToast?: string | number },
  ): void {
    const opts = typeof options === 'string' ? { description: options } : options

    if (opts?.dismissLoadingToast !== undefined) {
      toast.dismiss(opts.dismissLoadingToast)
    }
    toast.success(message, {
      ...DEFAULT_TOAST_OPTIONS,
      description: opts?.description,
    })
  }

  /**
   * Display info toast
   *
   * @param message - Main message to display
   * @param options - Can be a string (description) or object with description and dismissLoadingToast
   */
  function info(
    message: string,
    options?: string | { description?: string; dismissLoadingToast?: string | number },
  ): void {
    const opts = typeof options === 'string' ? { description: options } : options

    if (opts?.dismissLoadingToast !== undefined) {
      toast.dismiss(opts.dismissLoadingToast)
    }
    toast.info(message, {
      ...DEFAULT_TOAST_OPTIONS,
      description: opts?.description,
    })
  }

  /**
   * Display warning toast
   *
   * @param message - Main message to display
   * @param options - Can be a string (description) or object with description and dismissLoadingToast
   */
  function warning(
    message: string,
    options?: string | { description?: string; dismissLoadingToast?: string | number },
  ): void {
    const opts = typeof options === 'string' ? { description: options } : options

    if (opts?.dismissLoadingToast !== undefined) {
      toast.dismiss(opts.dismissLoadingToast)
    }
    toast.warning(message, {
      ...DEFAULT_TOAST_OPTIONS,
      description: opts?.description,
    })
  }

  /**
   * Display loading toast and return dismiss function
   */
  function loading(message: string, description?: string) {
    return toast.loading(message, {
      description,
    })
  }

  /**
   * Display promise toast that updates based on promise state
   */
  function promise<T>(
    promise: Promise<T> | (() => Promise<T>),
    options: {
      loading: string
      success: string | ((data: T) => string)
      error: string | ((err: unknown) => string)
      description?: string | ((data: T) => string) | ((err: unknown) => string)
    },
  ) {
    return toast.promise(promise, options)
  }

  /**
   * Dismiss a specific toast by ID
   */
  function dismiss(toastId?: string | number) {
    return toast.dismiss(toastId)
  }

  return {
    error,
    success,
    info,
    warning,
    loading,
    promise,
    dismiss,
  }
}
