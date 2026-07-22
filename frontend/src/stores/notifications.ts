import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {Client} from '@stomp/stompjs'
import type {NotificationMessage, OrderStatus} from '@/types'
import {useClientsStore} from "@/stores/clients.ts";
import {useOrdersStore} from "@/stores/orders.ts";
import {useAuthStore} from "@/stores/auth.ts";
import userManager from '@/auth/userManager'

const BROADCAST_DESTINATION = '/topic/notifications'
const USER_DESTINATION = '/user/queue/notifications'

export const useNotificationsStore = defineStore('notifications', () => {
    const notifications = ref<NotificationMessage[]>([])
    const unreadCount = ref(0)
    const connected = ref(false)

    const clientStore = useClientsStore();
    const ordersStore = useOrdersStore();
    const authStore = useAuthStore();

    let stompClient: Client | null = null

    const hasUnread = computed(() => unreadCount.value > 0)

    function handleNotification(notification: NotificationMessage): void {
        notifications.value.unshift(notification)
        unreadCount.value += 1

        if (notification.type === 'CLIENT_CREATED') {
            clientStore.fetchAll()
        } else if (notification.type === 'PAYMENT_CONFIRMED' || notification.type === 'PAYMENT_FAILED') {
            if (notification.orderId && notification.orderStatus) {
                ordersStore.updateOrderStatus(notification.orderId, notification.orderStatus as OrderStatus)
            }
        }
    }

    async function connect(): Promise<void> {
        if (stompClient) return

        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
        const user = await userManager.getUser()

        stompClient = new Client({
            brokerURL: `${protocol}://${window.location.host}/ws-notifications`,
            connectHeaders: user?.access_token ? { Authorization: `Bearer ${user.access_token}` } : {},
            reconnectDelay: 5000,
            onConnect: () => {
                connected.value = true

                if (authStore.isAdmin) {
                    stompClient?.subscribe(BROADCAST_DESTINATION, (frame) => {
                        handleNotification(JSON.parse(frame.body) as NotificationMessage)
                    })
                }

                if (authStore.isClient) {
                    stompClient?.subscribe(USER_DESTINATION, (frame) => {
                        handleNotification(JSON.parse(frame.body) as NotificationMessage)
                    })
                }
            },
            onDisconnect: () => {
                connected.value = false
            },
        })

        stompClient.activate()
    }

    function disconnect(): void {
        stompClient?.deactivate()
        stompClient = null
        connected.value = false
    }

    function markAllRead(): void {
        unreadCount.value = 0
    }

    return {notifications, unreadCount, connected, hasUnread, connect, disconnect, markAllRead}
})
