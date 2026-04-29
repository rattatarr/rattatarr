import { describe, it, expect } from 'vitest'
import {
  JOB_TYPE,
  OperationStatus,
  ButtonSeverity,
  Icon,
  isOperationStatus,
  isButtonSeverity,
} from '../enums'

describe('JOB_TYPE', () => {
  it('values match schema string literals', () => {
    expect(JOB_TYPE.JELLYFIN_SYNC).toBe('JELLYFIN_SYNC')
    expect(JOB_TYPE.JELLYFIN_ACTIVITY_POLL).toBe('JELLYFIN_ACTIVITY_POLL')
    expect(JOB_TYPE.CSV_IMPORT).toBe('CSV_IMPORT')
    expect(JOB_TYPE.RADARR_IMPORT).toBe('RADARR_IMPORT')
    expect(JOB_TYPE.RADARR_RATINGS_REFRESH).toBe('RADARR_RATINGS_REFRESH')
    expect(JOB_TYPE.SONARR_IMPORT).toBe('SONARR_IMPORT')
  })

  it('has exactly 6 entries', () => {
    expect(Object.keys(JOB_TYPE)).toHaveLength(6)
  })

  it('all values are uppercase strings', () => {
    for (const value of Object.values(JOB_TYPE)) {
      expect(value).toBe(value.toUpperCase())
    }
  })
})

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
