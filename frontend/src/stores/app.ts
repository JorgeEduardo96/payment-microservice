import { defineStore } from 'pinia'
import { reactive } from 'vue'
import i18n from '@/plugins/i18n'

interface Snackbar {
  show: boolean
  message: string
  color: string
}

export const useAppStore = defineStore('app', () => {
  const snackbar = reactive<Snackbar>({
    show: false,
    message: '',
    color: 'success',
  })

  function notify(message: string, color = 'success'): void {
    snackbar.message = message
    snackbar.color = color
    snackbar.show = true
  }

  function notifyError(err: unknown): void {
    const error = err as { response?: { data?: { detail?: string; title?: string } }; message?: string }
    const message =
      error?.response?.data?.detail ||
      error?.response?.data?.title ||
      error?.message ||
      i18n.global.t('errors.unexpected')
    notify(message, 'error')
  }

  return { snackbar, notify, notifyError }
})
