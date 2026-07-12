import { defineStore } from 'pinia'
import { ref } from 'vue'
import { clientApi } from '@/api/clientApi'
import type { Client, CreateClientPayload, UpdateClientPayload } from '@/types'

export const useClientsStore = defineStore('clients', () => {
  const clients = ref<Client[]>([])
  const loading = ref(false)

  async function fetchAll(): Promise<void> {
    loading.value = true
    try {
      const { data } = await clientApi.getAll()
      clients.value = data
    } finally {
      loading.value = false
    }
  }

  async function create(data: CreateClientPayload): Promise<Client> {
    loading.value = true
    try {
      const { data: client } = await clientApi.create(data)
      clients.value.unshift(client)
      return client
    } finally {
      loading.value = false
    }
  }

  async function fetchById(id: string): Promise<Client> {
    loading.value = true
    try {
      const { data: client } = await clientApi.getById(id)
      const idx = clients.value.findIndex((c) => c.id === id)
      if (idx >= 0) clients.value[idx] = client
      else clients.value.unshift(client)
      return client
    } finally {
      loading.value = false
    }
  }

  async function update(id: string, data: UpdateClientPayload): Promise<Client> {
    loading.value = true
    try {
      const { data: client } = await clientApi.update(id, data)
      const idx = clients.value.findIndex((c) => c.id === id)
      if (idx >= 0) clients.value[idx] = client
      return client
    } finally {
      loading.value = false
    }
  }

  return { clients, loading, fetchAll, create, fetchById, update }
})
