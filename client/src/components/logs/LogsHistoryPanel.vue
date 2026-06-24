<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import { useInfiniteLogs } from '@/queries/useInfiniteLogs'
  import { useSentinelInfiniteScroll } from '@/composables/useSentinelInfiniteScroll'
  import { provideLogsContext } from '@/composables'
  import ProgressSpinner from 'primevue/progressspinner'
  import LogFilters from './LogFilters.vue'
  import LogTerminal from './LogTerminal.vue'
  import LogEntry from './LogEntry.vue'
  import type { LogsFilters } from '@/api/logs'
  import { Icon } from '@/utils'

  // State
  const filters = ref<LogsFilters>({
    level: undefined,
    logger: undefined,
    requestId: undefined,
    startDate: undefined,
    endDate: undefined,
  })
  const autoRefresh = ref(false)
  const newLogTimestamps = ref<Set<number>>(new Set())

  // Provide logs context for child components
  provideLogsContext({ filters, autoRefresh })

  // Query
  const { data, isLoading, isFetchingNextPage, hasNextPage, fetchNextPage, error } =
    useInfiniteLogs(filters, 50, autoRefresh)

  // Computed
  const allLogs = computed(() => data.value?.pages.flatMap((page) => page.logs ?? []) ?? [])
  const totalCount = computed(() => data.value?.pages[0]?.pagination?.totalElements ?? 0)

  // Animation for new logs when auto-refresh is enabled
  watch(
    allLogs,
    (newLogs, oldLogs) => {
      if (!autoRefresh.value || !oldLogs || newLogs.length <= oldLogs.length) return

      // Find newly arrived logs
      const oldTimestamps = new Set(oldLogs.map((log) => log.timestamp).filter(Boolean))
      const newEntries = newLogs.filter((log) => log.timestamp && !oldTimestamps.has(log.timestamp))

      // Mark new logs for animation
      newEntries.forEach((log) => {
        if (log.timestamp) newLogTimestamps.value.add(log.timestamp)
      })

      // Clear animation flags after animation completes
      setTimeout(() => newLogTimestamps.value.clear(), 800)
    },
    { flush: 'post' },
  )

  // Infinite scroll
  const { sentinel } = useSentinelInfiniteScroll(
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  )
</script>

<template>
  <div class="logs-history-panel">
    <!-- Filters -->
    <LogFilters />

    <!-- Logs Terminal -->
    <LogTerminal :count="`${totalCount} logs found`" show-column-headers>
      <!-- Loading State -->
      <div v-if="isLoading" class="state-container">
        <ProgressSpinner />
        <p>Loading logs...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="state-container error-state">
        <i :class="Icon.EXCLAMATION_TRIANGLE" />
        <p>Failed to load logs: {{ error.message }}</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="allLogs.length === 0" class="state-container">
        <i class="pi pi-inbox" />
        <p>No logs found matching your filters</p>
        <p class="state-hint">Try adjusting your search criteria</p>
      </div>

      <!-- Logs List -->
      <template v-else>
        <LogEntry
          v-for="(log, index) in allLogs"
          :key="`${log.timestamp}-${index}`"
          :log="log"
          :class="{ 'pulse-new': log.timestamp && newLogTimestamps.has(log.timestamp) }"
        />

        <!-- Infinite scroll sentinel -->
        <div ref="sentinel" class="scroll-sentinel" />

        <!-- Loading more indicator -->
        <div v-if="isFetchingNextPage" class="status-message">
          <ProgressSpinner style="width: 2rem; height: 2rem" />
          <p>Loading more logs...</p>
        </div>

        <!-- End of results -->
        <div v-else-if="!hasNextPage" class="status-message">
          <p>No more logs to load</p>
        </div>
      </template>
    </LogTerminal>
  </div>
</template>

<style scoped>
  .logs-history-panel {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.5rem;
    overflow: hidden;
    box-sizing: border-box;
  }

  /* New log animation */
  :deep(.pulse-new) {
    animation: log-pulse 0.8s ease-out;
  }

  @keyframes log-pulse {
    0% {
      opacity: 0;
      transform: translateX(-10px);
      background-color: var(--p-primary-color);
    }
    50% {
      opacity: 1;
      transform: translateX(0);
      background-color: rgba(var(--p-primary-500), 0.2);
    }
    100% {
      opacity: 1;
      transform: translateX(0);
      background-color: transparent;
    }
  }

  /* Infinite scroll sentinel */
  .scroll-sentinel {
    height: 1px;
    visibility: hidden;
  }

  /* State containers (loading, error, empty) */
  .state-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    color: var(--p-text-muted-color);
    font-family: var(--p-font-family);
  }

  .state-container p {
    margin: 0;
  }

  .state-container i {
    font-size: 4rem;
    opacity: 0.5;
  }

  .error-state {
    color: var(--p-red-500);
  }

  .error-state i {
    font-size: 3rem;
    opacity: 1;
  }

  .state-hint {
    font-size: 0.875rem;
    opacity: 0.7;
  }

  /* Status messages (loading more, end of results) */
  .status-message {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    padding: 1.5rem;
    color: var(--p-text-muted-color);
    font-family: var(--p-font-family);
    font-size: 0.875rem;
  }

  .status-message p {
    margin: 0;
  }
</style>
