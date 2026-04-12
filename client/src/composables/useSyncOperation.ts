import { ref, computed, type Ref } from 'vue'
import { useToast } from './useToast'
import type { UseMutationReturnType } from '@tanstack/vue-query'
import { OperationStatus, ButtonSeverity, Icon } from '@/utils/enums'

export type SyncStatus = OperationStatus

export interface SyncButtonProps {
  severity?: ButtonSeverity
  icon?: string
  loading: boolean
}

export interface UseSyncOperationReturn {
  /** Current sync status */
  status: Ref<SyncStatus>
  /** Button props based on current status */
  buttonProps: Ref<SyncButtonProps>
  /** Execute sync operation with error handling */
  execute: () => Promise<void>
  /** Reset status to idle */
  reset: () => void
}

export interface SyncOperationOptions<TData = unknown, TError = Error, TVariables = void> {
  /** TanStack Query mutation hook */
  mutation: UseMutationReturnType<TData, TError, TVariables, unknown>
  /** Variables to pass to mutation (if needed) */
  mutationVariables?: TVariables
  /** Success toast message */
  successMessage?: string
  /** Success toast description (can use response data) */
  successDescription?: string | ((data: TData) => string)
  /** Error toast message */
  errorMessage?: string
  /** Callback after successful execution */
  onSuccess?: (data: TData) => void
  /** Callback after error */
  onError?: (error: TError) => void
  /** Auto-reset delay in milliseconds (default: 3000) */
  autoResetDelay?: number
}

/**
 * Composable for managing sync operation state and execution
 *
 * @example
 * ```ts
 * const syncMutation = useSyncJellyfinMedia()
 * const { status, buttonProps, execute } = useSyncOperation({
 *   mutation: syncMutation,
 *   successMessage: 'Sync started',
 *   successDescription: 'Media synchronization in progress',
 * })
 *
 * // In template
 * <Button
 *   :severity="buttonProps.severity"
 *   :icon="buttonProps.icon"
 *   :loading="buttonProps.loading"
 *   @click="execute"
 * />
 * ```
 */
export function useSyncOperation<TData = unknown, TError = Error, TVariables = void>(
  options: SyncOperationOptions<TData, TError, TVariables>,
): UseSyncOperationReturn {
  const toast = useToast()

  const status = ref<SyncStatus>(OperationStatus.IDLE) as Ref<SyncStatus>
  const autoResetDelay = options.autoResetDelay ?? 3000

  const buttonProps = computed<SyncButtonProps>(() => {
    switch (status.value) {
      case OperationStatus.LOADING:
        return { loading: true }
      case OperationStatus.SUCCESS:
        return { severity: ButtonSeverity.SUCCESS, icon: Icon.CHECK, loading: false }
      case OperationStatus.ERROR:
        return { severity: ButtonSeverity.DANGER, icon: Icon.TIMES, loading: false }
      default:
        return { loading: false }
    }
  })

  function reset() {
    status.value = OperationStatus.IDLE
  }

  async function execute() {
    status.value = OperationStatus.LOADING

    try {
      const data = await options.mutation.mutateAsync(options.mutationVariables as TVariables)

      status.value = OperationStatus.SUCCESS

      // Show success toast
      const description =
        typeof options.successDescription === 'function'
          ? options.successDescription(data)
          : options.successDescription

      toast.success(options.successMessage || 'Operation successful', {
        description,
      })

      // Call success callback
      options.onSuccess?.(data)

      // Auto-reset after delay
      setTimeout(reset, autoResetDelay)
    } catch (error) {
      status.value = OperationStatus.ERROR

      // Show error toast
      toast.error(error as Error, {
        fallbackMessage: options.errorMessage || 'Operation failed',
      })

      // Call error callback
      options.onError?.(error as TError)

      // Auto-reset after delay
      setTimeout(reset, autoResetDelay)
    }
  }

  return {
    status,
    buttonProps,
    execute,
    reset,
  }
}
