export interface Client {
  id: string
  name: string
  email: string
  cpf: string
  createdAt: string
}

export type OrderStatus = 'PAID' | 'FAILED' | 'PENDING_PAYMENT'
export type PaymentMethod = 'CARD' | 'CASH'

export interface Order {
  id: string
  clientId: string
  clientName: string
  total: number
  paymentMethod: PaymentMethod
  status: OrderStatus
  shippingAddress: string
  notes?: string | null
}

export interface CreateClientPayload {
  name: string
  email: string
  cpf: string
}

export interface UpdateClientPayload {
  name: string
  email: string
}

export interface CreateOrderPayload {
  clientId: string | null
  total: number
  paymentMethod: string
  shippingAddress: string
  notes?: string | null
}
