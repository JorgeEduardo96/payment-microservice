<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-center justify-space-between mb-6">
      <div>
        <h2 class="text-h5 font-weight-bold">Orders</h2>
        <div class="text-body-2 text-medium-emphasis">View and manage client orders</div>
      </div>
      <v-btn
          color="primary"
          prepend-icon="mdi-cart-plus"
          rounded="lg"
          :disabled="!activeClientId"
          @click="openCreate"
      >
        New Order
      </v-btn>
    </div>

    <!-- Client Selector -->
    <v-card rounded="xl" elevation="0" border class="mb-6 pa-4">
      <v-row align="center" no-gutters>
        <v-col cols="12" md="5" class="pr-md-3 mb-3 mb-md-0">
          <v-select
              v-model="selectedKnownClient"
              :items="clientsStore.clients"
              item-title="name"
              item-value="id"
              label="Select from loaded clients"
              prepend-inner-icon="mdi-account"
              variant="outlined"
              density="compact"
              hide-details
              clearable
              rounded="lg"
              @update:model-value="onKnownClientSelect"
          >
            <template #item="{ props, item }">
              <v-list-item v-bind="props" :subtitle="item.raw.email"/>
            </template>
          </v-select>
        </v-col>
        <v-col cols="12" md="1" class="text-center text-medium-emphasis mb-3 mb-md-0">
          or
        </v-col>
        <v-col cols="12" md="4" class="pr-md-3 mb-3 mb-md-0">
          <v-text-field
              v-model="manualClientId"
              label="Enter Client UUID"
              placeholder="550e8400-e29b-41d4-a716-..."
              prepend-inner-icon="mdi-identifier"
              variant="outlined"
              density="compact"
              hide-details
              rounded="lg"
              @keyup.enter="loadOrders"
          />
        </v-col>
        <v-col cols="12" md="2" class="d-flex gap-2">
          <v-btn
              color="primary"
              variant="tonal"
              rounded="lg"
              :loading="store.loading"
              @click="loadOrders"
          >
            Load Orders
          </v-btn>
          <v-btn
              v-if="activeClientId"
              icon="mdi-refresh"
              variant="text"
              rounded="lg"
              :loading="store.loading"
              @click="loadOrders"
          />
        </v-col>
      </v-row>
      <div v-if="activeClientId" class="mt-3">
        <v-chip size="small" color="primary" variant="tonal" prepend-icon="mdi-account-check">
          Viewing orders for: {{ activeClientLabel }}
        </v-chip>
      </div>
    </v-card>

    <!-- Orders Table -->
    <v-card rounded="xl" elevation="0" border>
      <v-data-table
          :headers="headers"
          :items="store.orders"
          :loading="store.loading"
          item-value="id"
          hover
      >
        <template #top>
          <div v-if="store.orders.length > 0" class="d-flex align-center justify-space-between pa-4 pb-0">
            <span class="text-body-2 text-medium-emphasis">
              {{ store.orders.length }} order(s) found
            </span>
            <v-btn
                variant="text"
                size="small"
                prepend-icon="mdi-information-outline"
                color="info"
            >
              Payment status updates asynchronously via Kafka
            </v-btn>
          </div>
        </template>

        <template #item.id="{ item }">
          <code class="text-caption">{{ item.id }}</code>
        </template>

        <template #item.total="{ item }">
          <span class="font-weight-medium">{{ formatCurrency(item.total) }}</span>
        </template>

        <template #item.paymentMethod="{ item }">
          <v-chip
              size="small"
              :color="item.paymentMethod === 'CASH' ? 'teal' : 'blue'"
              variant="tonal"
              :prepend-icon="item.paymentMethod === 'CASH' ? 'mdi-cash' : 'mdi-credit-card'"
          >
            {{ item.paymentMethod }}
          </v-chip>
        </template>

        <template #item.status="{ item }">
          <v-chip
              size="small"
              :color="statusColor(item.status)"
              variant="tonal"
              :prepend-icon="statusIcon(item.status)"
          >
            {{ statusLabel(item.status) }}
          </v-chip>
        </template>

        <template #item.shippingAddress="{ item }">
          <span class="text-body-2">{{ item.shippingAddress }}</span>
        </template>

        <template #no-data>
          <div class="text-center pa-8 text-medium-emphasis">
            <v-icon size="48" class="mb-3">mdi-cart-off</v-icon>
            <div v-if="!activeClientId">Select a client above to view their orders.</div>
            <div v-else>No orders found for this client.</div>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Create Order Dialog -->
    <v-dialog v-model="dialog" max-width="520" persistent>
      <v-card rounded="xl">
        <v-card-title class="pa-5 pb-3">
          <v-icon start color="primary">mdi-cart-plus</v-icon>
          New Order
        </v-card-title>
        <v-divider/>
        <v-card-text class="pa-5">
          <v-form ref="formRef" @submit.prevent="submit">
            <!-- Client (readonly) -->
            <v-text-field
                :model-value="activeClientLabel"
                label="Client"
                prepend-inner-icon="mdi-account"
                variant="outlined"
                density="comfortable"
                readonly
                class="mb-3"
            />

            <!-- Total -->
            <v-text-field
                v-model="form.total"
                label="Total (R$)"
                :rules="[required, positiveNumber]"
                prepend-inner-icon="mdi-currency-brl"
                variant="outlined"
                density="comfortable"
                type="number"
                min="0.01"
                step="0.01"
                class="mb-3"
            />

            <!-- Payment Method -->
            <v-select
                v-model="form.paymentMethod"
                :items="paymentMethods"
                label="Payment Method"
                :rules="[required]"
                prepend-inner-icon="mdi-credit-card"
                variant="outlined"
                density="comfortable"
                class="mb-3"
            >
              <template #item="{ props, item }">
                <v-list-item v-bind="props">
                  <template #append>
                    <v-chip size="x-small" color="success" v-if="item.raw.value === 'CASH'">
                      10% off
                    </v-chip>
                  </template>
                </v-list-item>
              </template>
            </v-select>

            <!-- Discount hint -->
            <v-alert
                v-if="form.paymentMethod"
                density="compact"
                rounded="lg"
                class="mb-3"
                :color="form.paymentMethod === 'CASH' ? 'success' : 'info'"
                variant="tonal"
            >
              <span v-if="form.paymentMethod === 'CASH'">
                Cash payment — 10% discount applied by the backend.
                Final: <strong>{{ discountedTotal }}</strong>
              </span>
              <span v-else>
                Card payment — no discount. Total: <strong>{{ formatCurrency(Number(form.total)) }}</strong>
              </span>
            </v-alert>

            <!-- Shipping Address -->
            <v-text-field
                v-model="form.shippingAddress"
                label="Shipping Address"
                :rules="[required]"
                prepend-inner-icon="mdi-map-marker"
                variant="outlined"
                density="comfortable"
                class="mb-3"
            />

            <!-- Notes -->
            <v-textarea
                v-model="form.notes"
                label="Notes (optional)"
                prepend-inner-icon="mdi-note-text"
                variant="outlined"
                density="comfortable"
                rows="2"
                auto-grow
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-5 pt-0">
          <v-spacer/>
          <v-btn variant="text" rounded="lg" @click="dialog = false">Cancel</v-btn>
          <v-btn color="primary" rounded="lg" :loading="store.loading" @click="submit">
            Place Order
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {useRoute} from 'vue-router'
import {useOrdersStore} from '@/stores/orders'
import {useClientsStore} from '@/stores/clients'
import {useAppStore} from '@/stores/app'
import type {OrderStatus} from '@/types'

