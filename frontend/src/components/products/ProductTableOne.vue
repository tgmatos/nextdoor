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
                <button 
                  @click="toggleSort" 
                  type="button" 
                  class="flex items-center gap-2 hover:text-primary transition-colors"
                >
                  <span class="font-medium text-black dark:text-white"></span>

                  <div class="flex flex-col gap-0.5">
                    
                    <span v-if="sortBy.value === 'price_asc'">
                      <svg class="fill-current" width="14" height="14" viewBox="0 0 24 24"><path d="M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z"/></svg>
                    </span>

                    <span v-else-if="sortBy.value === 'price_desc'">
                      <svg class="fill-current" width="14" height="14" viewBox="0 0 24 24"><path d="M20 12l-1.41-1.41L13 16.17V4h-2v12.17l-5.58-5.59L4 12l8 8 8-8z"/></svg>
                    </span>

                    <span v-else class="opacity-50">
                      <svg class="fill-current" width="14" height="14" viewBox="0 0 24 24"><path d="M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z"/></svg>
                    </span>
                  </div>
                </button>
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

    <!-- Update Modal -->
    <Modal v-if="isUpdateModalOpen" :fullScreenBackdrop="true" @close="closeUpdateModal">
      <template #body>
        <div class="relative w-full max-w-lg rounded-2xl bg-white p-6 shadow-lg dark:bg-gray-800 max-h-[90vh] overflow-y-auto">
          <button
            @click="closeUpdateModal"
            class="absolute right-4 top-4 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          
          <h3 class="mb-6 text-xl font-bold text-gray-900 dark:text-white">Update Product</h3>
          
          <form @submit.prevent="submitUpdate" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Name</label>
              <input v-model="updateForm.name" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white sm:text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Description</label>
              <textarea v-model="updateForm.description" rows="3" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white sm:text-sm"></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Price</label>
              <input v-model="updateForm.price" type="number" step="0.01" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white sm:text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Quantity</label>
              <input v-model="updateForm.quantity" type="number" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white sm:text-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Image</label>
              <div class="flex flex-col gap-4">
                <div v-if="updateForm.image" class="h-20 w-20 overflow-hidden rounded-lg border border-gray-200 dark:border-gray-700">
                  <img 
                    :src="updateForm.image.startsWith('data:') ? updateForm.image : `data:image/png;base64,${updateForm.image}`" 
                    alt="Product Image Preview" 
                    class="h-full w-full object-cover"
                  />
                </div>
                <input
                  type="file"
                  accept="image/*"
                  @change="handleImageUpload"
                  class="w-full cursor-pointer rounded-lg border-[1.5px] border-stroke bg-transparent font-medium outline-none transition file:mr-5 file:border-collapse file:cursor-pointer file:border-0 file:border-r file:border-solid file:border-stroke file:bg-whiter file:px-5 file:py-3 file:hover:bg-brand-500 file:hover:bg-opacity-10 focus:border-brand-500 active:border-brand-500 disabled:cursor-default disabled:bg-whiter dark:border-gray-700 dark:bg-gray-800 dark:file:bg-white/30 dark:file:text-white dark:focus:border-brand-500"
                />
              </div>
            </div>
            
            <div class="flex justify-end gap-3 mt-6">
              <button type="button" @click="closeUpdateModal" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600">Cancel</button>
              <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">Save Changes</button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'
import Modal from '@/components/ui/Modal.vue'

type Product = {
  id: string
  name: string
  quantity: number
  price: number
  description: string
  image: string
  inserted_at: string
  updated_at: string
}

const products = ref<Product[]>([])
const searchQuery = ref('')

const sortBy = ref('updated_at')

const sortStates = ['updated_at', 'price_asc', 'price_desc'];

const toggleSort = () => {
  const currentIndex = sortStates.indexOf(sortBy.value);
  const nextIndex = (currentIndex + 1) % sortStates.length;
  sortBy.value = sortStates[nextIndex];
};

const sortLabel = computed(() => {
  if (sortBy.value === 'price_asc') return 'Price: Low to High';
  if (sortBy.value === 'price_desc') return 'Price: High to Low';
  return 'Sort by: Default';
});

const filteredProducts = computed(() => {
  const query = searchQuery.value.toLowerCase()

  const filtered =  [...products.value].filter(product =>
    product.name.toLowerCase().includes(query) ||
    product.description.toLowerCase().includes(query)
  )



  //test using updated_at
  return filtered.sort((a, b) => {
    if(sortBy.value == "price_asc"){
      return a.price - b.price
    }else if(sortBy.value == "price_desc"){ 
      return b.price - a.price
    } else {
      return new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()
    }
  })
});

const getProducts = async () => {
  try {
    const token = getToken()
    const response = await apiClient.get<{ products: Product[] }>('/api/store/product', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    if (response.data.products != null) {
      products.value = response.data.products
    }
  } catch (error) {
    console.log('Error fetching products:', error)
  }
}

const deleteProduct = async (productId: string) => {
  try {
    const token = getToken()
    await apiClient.delete<{ products: Product[] }>(`/api/store/product/${productId}`, {
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
  getProducts()
})

const selectedProduct = ref<Product | null>(null)

const toggleProduct = (product: Product) => {
  if (selectedProduct.value?.id === product.id) {
    selectedProduct.value = null // Deselect if clicking the same product
  } else {
    selectedProduct.value = product // Select the new product
  }
}

const handleOutsideClick = () => {
  selectedProduct.value = null // Deselect when clicking outside
}

const isUpdateModalOpen = ref(false)
const updateForm = reactive({
  id: '',
  name: '',
  description: '',
  price: 0,
  quantity: 0,
  image: '' // Assuming image handling might be needed, though not explicitly in the form yet
})

const openUpdateModal = () => {
  if (selectedProduct.value) {
    updateForm.id = selectedProduct.value.id
    updateForm.name = selectedProduct.value.name
    updateForm.description = selectedProduct.value.description
    updateForm.price = selectedProduct.value.price
    updateForm.quantity = selectedProduct.value.quantity
    updateForm.image = selectedProduct.value.image
    isUpdateModalOpen.value = true
  }
}

const closeUpdateModal = () => {
  isUpdateModalOpen.value = false
}

const submitUpdate = async () => {
  try {
    const token = getToken()
    const payload = {
      product: {
        name: updateForm.name,
        description: updateForm.description,
        price: updateForm.price,
        quantity: updateForm.quantity,
        image: updateForm.image
      }
    }

    const response = await apiClient.patch<{ product: Product }>(`/api/store/product/${updateForm.id}`, payload, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (response.data.product) {
      // Update local product list
      const index = products.value.findIndex(p => p.id === updateForm.id)
      if (index !== -1) {
        products.value[index] = response.data.product
      }
      selectedProduct.value = null // Deselect
      closeUpdateModal()
    }
  } catch (error) {
    console.error('Error updating product:', error)
  }
}

const handleUpdate = () => {
  openUpdateModal()
}

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    const reader = new FileReader()
    
    reader.onload = (e) => {
      const result = e.target?.result as string
      // Remove the data URL prefix to store just the base64 string
      const base64String = result.replace(/^data:image\/[a-z]+;base64,/, '')
      
      updateForm.image = base64String
    }
    
    reader.readAsDataURL(file)
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
