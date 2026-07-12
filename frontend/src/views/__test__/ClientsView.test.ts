import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises, type VueWrapper } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createMemoryHistory, type Router } from 'vue-router'
import { VDialog } from 'vuetify/components'
import ClientsView from '../ClientsView.vue'
import { useAppStore } from '@/stores/app'
import { clientApi } from '@/api/clientApi'
import type { Client } from '@/types'

vi.mock('@/api/clientApi', () => ({
  clientApi: { getAll: vi.fn(), create: vi.fn(), getById: vi.fn(), update: vi.fn() },
}))

const client: Client = { id: 'c1', name: 'John Doe', email: 'john@doe.com', cpf: '52998224725', createdAt: '2026-01-01' }

function createTestRouter(): Router {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/clients', component: { template: '<div />' } },
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

function findButtonByIcon(iconClass: string): HTMLButtonElement {
  const button = Array.from(document.body.querySelectorAll<HTMLButtonElement>('button')).find((btn) =>
    btn.querySelector(`.${iconClass}`),
  )
  if (!button) throw new Error(`Button with icon "${iconClass}" not found`)
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

let activeWrapper: VueWrapper | null = null

async function mountView() {
  const router = createTestRouter()
  await router.push('/clients')
  await router.isReady()

  const wrapper = mount(ClientsView, {
    attachTo: document.body,
    global: { plugins: [router] },
  })
  activeWrapper = wrapper
  await flushPromises()
  return { wrapper, router }
}

describe('ClientsView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(clientApi.getAll).mockResolvedValue({ data: [] } as never)
  })

  afterEach(() => {
    activeWrapper?.unmount()
    activeWrapper = null
  })

  it('fetches all clients on mount', async () => {
    await mountView()

    expect(clientApi.getAll).toHaveBeenCalled()
  })

  it('creates a client with the CPF digits stripped and notifies success', async () => {
    vi.mocked(clientApi.create).mockResolvedValue({ data: client } as never)
    const { wrapper } = await mountView()

    findButtonByText('New Client').click()
    await flushPromises()

    setInputValue(findInputByLabel('Full Name'), 'John Doe')
    setInputValue(findInputByLabel('Email'), 'john@doe.com')
    setInputValue(findInputByLabel('CPF'), '529.982.247-25')
    await flushPromises()

    findButtonByText('Create Client').click()

    await vi.waitFor(() => {
      expect(wrapper.findComponent(VDialog).props('modelValue')).toBe(false)
    })
    expect(clientApi.create).toHaveBeenCalledWith({ name: 'John Doe', email: 'john@doe.com', cpf: '52998224725' })
    expect(useAppStore().snackbar.message).toBe('Client created successfully')
  })

  it('notifies an error when creation fails', async () => {
    vi.mocked(clientApi.create).mockRejectedValue({ message: 'Network Error' })
    await mountView()

    findButtonByText('New Client').click()
    await flushPromises()

    setInputValue(findInputByLabel('Full Name'), 'John Doe')
    setInputValue(findInputByLabel('Email'), 'john@doe.com')
    setInputValue(findInputByLabel('CPF'), '529.982.247-25')
    await flushPromises()

    findButtonByText('Create Client').click()

    await vi.waitFor(() => {
      expect(useAppStore().snackbar.color).toBe('error')
    })
    expect(useAppStore().snackbar.message).toBe('Network Error')
  })

  it('searches for a client by id and notifies success', async () => {
    vi.mocked(clientApi.getById).mockResolvedValue({ data: client } as never)
    await mountView()

    setInputValue(findInputByLabel('Fetch client by UUID'), client.id)
    await flushPromises()

    findButtonByText('Search').click()
    await flushPromises()

    expect(clientApi.getById).toHaveBeenCalledWith(client.id)
    expect(useAppStore().snackbar.message).toBe('Client loaded successfully')
  })

  it('navigates to orders with the clientId query when the cart icon is clicked', async () => {
    vi.mocked(clientApi.getAll).mockResolvedValue({ data: [client] } as never)
    const { router } = await mountView()

    findButtonByIcon('mdi-cart-outline').click()

    await vi.waitFor(() => {
      expect(router.currentRoute.value.path).toBe('/orders')
    })
    expect(router.currentRoute.value.query.clientId).toBe(client.id)
  })
})
