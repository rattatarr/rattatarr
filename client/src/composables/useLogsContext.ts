import { inject, provide, type Ref } from 'vue'
import type { LogsFilters } from '@/api/logs'

export interface LogsContext {
  filters: Ref<LogsFilters>
  autoRefresh: Ref<boolean>
}

const LOGS_CONTEXT_KEY = Symbol('logs-context')

export function provideLogsContext(context: LogsContext) {
  provide(LOGS_CONTEXT_KEY, context)
}

export function useLogsContext(): LogsContext {
  const context = inject<LogsContext>(LOGS_CONTEXT_KEY)

  if (!context) {
    throw new Error('useLogsContext must be used within a component that provides LogsContext')
  }

  return context
}
