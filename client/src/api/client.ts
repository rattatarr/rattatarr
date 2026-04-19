import createClient from 'openapi-fetch'
import type { paths } from '@/schemas/schema'

/**
 * Base API client using openapi-fetch
 *
 * Type-safe client generated from backend OpenAPI schema.
 * All request/response types are automatically inferred.
 */
export const apiClient = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_BASE_URL || '/',
})

/**
 * Typed API error class
 *
 * Wraps HTTP errors with structured information for better error handling.
 */
export class APIError extends Error {
  /** HTTP status code (e.g., 404, 500) */
  readonly status: number

  /** HTTP status text (e.g., "Not Found", "Internal Server Error") */
  readonly statusText: string

  /** Response body (may contain backend error details) */
  readonly body?: unknown

  /** Request URL that failed */
  readonly url?: string

  constructor(options: {
    status: number
    statusText: string
    message: string
    body?: unknown
    url?: string
  }) {
    super(options.message)
    this.name = 'APIError'
    this.status = options.status
    this.statusText = options.statusText
    this.body = options.body
    this.url = options.url

    // Maintains proper stack trace for where error was thrown (only available on V8)
    if ('captureStackTrace' in Error) {
      ;(
        Error as { captureStackTrace(target: object, constructor: typeof APIError): void }
      ).captureStackTrace(this, APIError)
    }
  }

  /**
   * Check if this is a client error (4xx)
   */
  isClientError(): boolean {
    return this.status >= 400 && this.status < 500
  }

  /**
   * Check if this is a server error (5xx)
   */
  isServerError(): boolean {
    return this.status >= 500
  }

  /**
   * Check if this is a specific status code
   */
  isStatus(code: number): boolean {
    return this.status === code
  }

  /**
   * Get a user-friendly error message
   */
  getUserMessage(): string {
    // Try to extract message from backend response
    if (this.body && typeof this.body === 'object' && 'message' in this.body) {
      return String(this.body.message)
    }

    // Fallback to HTTP status text
    return this.statusText
  }
}

/**
 * Helper to handle API responses consistently
 *
 * Throws APIError if response has error field, otherwise returns data
 */
export async function handleResponse<T>(response: {
  data?: T
  error?: {
    status?: number
    statusText?: string
    body?: unknown
    url?: string
  }
  response?: Response
}): Promise<T> {
  if (response.error) {
    // openapi-fetch puts the parsed JSON response body directly in response.error,
    // not in response.error.body — fall back to the whole error object as body.
    const rawError = response.error as Record<string, unknown>
    const body: unknown = rawError['body'] !== undefined ? rawError['body'] : response.error
    const status =
      response.response?.status ??
      (typeof rawError['status'] === 'number' ? rawError['status'] : 500)
    const statusText =
      response.response?.statusText ?? String(rawError['statusText'] ?? 'Unknown Error')
    const url = response.response?.url ?? String(rawError['url'] ?? '')

    // Extract message from error body or use status text
    let message = statusText
    if (body && typeof body === 'object' && 'message' in body) {
      message = String((body as Record<string, unknown>)['message'])
    }

    throw new APIError({
      status,
      statusText,
      message,
      body,
      url,
    })
  }

  if (!response.data) {
    throw new APIError({
      status: 500,
      statusText: 'No Data',
      message: 'No data in response',
    })
  }

  return response.data
}
