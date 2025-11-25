<template>
  <div
    class="overflow-hidden rounded-2xl border border-gray-200 bg-white px-4 pb-3 pt-4 dark:border-gray-800 dark:bg-white/[0.03] sm:px-6"
  >
    <div class="flex flex-col gap-2 mb-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">Recent Orders</h3>
      </div>
      <div class="flex items-center gap-3">

        <div class="relative">
          <select
            v-model="filterStatus"
            class="h-10 rounded-lg border border-gray-300 bg-white px-3 pr-8 text-sm text-gray-700 shadow-theme-xs focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
          >
            <option value="">Todos os Status</option>
            <option value="ESPERANDO">ESPERANDO</option>
            <option value="ACEITO">ACEITO</option>
            <option value="PREPARACAO">PREPARACAO</option>
            <option value="ROTA">ROTA</option>
            <option value="CONCLUIDO">CONCLUIDO</option>
            <option value="CANCELADO">CANCELADO</option>
            <option value="RECUSADO">RECUSADO</option>
          </select>
        </div>
      </div>
    </div>

    <div class="max-w-full overflow-x-auto custom-scrollbar">
      <table class="min-w-full">
        <thead>
          <tr class="border-t border-gray-100 dark:border-gray-800">
            <th class="py-3 text-left">
              <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">ID do Pedido</p>
            </th>
            <th class="py-3 text-left">
              <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Método de Pagamento</p>
            </th>
            <th class="py-3 text-left">
              <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Total</p>
            </th>
            <th class="py-3 text-left">
              <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Status</p>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="order in filteredOrders"
            :key="order.id"
            class="border-t border-gray-100 dark:border-gray-800"
          >
            <td class="py-3 whitespace-nowrap">
              <div class="flex items-center gap-3">
                <div class="flex items-center gap-2">
                  <button 
                    @click="openOrderDetails(order.id)"
                    class="rounded-lg bg-brand-500 px-3 py-1 text-xs font-medium text-white hover:bg-brand-600 focus:outline-none focus:ring-2 focus:ring-brand-500/50 dark:bg-brand-500 dark:hover:bg-brand-600"
                  >
                    Informações
                  </button>
                  <p class="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                    #{{ order.id.slice(0, 8) }}...
                  </p>
                </div>
              </div>
            </td>

            <td class="py-3 whitespace-nowrap">
              <p class="text-gray-500 text-theme-sm dark:text-gray-400">
                {{ order.payment_method }}
              </p>
            </td>

            <td class="py-3 whitespace-nowrap">
              <p class="text-gray-500 text-theme-sm dark:text-gray-400">
                R$ {{ order.total }}
              </p>
            </td>

            <td class="py-3 whitespace-nowrap">
              <select
                :value="order.status"
                @change="updateOrderStatus(order, $event)"
                :disabled="!validTransitions[order.status]?.length"
                :class="[
                  'rounded-full px-3 py-1 text-theme-xs font-medium focus:outline-none focus:ring-2 focus:ring-offset-0 transition-all border-0',
                  validTransitions[order.status]?.length > 0 ? 'cursor-pointer' : 'cursor-not-allowed opacity-75',
                  getStatusColor(order.status)
                ]"
              >
                <option :value="order.status" disabled selected>{{ order.status }}</option>
                <option 
                  v-for="nextStatus in validTransitions[order.status]" 
                  :key="nextStatus" 
                  :value="nextStatus"
                  class="bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
                >
                  {{ nextStatus }}
                </option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      v-if="showAlert"
      :class="[
        'fixed top-4 right-4 z-50 rounded-lg px-4 py-3 shadow-lg transition-all duration-300',
        alertType === 'success' ? 'bg-success-50 text-success-800 dark:bg-success-500/20 dark:text-success-400' : 
        alertType === 'error' ? 'bg-error-50 text-error-800 dark:bg-error-500/20 dark:text-error-400' :
        'bg-warning-50 text-warning-800 dark:bg-warning-500/20 dark:text-warning-400'
      ]"
    >
      <p class="text-sm font-medium">{{ alertMessage }}</p>
    </div>

    <Modal v-if="selectedOrder" :fullScreenBackdrop="true" @close="closeModal">
      <template #body>
        <div class="relative w-full max-w-3xl rounded-2xl bg-white p-6 shadow-lg dark:bg-gray-800 max-h-[90vh] overflow-y-auto">
          <button
            @click="closeModal"
            class="absolute right-4 top-4 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          
          <h3 class="mb-6 text-xl font-bold text-gray-900 dark:text-white">Detalhes do Pedido</h3>
          
          <div class="grid gap-6 md:grid-cols-2">
            <div class="space-y-4">
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">ID do Pedido</p>
                <p class="font-medium text-gray-900 dark:text-white">{{ selectedOrder.id }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">Status</p>
                <span :class="['inline-block rounded-full px-2.5 py-0.5 text-xs font-medium', getStatusColor(selectedOrder.status)]">
                  {{ selectedOrder.status }}
                </span>
              </div>
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">Total</p>
                <p class="font-medium text-gray-900 dark:text-white">R$ {{ selectedOrder.total }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">Método de Pagamento</p>
                <p class="font-medium text-gray-900 dark:text-white">{{ selectedOrder.payment_method }}</p>
              </div>
            </div>
            
            <div class="space-y-4">
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">Data de Criação</p>
                <p class="font-medium text-gray-900 dark:text-white">{{ formatDate(selectedOrder.inserted_at) }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500 dark:text-gray-400">Última Atualização</p>
                <p class="font-medium text-gray-900 dark:text-white">{{ formatDate(selectedOrder.updated_at) }}</p>
              </div>
            </div>
          </div>

          <div class="mt-8">
            <h4 class="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Produtos</h4>
            <div class="space-y-4">
              <div v-for="product in selectedOrder.order_product" :key="product.id" class="flex items-center gap-4 rounded-lg border border-gray-100 p-4 dark:border-gray-700">
                <div class="h-16 w-16 flex-shrink-0 overflow-hidden rounded-md border border-gray-200 dark:border-gray-700">
                  <img 
                    :src="product.image.startsWith('data:') ? product.image : `data:image/png;base64,${product.image}`" 
                    :alt="product.name" 
                    class="h-full w-full object-cover"
                  />
                </div>
                <div class="flex-1">
                  <h5 class="font-medium text-gray-900 dark:text-white">{{ product.name }}</h5>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ product.description }}</p>
                    <p class="text-sm font-medium text-brand-600 dark:text-brand-400">R$ {{ product.price }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'
import Modal from '../ui/Modal.vue'

interface OrderProduct {
  id: string
  name: string
  description: string
  image: string
  price: number
  quantity: number
  inserted_at?: string
  updated_at?: string
}

interface OrderResponse {
  id: string
  status_order?: string
  status?: string
  total: string
  payment_method: string
  inserted_at: string
  updated_at: string
  order_product?: OrderProduct[]
}

interface Order extends Omit<OrderResponse, 'status_order' | 'status'> {
  status: string
}

const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return 'Data Inválida'
  return date.toLocaleString('pt-BR')
}

const orders = ref<Order[]>([])
const filterStatus = ref('')
const selectedOrderId = ref('')
const showAlert = ref(false)
const alertMessage = ref('')
const alertType = ref<'success' | 'error' | 'warning'>('success')

const validTransitions: Record<string, string[]> = {
  "ESPERANDO": ["ACEITO", "RECUSADO"],
  "ACEITO": ["PREPARACAO", "CANCELADO"],
  "PREPARACAO": ["ROTA", "CANCELADO"],
  "ROTA": ["CONCLUIDO", "CANCELADO"],
  "CONCLUIDO": [],
  "CANCELADO": [],
  "RECUSADO": []
}

const filteredOrders = computed(() => {
  if (!filterStatus.value) {
    return orders.value
  }
  const filtered = orders.value.filter(order => order.status === filterStatus.value)
  return filtered.sort((a, b) => {
    return new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()
  })
})

const selectedOrder = ref<Order | null>(null)

const closeModal = () => {
  selectedOrderId.value = ''
  selectedOrder.value = null
}

const openOrderDetails = async (orderId: string) => {
  selectedOrderId.value = orderId
  const token = getToken()

  if (!token) {
    displayAlert('Sessão expirada. Faça login novamente.', 'error')
    return
  }

  try {
    const response = await apiClient.get<OrderResponse | { order: OrderResponse }>(`/api/store/order/${orderId}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    
    console.log('Order Details Response:', response.data)

    // Handle both { order: ... } and direct object response
    const orderData = 'order' in response.data ? response.data.order : response.data

    if (orderData) {
      selectedOrder.value = {
        id: orderData.id,
        total: orderData.total,
        payment_method: orderData.payment_method,
        inserted_at: orderData.inserted_at,
        updated_at: orderData.updated_at,
        order_product: orderData.order_product,
        status: orderData.status_order || orderData.status || 'UNKNOWN'
      }
    }
  } catch (error) {
    console.error('Error fetching order details:', error)
    displayAlert('Erro ao carregar detalhes do pedido', 'error')
  }
}

const getStatusColor = (status: string) => {
  if (['ACEITO', 'PREPARACAO', 'ROTA', 'CONCLUIDO'].includes(status)) {
    return 'bg-success-50 text-success-600 dark:bg-success-500/15 dark:text-success-500'
  }
  if (status === 'ESPERANDO') {
    return 'bg-warning-50 text-warning-600 dark:bg-warning-500/15 dark:text-orange-400'
  }
  return 'bg-error-50 text-error-600 dark:bg-error-500/15 dark:text-error-500'
}

const displayAlert = (message: string, type: 'success' | 'error' | 'warning') => {
  alertMessage.value = message
  alertType.value = type
  showAlert.value = true
  
  setTimeout(() => {
    showAlert.value = false
  }, 3000)
}

const getOrders = async () => {
  try {
    const token = getToken()
    const response = await apiClient.get<{ orders: OrderResponse[] }>('/api/store/order', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    
    console.log('API Response Orders:', response.data.orders)

    if (response.data.orders) {
      orders.value = response.data.orders.map((order) => ({
        id: order.id,
        total: order.total,
        payment_method: order.payment_method,
        inserted_at: order.inserted_at,
        updated_at: order.updated_at,
        order_product: order.order_product,
        status: order.status_order || order.status || 'UNKNOWN'
      }))
    }
  } catch (error) {
    console.log('Error fetching orders:', error)
  }
}

const updateOrderStatus = async (order: Order, event: Event) => {
  const newStatus = (event.target as HTMLSelectElement).value
  
  if (!validTransitions[order.status]?.length) {
    displayAlert('Este status não pode ser alterado', 'warning')
    return
  }

  if (!newStatus || newStatus === order.status) return

  const previousStatus = order.status

  try {
    const token = getToken()
    const payload = {
      before: order.status,
      after: newStatus
    }
    
    await apiClient.patch(`/api/store/order/${order.id}`, payload, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    
    order.status = newStatus
    displayAlert(`Status alterado de ${previousStatus} para ${newStatus}`, 'success')
    
  } catch (error) {
    order.status = previousStatus
    ;(event.target as HTMLSelectElement).value = previousStatus
    displayAlert('Erro ao atualizar status do pedido', 'error')
    console.error('Error updating order status:', error)
  }
}

onMounted(() => {
  getOrders()
})
</script>