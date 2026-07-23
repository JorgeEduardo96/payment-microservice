<template>
  <v-app :theme="theme">
    <!-- Navigation Drawer -->
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent color="surface">
      <v-list-item
          prepend-icon="mdi-cash-multiple"
          :title="t('app.name')"
          nav
          class="py-4"
      >
        <template #append>
          <v-btn
              :icon="rail ? 'mdi-chevron-right' : 'mdi-chevron-left'"
              variant="text"
              size="small"
              @click="rail = !rail"
          />
        </template>
      </v-list-item>

      <v-divider/>

      <v-list density="compact" nav class="mt-2">
        <v-list-item
            v-for="item in navItems"
            :key="item.to"
            :prepend-icon="item.icon"
            :title="item.title"
            :to="item.to"
            :value="item.to"
            color="primary"
            rounded="lg"
            class="mb-1"
        />
      </v-list>

      <template #append>
        <v-divider/>
        <v-list density="compact" nav class="my-1">
          <v-list-item
              :prepend-icon="theme === 'dark' ? 'mdi-weather-sunny' : 'mdi-weather-night'"
              :title="theme === 'dark' ? t('app.lightMode') : t('app.darkMode')"
              rounded="lg"
              @click="toggleTheme"
          />
        </v-list>
      </template>
    </v-navigation-drawer>

    <!-- App Bar -->
    <v-app-bar elevation="0" border="b">
      <v-app-bar-nav-icon class="d-lg-none" @click="drawer = !drawer"/>
      <v-app-bar-title>
        <v-breadcrumbs :items="breadcrumbs" density="compact" class="pa-0"/>
      </v-app-bar-title>
      <template #append>
        <v-chip color="success" size="small" variant="tonal" class="mr-4">
          <v-icon start size="8">mdi-circle</v-icon>
          {{ t('app.gatewayBadge') }}
        </v-chip>
        <v-menu>
          <template #activator="{ props }">
            <v-btn icon variant="text" v-bind="props" class="mr-2">
              <v-icon>mdi-translate</v-icon>
            </v-btn>
          </template>
          <v-list density="compact">
            <v-list-item
                v-for="loc in SUPPORTED_LOCALES"
                :key="loc"
                :title="localeLabels[loc]"
                :active="locale === loc"
                @click="setLocale(loc)"
            />
          </v-list>
        </v-menu>
        <NotificationBell class="mr-2"/>
        <v-menu v-if="authStore.isAuthenticated">
          <template #activator="{ props }">
            <v-btn variant="text" prepend-icon="mdi-account-circle" v-bind="props">
              {{ authStore.username }}
            </v-btn>
          </template>
          <v-list>
            <v-list-item prepend-icon="mdi-logout" :title="t('app.logout')" @click="authStore.logout()"/>
          </v-list>
        </v-menu>
      </template>
    </v-app-bar>

    <!-- Main Content -->
    <v-main>
      <v-container fluid class="pa-6">
        <router-view/>
      </v-container>
    </v-main>

    <!-- Global Snackbar -->
    <v-snackbar
        v-model="appStore.snackbar.show"
        :color="appStore.snackbar.color"
        :timeout="4000"
        location="bottom right"
        rounded="lg"
    >
      <v-icon class="mr-2">
        {{ appStore.snackbar.color === 'error' ? 'mdi-alert-circle' : 'mdi-check-circle' }}
      </v-icon>
      {{ appStore.snackbar.message }}
      <template #actions>
        <v-btn variant="text" icon="mdi-close" @click="appStore.snackbar.show = false"/>
      </template>
    </v-snackbar>
  </v-app>
</template>

<script setup lang="ts">
import {computed, onUnmounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {useI18n} from 'vue-i18n'
import {useAppStore} from '@/stores/app'
import {useAuthStore} from '@/stores/auth'
import {useNotificationsStore} from '@/stores/notifications'
import NotificationBell from '@/components/NotificationBell.vue'
import {SUPPORTED_LOCALES, setLocale} from '@/plugins/i18n'
import type {SupportedLocale} from '@/plugins/i18n'

const drawer = ref(true)
const rail = ref(false)
const theme = ref('light')
const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()
const notificationsStore = useNotificationsStore()
const {t, locale} = useI18n()

const localeLabels: Record<SupportedLocale, string> = {
  en: 'English',
  pt: 'Português',
  es: 'Español',
}

watch(
    () => authStore.isAdmin || authStore.isClient,
    (shouldConnect) => {
      if (shouldConnect) notificationsStore.connect()
      else notificationsStore.disconnect()
    },
    {immediate: true},
)
onUnmounted(() => notificationsStore.disconnect())

const navItems = computed(() => [
  {title: t('nav.dashboard'), icon: 'mdi-view-dashboard-outline', to: '/'},
  ...(authStore.isAdmin ? [{title: t('nav.clients'), icon: 'mdi-account-group-outline', to: '/clients'}] : []),
  {title: t('nav.orders'), icon: 'mdi-cart-outline', to: '/orders'},
])

const breadcrumbs = computed(() => {
  const routeName = route.name?.toString() ?? ''
  if (routeName === 'home') return [{title: t('nav.dashboard'), disabled: true}]
  const titleKey = route.meta.titleKey
  return [
    {title: t('nav.dashboard'), to: '/', disabled: false},
    {title: titleKey ? t(titleKey) : routeName, disabled: true},
  ]
})

const toggleTheme = (): void => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
}
</script>
