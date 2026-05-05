<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-center justify-space-between mb-6">
      <div>
        <h2 class="text-h5 font-weight-bold">Clients</h2>
        <div class="text-body-2 text-medium-emphasis">Manage your registered clients</div>
      </div>
      <v-btn color="primary" prepend-icon="mdi-account-plus" rounded="lg" @click="openCreate">
        New Client
      </v-btn>
    </div>

    <!-- Search Bar -->
    <v-card rounded="xl" elevation="0" border class="mb-6 pa-4">
      <v-row align="center" no-gutters>
        <v-col>
          <v-text-field
            v-model="searchId"
            label="Fetch client by UUID"
            placeholder="e.g. 550e8400-e29b-41d4-a716-446655440000"
            prepend-inner-icon="mdi-identifier"
            variant="outlined"
            density="compact"
            hide-details
            rounded="lg"
            @keyup.enter="searchClient"
          />
        </v-col>
        <v-col cols="auto" class="ml-3">
          <v-btn
            color="primary"
            variant="tonal"
            rounded="lg"
            :loading="store.loading"
            @click="searchClient"
          >
            Search
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <!-- Table -->
    <v-card rounded="xl" elevation="0" border>
      <v-data-table
        :headers="headers"
        :items="store.clients"
        :loading="store.loading"
        item-value="id"
        hover
      >
        <template #top>
          <div class="d-flex align-center justify-space-between pa-4 pb-0">
            <span class="text-body-2 text-medium-emphasis">
              {{ store.clients.length }} client(s) found
            </span>
            <v-btn
              icon="mdi-refresh"
              variant="text"
              size="small"
              :loading="store.loading"
              @click="store.fetchAll()"
            />
          </div>
        </template>

        <template #item.cpf="{ item }">
          <code class="text-body-2">{{ formatCpf(item.cpf) }}</code>
        </template>

        <template #item.createdAt="{ item }">
          <span class="text-body-2">{{ formatDate(item.createdAt) }}</span>
        </template>

        <template #item.actions="{ item }">
          <v-btn
            icon="mdi-pencil-outline"
            variant="text"
            size="small"
            color="primary"
            @click="openEdit(item)"
          />
          <v-btn
            icon="mdi-cart-outline"
            variant="text"
            size="small"
            color="secondary"
            @click="goToOrders(item.id)"
          />
        </template>

        <template #no-data>
          <div class="text-center pa-8 text-medium-emphasis">
            <v-icon size="48" class="mb-3">mdi-account-off</v-icon>
            <div>No clients found. Create the first one.</div>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Create / Edit Dialog -->
    <v-dialog v-model="dialog" max-width="500" persistent>
      <v-card rounded="xl">
        <v-card-title class="pa-5 pb-3">
          <v-icon start :color="isEditing ? 'primary' : 'success'">
            {{ isEditing ? 'mdi-account-edit' : 'mdi-account-plus' }}
          </v-icon>
          {{ isEditing ? 'Edit Client' : 'New Client' }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-5">
          <v-form ref="formRef" @submit.prevent="submit">
            <v-text-field
              v-model="form.name"
              label="Full Name"
              :rules="[required]"
              prepend-inner-icon="mdi-account"
              variant="outlined"
              density="comfortable"
              class="mb-3"
            />
            <v-text-field
              v-model="form.email"
              label="Email"
              :rules="[required, validEmail]"
              prepend-inner-icon="mdi-email"
              variant="outlined"
              density="comfortable"
              type="email"
              class="mb-3"
            />
            <v-text-field
              v-if="!isEditing"
              v-model="form.cpf"
              label="CPF"
              :rules="[required, validCpf]"
              prepend-inner-icon="mdi-card-account-details"
              variant="outlined"
              density="comfortable"
              placeholder="000.000.000-00"
              hint="Brazilian CPF — 11 digits"
              persistent-hint
            />
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-5 pt-0">
          <v-spacer />
          <v-btn variant="text" rounded="lg" @click="dialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            rounded="lg"
            :loading="store.loading"
            @click="submit"
          >
            {{ isEditing ? 'Save Changes' : 'Create Client' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useClientsStore } from '@/stores/clients'
import { useAppStore } from '@/stores/app'
import type { Client } from '@/types'

interface ClientForm {
  name: string
  email: string
  cpf: string
}

interface FormInstance {
  validate: () => Promise<{ valid: boolean }>
}

const store = useClientsStore()
const appStore = useAppStore()
const router = useRouter()

const searchId = ref<string>('')
const dialog = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance | null>(null)

const form = reactive<ClientForm>({ name: '', email: '', cpf: '' })

const headers = [
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Email', key: 'email', sortable: true },
  { title: 'CPF', key: 'cpf', sortable: false },
  { title: 'Created At', key: 'createdAt', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

const required = (v: string): true | string => !!v || 'Required'
const validEmail = (v: string): true | string => /.+@.+\..+/.test(v) || 'Invalid email'
const validCpf = (v: string): true | string => {
  const digits = (v || '').replace(/\D/g, '')
  return digits.length === 11 || 'CPF must have 11 digits'
}

const formatCpf = (cpf: string): string => {
  if (!cpf) return '—'
  const d = cpf.replace(/\D/g, '')
  if (d.length === 11) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`
  return cpf
}

const formatDate = (value: string): string => {
  if (!value) return '—'
  return new Date(value).toLocaleString()
}

const searchClient = async (): Promise<void> => {
  if (!searchId.value.trim()) return
  try {
    await store.fetchById(searchId.value.trim())
    appStore.notify('Client loaded successfully')
    searchId.value = ''
  } catch (err) {
    appStore.notifyError(err)
  }
}

const openCreate = (): void => {
  isEditing.value = false
  editingId.value = null
  form.name = ''
  form.email = ''
  form.cpf = ''
  dialog.value = true
}

const openEdit = (client: Client): void => {
  isEditing.value = true
  editingId.value = client.id
  form.name = client.name
  form.email = client.email
  form.cpf = client.cpf
  dialog.value = true
}

const submit = async (): Promise<void> => {
  const { valid } = await formRef.value!.validate()
  if (!valid) return

  try {
    if (isEditing.value) {
      await store.update(editingId.value!, { name: form.name, email: form.email })
      appStore.notify('Client updated successfully')
    } else {
      await store.create({ name: form.name, email: form.email, cpf: form.cpf.replace(/\D/g, '') })
      appStore.notify('Client created successfully', 'success')
    }
    dialog.value = false
  } catch (err) {
    appStore.notifyError(err)
  }
}

const goToOrders = (clientId: string): void => {
  router.push({ path: '/orders', query: { clientId } })
}

onMounted(async () => {
  try {
    await store.fetchAll()
  } catch (err) {
    appStore.notifyError(err)
  }
})
</script>
