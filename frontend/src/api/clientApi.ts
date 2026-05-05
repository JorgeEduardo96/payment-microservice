import type { AxiosResponse } from 'axios'
import http from './http'
import type { Client, CreateClientPayload, UpdateClientPayload } from '@/types'

export const clientApi = {
  getAll: (): Promise<AxiosResponse<Client[]>> => http.get('/client'),
  create: (data: CreateClientPayload): Promise<AxiosResponse<Client>> => http.post('/client', data),
  getById: (id: string): Promise<AxiosResponse<Client>> => http.get(`/client/${id}`),
  update: (id: string, data: UpdateClientPayload): Promise<AxiosResponse<Client>> => http.put(`/client/${id}`, data),
}