interface OrderForm {
  total: string
  paymentMethod: string | null
  shippingAddress: string
  notes: string
}

interface FormInstance {
  validate: () => Promise<{ valid: boolean }>
}

const store = useOrdersStore()
const clientsStore = useClientsStore()
const appStore = useAppStore()
const route = useRoute()

const manualClientId = ref<string>('')
const selectedKnownClient = ref<string | null>(null)
const dialog = ref<boolean>(false)
const formRef = ref<FormInstance | null>(null)

const form = reactive<OrderForm>({
  total: '',
  paymentMethod: null,
  shippingAddress: '',
  notes: '',
})

const paymentMethods = [
  {title: 'Card', value: 'CARD'},
  {title: 'Cash (10% discount)', value: 'CASH'},
]

const headers = [
  {title: 'ID', key: 'id', sortable: false, width: '350px'},
  {title: 'Total', key: 'total', sortable: true},
  {title: 'Method', key: 'paymentMethod', sortable: true},
  {title: 'Status', key: 'status', sortable: true},
  {title: 'Shipping Address', key: 'shippingAddress', sortable: false},
  {title: 'Client', key: 'clientName', sortable: true},
]

const activeClientId = computed(() => store.activeClientId)

const activeClientLabel = computed<string>(() => {
  const known = clientsStore.clients.find((c) => c.id === activeClientId.value)
  return known ? `${known.name} (${known.email})` : activeClientId.value ?? ''
})

