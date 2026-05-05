import type { AxiosResponse } from 'axios'
import http from './http'
import type { Order, CreateOrderPayload } from '@/types'

export const orderApi = {
  create: (data: CreateOrderPayload): Promise<AxiosResponse<Order>> => http.post('/order', data),
  getByClientId: (clientId: string): Promise<AxiosResponse<Order[]>> => http.get(`/order/client/${clientId}`),
}
