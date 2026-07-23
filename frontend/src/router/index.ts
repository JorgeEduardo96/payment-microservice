import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import i18n from '@/plugins/i18n'

declare module 'vue-router' {
  interface RouteMeta {
    titleKey?: string
    requiresAdmin?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { titleKey: 'nav.dashboard' },
  },
  {
    path: '/clients',
    name: 'clients',
    component: () => import('@/views/ClientsView.vue'),
    meta: { titleKey: 'nav.clients', requiresAdmin: true },
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('@/views/OrdersView.vue'),
    meta: { titleKey: 'nav.orders' },
  },
  {
    path: '/callback',
    name: 'callback',
    component: () => import('@/views/CallbackView.vue'),
    meta: { titleKey: 'routeTitles.signingIn' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

let authInitialized = false

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authInitialized) {
    await authStore.init()
    authInitialized = true
  }

  if (to.name === 'callback') return true

  if (!authStore.isAuthenticated) {
    await authStore.login()
    return false
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    useAppStore().notify(i18n.global.t('errors.noPermission'), 'warning')
    return { name: 'home' }
  }

  return true
})

router.afterEach((to) => {
  const title = to.meta.titleKey ? i18n.global.t(to.meta.titleKey) : 'Payment'
  document.title = `${title} — ${i18n.global.t('app.documentTitleSuffix')}`
})

export default router
