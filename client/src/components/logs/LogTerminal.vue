<script setup lang="ts">
  interface Props {
    title?: string
    count: string
    showColumnHeaders?: boolean
  }

  withDefaults(defineProps<Props>(), {
    title: undefined,
    showColumnHeaders: false,
  })
</script>

<template>
  <div class="logs-terminal">
    <!-- Title Header (only if no column headers) -->
    <div v-if="!showColumnHeaders && title" class="terminal-header">
      <span class="terminal-title">{{ title }}</span>
      <span class="terminal-count">{{ count }}</span>
    </div>

    <!-- Column Headers with Count -->
    <div v-if="showColumnHeaders" class="terminal-column-headers">
      <span class="header-expand" />
      <span class="header-time">Timestamp</span>
      <span class="header-level">Level</span>
      <span class="header-request">Request ID</span>
      <span class="header-logger">Logger</span>
      <span class="header-message">Message</span>
      <span class="header-count">{{ count }}</span>
    </div>

    <div class="terminal-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
  .logs-terminal {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: var(--p-surface-800);
    border: 1px solid var(--p-surface-border);
    border-radius: var(--p-border-radius);
    overflow: hidden;
    min-height: 0;
    box-sizing: border-box;
  }

  .terminal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 1rem;
    background: var(--p-surface-700);
    border-bottom: 1px solid var(--p-surface-border);
    font-family: var(--p-font-family);
    font-size: 0.875rem;
    flex-shrink: 0;
  }

  .terminal-title {
    font-weight: 600;
  }

  .terminal-count {
    color: var(--p-text-muted-color);
    font-size: 0.8125rem;
  }

  .terminal-column-headers {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem 1rem;
    background: var(--p-surface-700);
    border-bottom: 1px solid var(--p-surface-border);
    font-family: var(--p-font-family);
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--p-text-muted-color);
    flex-shrink: 0;
  }

  .header-expand {
    flex-shrink: 0;
    width: 20px;
  }

  .header-time {
    flex-shrink: 0;
    width: 180px;
  }

  .header-level {
    flex-shrink: 0;
    width: 60px;
    text-align: center;
  }

  .header-request {
    flex-shrink: 0;
    width: 110px;
  }

  .header-logger {
    flex-shrink: 0;
    width: 200px;
  }

  .header-message {
    flex: 1;
  }

  .header-count {
    flex-shrink: 0;
    color: var(--p-text-muted-color);
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: none;
    margin-left: auto;
    padding-left: 1rem;
  }

  .terminal-body {
    flex: 1;
    overflow-y: auto;
    background: var(--p-surface-800);
    font-family: 'Courier New', monospace;
    font-size: 0.8125rem;
  }

  /* Mobile responsive */
  @media (max-width: 768px) {
    .terminal-column-headers {
      gap: 0.5rem;
      padding: 0.5rem 0.75rem;
      font-size: 0.7rem;
    }

    .header-time {
      width: auto;
      flex: 0 0 auto;
    }

    .header-level {
      width: 50px;
    }

    /* Hide logger header on mobile */
    .header-logger {
      display: none;
    }

    .header-request {
      width: auto;
    }

    .header-message {
      flex: 1;
    }

    .header-count {
      font-size: 0.7rem;
      padding-left: 0.5rem;
    }
  }

  @media (max-width: 480px) {
    .terminal-column-headers {
      gap: 0.375rem;
      padding: 0.5rem;
      font-size: 0.65rem;
    }

    .header-level {
      width: 45px;
    }

    .header-count {
      font-size: 0.65rem;
    }
  }
</style>
