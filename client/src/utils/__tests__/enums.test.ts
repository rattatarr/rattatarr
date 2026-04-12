import { describe, it, expect } from 'vitest'
import {
  OperationStatus,
  ButtonSeverity,
  Icon,
  isOperationStatus,
  isButtonSeverity,
} from '../enums'

describe('OperationStatus enum', () => {
  it('should have all expected values', () => {
    expect(OperationStatus.IDLE).toBe('idle')
    expect(OperationStatus.LOADING).toBe('loading')
    expect(OperationStatus.TESTING).toBe('testing')
    expect(OperationStatus.SUCCESS).toBe('success')
    expect(OperationStatus.ERROR).toBe('error')
  })
})

describe('ButtonSeverity enum', () => {
  it('should have all expected values', () => {
    expect(ButtonSeverity.PRIMARY).toBe('primary')
    expect(ButtonSeverity.SECONDARY).toBe('secondary')
    expect(ButtonSeverity.SUCCESS).toBe('success')
    expect(ButtonSeverity.INFO).toBe('info')
    expect(ButtonSeverity.WARNING).toBe('warning')
    expect(ButtonSeverity.HELP).toBe('help')
    expect(ButtonSeverity.DANGER).toBe('danger')
    expect(ButtonSeverity.CONTRAST).toBe('contrast')
  })
})

describe('Icon enum', () => {
  it('should have action icons', () => {
    expect(Icon.CHECK).toBe('pi pi-check')
    expect(Icon.TIMES).toBe('pi pi-times')
    expect(Icon.PLUS).toBe('pi pi-plus')
    expect(Icon.TRASH).toBe('pi pi-trash')
    expect(Icon.SAVE).toBe('pi pi-save')
  })

  it('should have navigation icons', () => {
    expect(Icon.HOME).toBe('pi pi-home')
    expect(Icon.CHEVRON_UP).toBe('pi pi-chevron-up')
    expect(Icon.CHEVRON_DOWN).toBe('pi pi-chevron-down')
  })

  it('should have status icons', () => {
    expect(Icon.INFO_CIRCLE).toBe('pi pi-info-circle')
    expect(Icon.CHECK_CIRCLE).toBe('pi pi-check-circle')
    expect(Icon.EXCLAMATION_TRIANGLE).toBe('pi pi-exclamation-triangle')
  })

  it('should have media icons', () => {
    expect(Icon.STAR).toBe('pi pi-star')
    expect(Icon.FILM).toBe('pi pi-video')
  })
})

describe('isOperationStatus', () => {
  it('should return true for valid operation statuses', () => {
    expect(isOperationStatus('idle')).toBe(true)
    expect(isOperationStatus('loading')).toBe(true)
    expect(isOperationStatus('testing')).toBe(true)
    expect(isOperationStatus('success')).toBe(true)
    expect(isOperationStatus('error')).toBe(true)
  })

  it('should return false for invalid values', () => {
    expect(isOperationStatus('invalid')).toBe(false)
    expect(isOperationStatus('pending')).toBe(false)
    expect(isOperationStatus('')).toBe(false)
    expect(isOperationStatus('IDLE')).toBe(false) // Case sensitive
  })
})

describe('isButtonSeverity', () => {
  it('should return true for valid button severities', () => {
    expect(isButtonSeverity('primary')).toBe(true)
    expect(isButtonSeverity('secondary')).toBe(true)
    expect(isButtonSeverity('success')).toBe(true)
    expect(isButtonSeverity('danger')).toBe(true)
  })

  it('should return false for invalid values', () => {
    expect(isButtonSeverity('invalid')).toBe(false)
    expect(isButtonSeverity('error')).toBe(false)
    expect(isButtonSeverity('')).toBe(false)
    expect(isButtonSeverity('PRIMARY')).toBe(false) // Case sensitive
  })
})
