import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAppStore } from '../app'

describe('useAppStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('notify sets message, color and shows the snackbar', () => {
    const store = useAppStore()

    store.notify('Saved successfully')

    expect(store.snackbar.message).toBe('Saved successfully')
    expect(store.snackbar.color).toBe('success')
    expect(store.snackbar.show).toBe(true)
  })

  it('notify accepts a custom color', () => {
    const store = useAppStore()

    store.notify('Careful', 'warning')

    expect(store.snackbar.color).toBe('warning')
  })

  it('notifyError uses the response detail when available', () => {
    const store = useAppStore()

    store.notifyError({ response: { data: { detail: 'CPF already in use' } } })

    expect(store.snackbar.message).toBe('CPF already in use')
    expect(store.snackbar.color).toBe('error')
  })

  it('notifyError falls back to the response title when detail is missing', () => {
    const store = useAppStore()

    store.notifyError({ response: { data: { title: 'Bad Request' } } })

    expect(store.snackbar.message).toBe('Bad Request')
  })

  it('notifyError falls back to the error message when there is no response', () => {
    const store = useAppStore()

    store.notifyError({ message: 'Network Error' })

    expect(store.snackbar.message).toBe('Network Error')
  })

  it('notifyError falls back to a generic message when nothing else is available', () => {
    const store = useAppStore()

    store.notifyError({})

    expect(store.snackbar.message).toBe('An unexpected error occurred')
  })
})
