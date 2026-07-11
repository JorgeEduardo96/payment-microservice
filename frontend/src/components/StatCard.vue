<template>
  <v-card rounded="xl" elevation="0" border style="cursor: pointer" :to="to">
    <v-card-text class="pa-5">
      <div class="d-flex align-center justify-space-between mb-3">
        <v-icon :color="iconColor" size="28">{{ icon }}</v-icon>
        <div v-if="value" class="text-h4 font-weight-bold" :class="`text-${iconColor}`">{{ value }}</div>
      </div>
      <div class="text-body-2 text-medium-emphasis">{{ label }}</div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
defineProps<{
  icon: string
  iconColor: string
  label: string
  value: number
  to: string
}>()
</script>
