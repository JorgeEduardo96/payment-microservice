import { describe, it, expect, vi } from 'vitest'
import http from '../http'
import { orderApi } from '../orderApi'
import type { CreateOrderPayload } from '@/types'

vi.mock('../http', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('orderApi', () => {
  it('create calls POST /order with the payload', () => {
    const payload: CreateOrderPayload = {
      clientId: 'client-1',
      total: 100,
      paymentMethod: 'CARD',
      shippingAddress: 'Main St',
    }
    orderApi.create(payload)
    expect(http.post).toHaveBeenCalledWith('/order', payload)
  })

  it('getByClientId calls GET /order/client/:clientId', () => {
    orderApi.getByClientId('client-1')
    expect(http.get).toHaveBeenCalledWith('/order/client/client-1')
  })

  it('getAll calls GET /order', () => {
    orderApi.getAll()
    expect(http.get).toHaveBeenCalledWith('/order')
  })
})
