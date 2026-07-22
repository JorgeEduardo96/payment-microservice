import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from 'oidc-client-ts'
import userManager from '@/auth/userManager'

interface RealmAccessClaim {
  realm_access?: { roles?: string[] }
  clientId?: string
}

function decodeJwtPayload(token: string): RealmAccessClaim {
  const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
  const json = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
      .join(''),
  )
  return JSON.parse(json)
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!user.value && !user.value.expired)
  const accessToken = computed<string | null>(() => user.value?.access_token ?? null)
  const username = computed<string | null>(() => (user.value?.profile.preferred_username as string) ?? null)

  const roles = computed<string[]>(() => {
    if (!user.value?.access_token) return []
    return decodeJwtPayload(user.value.access_token).realm_access?.roles ?? []
  })
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isClient = computed(() => roles.value.includes('CLIENT'))

  const clientId = computed<string | null>(() => {
    if (!user.value?.access_token) return null
    return decodeJwtPayload(user.value.access_token).clientId ?? null
  })

  async function init(): Promise<void> {
    user.value = await userManager.getUser()
  }

  function login(): Promise<void> {
    return userManager.signinRedirect()
  }

  async function handleCallback(): Promise<void> {
    user.value = await userManager.signinRedirectCallback()
  }

  function logout(): Promise<void> {
    return userManager.signoutRedirect()
  }

  userManager.events.addUserLoaded((loadedUser) => {
    user.value = loadedUser
  })
  userManager.events.addUserUnloaded(() => {
    user.value = null
  })

  return { user, isAuthenticated, accessToken, username, roles, isAdmin, isClient, clientId, init, login, handleCallback, logout }
})
