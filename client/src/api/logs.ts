import { apiClient, handleResponse } from './client'
import { buildLogsQueryParams } from './queryParams'
import type { LogsResponseWrapper, Pageable } from '@/types'

/**
 * Filters for querying logs
 */
export interface LogsFilters {
  /** Filter by log level (DEBUG, INFO, WARN, ERROR) - case insensitive */
  level?: string
  /** Start date-time in ISO-8601 format (e.g., '2024-02-14T10:30:00' or '2024-02-14T10:30:00Z') */
  startDate?: string
  /** End date-time in ISO-8601 format (e.g., '2024-02-14T15:30:00' or '2024-02-14T15:30:00Z') */
  endDate?: string
  /** Filter by logger name (substring match, case insensitive) */
  logger?: string
  /** Filter by MDC requestId (exact match, case insensitive) to trace all logs of one request */
  requestId?: string
}

/**
 * Get application logs with pagination and filtering
 *
 * @param pageable - Pagination parameters (page, size, sort)
 * @param filters - Optional filters (level, date range, logger)
 * @returns Paginated logs response with metadata
 */
export async function getLogs(
  pageable: Pageable,
  filters?: LogsFilters,
): Promise<LogsResponseWrapper> {
  const queryParams = buildLogsQueryParams(pageable, filters)
  const response = await apiClient.GET('/api/v1/logs', {
    params: { query: queryParams as any },
  })
  return handleResponse(response)
}
