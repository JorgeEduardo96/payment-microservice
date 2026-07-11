import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useClientsStore } from '../clients'
import { clientApi } from '@/api/clientApi'
import type { Client } from '@/types'

vi.mock('@/api/clientApi', () => ({
  clientApi: {
    getAll: vi.fn(),
    create: vi.fn(),
    getById: vi.fn(),
    update: vi.fn(),
  },
}))

const client1: Client = { id: '1', name: 'John Doe', email: 'john@doe.com', cpf: '52998224725', createdAt: '2026-01-01' }
const client2: Client = { id: '2', name: 'Jane Doe', email: 'jane@doe.com', cpf: '11144477735', createdAt: '2026-01-02' }

describe('useClientsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchAll populates clients from the API', async () => {
    vi.mocked(clientApi.getAll).mockResolvedValue({ data: [client1, client2] } as never)

    const store = useClientsStore()
    await store.fetchAll()

    expect(store.clients).toEqual([client1, client2])
    expect(store.loading).toBe(false)
  })

  it('create prepends the new client to the list', async () => {
    vi.mocked(clientApi.create).mockResolvedValue({ data: client1 } as never)

    const store = useClientsStore()
    store.clients = [client2]
    const result = await store.create({ name: client1.name, email: client1.email, cpf: client1.cpf })

    expect(result).toEqual(client1)
    expect(store.clients).toEqual([client1, client2])
  })

  it('fetchById inserts the client when not already loaded', async () => {
    vi.mocked(clientApi.getById).mockResolvedValue({ data: client1 } as never)

    const store = useClientsStore()
    await store.fetchById(client1.id)

    expect(store.clients).toEqual([client1])
  })

  it('fetchById replaces the client when already loaded', async () => {
    const updated = { ...client1, name: 'John Updated' }
    vi.mocked(clientApi.getById).mockResolvedValue({ data: updated } as never)

    const store = useClientsStore()
    store.clients = [client1, client2]
    await store.fetchById(client1.id)

    expect(store.clients).toEqual([updated, client2])
  })

  it('update replaces the client in the list', async () => {
    const updated = { ...client1, name: 'John Updated' }
    vi.mocked(clientApi.update).mockResolvedValue({ data: updated } as never)

    const store = useClientsStore()
    store.clients = [client1, client2]
    await store.update(client1.id, { name: updated.name, email: updated.email })

    expect(store.clients).toEqual([updated, client2])
    expect(clientApi.update).toHaveBeenCalledWith(client1.id, { name: updated.name, email: updated.email })
  })

  it('sets loading to true while fetching and false afterwards', async () => {
    let resolvePromise: (value: unknown) => void = () => {}
    vi.mocked(clientApi.getAll).mockReturnValue(
      new Promise((resolve) => {
        resolvePromise = resolve
      }) as never,
    )

    const store = useClientsStore()
    const fetchPromise = store.fetchAll()

    expect(store.loading).toBe(true)

    resolvePromise({ data: [] })
    await fetchPromise

    expect(store.loading).toBe(false)
  })
})
