import { describe, it, expect } from 'vitest'
import { APIError, handleResponse } from '@/api'

describe('APIError', () => {
  it('should create an error with all properties', () => {
    const error = new APIError({
      status: 404,
      statusText: 'Not Found',
      message: 'Resource not found',
      body: { detail: 'User not found' },
      url: '/api/users/123',
    })

    expect(error.name).toBe('APIError')
    expect(error.status).toBe(404)
    expect(error.statusText).toBe('Not Found')
    expect(error.message).toBe('Resource not found')
    expect(error.body).toEqual({ detail: 'User not found' })
    expect(error.url).toBe('/api/users/123')
  })

  it('should identify client errors (4xx)', () => {
    const error = new APIError({
      status: 404,
      statusText: 'Not Found',
      message: 'Not found',
    })

    expect(error.isClientError()).toBe(true)
    expect(error.isServerError()).toBe(false)
  })

  it('should identify server errors (5xx)', () => {
    const error = new APIError({
      status: 500,
      statusText: 'Internal Server Error',
      message: 'Server error',
    })

    expect(error.isClientError()).toBe(false)
    expect(error.isServerError()).toBe(true)
  })

  it('should check for specific status code', () => {
    const error = new APIError({
      status: 401,
      statusText: 'Unauthorized',
      message: 'Unauthorized',
    })

    expect(error.isStatus(401)).toBe(true)
    expect(error.isStatus(404)).toBe(false)
  })

  it('should extract user message from body', () => {
    const error = new APIError({
      status: 400,
      statusText: 'Bad Request',
      message: 'Bad Request',
      body: { message: 'Invalid input data' },
    })

    expect(error.getUserMessage()).toBe('Invalid input data')
  })

  it('should fallback to statusText when no body message', () => {
    const error = new APIError({
      status: 404,
      statusText: 'Not Found',
      message: 'Not Found',
    })

    expect(error.getUserMessage()).toBe('Not Found')
  })
})

describe('handleResponse', () => {
  it('should return data when response is successful', async () => {
    const response = {
      data: { id: '1', name: 'Test' },
    }

    const result = await handleResponse(response)

    expect(result).toEqual({ id: '1', name: 'Test' })
  })

  it('should throw APIError when response has error field', async () => {
    const response = {
      error: {
        status: 404,
        statusText: 'Not Found',
        body: { message: 'User not found' },
        url: '/api/users/123',
      },
    }

    await expect(handleResponse(response)).rejects.toThrow(APIError)
    await expect(handleResponse(response)).rejects.toMatchObject({
      status: 404,
      statusText: 'Not Found',
      message: 'User not found',
    })
  })

  it('should use default values when error info is missing', async () => {
    const response = {
      error: {},
    }

    await expect(handleResponse(response)).rejects.toMatchObject({
      status: 500,
      statusText: 'Unknown Error',
    })
  })

  it('should throw APIError when data is missing', async () => {
    const response = {}

    await expect(handleResponse(response)).rejects.toThrow(APIError)
    await expect(handleResponse(response)).rejects.toMatchObject({
      status: 500,
      statusText: 'No Data',
      message: 'No data in response',
    })
  })

  it('should extract message from error body', async () => {
    const response = {
      error: {
        status: 400,
        statusText: 'Bad Request',
        body: { message: 'Invalid email format' },
      },
    }

    await expect(handleResponse(response)).rejects.toMatchObject({
      status: 400,
      message: 'Invalid email format',
    })
  })

  it('should handle Response object in error', async () => {
    const mockResponse = new Response('Error', {
      status: 503,
      statusText: 'Service Unavailable',
    })

    const response = {
      error: {},
      response: mockResponse,
    }

    await expect(handleResponse(response)).rejects.toMatchObject({
      status: 503,
      statusText: 'Service Unavailable',
    })
  })
})
