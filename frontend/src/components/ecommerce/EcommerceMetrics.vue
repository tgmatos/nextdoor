<template>
  <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 md:gap-6">
    <div
      class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6"
    >
     

      <div class="flex items-end justify-between mt-5">
        <div>
          <span class="text-sm text-gray-500 dark:text-gray-400">Pedidos</span>
          <h4 class="mt-2 font-bold text-gray-800 text-title-sm dark:text-white/90"> {{ orders.length }}</h4>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"> 
import { ref, onMounted } from 'vue'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'

// Define Interface based on your API output
interface Order {
  id: string
  status: string
  total: string
  payment_method: string
}

// Initialize as empty array
const orders = ref<Order[]>([])

const getOrders = async () => {
  try {
    const token = getToken()
    // Adjust URL endpoint if needed (e.g. /api/orders vs /api/store/orders)
    const response = await apiClient.get<{ orders: Order[] }>('/api/store/order', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    
    // Map response: { "orders": [...] }
    if (response.data.orders) {
      orders.value = response.data.orders
    }
  } catch (error) {
    console.log('Error fetching orders:', error)
  }
}

onMounted(() => {
  getOrders()
})

</script>
