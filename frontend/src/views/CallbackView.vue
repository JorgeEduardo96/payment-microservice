<template>
  <v-container class="d-flex flex-column align-center justify-center" style="height: 100vh">
    <v-progress-circular indeterminate color="primary" size="48" />
    <p class="mt-4 text-medium-emphasis">Signing you in…</p>
  </v-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  await authStore.handleCallback()
  await router.push('/')
})
</script>
