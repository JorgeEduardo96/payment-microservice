import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOrdersStore } from '../orders'
import { orderApi } from '@/api/orderApi'
import type { Order } from '@/types'

vi.mock('@/api/orderApi', () => ({
  orderApi: {
    create: vi.fn(),
    getByClientId: vi.fn(),
    getAll: vi.fn(),
  },
}))

const order1: Order = {
  id: 'o1',
  clientId: 'c1',
  clientName: 'John Doe',
  total: 100,
  paymentMethod: 'CARD',
  status: 'PENDING_PAYMENT',
  shippingAddress: 'Main St',
}

const order2: Order = {
  id: 'o2',
  clientId: 'c2',
  clientName: 'Jane Doe',
  total: 50,
  paymentMethod: 'CASH',
  status: 'PAID',
  shippingAddress: 'Second St',
}

describe('useOrdersStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchByClient populates orders and sets activeClientId', async () => {
    vi.mocked(orderApi.getByClientId).mockResolvedValue({ data: [order1] } as never)

    const store = useOrdersStore()
    const result = await store.fetchByClient('c1')

    expect(result).toEqual([order1])
    expect(store.orders).toEqual([order1])
    expect(store.activeClientId).toBe('c1')
  })

  it('fetchAll populates allOrders without touching orders/activeClientId', async () => {
    vi.mocked(orderApi.getAll).mockResolvedValue({ data: [order1, order2] } as never)

    const store = useOrdersStore()
    const result = await store.fetchAll()

    expect(result).toEqual([order1, order2])
    expect(store.allOrders).toEqual([order1, order2])
    expect(store.orders).toEqual([])
    expect(store.activeClientId).toBeNull()
  })

  it('create prepends the order when it belongs to the active client', async () => {
    vi.mocked(orderApi.create).mockResolvedValue({ data: order1 } as never)

    const store = useOrdersStore()
    store.activeClientId = 'c1'
    const result = await store.create({
      clientId: 'c1',
      total: 100,
      paymentMethod: 'CARD',
      shippingAddress: 'Main St',
    })

    expect(result).toEqual(order1)
    expect(store.orders).toEqual([order1])
  })

  it('create does not touch orders when it belongs to a different client', async () => {
    vi.mocked(orderApi.create).mockResolvedValue({ data: order1 } as never)

    const store = useOrdersStore()
    store.activeClientId = 'someone-else'
    await store.create({
      clientId: 'c1',
      total: 100,
      paymentMethod: 'CARD',
      shippingAddress: 'Main St',
    })

    expect(store.orders).toEqual([])
  })

  it('clear resets orders and activeClientId', () => {
    const store = useOrdersStore()
    store.orders = [order1]
    store.activeClientId = 'c1'

    store.clear()

    expect(store.orders).toEqual([])
    expect(store.activeClientId).toBeNull()
  })
})