const discountedTotal = computed<string>(() => {
  const val = parseFloat(form.total)
  if (isNaN(val)) return '—'
  return formatCurrency(val * 0.9)
})

const required = (v: string): true | string => !!v || 'Required'
const positiveNumber = (v: string): true | string => parseFloat(v) > 0 || 'Must be greater than 0'

const formatCurrency = (value: number | string | null): string => {
  if (value == null || value === '') return '—'
  return new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'}).format(Number(value))
}

const statusColor = (status: OrderStatus | string): string => {
  const map: Record<string, string> = {PAID: 'success', FAILED: 'error', PENDING_PAYMENT: 'warning'}
  return map[status] ?? 'grey'
}

const statusIcon = (status: OrderStatus | string): string => {
  const map: Record<string, string> = {
    PAID: 'mdi-check-circle',
    FAILED: 'mdi-close-circle',
    PENDING_PAYMENT: 'mdi-clock-outline',
  }
  return map[status] ?? 'mdi-help-circle'
}

const statusLabel = (status: OrderStatus | string): string => {
  const map: Record<string, string> = {PAID: 'Paid', FAILED: 'Failed', PENDING_PAYMENT: 'Pending Payment'}
  return map[status] ?? status
}

const onKnownClientSelect = (id: string | null): void => {
  if (id) manualClientId.value = ''
}

const loadOrders = async (): Promise<void> => {
  const id = selectedKnownClient.value || manualClientId.value.trim()
  if (!id) {
    appStore.notify('Please select or enter a client ID', 'warning')
    return
  }
  try {
    await store.fetchByClient(id)
    appStore.notify(`${store.orders.length} order(s) loaded`)
  } catch (err) {
    appStore.notifyError(err)
  }
}

const openCreate = (): void => {
  form.total = ''
  form.paymentMethod = null
  form.shippingAddress = ''
  form.notes = ''
  dialog.value = true
}

const submit = async (): Promise<void> => {
  const {valid} = await formRef.value!.validate()
  if (!valid) return

  try {
    const payload = {
      clientId: activeClientId.value,
      total: parseFloat(form.total),
      paymentMethod: form.paymentMethod!,
      shippingAddress: form.shippingAddress,
      notes: form.notes || null,
    }
    await store.create(payload)
    appStore.notify('Order placed! Payment is processing asynchronously via Kafka.')
    dialog.value = false
  } catch (err) {
    appStore.notifyError(err)
  }
}

onMounted(async () => {
  if (clientsStore.clients.length === 0) {
    await clientsStore.fetchAll()
  }

  // Auto-load if redirected from clients page with a clientId query param
  const clientId = route.query.clientId as string | undefined
  if (clientId) {
    const known = clientsStore.clients.find((c) => c.id === clientId)
    if (known) selectedKnownClient.value = clientId
    else manualClientId.value = clientId
    manualClientId.value = clientId
    store.activeClientId = clientId
    await loadOrders()
  }
})
</script>
