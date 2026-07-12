<template>
  <v-app :theme="theme">
    <!-- Navigation Drawer -->
    <v-navigation-drawer v-model="drawer" :rail="rail" permanent color="surface">
      <v-list-item
        prepend-icon="mdi-cash-multiple"
        title="Payment App"
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

      <v-divider />

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
        <v-divider />
        <v-list density="compact" nav class="my-1">
          <v-list-item
            :prepend-icon="theme === 'dark' ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            :title="theme === 'dark' ? 'Light Mode' : 'Dark Mode'"
            rounded="lg"
            @click="toggleTheme"
          />
        </v-list>
      </template>
    </v-navigation-drawer>

    <!-- App Bar -->
    <v-app-bar elevation="0" border="b">
      <v-app-bar-nav-icon class="d-lg-none" @click="drawer = !drawer" />
      <v-app-bar-title>
        <v-breadcrumbs :items="breadcrumbs" density="compact" class="pa-0" />
      </v-app-bar-title>
      <template #append>
        <v-chip color="success" size="small" variant="tonal" class="mr-4">
          <v-icon start size="8">mdi-circle</v-icon>
          API Gateway :8080
        </v-chip>
        <NotificationBell class="mr-2" />
      </template>
    </v-app-bar>

    <!-- Main Content -->
    <v-main>
      <v-container fluid class="pa-6">
        <router-view />
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
        <v-btn variant="text" icon="mdi-close" @click="appStore.snackbar.show = false" />
      </template>
    </v-snackbar>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useNotificationsStore } from '@/stores/notifications'
import NotificationBell from '@/components/NotificationBell.vue'

const drawer = ref(true)
const rail = ref(false)
const theme = ref('light')
const route = useRoute()
const appStore = useAppStore()
const notificationsStore = useNotificationsStore()

onMounted(() => notificationsStore.connect())
onUnmounted(() => notificationsStore.disconnect())

const navItems = [
  { title: 'Dashboard', icon: 'mdi-view-dashboard-outline', to: '/' },
  { title: 'Clients', icon: 'mdi-account-group-outline', to: '/clients' },
  { title: 'Orders', icon: 'mdi-cart-outline', to: '/orders' },
]

const breadcrumbs = computed(() => {
  const routeName = route.name?.toString() ?? ''
  if (routeName === 'home') return [{ title: 'Dashboard', disabled: true }]
  return [
    { title: 'Dashboard', to: '/', disabled: false },
    { title: routeName.charAt(0).toUpperCase() + routeName.slice(1), disabled: true },
  ]
})

const toggleTheme = (): void => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
}
</script>
