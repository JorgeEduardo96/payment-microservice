import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises, type VueWrapper } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import HomeView from '../HomeView.vue'
import { useClientsStore } from '@/stores/clients'
import { useOrdersStore } from '@/stores/orders'
import { clientApi } from '@/api/clientApi'
import { orderApi } from '@/api/orderApi'
import type { Client, Order } from '@/types'

vi.mock('@/api/clientApi', () => ({
  clientApi: { getAll: vi.fn(), create: vi.fn(), getById: vi.fn(), update: vi.fn() },
}))
vi.mock('@/api/orderApi', () => ({
  orderApi: { create: vi.fn(), getByClientId: vi.fn(), getAll: vi.fn() },
}))

const client: Client = { id: 'c1', name: 'John Doe', email: 'john@doe.com', cpf: '52998224725', createdAt: '2026-01-01' }
const paidOrder: Order = {
  id: 'o1',
  clientId: 'c1',
  clientName: 'John Doe',
  total: 100,
  paymentMethod: 'CARD',
  status: 'PAID',
  shippingAddress: 'Main St',
}
const failedOrder: Order = {
  id: 'o2',
  clientId: 'c1',
  clientName: 'John Doe',
  total: 50,
  paymentMethod: 'CASH',
  status: 'FAILED',
  shippingAddress: 'Second St',
}

let activeWrapper: VueWrapper | null = null

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    activeWrapper?.unmount()
    activeWrapper = null
  })

  it('fetches clients and orders on mount when the stores are empty', async () => {
    vi.mocked(clientApi.getAll).mockResolvedValue({ data: [client] } as never)
    vi.mocked(orderApi.getAll).mockResolvedValue({ data: [paidOrder, failedOrder] } as never)

    activeWrapper = mount(HomeView, { attachTo: document.body })
    await flushPromises()

    expect(clientApi.getAll).toHaveBeenCalled()
    expect(orderApi.getAll).toHaveBeenCalled()
  })

  it('does not refetch when the stores are already populated', async () => {
    const clientsStore = useClientsStore()
    const ordersStore = useOrdersStore()
    clientsStore.clients = [client]
    ordersStore.allOrders = [paidOrder]

    activeWrapper = mount(HomeView, { attachTo: document.body })
    await flushPromises()

    expect(clientApi.getAll).not.toHaveBeenCalled()
    expect(orderApi.getAll).not.toHaveBeenCalled()
  })

  it('shows the client and order stats', async () => {
    const clientsStore = useClientsStore()
    const ordersStore = useOrdersStore()
    clientsStore.clients = [client]
    ordersStore.allOrders = [paidOrder, failedOrder]

    activeWrapper = mount(HomeView, { attachTo: document.body })
    await flushPromises()

    expect(activeWrapper.text()).toContain('Clients Loaded')
    expect(activeWrapper.text()).toContain('Orders Viewed')
    const values = activeWrapper.findAll('div.text-h4').map((el) => el.text())
    expect(values).toEqual(['1', '2', '1', '1'])
  })
})
