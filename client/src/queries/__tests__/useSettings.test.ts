import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useSettings, useSetting, useUpdateSettings } from '@/queries'

// Mock API
const mockGetAllSettings = vi.fn()
const mockGetSettingByKey = vi.fn()
const mockUpdateSettings = vi.fn()

vi.mock('@/api/settings', () => ({
  getAllSettings: () => mockGetAllSettings(),
  getSettingByKey: (...args: never[]) => mockGetSettingByKey(...args),
  updateSettings: (...args: never[]) => mockUpdateSettings(...args),
}))

// Mock TanStack Query
const mockUseQuery = vi.fn()
const mockUseMutation = vi.fn()
const mockInvalidateQueries = vi.fn()
const mockUseQueryClient = vi.fn(() => ({
  invalidateQueries: mockInvalidateQueries,
}))

vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
  useMutation: (options: never) => mockUseMutation(options),
  useQueryClient: () => mockUseQueryClient(),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  settingsKeys: {
    all: ['settings'],
    list: () => ['settings', 'list'],
    detail: (key: string) => ['settings', 'detail', key],
  },
}))

describe('useSettings', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: ref(null),
      isLoading: ref(false),
    })
  })

  it('creates query with correct query key', () => {
    useSettings()

    expect(mockUseQuery).toHaveBeenCalledWith(
      expect.objectContaining({
        queryKey: ['settings', 'list'],
      }),
    )
  })

  it('calls API in queryFn', async () => {
    mockGetAllSettings.mockResolvedValue({ settings: [] })

    useSettings()

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAllSettings).toHaveBeenCalled()
  })
})

describe('useSetting', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: ref(null),
      isLoading: ref(false),
    })
  })

  it('creates query with correct query key', () => {
    const key = ref('app.name')
    useSetting(key)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['settings', 'detail', 'app.name'])
  })

  it('calls API with correct key in queryFn', async () => {
    mockGetSettingByKey.mockResolvedValue({ key: 'app.name', value: 'MyApp' })

    useSetting('app.name')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetSettingByKey).toHaveBeenCalledWith('app.name')
  })

  it('is disabled when key is empty', () => {
    const key = ref('')
    useSetting(key)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })

  it('is enabled when key is provided', () => {
    const key = ref('app.version')
    useSetting(key)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(true)
  })

  it('updates query key when key changes', () => {
    const key = ref('app.name')
    useSetting(key)

    const call = mockUseQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    key.value = 'app.version'
    const key2 = call.queryKey.value

    expect(key1).toEqual(['settings', 'detail', 'app.name'])
    expect(key2).toEqual(['settings', 'detail', 'app.version'])
  })
})

describe('useUpdateSettings', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    const settings = {
      settings: [
        { key: 'app.name', value: 'NewName' },
        { key: 'app.version', value: '2.0.0' },
      ],
    }
    mockUpdateSettings.mockResolvedValue({ settings: [] })

    useUpdateSettings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(settings)

    expect(mockUpdateSettings).toHaveBeenCalledWith(settings)
  })

  it('invalidates all settings queries on success', async () => {
    useUpdateSettings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['settings'],
    })
  })

  it('handles empty settings update', async () => {
    const emptySettings = { settings: [] }
    mockUpdateSettings.mockResolvedValue({ settings: [] })

    useUpdateSettings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(emptySettings)

    expect(mockUpdateSettings).toHaveBeenCalledWith(emptySettings)
  })

  it('handles single setting update', async () => {
    const singleSetting = {
      settings: [{ key: 'app.debug', value: 'true' }],
    }
    mockUpdateSettings.mockResolvedValue({ settings: [{ key: 'app.debug', value: 'true' }] })

    useUpdateSettings()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(singleSetting)

    expect(mockUpdateSettings).toHaveBeenCalledWith(singleSetting)
  })
})
