import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { usePeople, usePerson } from '@/queries/usePeople'

// Mock API
const mockGetAllPeople = vi.fn()
const mockGetPersonById = vi.fn()

vi.mock('@/api/people', () => ({
  getAllPeople: (...args: never[]) => mockGetAllPeople(...args),
  getPersonById: (...args: never[]) => mockGetPersonById(...args),
}))

// Mock TanStack Query
const mockUseQuery = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useQuery: (options: never) => mockUseQuery(options),
}))

// Mock query keys
vi.mock('../queryKeys', () => ({
  peopleKeys: {
    all: ['people'],
    lists: () => ['people', 'list'],
    list: (pageable: never, filters: never) => ['people', 'list', { pageable, filters }],
    details: () => ['people', 'detail'],
    detail: (id: string) => ['people', 'detail', id],
  },
}))

describe('usePeople', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct query key', () => {
    const pageable = ref({ page: 0, size: 20, sort: ['name,asc'] })
    const filters = ref({ name: 'John' })

    usePeople(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual([
      'people',
      'list',
      {
        pageable: { page: 0, size: 20, sort: ['name,asc'] },
        filters: { name: 'John' },
      },
    ])
  })

  it('query key updates reactively when pageable changes', () => {
    const pageable = ref({ page: 0, size: 20, sort: ['name,asc'] })
    const filters = ref({ name: '' })

    usePeople(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!

    pageable.value = { page: 1, size: 20, sort: ['name,asc'] }

    expect(call.queryKey.value).toEqual([
      'people',
      'list',
      {
        pageable: { page: 1, size: 20, sort: ['name,asc'] },
        filters: { name: '' },
      },
    ])
  })

  it('query key updates reactively when filters change', () => {
    const pageable = ref({ page: 0, size: 20, sort: ['name,asc'] })
    const filters = ref({ name: '' })

    usePeople(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!

    filters.value = { name: 'Jane' }

    expect(call.queryKey.value).toEqual([
      'people',
      'list',
      {
        pageable: { page: 0, size: 20, sort: ['name,asc'] },
        filters: { name: 'Jane' },
      },
    ])
  })

  it('calls API with correct parameters', async () => {
    mockGetAllPeople.mockResolvedValue({ people: [], pagination: { totalElements: 0 } })

    const pageable = { page: 0, size: 20, sort: ['name,asc'] }
    const filters = { name: 'John' }

    usePeople(pageable, filters)

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetAllPeople).toHaveBeenCalledWith(pageable, filters)
  })
})

describe('usePerson', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseQuery.mockReturnValue({ data: ref(null) })
  })

  it('creates query with correct detail query key', () => {
    usePerson('person-123')

    const call = mockUseQuery.mock.calls[0]![0]!
    const queryKey = call.queryKey.value

    expect(queryKey).toEqual(['people', 'detail', 'person-123'])
  })

  it('calls API with correct person ID', async () => {
    mockGetPersonById.mockResolvedValue({ id: 'person-123', name: 'John Doe' })

    usePerson('person-123')

    const call = mockUseQuery.mock.calls[0]![0]!
    await call.queryFn()

    expect(mockGetPersonById).toHaveBeenCalledWith('person-123')
  })

  it('is disabled when id is undefined', () => {
    usePerson(undefined)

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.enabled.value).toBe(false)
  })

  it('is disabled when id is empty string', () => {
    usePerson('')

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.enabled.value).toBe(false)
  })

  it('is enabled when id is provided', () => {
    usePerson('person-123')

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.enabled.value).toBe(true)
  })

  it('query key updates reactively when id changes', () => {
    const id = ref<string | undefined>('person-abc')

    usePerson(id)

    const call = mockUseQuery.mock.calls[0]![0]!

    id.value = 'person-xyz'

    expect(call.queryKey.value).toEqual(['people', 'detail', 'person-xyz'])
  })

  it('is disabled when reactive id becomes undefined', () => {
    const id = ref<string | undefined>('person-abc')

    usePerson(id)

    const call = mockUseQuery.mock.calls[0]![0]!
    expect(call.enabled.value).toBe(true)

    id.value = undefined

    expect(call.enabled.value).toBe(false)
  })

  it('uses empty string for query key when id is undefined', () => {
    const id = ref<string | undefined>(undefined)

    usePerson(id)

    const call = mockUseQuery.mock.calls[0]![0]!

    expect(call.queryKey.value).toEqual(['people', 'detail', ''])
  })
})
