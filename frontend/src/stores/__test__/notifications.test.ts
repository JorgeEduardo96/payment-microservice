import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import type { NotificationMessage } from '@/types'

interface CapturedStompConfig {
  onConnect?: () => void
  onDisconnect?: () => void
}

const activateMock = vi.fn()
const deactivateMock = vi.fn()
const subscribeMock = vi.fn()
let capturedConfig: CapturedStompConfig = {}

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn().mockImplementation(function (config: CapturedStompConfig) {
    capturedConfig = config
    return {
      activate: activateMock,
      deactivate: deactivateMock,
      subscribe: subscribeMock,
    }
  }),
}))

const fetchAllMock = vi.fn()
vi.mock('@/stores/clients.ts', () => ({
  useClientsStore: () => ({ fetchAll: fetchAllMock }),
}))

const updateOrderStatusMock = vi.fn()
vi.mock('@/stores/orders.ts', () => ({
  useOrdersStore: () => ({ updateOrderStatus: updateOrderStatusMock }),
}))

const authStoreState = { isAdmin: true, isClient: false }
vi.mock('@/stores/auth.ts', () => ({
  useAuthStore: () => authStoreState,
}))

vi.mock('@/auth/userManager', () => ({
  default: { getUser: vi.fn().mockResolvedValue({ access_token: 'fake-access-token' }) },
}))

const notification: NotificationMessage = {
  type: 'CLIENT_CREATED',
  title: 'New client registered',
  message: 'John Doe',
  timestamp: '2026-01-01T00:00:00Z',
}

describe('useNotificationsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.resetModules()
    capturedConfig = {}
    authStoreState.isAdmin = true
    authStoreState.isClient = false
  })

  it('connect activates a STOMP client pointing at /ws-notifications', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()

    expect(activateMock).toHaveBeenCalled()
    expect(store.connected).toBe(false)
  })

  it('connect is a no-op when already connected', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    await store.connect()

    expect(activateMock).toHaveBeenCalledTimes(1)
  })

  it('marks connected and subscribes to the notifications topic on onConnect', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()

    expect(store.connected).toBe(true)
    expect(subscribeMock).toHaveBeenCalledWith('/topic/notifications', expect.any(Function))
  })

  it('appends incoming notifications and increments unreadCount', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()
    const handler = subscribeMock.mock.calls[0][1]
    handler({ body: JSON.stringify(notification) })

    expect(store.notifications).toEqual([notification])
    expect(store.unreadCount).toBe(1)
    expect(store.hasUnread).toBe(true)
    expect(fetchAllMock).toHaveBeenCalled()
  })

  it('onDisconnect marks the store as disconnected', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()
    capturedConfig.onDisconnect?.()

    expect(store.connected).toBe(false)
  })

  it('disconnect deactivates the STOMP client and resets state', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()
    store.disconnect()

    expect(deactivateMock).toHaveBeenCalled()
    expect(store.connected).toBe(false)
  })

  it('markAllRead resets unreadCount without clearing notifications', async () => {
    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()
    const handler = subscribeMock.mock.calls[0][1]
    handler({ body: JSON.stringify(notification) })

    store.markAllRead()

    expect(store.unreadCount).toBe(0)
    expect(store.hasUnread).toBe(false)
    expect(store.notifications).toEqual([notification])
  })

  it('subscribes to the per-user queue instead of the broadcast topic for CLIENT-role users', async () => {
    authStoreState.isAdmin = false
    authStoreState.isClient = true

    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()

    expect(subscribeMock).toHaveBeenCalledWith('/user/queue/notifications', expect.any(Function))
    expect(subscribeMock).not.toHaveBeenCalledWith('/topic/notifications', expect.any(Function))
  })

  it('updates the matching order status when a PAYMENT_CONFIRMED notification arrives', async () => {
    authStoreState.isAdmin = false
    authStoreState.isClient = true

    const paymentNotification: NotificationMessage = {
      type: 'PAYMENT_CONFIRMED',
      title: 'Payment confirmed',
      message: 'Your order has been paid.',
      timestamp: '2026-01-01T00:00:00Z',
      orderId: 'order-1',
      orderStatus: 'PAID',
    }

    const { useNotificationsStore } = await import('../notifications')
    const store = useNotificationsStore()

    await store.connect()
    capturedConfig.onConnect?.()
    const handler = subscribeMock.mock.calls[0][1]
    handler({ body: JSON.stringify(paymentNotification) })

    expect(updateOrderStatusMock).toHaveBeenCalledWith('order-1', 'PAID')
  })
})
