import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAdmin?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: 'Dashboard' },
  },
  {
    path: '/clients',
    name: 'clients',
    component: () => import('@/views/ClientsView.vue'),
    meta: { title: 'Clients', requiresAdmin: true },
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('@/views/OrdersView.vue'),
    meta: { title: 'Orders' },
  },
  {
    path: '/callback',
    name: 'callback',
    component: () => import('@/views/CallbackView.vue'),
    meta: { title: 'Signing in' },
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
    useAppStore().notify('You do not have permission to access this page', 'warning')
    return { name: 'home' }
  }

  return true
})

router.afterEach((to) => {
  document.title = `${to.meta.title ?? 'Payment'} — Payment Microservice`
})

export default router
