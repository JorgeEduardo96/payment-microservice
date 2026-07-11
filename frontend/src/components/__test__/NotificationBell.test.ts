import { describe, it, expect, afterEach, vi } from 'vitest'
import { mount, flushPromises, type VueWrapper } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { VBadge } from 'vuetify/components'
import NotificationBell from '../NotificationBell.vue'
import { useNotificationsStore } from '@/stores/notifications'
import type { NotificationMessage } from '@/types'

let activeWrapper: VueWrapper | null = null

function setup(notifications: NotificationMessage[] = [], unreadCount = 0) {
  const pinia = createTestingPinia({ stubActions: false, createSpy: (fn) => vi.fn(fn) })
  const store = useNotificationsStore(pinia)
  store.notifications = notifications
  store.unreadCount = unreadCount

  const wrapper = mount(NotificationBell, {
    attachTo: document.body,
    global: { plugins: [pinia] },
  })
  activeWrapper = wrapper

  return { wrapper, store }
}

describe('NotificationBell', () => {
  afterEach(() => {
    activeWrapper?.unmount()
    activeWrapper = null
  })

  it('binds the unread count and visibility to the badge', () => {
    const { wrapper } = setup([], 3)

    const badge = wrapper.findComponent(VBadge)

    expect(badge.props('modelValue')).toBe(true)
    expect(badge.props('content')).toBe(3)
  })

  it('hides the badge when there are no unread notifications', () => {
    const { wrapper } = setup([], 0)

    const badge = wrapper.findComponent(VBadge)

    expect(badge.props('modelValue')).toBe(false)
  })

  it('shows the empty state when there are no notifications', async () => {
    setup([], 0)

    await document.body.querySelector('button')?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await flushPromises()

    expect(document.body.textContent).toContain('Nenhuma notificação por enquanto')
  })

  it('lists notifications and marks them as read when opened', async () => {
    const notification: NotificationMessage = {
      type: 'CLIENT_CREATED',
      title: 'New client',
      message: 'John Doe',
      timestamp: '2026-01-01T10:00:00Z',
    }
    const { store } = setup([notification], 1)

    await document.body.querySelector('button')?.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await flushPromises()

    expect(document.body.textContent).toContain('New client')
    expect(document.body.textContent).toContain('John Doe')
    expect(store.unreadCount).toBe(0)
  })
})
