import { describe, it, expect, vi } from 'vitest'
import http from '../http'
import { clientApi } from '../clientApi'
import type { CreateClientPayload, UpdateClientPayload } from '@/types'

vi.mock('../http', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}))

describe('clientApi', () => {
  it('getAll calls GET /client', () => {
    clientApi.getAll()
    expect(http.get).toHaveBeenCalledWith('/client')
  })

  it('create calls POST /client with the payload', () => {
    const payload: CreateClientPayload = { name: 'John Doe', email: 'john@doe.com', cpf: '52998224725' }
    clientApi.create(payload)
    expect(http.post).toHaveBeenCalledWith('/client', payload)
  })

  it('getById calls GET /client/:id', () => {
    clientApi.getById('abc-123')
    expect(http.get).toHaveBeenCalledWith('/client/abc-123')
  })

  it('update calls PUT /client/:id with the payload', () => {
    const payload: UpdateClientPayload = { name: 'John Doe', email: 'john@doe.com' }
    clientApi.update('abc-123', payload)
    expect(http.put).toHaveBeenCalledWith('/client/abc-123', payload)
  })
})
