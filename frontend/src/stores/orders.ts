import { defineStore } from 'pinia'
import { ref } from 'vue'
import { orderApi } from '@/api/orderApi'
import type { Order, CreateOrderPayload } from '@/types'

export const useOrdersStore = defineStore('orders', () => {
  const orders = ref<Order[]>([])
  const loading = ref(false)
  const activeClientId = ref<string | null>(null)

  async function create(data: CreateOrderPayload): Promise<Order> {
    loading.value = true
    try {
      const { data: order } = await orderApi.create(data)
      if (activeClientId.value === data.clientId) {
        orders.value.unshift(order)
      }
      return order
    } finally {
      loading.value = false
    }
  }

  async function fetchByClient(clientId: string): Promise<Order[]> {
    loading.value = true
    activeClientId.value = clientId
    try {
      const { data } = await orderApi.getByClientId(clientId)
      orders.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  function clear(): void {
    orders.value = []
    activeClientId.value = null
  }

  return { orders, loading, activeClientId, create, fetchByClient, clear }
})
