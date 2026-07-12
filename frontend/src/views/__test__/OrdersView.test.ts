import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises, type VueWrapper } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createMemoryHistory, type Router } from 'vue-router'
import { VSelect } from 'vuetify/components'
import OrdersView from '../OrdersView.vue'
import { useAppStore } from '@/stores/app'
import { useOrdersStore } from '@/stores/orders'
import { clientApi } from '@/api/clientApi'
import { orderApi } from '@/api/orderApi'
import type { Order } from '@/types'

vi.mock('@/api/clientApi', () => ({
  clientApi: { getAll: vi.fn(), create: vi.fn(), getById: vi.fn(), update: vi.fn() },
}))
vi.mock('@/api/orderApi', () => ({
  orderApi: { create: vi.fn(), getByClientId: vi.fn(), getAll: vi.fn() },
}))

const order: Order = {
  id: 'o1',
  clientId: 'c1',
  clientName: 'John Doe',
  total: 100,
  paymentMethod: 'CARD',
  status: 'PENDING_PAYMENT',
  shippingAddress: 'Main St',
}

function createTestRouter(): Router {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/orders', component: { template: '<div />' } },
    ],
  })
}

function findButtonByText(text: string): HTMLButtonElement {
  const button = Array.from(document.body.querySelectorAll<HTMLButtonElement>('button')).find((btn) =>
    btn.textContent?.includes(text),
  )
  if (!button) throw new Error(`Button with text "${text}" not found`)
  return button
}

function findInputByLabel(label: string): HTMLInputElement {
  const target = Array.from(document.body.querySelectorAll<HTMLLabelElement>('label[for]')).find(
    (el) => el.textContent?.trim() === label,
  )
  if (!target) throw new Error(`Label "${label}" not found`)
  const input = document.getElementById(target.getAttribute('for')!) as HTMLInputElement | null
  if (!input) throw new Error(`Input for label "${label}" not found`)
  return input
}

function setInputValue(input: HTMLInputElement, value: string): void {
  input.value = value
  input.dispatchEvent(new Event('input'))
}

async function selectOption(wrapper: VueWrapper, selectLabel: string, optionValue: string): Promise<void> {
  const select = wrapper.findAllComponents(VSelect).find((c) => c.props('label') === selectLabel)
  if (!select) throw new Error(`VSelect with label "${selectLabel}" not found`)
  await select.setValue(optionValue)
}

let activeWrapper: VueWrapper | null = null

async function mountView(initialPath = '/orders') {
  const router = createTestRouter()
  await router.push(initialPath)
  await router.isReady()

  const wrapper = mount(OrdersView, {
    attachTo: document.body,
    global: { plugins: [router] },
  })
  activeWrapper = wrapper
  await flushPromises()
  return { wrapper, router }
}

describe('OrdersView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(clientApi.getAll).mockResolvedValue({ data: [] } as never)
  })

  afterEach(() => {
    activeWrapper?.unmount()
    activeWrapper = null
  })

  it('fetches clients on mount when the store is empty', async () => {
    await mountView()

    expect(clientApi.getAll).toHaveBeenCalled()
  })

  it('auto-loads orders when redirected with a clientId query param', async () => {
    vi.mocked(orderApi.getByClientId).mockResolvedValue({ data: [order] } as never)

    const { router } = await mountView('/orders?clientId=c1')
    await vi.waitFor(() => {
      expect(orderApi.getByClientId).toHaveBeenCalledWith('c1')
    })

    expect(useOrdersStore().activeClientId).toBe('c1')
    expect(router.currentRoute.value.query.clientId).toBe('c1')
  })

  it('warns when trying to load orders without selecting a client', async () => {
    await mountView()

    findButtonByText('Load Orders').click()
    await flushPromises()

    expect(useAppStore().snackbar.message).toBe('Please select or enter a client ID')
    expect(useAppStore().snackbar.color).toBe('warning')
    expect(orderApi.getByClientId).not.toHaveBeenCalled()
  })

  it('loads orders for a manually entered client id and notifies the count', async () => {
    vi.mocked(orderApi.getByClientId).mockResolvedValue({ data: [order] } as never)
    await mountView()

    setInputValue(findInputByLabel('Enter Client UUID'), 'c1')
    await flushPromises()
    findButtonByText('Load Orders').click()

    await vi.waitFor(() => {
      expect(useAppStore().snackbar.message).toBe('1 order(s) loaded')
    })
    expect(orderApi.getByClientId).toHaveBeenCalledWith('c1')
  })

  it('disables the New Order button until a client is active', async () => {
    const { wrapper } = await mountView()

    expect(findButtonByText('New Order').disabled).toBe(true)

    vi.mocked(orderApi.getByClientId).mockResolvedValue({ data: [] } as never)
    setInputValue(findInputByLabel('Enter Client UUID'), 'c1')
    await flushPromises()
    findButtonByText('Load Orders').click()
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(findButtonByText('New Order').disabled).toBe(false)
  })

  it('creates an order for the active client with the correct payload', async () => {
    vi.mocked(orderApi.getByClientId).mockResolvedValue({ data: [] } as never)
    vi.mocked(orderApi.create).mockResolvedValue({ data: order } as never)
    const { wrapper } = await mountView()

    setInputValue(findInputByLabel('Enter Client UUID'), 'c1')
    await flushPromises()
    findButtonByText('Load Orders').click()
    await flushPromises()

    findButtonByText('New Order').click()
    await flushPromises()

    setInputValue(findInputByLabel('Total (R$)'), '100')
    await selectOption(wrapper, 'Payment Method', 'CARD')
    setInputValue(findInputByLabel('Shipping Address'), 'Main St')
    await flushPromises()

    findButtonByText('Place Order').click()

    await vi.waitFor(() => {
      expect(orderApi.create).toHaveBeenCalled()
    })
    expect(orderApi.create).toHaveBeenCalledWith({
      clientId: 'c1',
      total: 100,
      paymentMethod: 'CARD',
      shippingAddress: 'Main St',
      notes: null,
    })
    expect(useAppStore().snackbar.message).toBe('Order placed! Payment is processing asynchronously via Kafka.')
  })
})
