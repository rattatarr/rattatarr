import { fileURLToPath, URL } from 'node:url'
import { execSync } from 'node:child_process'
import { readFileSync } from 'node:fs'
import { defineConfig, loadEnv } from 'vite'
import type { ConfigEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

function getAppVersion(): string {
  try {
    // Try to get the latest git tag
    const gitTag = execSync('git describe --tags --abbrev=0', {
      encoding: 'utf8',
      stdio: ['pipe', 'pipe', 'ignore'],
    }).trim()

    if (gitTag) {
      return gitTag
    }
  } catch {
    // Git tag not available, fall through to package.json
  }

  try {
    // Fall back to package.json version
    const packageJson = JSON.parse(readFileSync('./package.json', 'utf8'))
    if (packageJson.version && packageJson.version !== '0.0.0') {
      return `v${packageJson.version}`
    }
  } catch {
    // package.json not readable, fall through to default
  }

  return 'v0.0.1'
}

export default defineConfig(({ mode }: ConfigEnv) => {
  const isProduction = mode === 'production'
  const env = loadEnv(mode, process.cwd(), 'VITE_')
  const baseServerURL = env.VITE_BASE_SERVER_URL || 'rattatarr.server:8080'
  const port = Number(env.VITE_PORT) || 3000
  const appVersion = getAppVersion()

  return {
    plugins: [vue(), ...(!isProduction ? [vueDevTools()] : [])],
    define: {
      // Inject version at build time
      'import.meta.env.VITE_APP_VERSION': JSON.stringify(appVersion),
      // sockjs-client uses Node.js `global` — polyfill for browser
      global: 'globalThis',
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '@/types': fileURLToPath(new URL('./src/schemas/types', import.meta.url)),
      },
    },
    server: {
      proxy: {
        '/api': {
          target: `http://${baseServerURL}`,
          changeOrigin: true,
        },
        '/ws': {
          target: `http://${baseServerURL}`,
          changeOrigin: true,
          ws: true,
        },
      },
      host: '0.0.0.0',
      strictPort: true,
      port,
    },
  }
})
