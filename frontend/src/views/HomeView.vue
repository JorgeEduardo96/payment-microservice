<template>
  <div>
    <!-- Hero Banner -->
    <v-card rounded="xl" class="mb-6 overflow-hidden" elevation="0" border>
      <div class="hero-bg pa-8">
        <v-row align="center">
          <v-col cols="12" md="8">
            <div class="text-overline text-medium-emphasis mb-1">Microservices Platform</div>
            <h1 class="text-h4 font-weight-bold mb-2">Payment Microservice</h1>
            <p class="text-body-1 text-medium-emphasis mb-4">
              Manage clients and orders through a distributed Spring Boot microservices architecture
              with Kafka event streaming, gRPC communication, and real-time WebSocket notifications.
            </p>
            <v-btn v-if="isAuthenticatedAndAdmin" color="primary" to="/clients" prepend-icon="mdi-account-plus"
                   class="mr-2">
              New Client
            </v-btn>
            <v-btn variant="tonal" to="/orders" prepend-icon="mdi-cart-plus">
              New Order
            </v-btn>
          </v-col>
          <v-col cols="12" md="4" class="d-none d-md-flex justify-end">
            <v-icon size="140" color="primary" style="opacity: 0.15">mdi-cash-multiple</v-icon>
          </v-col>
        </v-row>
      </div>
    </v-card>

    <!-- Stats Row -->
    <v-row class="mb-6">
      <v-col cols="12" sm="6" md="3" v-if="isAuthenticatedAndAdmin">
        <StatCard
            icon="mdi-account-group"
            icon-color="primary"
            label="Clients Loaded"
            :value="clientsStore.clients.length"
            to="/clients"
        />
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <StatCard
            icon="mdi-cart"
            icon-color="secondary"
            label="Orders Viewed"
            :value="relevantOrders.length"
            to="/orders"
        />
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <StatCard
            icon="mdi-check-circle"
            icon-color="success"
            label="Paid Orders"
            :value="paidOrders"
            to="/orders"
        />
      </v-col>
      <v-col cols="12" sm="6" md="3">
        <StatCard
            icon="mdi-close-circle"
            icon-color="error"
            label="Failed Orders"
            :value="failedOrders"
            to="/orders"
        />
      </v-col>
    </v-row>

    <!-- Services + Flow -->
    <v-row>
      <v-col cols="12" md="6">
        <v-card rounded="xl" elevation="0" border height="100%">
          <v-card-title class="pa-5 pb-0">
            <v-icon start color="primary">mdi-server-network</v-icon>
            Services Architecture
          </v-card-title>
          <v-card-text class="pa-5">
            <v-list density="compact" lines="two">
              <v-list-item
                  v-for="svc in services"
                  :key="svc.name"
                  :prepend-icon="svc.icon"
                  :title="svc.name"
                  :subtitle="svc.description"
                  rounded="lg"
              >
                <template #append>
                  <v-chip size="x-small" :color="svc.color" variant="tonal">
                    :{{ svc.port }}
                  </v-chip>
                </template>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card rounded="xl" elevation="0" border height="100%">
          <v-card-title class="pa-5 pb-0">
            <v-icon start color="secondary">mdi-transit-connection-variant</v-icon>
            Order Flow
          </v-card-title>
          <v-card-text class="pa-5">
            <v-timeline density="compact" side="end">
              <v-timeline-item
                  v-for="step in orderFlow"
                  :key="step.title"
                  :dot-color="step.color"
                  size="small"
              >
                <div class="text-subtitle-2 font-weight-medium">{{ step.title }}</div>
                <div class="text-caption text-medium-emphasis">{{ step.description }}</div>
              </v-timeline-item>
            </v-timeline>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted} from 'vue'
import {useClientsStore} from '@/stores/clients'
import {useOrdersStore} from '@/stores/orders'
import StatCard from '@/components/StatCard.vue'
import {useAuthStore} from "@/stores/auth.ts";

interface Service {
  name: string
  description: string
  icon: string
  port: number
  color: string
}

interface OrderFlowStep {
  title: string
  description: string
  color: string
}

const clientsStore = useClientsStore()
const ordersStore = useOrdersStore()
const authStore = useAuthStore()

const relevantOrders = computed(() => authStore.isAdmin ? ordersStore.allOrders : ordersStore.orders)

const paidOrders = computed(() => relevantOrders.value.filter((o) => o.status === 'PAID').length)
const failedOrders = computed(() => relevantOrders.value.filter((o) => o.status === 'FAILED').length)

const isAuthenticatedAndAdmin = computed(() => {
  return authStore.isAuthenticated && authStore.isAdmin
})

onMounted(async () => {
  if (clientsStore.clients.length === 0 && isAuthenticatedAndAdmin.value) {
    await clientsStore.fetchAll()
  }
  if (authStore.isAdmin) {
    if (ordersStore.allOrders.length === 0) {
      await ordersStore.fetchAll()
    }
  } else if (authStore.isClient && authStore.clientId) {
    if (ordersStore.orders.length === 0) {
      await ordersStore.fetchByClient(authStore.clientId)
    }
  }
})

const services: Service[] = [
  {
    name: 'API Gateway',
    description: 'Entry point — routes to all services',
    icon: 'mdi-gate',
    port: 8080,
    color: 'primary'
  },
  {
    name: 'Client Service',
    description: 'REST API for client management',
    icon: 'mdi-account-cog',
    port: 8081,
    color: 'secondary'
  },
  {
    name: 'Order Service',
    description: 'REST API + Kafka consumer for orders',
    icon: 'mdi-gift',
    port: 8082,
    color: 'info'
  },
  {
    name: 'Payment Service',
    description: 'gRPC server for payment processing',
    icon: 'mdi-credit-card-settings',
    port: 9090,
    color: 'warning'
  },
  {
    name: 'Notification Service',
    description: 'SendGrid email + WebSocket/STOMP notifications',
    icon: 'mdi-email-fast',
    port: 8084,
    color: 'success'
  },
]

const orderFlow: OrderFlowStep[] = [
  {title: 'Create Client', description: 'POST /client — registered in client-service', color: 'primary'},
  {title: 'Place Order', description: 'POST /order — status: PENDING_PAYMENT', color: 'secondary'},
  {title: 'gRPC Payment', description: 'order-service calls payment-service via gRPC', color: 'warning'},
  {title: 'Kafka Event', description: 'payment-service publishes result to Kafka topic', color: 'info'},
  {title: 'Status Updated', description: 'order-service consumes event → PAID or FAILED', color: 'success'},
  {title: 'Email Sent', description: 'notification-service sends email via SendGrid', color: 'success'},
]
</script>

<style scoped>
.hero-bg {
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.06) 0%, rgba(var(--v-theme-secondary), 0.06) 100%);
}
</style>
