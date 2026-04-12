import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useSyncOperation } from '@/composables'
import { OperationStatus, ButtonSeverity, Icon } from '@/utils/enums'

// Mock useToast
const mockToastSuccess = vi.fn()
const mockToastError = vi.fn()
const mockToastLoading = vi.fn()

vi.mock('../useToast', () => ({
  useToast: () => ({
    success: mockToastSuccess,
    error: mockToastError,
    loading: mockToastLoading,
  }),
}))

describe('useSyncOperation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('initializes with IDLE status', () => {
    const mockMutation = {
      mutateAsync: vi.fn(),
      isPending: ref(false),
    } as any

    const { status } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    expect(status.value).toBe(OperationStatus.IDLE)
  })

  it('shows correct button props for IDLE status', () => {
    const mockMutation = {
      mutateAsync: vi.fn(),
      isPending: ref(false),
    } as any

    const { buttonProps } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    expect(buttonProps.value).toEqual({ loading: false })
  })

  it('shows correct button props for LOADING status', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => new Promise(() => {})), // Never resolves
      isPending: ref(false),
    } as any

    const { buttonProps, execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    execute()

    expect(buttonProps.value).toEqual({ loading: true })
  })

  it('shows correct button props for SUCCESS status', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({ data: 'success' })),
      isPending: ref(false),
    } as any

    const { buttonProps, execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    await execute()

    expect(buttonProps.value).toEqual({
      severity: ButtonSeverity.SUCCESS,
      icon: Icon.CHECK,
      loading: false,
    })
  })

  it('shows correct button props for ERROR status', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.reject(new Error('Failed'))),
      isPending: ref(false),
    } as any

    const { buttonProps, execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
      errorMessage: 'Failed',
    })

    await execute()

    expect(buttonProps.value).toEqual({
      severity: ButtonSeverity.DANGER,
      icon: Icon.TIMES,
      loading: false,
    })
  })

  it('executes mutation and shows success toast', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({ count: 5 })),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Sync completed',
      successDescription: '5 items synced',
    })

    await execute()

    expect(mockMutation.mutateAsync).toHaveBeenCalled()
    expect(mockToastSuccess).toHaveBeenCalledWith('Sync completed', {
      description: '5 items synced',
    })
  })

  it('executes mutation with variables', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({})),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
      mutationVariables: { id: '123' },
      successMessage: 'Success',
    })

    await execute()

    expect(mockMutation.mutateAsync).toHaveBeenCalledWith({ id: '123' })
  })

  it('uses dynamic success description function', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({ count: 10 })),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Sync completed',
      successDescription: (data: any) => `${data.count} items synced`,
    })

    await execute()

    expect(mockToastSuccess).toHaveBeenCalledWith('Sync completed', {
      description: '10 items synced',
    })
  })

  it('calls onSuccess callback on success', async () => {
    const onSuccessCallback = vi.fn()
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({ result: 'ok' })),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
      onSuccess: onSuccessCallback,
    })

    await execute()

    expect(onSuccessCallback).toHaveBeenCalledWith({ result: 'ok' })
  })

  it('shows error toast on failure', async () => {
    const mockError = new Error('Network error')
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.reject(mockError)),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
      errorMessage: 'Sync failed',
    })

    await execute()

    expect(mockToastError).toHaveBeenCalledWith(mockError, {
      fallbackMessage: 'Sync failed',
    })
  })

  it('calls onError callback on failure', async () => {
    const onErrorCallback = vi.fn()
    const mockError = new Error('Failed')
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.reject(mockError)),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
      onError: onErrorCallback,
    })

    await execute()

    expect(onErrorCallback).toHaveBeenCalledWith(mockError)
  })

  it('auto-resets to IDLE after success with default delay', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({})),
      isPending: ref(false),
    } as any

    const { status, execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    await execute()

    expect(status.value).toBe(OperationStatus.SUCCESS)

    // Fast-forward 3 seconds (default delay)
    vi.advanceTimersByTime(3000)

    expect(status.value).toBe(OperationStatus.IDLE)
  })

  it('auto-resets to IDLE after error with default delay', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.reject(new Error('Failed'))),
      isPending: ref(false),
    } as any

    const { status, execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    await execute()

    expect(status.value).toBe(OperationStatus.ERROR)

    // Fast-forward 3 seconds (default delay)
    vi.advanceTimersByTime(3000)

    expect(status.value).toBe(OperationStatus.IDLE)
  })

  it('auto-resets with custom delay', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({})),
      isPending: ref(false),
    } as any

    const { status, execute } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
      autoResetDelay: 5000,
    })

    await execute()

    expect(status.value).toBe(OperationStatus.SUCCESS)

    // Fast-forward 4 seconds (not enough)
    vi.advanceTimersByTime(4000)
    expect(status.value).toBe(OperationStatus.SUCCESS)

    // Fast-forward 1 more second (total 5 seconds)
    vi.advanceTimersByTime(1000)
    expect(status.value).toBe(OperationStatus.IDLE)
  })

  it('manually resets to IDLE', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({})),
      isPending: ref(false),
    } as any

    const { status, execute, reset } = useSyncOperation({
      mutation: mockMutation,
      successMessage: 'Success',
    })

    await execute()

    expect(status.value).toBe(OperationStatus.SUCCESS)

    reset()

    expect(status.value).toBe(OperationStatus.IDLE)
  })

  it('uses default success message when not provided', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.resolve({})),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
    })

    await execute()

    expect(mockToastSuccess).toHaveBeenCalledWith('Operation successful', {
      description: undefined,
    })
  })

  it('uses default error message when not provided', async () => {
    const mockMutation = {
      mutateAsync: vi.fn(() => Promise.reject(new Error('Failed'))),
      isPending: ref(false),
    } as any

    const { execute } = useSyncOperation({
      mutation: mockMutation,
    })

    await execute()

    expect(mockToastError).toHaveBeenCalledWith(expect.any(Error), {
      fallbackMessage: 'Operation failed',
    })
  })
})
