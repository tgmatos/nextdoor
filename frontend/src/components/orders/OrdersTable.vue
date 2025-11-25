<template>
  <div>
    <div @click="handleOutsideClick">
      <!-- Search Bar -->
      <div class="mb-4">
        <div class="relative">
          <button class="absolute -translate-y-1/2 left-4 top-1/2">
            <svg
              class="fill-gray-500 dark:fill-gray-400"
              width="20"
              height="20"
              viewBox="0 0 20 20"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fill-rule="evenodd"
                clip-rule="evenodd"
                d="M3.04175 9.37363C3.04175 5.87693 5.87711 3.04199 9.37508 3.04199C12.8731 3.04199 15.7084 5.87693 15.7084 9.37363C15.7084 12.8703 12.8731 15.7053 9.37508 15.7053C5.87711 15.7053 3.04175 12.8703 3.04175 9.37363ZM9.37508 1.54199C5.04902 1.54199 1.54175 5.04817 1.54175 9.37363C1.54175 13.6991 5.04902 17.2053 9.37508 17.2053C11.2674 17.2053 13.003 16.5344 14.357 15.4176L17.177 18.238C17.4699 18.5309 17.9448 18.5309 18.2377 18.238C18.5306 17.9451 18.5306 17.4703 18.2377 17.1774L15.418 14.3573C16.5365 13.0033 17.2084 11.2669 17.2084 9.37363C17.2084 5.04817 13.7011 1.54199 9.37508 1.54199Z"
              />
            </svg>
          </button>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Buscar produtos..."
            class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-200 bg-transparent py-2.5 pl-12 pr-14 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-800 dark:bg-gray-900 dark:bg-white/[0.03] dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800 xl:w-[430px]"
          />
        </div>
      </div>

      <!-- Table -->
      <div class="overflow-hidden rounded-xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
        <div class="max-w-full overflow-x-auto custom-scrollbar">
          <table class="min-w-full">
            <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-5 py-3 text-left w-3/11 sm:px-6">
                <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Produto</p>
              </th>
              <th class="px-5 py-3 text-left w-2/11 sm:px-6">
                <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Descrição</p>
              </th>
              <th class="px-5 py-3 text-left w-2/11 sm:px-6">
                <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Preço</p>
              </th>
              <th class="px-5 py-3 text-left w-2/11 sm:px-6">
                <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">Quantidade</p>
              </th>
              <th class="px-5 py-3 text-left w-2/11 sm:px-6">
                <p class="font-medium text-gray-500 text-theme-xs dark:text-gray-400">
                  Ultima atualização
                </p>
              </th>
            </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
            <tr
              v-for="product in filteredProducts"
              :key="product.id"
              class="border-t border-gray-100 dark:border-gray-800 cursor-pointer transition-colors duration-200"
              :class="{
                'bg-blue-50 dark:bg-blue-900/20': selectedProduct?.id === product.id,
                'hover:bg-gray-50 dark:hover:bg-gray-800/50': selectedProduct?.id !== product.id
              }"
              @click.stop="toggleProduct(product)"
            >
              <!-- Table cells remain the same -->
              <td class="px-5 py-4 sm:px-6">
                <div class="flex items-center gap-3">
                  <div>
                    <span class="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                      {{ product.name }}
                    </span>
                  </div>
                </div>
              </td>
              <td class="px-5 py-4 sm:px-6">
                <p class="text-gray-500 text-theme-sm dark:text-gray-400">
                  {{ product.description }}
                </p>
              </td>
              <td class="px-5 py-4 sm:px-6">
                <div class="flex items-center gap-3">
                  <div>
                    <span class="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                      {{ product.price }}
                    </span>
                  </div>
                </div>
              </td>
              <td class="px-5 py-4 sm:px-6">
                <span class="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                  {{ product.quantity }}
                </span>
              </td>
              <td class="px-5 py-4 sm:px-6">
                <p class="text-gray-500 text-theme-sm dark:text-gray-400">
                  {{ new Date(product.updated_at).toLocaleDateString() }}
                </p>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- Action Buttons -->
      <div v-if="selectedProduct" class="fixed bottom-6 right-6 flex gap-4">
        <button
          @click.stop="handleUpdate"
          class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500/50 shadow-lg"
        >
          Update
        </button>
        <button
          @click.stop="handleDelete"
          class="px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500/50 shadow-lg"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'

type Order = {
  id: string

}

const orders = ref<Order[]>([])
const searchQuery = ref('')

const sortBy = ref('updated_at')

const filteredProducts = computed(() => {
  const query = searchQuery.value.toLowerCase()

  const filtered =  products.value.filter(product =>
    product.name.toLowerCase().includes(query) ||
    product.description.toLowerCase().includes(query)
  )



  //test using updated_at
  return filtered.sort((a, b) => { 
    return new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()
  })
});

const getOrders = async () => {
  try {
    const token = getToken()
    const response = await apiClient.get<{ orders: Order[] }>('/api/store/order', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    if (response.data.orders != null) {
      order.value = response.data.orders
    }
  } catch (error) {
    console.log('Error fetching products:', error)
  }
}

const deleteProduct = async (productId: string) => {
  try {
    const token = getToken()
    await apiClient.delete<{ products: Order[] }>(`/api/store/product/${productId}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    window.location.reload()
  } catch (error) {
    console.log('Error fetching products:', error)
  }
}

onMounted(() => {
  getOrders()
})

const selectedProduct = ref<Order | null>(null)

const toggleProduct = (product: Order) => {
  if (selectedProduct.value?.id === product.id) {
    selectedProduct.value = null // Deselect if clicking the same product
  } else {
    selectedProduct.value = product // Select the new product
  }
}

const handleOutsideClick = () => {
  selectedProduct.value = null // Deselect when clicking outside
}

const handleUpdate = () => {
  if (selectedProduct.value) {
    // Implement update logic here
    console.log('Update product:', selectedProduct.value)
  }
}

const handleDelete = () => {
  if (selectedProduct.value) {
    // Implement delete logic here
    deleteProduct(selectedProduct.value.id)
    console.log('Delete product:', selectedProduct.value)
  }
}
</script>

<style scoped>
/* Add any additional styles here if needed */
</style>
