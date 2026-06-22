<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { useClipboard } from '@vueuse/core'
  import Button from 'primevue/button'
  import { useToast, useLogsContext } from '@/composables'
  import { Icon } from '@/utils'
  import type { LogEvent } from '@/types'

  interface Props {
    log: LogEvent
  }

  const props = defineProps<Props>()
  const toast = useToast()
  const { filters } = useLogsContext()

  // Expandable "more details" panel (MDC + stack trace + metadata)
  const expanded = ref(false)
  const toggleDetails = () => {
    expanded.value = !expanded.value
  }

  // Single clipboard instance for all copy operations
  const { copy } = useClipboard()

  const copyMessage = () => {
    if (!props.log.message) return
    copy(props.log.message)
    toast.success('Message copied to clipboard')
  }

  const copyMdc = () => {
    if (!mdcJson.value) return
    copy(mdcJson.value)
    toast.success('MDC copied to clipboard')
  }

  const copyStackTrace = () => {
    if (!props.log.stackTrace) return
    copy(props.log.stackTrace)
    toast.success('Stack trace copied to clipboard')
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

  // Short logger (last segment); full name kept in tooltip
  const shortLogger = computed(() => props.log.logger?.split('.').pop() ?? 'N/A')

  // MDC correlation fields
  const requestId = computed(() => props.log.mdc?.requestId)
  const jobType = computed(() => props.log.mdc?.jobType)
  const jobId = computed(() => props.log.mdc?.jobId)
  const isScheduled = computed(() => props.log.mdc?.jobSource === 'SCHEDULER')

  const mdcEntries = computed(() => Object.entries(props.log.mdc ?? {}))
  const mdcJson = computed(() =>
    mdcEntries.value.length > 0 ? JSON.stringify(props.log.mdc, null, 2) : null,
  )

  // Filter the whole stream down to this request
  const filterByRequestId = () => {
    if (!requestId.value) return
    filters.value = { ...filters.value, requestId: requestId.value }
    toast.success(`Filtering by request ${requestId.value}`)
  }
</script>

<template>
  <div class="log-entry" :class="{ expanded }">
    <div class="log-row" role="button" :aria-expanded="expanded" @click="toggleDetails">
      <!-- Expand toggle -->
      <button
        type="button"
        class="expand-toggle"
        :aria-expanded="expanded"
        aria-label="Toggle details"
        @click.stop="toggleDetails"
      >
        <i :class="expanded ? Icon.CHEVRON_DOWN : Icon.CHEVRON_RIGHT" />
      </button>

      <!-- Time -->
      <span class="log-time">{{ formattedTime }}</span>

      <!-- Level -->
      <span class="log-level" :class="levelClass">
        {{ log.level ?? 'UNKNOWN' }}
      </span>

      <!-- Request ID (dedicated, filterable column) -->
      <span class="log-request">
        <button
          v-if="requestId"
          type="button"
          class="request-chip"
          :title="`Filter logs by request ${requestId}`"
          @click.stop="filterByRequestId"
        >
          {{ requestId }}
        </button>
        <span v-else class="request-empty">—</span>
      </span>

      <!-- Logger (short, full name in tooltip) -->
      <span class="log-logger" :title="log.logger">
        {{ shortLogger }}
      </span>

      <!-- Message + correlation chips + inline copy -->
      <div class="log-message-wrapper">
        <span class="log-message">{{ log.message ?? '' }}</span>
        <span v-if="jobType || isScheduled" class="log-chips">
          <span v-if="jobType" class="mdc-chip chip-job" :title="jobId ? `jobId: ${jobId}` : undefined">
            {{ jobType }}
          </span>
          <span v-if="isScheduled" class="mdc-chip chip-scheduler">scheduler</span>
        </span>
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

    <!-- Details dropdown: metadata + MDC + stack trace -->
    <div v-if="expanded" class="log-details">
      <dl class="details-meta">
        <div class="meta-item">
          <dt>Logger</dt>
          <dd>{{ log.logger ?? 'N/A' }}</dd>
        </div>
        <div class="meta-item">
          <dt>Thread</dt>
          <dd>{{ log.thread ?? 'N/A' }}</dd>
        </div>
        <div class="meta-item">
          <dt>Sequence</dt>
          <dd>{{ log.sequence ?? 'N/A' }}</dd>
        </div>
        <div class="meta-item">
          <dt>Service</dt>
          <dd>{{ log.serviceName ?? 'N/A' }}</dd>
        </div>
      </dl>

      <!-- MDC -->
      <div v-if="mdcJson" class="details-section">
        <div class="details-section-header">
          <span class="details-label">MDC</span>
          <Button
            icon="pi pi-copy"
            severity="secondary"
            text
            rounded
            size="small"
            aria-label="Copy MDC"
            @click.stop="copyMdc"
          />
        </div>
        <pre class="details-pre mdc-pre">{{ mdcJson }}</pre>
      </div>

      <!-- Stack trace -->
      <div v-if="log.stackTrace" class="details-section">
        <div class="details-section-header">
          <span class="details-label stacktrace-label">Stack trace</span>
          <Button
            icon="pi pi-copy"
            severity="secondary"
            text
            rounded
            size="small"
            aria-label="Copy stack trace"
            @click.stop="copyStackTrace"
          />
        </div>
        <pre class="details-pre stacktrace-pre">{{ log.stackTrace }}</pre>
      </div>
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

  .log-entry.expanded {
    background: var(--p-surface-700);
  }

  .log-row {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
    cursor: pointer;
  }

  .expand-toggle {
    flex-shrink: 0;
    width: 20px;
    padding: 0;
    background: none;
    border: none;
    color: var(--p-text-muted-color);
    cursor: pointer;
    font-size: 0.75rem;
    line-height: 1.5;
  }

  .expand-toggle:hover {
    color: var(--p-text-color);
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

  /* Request ID column */
  .log-request {
    flex-shrink: 0;
    width: 110px;
    overflow: hidden;
  }

  .request-chip {
    max-width: 100%;
    padding: 0.0625rem 0.375rem;
    background: var(--p-surface-900);
    border: 1px solid var(--p-surface-500);
    border-radius: 3px;
    color: var(--p-primary-color);
    font-family: inherit;
    font-size: 0.75rem;
    cursor: pointer;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .request-chip:hover {
    border-color: var(--p-primary-color);
    background: var(--p-surface-800);
  }

  .request-empty {
    color: var(--p-text-muted-color);
    opacity: 0.5;
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
    flex-wrap: wrap;
  }

  .log-message {
    flex: 1;
    color: var(--p-text-color);
    word-break: break-word;
  }

  .log-chips {
    display: inline-flex;
    gap: 0.375rem;
    flex-shrink: 0;
  }

  .mdc-chip {
    padding: 0.0625rem 0.375rem;
    border-radius: 3px;
    font-size: 0.6875rem;
    font-weight: 600;
    white-space: nowrap;
  }

  .chip-job {
    color: var(--p-purple-700);
    background: var(--p-purple-50);
  }

  .chip-scheduler {
    color: var(--p-teal-700);
    background: var(--p-teal-50);
  }

  .copy-btn {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity 0.2s;
  }

  .log-entry:hover .copy-btn {
    opacity: 1;
  }

  /* Details dropdown */
  .log-details {
    margin-top: 0.5rem;
    margin-left: 20px;
    padding: 0.75rem;
    background: var(--p-surface-900);
    border: 1px solid var(--p-surface-600);
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .details-meta {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 0.5rem 1rem;
    margin: 0;
  }

  .meta-item {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }

  .meta-item dt {
    font-size: 0.6875rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--p-text-muted-color);
  }

  .meta-item dd {
    margin: 0;
    color: var(--p-text-color);
    word-break: break-all;
  }

  .details-section {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .details-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .details-label {
    font-size: 0.6875rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--p-text-muted-color);
  }

  .stacktrace-label {
    color: var(--p-red-500);
  }

  .details-pre {
    margin: 0;
    padding: 0.5rem;
    background: var(--p-surface-800);
    border: 1px solid var(--p-surface-600);
    border-radius: 4px;
    font-family: 'Courier New', 'Courier', monospace;
    font-size: 0.75rem;
    overflow-x: auto;
    white-space: pre;
  }

  .mdc-pre {
    color: var(--p-green-700);
  }

  .stacktrace-pre {
    color: var(--p-red-600);
    max-height: 320px;
    overflow-y: auto;
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

    .log-logger {
      display: none;
    }

    .log-request {
      width: auto;
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

    .log-details {
      margin-left: 0;
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
