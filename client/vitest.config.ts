import { fileURLToPath, URL } from 'node:url'
import { defineConfig, configDefaults } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@/types': fileURLToPath(new URL('./src/schemas/types', import.meta.url)),
    },
  },

  test: {
    // Use happy-dom as the browser environment (faster than jsdom)
    environment: 'happy-dom',

    // Exclude files from coverage and testing
    exclude: [...configDefaults.exclude, 'e2e/**'],

    // Setup files to run before tests
    setupFiles: ['./src/test-utils/setup.ts'],

    // Root directory for tests
    root: fileURLToPath(new URL('./', import.meta.url)),

    // Reporters for test output
    reporters: ['default', 'junit'],

    // JUnit output for CI/CD
    outputFile: {
      junit: './test-results/junit.xml',
    },

    // Coverage configuration
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test-utils/',
        '**/*.d.ts',
        '**/*.config.*',
        '**/mockData/',
        'dist/',
        '**/__tests__/**',
        // Auto-generated schema
        'src/schemas/schema.d.ts',
      ],
      // Coverage thresholds
      thresholds: {
        lines: 70,
        functions: 70,
        branches: 70,
        statements: 70,
      },
    },

    // Global test timeout (ms)
    testTimeout: 10000,

    // Globals - enable Jest-like globals (describe, it, expect)
    globals: true,
  },
})
