import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useProfiles, useProfile, useCreateProfile } from '@/queries'

// Mock API
const mockGetAllProfiles = vi.fn()
const mockGetProfileById = vi.fn()
const mockCreateProfile = vi.fn()

vi.mock('@/api/profiles', () => ({
  getAllProfiles: (...args: never[]) => mockGetAllProfiles(...args),
  getProfileById: (...args: never[]) => mockGetProfileById(...args),
  createProfile: (...args: never[]) => mockCreateProfile(...args),
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
  profileKeys: {
    all: ['profiles'],
    lists: () => ['profiles', 'list'],
    list: (pageable: never, filters: never) => ['profiles', 'list', pageable, filters],
    detail: (id: string) => ['profiles', 'detail', id],
  },
}))

describe('useProfiles', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: ref(null),
      isLoading: ref(false),
    })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 0, size: 10 })
    const filters = ref({ name: 'test' })

    useProfiles(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['profiles', 'list', { page: 0, size: 10 }, { name: 'test' }])
  })

  it('calls API with correct parameters in queryFn', async () => {
    mockGetAllProfiles.mockResolvedValue({ profiles: [] })

    const pageable = { page: 1, size: 20 }
    const filters = { name: 'John' }

    useProfiles(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAllProfiles).toHaveBeenCalledWith(pageable, filters)
  })

  it('updates query key when pageable changes', () => {
    const pageable = ref({ page: 0, size: 10 })
    const filters = ref({ name: '' })

    useProfiles(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    pageable.value = { page: 1, size: 10 }
    const key2 = call.queryKey.value

    expect(key1[2]).toEqual({ page: 0, size: 10 })
    expect(key2[2]).toEqual({ page: 1, size: 10 })
  })

  it('updates query key when filters change', () => {
    const pageable = ref({ page: 0, size: 10 })
    const filters = ref({ name: 'Alice' })

    useProfiles(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    filters.value = { name: 'Bob' }
    const key2 = call.queryKey.value

    expect(key1[3]).toEqual({ name: 'Alice' })
    expect(key2[3]).toEqual({ name: 'Bob' })
  })
})

describe('useProfile', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({
      data: ref(null),
      isLoading: ref(false),
    })
  })

  it('creates query with correct query key', () => {
    const id = ref('profile-123')
    useProfile(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['profiles', 'detail', 'profile-123'])
  })

  it('calls API with correct ID in queryFn', async () => {
    mockGetProfileById.mockResolvedValue({ id: 'p1', name: 'Test' })

    useProfile('profile-456')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetProfileById).toHaveBeenCalledWith('profile-456')
  })

  it('is disabled when ID is empty', () => {
    const id = ref('')
    useProfile(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(false)
  })

  it('is enabled when ID is provided', () => {
    const id = ref('profile-789')
    useProfile(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const enabled = call.enabled.value

    expect(enabled).toBe(true)
  })

  it('updates query key when ID changes', () => {
    const id = ref('p1')
    useProfile(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    const key1 = call.queryKey.value

    id.value = 'p2'
    const key2 = call.queryKey.value

    expect(key1).toEqual(['profiles', 'detail', 'p1'])
    expect(key2).toEqual(['profiles', 'detail', 'p2'])
  })
})

describe('useCreateProfile', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseMutation.mockReturnValue({
      mutate: vi.fn(),
      mutateAsync: vi.fn(),
    })
  })

  it('creates mutation with correct mutationFn', async () => {
    const request = { name: 'New Profile' }
    mockCreateProfile.mockResolvedValue({ id: 'new-1', name: 'New Profile' })

    useCreateProfile()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(request)

    expect(mockCreateProfile).toHaveBeenCalledWith(request)
  })

  it('invalidates profile lists on success', async () => {
    useCreateProfile()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.onSuccess()

    expect(mockInvalidateQueries).toHaveBeenCalledWith({
      queryKey: ['profiles', 'list'],
    })
  })

  it('handles profile with empty name', async () => {
    const request = { name: '' }
    mockCreateProfile.mockResolvedValue({ id: 'p1', name: '' })

    useCreateProfile()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(request)

    expect(mockCreateProfile).toHaveBeenCalledWith(request)
  })

  it('handles profile with long name', async () => {
    const request = { name: 'Very Long Profile Name With Mnever Characters' }
    mockCreateProfile.mockResolvedValue({ id: 'p2', name: request.name })

    useCreateProfile()

    const call = mockUseMutation.mock.calls[0]![0]!
    await call.mutationFn(request)

    expect(mockCreateProfile).toHaveBeenCalledWith(request)
  })
})
