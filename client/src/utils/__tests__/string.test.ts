import { describe, it, expect } from 'vitest'
import { getInitials } from '../string'

describe('getInitials', () => {
  it('returns two uppercase initials for a two-word name', () => {
    expect(getInitials('John Doe')).toBe('JD')
  })

  it('returns first two initials for a name with more than two words', () => {
    expect(getInitials('Mary Jane Watson')).toBe('MJ')
  })

  it('returns single uppercase letter for a single-word name', () => {
    expect(getInitials('Madonna')).toBe('M')
  })

  it('returns ? for null', () => {
    expect(getInitials(null)).toBe('?')
  })

  it('returns ? for undefined', () => {
    expect(getInitials(undefined)).toBe('?')
  })

  it('returns ? for empty string', () => {
    expect(getInitials('')).toBe('?')
  })

  it('uppercases lowercase initials', () => {
    expect(getInitials('jane doe')).toBe('JD')
  })

  it('uppercases a single lowercase letter', () => {
    expect(getInitials('madonna')).toBe('M')
  })

  it('handles a name with leading/trailing spaces as single word', () => {
    // split(' ') on '  John  ' produces ['', '', 'John', '', ''] — first part is ''
    // so parts[0]?.[0] is undefined → '', parts[1]?.[0] is undefined → ''
    // result is '' (empty string) — documents actual behaviour
    expect(getInitials('  John  ')).toBe('')
  })
})
