/// <reference types="vite/client" />

/**
 * TypeScript environment variables declarations
 *
 * These types provide autocomplete and type safety for environment variables
 * used throughout the application.
 */

interface ImportMetaEnv {
  /**
   * Base URL for API requests (used in production builds)
   * @default 'http://127.0.0.1:8080'
   * @example 'http://localhost:8080' | 'https://api.rattatarr.com'
   */
  readonly VITE_API_BASE_URL?: string

  /**
   * Backend server for Vite proxy
   * @default 'rattatarr.server:8080'
   * @example '127.0.0.1:8080' | 'localhost:8080'
   */
  readonly VITE_BASE_SERVER_URL?: string

  /**
   * Development server port
   * @default 3000
   */
  readonly VITE_PORT?: string

  /**
   * Application version (injected at build time from git tag or package.json)
   * @default '0.0.1'
   */
  readonly VITE_APP_VERSION?: string

  /**
   * Application mode (development, production, test)
   */
  readonly MODE: string

  /**
   * Whether the app is running in development mode
   */
  readonly DEV: boolean

  /**
   * Whether the app is running in production mode
   */
  readonly PROD: boolean

  /**
   * Whether the app is running in SSR mode
   */
  readonly SSR: boolean

  /**
   * Base URL for the application (from vite.config.ts)
   */
  readonly BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
