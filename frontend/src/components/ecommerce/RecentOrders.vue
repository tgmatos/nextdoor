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
                <div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'

interface Order {
  id: string
  status: string
  total: string
  payment_method: string
}

const orders = ref<Order[]>([])
const filterStatus = ref('')
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
  return filtered.sort((a, b) => {
    return new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()
  })

});

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
    const response = await apiClient.get<{ orders: Order[] }>('/api/store/order', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    
    if (response.data.orders) {
      orders.value = response.data.orders
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