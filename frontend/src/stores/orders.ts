import {defineStore} from 'pinia'
import {ref} from 'vue'
import {orderApi} from '@/api/orderApi'
import type {CreateOrderPayload, Order, OrderStatus} from '@/types'

export const useOrdersStore = defineStore('orders', () => {
    const orders = ref<Order[]>([])
    const allOrders = ref<Order[]>([])
    const loading = ref(false)
    const activeClientId = ref<string | null>(null)

    async function create(data: CreateOrderPayload): Promise<Order> {
        loading.value = true
        try {
            const {data: order} = await orderApi.create(data)
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
            const {data} = await orderApi.getByClientId(clientId)
            orders.value = data
            return data
        } finally {
            loading.value = false
        }
    }

    async function fetchAll(): Promise<Order[]> {
        loading.value = true
        try {
            const {data} = await orderApi.getAll()
            allOrders.value = data
            return data
        } finally {
            loading.value = false
        }
    }

    function clear(): void {
        orders.value = []
        activeClientId.value = null
    }

    function updateOrderStatus(orderId: string, status: OrderStatus): void {
        const order = orders.value.find((o) => o.id === orderId)
        if (order) order.status = status

        const orderInAllOrders = allOrders.value.find((o) => o.id === orderId)
        if (orderInAllOrders) orderInAllOrders.status = status
    }

    return {orders, allOrders, loading, activeClientId, create, fetchByClient, fetchAll, clear, updateOrderStatus}
})
