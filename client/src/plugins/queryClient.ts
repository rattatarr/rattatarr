import { QueryClient } from '@tanstack/vue-query'
import { APIError } from '@/api/client'
import { toast } from 'vue-sonner'

/**
 * Global query client configuration
 *
 * Provides default error handling for all TanStack Query operations
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Global query defaults
      staleTime: 0,
      gcTime: 5 * 60 * 1000, // 5 minutes
      retry: (failureCount, error) => {
        // Don't retry client errors (4xx)
        if (error instanceof APIError && error.isClientError()) {
          return false
        }
        // Retry server errors up to 3 times
        return failureCount < 3
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000), // Exponential backoff up to 30 seconds

      // Note: TanStack Query v5 removed global onError for queries
      // Handle query errors in components using isError and error states
      // See: https://tanstack.com/query/latest/docs/framework/vue/guides/migrating-to-v5#removed-global-callbacks-for-queries
    },
    mutations: {
      // Global mutation error handling
      onError: (error) => {
        handleMutationError(error)
      },
    },
  },
})

/**
 * Handle mutation errors globally
 * Can be overridden by individual mutation's onError
 */
function handleMutationError(error: unknown): void {
  if (error instanceof APIError) {
    const message = error.getUserMessage()
    const description = getErrorDescription(error)

    toast.error(message, {
      description,
      duration: 5000,
      closeButton: true,
    })

    // Log for debugging
    console.error('Mutation error:', {
      status: error.status,
      url: error.url,
      body: error.body,
    })
  } else if (error instanceof Error) {
    toast.error(error.message || 'An error occurred', {
      duration: 5000,
      closeButton: true,
    })

    console.error('Mutation error:', error)
  } else {
    toast.error('An unexpected error occurred', {
      description: 'Please try again or contact support',
      duration: 5000,
      closeButton: true,
    })

    console.error('Unknown mutation error:', error)
  }
}

/**
 * Extract error description from APIError
 */
function getErrorDescription(err: APIError): string | undefined {
  if (err.body && typeof err.body === 'object') {
    const body = err.body as Record<string, unknown>

    // Spring Boot error response
    if (body.message && typeof body.message === 'string') {
      return body.message
    }

    // Generic detail field
    if (body.detail && typeof body.detail === 'string') {
      return body.detail
    }

    // Validation errors array
    if (Array.isArray(body.errors) && body.errors.length > 0) {
      return body.errors.map((e: unknown) => String(e)).join(', ')
    }

    // Field-level validation errors
    if (body.fieldErrors && typeof body.fieldErrors === 'object') {
      const fieldErrors = body.fieldErrors as Record<string, unknown>
      return Object.entries(fieldErrors)
        .map(([field, message]) => `${field}: ${String(message)}`)
        .join(', ')
    }
  }

  if (err.isClientError()) {
    return `Request error (${err.status})`
  } else if (err.isServerError()) {
    return 'Server error - please try again later'
  }

  return undefined
}
