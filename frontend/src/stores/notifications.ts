import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {Client} from '@stomp/stompjs'
import type {NotificationMessage} from '@/types'
import {useClientsStore} from "@/stores/clients.ts";

const DESTINATION = '/topic/notifications'

export const useNotificationsStore = defineStore('notifications', () => {
    const notifications = ref<NotificationMessage[]>([])
    const unreadCount = ref(0)
    const connected = ref(false)

    const clientStore = useClientsStore();

    let stompClient: Client | null = null

    const hasUnread = computed(() => unreadCount.value > 0)

    function connect(): void {
        if (stompClient) return

        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'

        stompClient = new Client({
            brokerURL: `${protocol}://${window.location.host}/ws-notifications`,
            reconnectDelay: 5000,
            onConnect: () => {
                connected.value = true
                stompClient?.subscribe(DESTINATION, (frame) => {
                    const notification = JSON.parse(frame.body) as NotificationMessage
                    notifications.value.unshift(notification)
                    unreadCount.value += 1

                    clientStore.fetchAll();
                })
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
