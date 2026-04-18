/**
 * Common enums used throughout the application
 */

export enum MediaSource {
  INTERNAL = 'internal',
  JELLYFIN = 'jellyfin',
  TMDB = 'tmdb',
  IMDB = 'imdb',
}

export type MediaSourceType = `${MediaSource}`
export type SearchResultSource = MediaSourceType

export enum MediaType {
  MOVIE = 'movie',
  SERIES = 'series',
}

export type MediaTypeString = `${MediaType}`

/**
 * Operation/sync status states
 */
export enum OperationStatus {
  IDLE = 'idle',
  LOADING = 'loading',
  TESTING = 'testing',
  SUCCESS = 'success',
  ERROR = 'error',
}

/**
 * PrimeVue button severity variants
 */
export enum ButtonSeverity {
  PRIMARY = 'primary',
  SECONDARY = 'secondary',
  SUCCESS = 'success',
  INFO = 'info',
  WARNING = 'warning',
  HELP = 'help',
  DANGER = 'danger',
  CONTRAST = 'contrast',
}

/**
 * PrimeVue icon names (commonly used)
 */
export enum Icon {
  // Actions
  CHECK = 'pi pi-check',
  TIMES = 'pi pi-times',
  PLUS = 'pi pi-plus',
  MINUS = 'pi pi-minus',
  TRASH = 'pi pi-trash',
  PENCIL = 'pi pi-pencil',
  SAVE = 'pi pi-save',
  UNDO = 'pi pi-undo',

  // Navigation
  HOME = 'pi pi-home',
  ARROW_LEFT = 'pi pi-arrow-left',
  ARROW_RIGHT = 'pi pi-arrow-right',
  CHEVRON_LEFT = 'pi pi-chevron-left',
  CHEVRON_RIGHT = 'pi pi-chevron-right',
  CHEVRON_DOWN = 'pi pi-chevron-down',
  CHEVRON_UP = 'pi pi-chevron-up',
  EXTERNAL_LINK = 'pi pi-external-link',

  // Status
  INFO_CIRCLE = 'pi pi-info-circle',
  EXCLAMATION_CIRCLE = 'pi pi-exclamation-circle',
  EXCLAMATION_TRIANGLE = 'pi pi-exclamation-triangle',
  CHECK_CIRCLE = 'pi pi-check-circle',
  TIMES_CIRCLE = 'pi pi-times-circle',

  // Loading
  SPINNER = 'pi pi-spinner',
  SPIN = 'pi pi-spin',

  // Media
  STAR = 'pi pi-star',
  FILM = 'pi pi-video',
  TV = 'pi pi-desktop',
  IMAGE = 'pi pi-image',
  FILE = 'pi pi-file',

  // UI Elements
  SEARCH = 'pi pi-search',
  FILTER = 'pi pi-filter',
  COG = 'pi pi-cog',
  USER = 'pi pi-user',
  USERS = 'pi pi-users',
  KEY = 'pi pi-key',
  LOCK = 'pi pi-lock',
  UNLOCK = 'pi pi-unlock',
  EYE = 'pi pi-eye',
  EYE_SLASH = 'pi pi-eye-slash',

  // Data
  DATABASE = 'pi pi-database',
  CLOUD = 'pi pi-cloud',
  DOWNLOAD = 'pi pi-download',
  UPLOAD = 'pi pi-upload',
  REFRESH = 'pi pi-refresh',
  SYNC = 'pi pi-sync',

  // Layout
  BARS = 'pi pi-bars',
  ELLIPSIS_V = 'pi pi-ellipsis-v',
  ELLIPSIS_H = 'pi pi-ellipsis-h',
  TH_LARGE = 'pi pi-th-large',
  LIST = 'pi pi-list',

  // Misc
  CALENDAR = 'pi pi-calendar',
  CLOCK = 'pi pi-clock',
  BOOKMARK = 'pi pi-bookmark',
  TAG = 'pi pi-tag',
  TAGS = 'pi pi-tags',
  QUESTION_CIRCLE = 'pi pi-question-circle',
  BOOK = 'pi pi-book',
  FILE_EXPORT = 'pi pi-file-export',
  FILE_EXCEL = 'pi pi-file-excel',
  COMMENTS = 'pi pi-comments',
  SEND = 'pi pi-send',
  COPY = 'pi pi-copy',
  BRIEFCASE = 'pi pi-briefcase',
  HISTORY = 'pi pi-history',
}

/**
 * Type guard to check if a string is a valid OperationStatus
 */
export function isOperationStatus(value: string): value is OperationStatus {
  return Object.values(OperationStatus).includes(value as OperationStatus)
}

/**
 * Type guard to check if a string is a valid ButtonSeverity
 */
export function isButtonSeverity(value: string): value is ButtonSeverity {
  return Object.values(ButtonSeverity).includes(value as ButtonSeverity)
}
