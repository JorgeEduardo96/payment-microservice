<template>
  <v-menu location="bottom end" :close-on-content-click="false" @update:model-value="onToggle">
    <template #activator="{ props }">
      <v-btn icon variant="text" v-bind="props">
        <v-badge :content="store.unreadCount" :model-value="store.hasUnread" color="error" floating>
          <v-icon>mdi-bell-outline</v-icon>
        </v-badge>
      </v-btn>
    </template>

    <v-card min-width="320" max-width="360">
      <v-card-title class="text-subtitle-1">Notificações</v-card-title>
      <v-divider />
      <v-list v-if="store.notifications.length" density="compact" max-height="320" class="overflow-y-auto">
        <v-list-item
          v-for="(notification, index) in store.notifications"
          :key="index"
          :title="notification.title"
          :subtitle="notification.message"
        >
          <template #append>
            <span class="text-caption text-medium-emphasis">{{ formatTime(notification.timestamp) }}</span>
          </template>
        </v-list-item>
      </v-list>
      <v-card-text v-else class="text-medium-emphasis text-center py-6">
        Nenhuma notificação por enquanto
      </v-card-text>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { useNotificationsStore } from '@/stores/notifications'

const store = useNotificationsStore()

function onToggle(open: boolean): void {
  if (open) store.markAllRead()
}

function formatTime(timestamp: string): string {
  return new Date(timestamp).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}
</script>
