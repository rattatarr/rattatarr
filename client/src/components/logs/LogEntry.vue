<script setup lang="ts">
  import { computed } from 'vue'
  import { useClipboard } from '@vueuse/core'
  import Button from 'primevue/button'
  import { useToast } from '@/composables'
  import type { LogEvent } from '@/types'

  interface Props {
    log: LogEvent
  }

  const props = defineProps<Props>()
  const toast = useToast()

  // Single clipboard instance for all copy operations
  const { copy } = useClipboard()

  // Copy handlers
  const copyMessage = () => {
    if (!props.log.message) return
    copy(props.log.message)
    toast.success('Message copied to clipboard')
  }

  const copyMdc = () => {
    if (!props.log.mdc) return
    copy(JSON.stringify(props.log.mdc, null, 2))
    toast.success('MDC copied to clipboard')
  }

  // Format timestamp
  const formattedTime = computed(() => {
    if (!props.log.timestamp) return 'N/A'
    const date = new Date(props.log.timestamp)
    const formatted = date.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    })
    const ms = date.getMilliseconds().toString().padStart(3, '0')
    return `${formatted}.${ms}`
  })

  // Level styling
  const levelClass = computed(() => {
    const level = props.log.level?.toUpperCase()
    return `level-${level?.toLowerCase() || 'default'}`
  })

  // Format MDC as JSON string
  const mdcJson = computed(() => {
    if (!props.log.mdc || Object.keys(props.log.mdc).length === 0) return null
    return JSON.stringify(props.log.mdc, null, 2)
  })
</script>

<template>
  <div class="log-entry">
    <div class="log-row">
      <!-- Time -->
      <span class="log-time">{{ formattedTime }}</span>

      <!-- Level -->
      <span class="log-level" :class="levelClass">
        {{ log.level ?? 'UNKNOWN' }}
      </span>

      <!-- Service Name -->
      <span class="log-service">{{ log.serviceName ?? 'N/A' }}</span>

      <!-- Logger -->
      <span class="log-logger" :title="log.logger">
        {{ log.logger ?? 'N/A' }}
      </span>

      <!-- Message with inline copy button -->
      <div class="log-message-wrapper">
        <span class="log-message">{{ log.message ?? '' }}</span>
        <Button
          v-if="log.message"
          icon="pi pi-copy"
          severity="secondary"
          text
          rounded
          size="small"
          class="copy-btn"
          aria-label="Copy message"
          @click.stop="copyMessage"
        />
      </div>
    </div>

    <!-- MDC with copy button -->
    <div v-if="mdcJson" class="log-mdc-row">
      <span class="mdc-label">MDC:</span>
      <pre class="mdc-content">{{ mdcJson }}</pre>
      <Button
        icon="pi pi-copy"
        severity="secondary"
        text
        rounded
        size="small"
        class="copy-btn"
        aria-label="Copy MDC"
        @click.stop="copyMdc"
      />
    </div>
  </div>
</template>

<style scoped>
  .log-entry {
    padding: 0.5rem 1rem;
    border-bottom: 1px solid var(--p-surface-100);
    font-family: 'Courier New', 'Courier', monospace;
    font-size: 0.8125rem;
    line-height: 1.5;
  }

  .log-entry:hover {
    background: var(--p-surface-600);
  }

  .log-row {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .log-time {
    flex-shrink: 0;
    width: 180px;
    color: var(--p-text-muted-color);
    font-weight: 500;
  }

  .log-level {
    flex-shrink: 0;
    width: 60px;
    font-weight: 700;
    text-align: center;
    padding: 0.125rem 0.25rem;
    border-radius: 3px;
  }

  .level-error {
    color: var(--p-red-700);
    background: var(--p-red-50);
  }

  .level-warn {
    color: var(--p-orange-700);
    background: var(--p-orange-50);
  }

  .level-info {
    color: var(--p-blue-700);
    background: var(--p-blue-50);
  }

  .level-debug {
    color: var(--p-gray-700);
    background: var(--p-gray-50);
  }

  .level-trace {
    color: var(--p-purple-700);
    background: var(--p-purple-50);
  }

  .level-default {
    color: var(--p-text-color);
    background: var(--p-surface-100);
  }

  .log-service {
    flex-shrink: 0;
    width: 120px;
    color: var(--p-primary-color);
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .log-logger {
    flex-shrink: 0;
    width: 200px;
    color: var(--p-text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .log-message-wrapper {
    flex: 1;
    display: flex;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .log-message {
    flex: 1;
    color: var(--p-text-color);
    word-break: break-word;
  }

  .copy-btn {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity 0.2s;
  }

  .log-entry:hover .copy-btn {
    opacity: 1;
  }

  .log-mdc-row {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    margin-top: 0.5rem;
    padding-left: 180px;
  }

  .mdc-label {
    flex-shrink: 0;
    color: var(--p-text-muted-color);
    font-weight: 600;
    font-size: 0.75rem;
  }

  .mdc-content {
    flex: 1;
    margin: 0;
    padding: 0.5rem;
    background: var(--p-surface-800);
    border: 1px solid var(--p-surface-600);
    border-radius: 4px;
    font-family: 'Courier New', 'Courier', monospace;
    font-size: 0.75rem;
    color: var(--p-green-700);
    overflow-x: auto;
  }

  /* Mobile responsive */
  @media (max-width: 768px) {
    .log-row {
      flex-wrap: wrap;
      gap: 0.5rem;
    }

    .log-time {
      width: auto;
      flex: 0 0 auto;
      font-size: 0.75rem;
    }

    .log-level {
      width: 50px;
      font-size: 0.7rem;
      padding: 0.1rem 0.2rem;
    }

    .log-service,
    .log-logger {
      display: none;
    }

    .log-message-wrapper {
      flex: 1 1 100%;
    }

    .log-message {
      font-size: 0.875rem;
    }

    .copy-btn {
      opacity: 1;
    }

    .log-mdc-row {
      padding-left: 0;
      flex-wrap: wrap;
    }

    .mdc-content {
      font-size: 0.7rem;
    }
  }

  @media (max-width: 480px) {
    .log-entry {
      padding: 0.5rem 0.75rem;
      font-size: 0.75rem;
    }

    .log-row {
      gap: 0.375rem;
    }

    .log-time {
      font-size: 0.7rem;
    }

    .log-level {
      width: 45px;
      font-size: 0.65rem;
    }

    .log-message {
      font-size: 0.8125rem;
      line-height: 1.4;
    }
  }
</style>
