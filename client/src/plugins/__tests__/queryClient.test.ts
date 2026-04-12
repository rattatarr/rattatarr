import { describe, it, expect } from 'vitest'
import { queryClient } from '../queryClient'

describe('queryClient', () => {
  it('creates query client instance', () => {
    expect(queryClient).toBeDefined()
    expect(queryClient).toHaveProperty('mount')
  })

  it('has default query options configured', () => {
    const options = queryClient.getDefaultOptions()
    expect(options.queries).toBeDefined()
  })
})
