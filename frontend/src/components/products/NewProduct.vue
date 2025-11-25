<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'
import { handleImageChange } from '@/utils/image'

const router = useRouter()
const product = ref({
  name: '',
  description: '',
  price: 0,
  quantity: 0,
  //storing in base64. do not change later
  image: '',
})

const error = ref('')
const isSubmitting = ref(false)


const createProduct = async () => {
  try {
    isSubmitting.value = true
    error.value = ''
    const token = getToken()

    const productData = {
      product: {
        name: product.value.name,
        description: product.value.description,
        image: product.value.image,
        price: product.value.price,
        quantity: product.value.quantity,
      },
    }

    await apiClient.post('api/store/product', productData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    router.push('/store/products')
  } catch (e) {
    error.value = 'Failed to create product. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <form @submit.prevent="createProduct" class="bg-white dark:bg-gray-900 rounded-lg">
    <div class="space-y-6 p-6">
      <!-- Error Message -->
      <div v-if="error" class="p-4 mb-4 text-sm text-red-700 bg-red-100 rounded-lg dark:bg-red-200 dark:text-red-800">
        {{ error }}
      </div>

      <!-- image -->
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
          Foto
        </label>
        <input 
        id="fotoproduto"
        type="file"
        placeholder="Foto do produto"
        required
        class="hidden"
        @change="async (e) => product.image = await handleImageChange(e) || ''"
        />

        <label
        for="fotoproduto"
        class="flex items-center cursor-pointer h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm shadow-theme-xs focus-within:border-brand-300 focus-within:ring-3 focus-within:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:focus-within:border-brand-800"
        >
        Foto do Produto
        </label>
      </div>

       

      <div v-if="product.image" class="image-preview">
        <img :src="product.image" alt="Preview" />
      </div>


      <!-- Product Name -->
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
          Nome
        </label>
        <input
          type="text"
          v-model="product.name"
          placeholder="Nome do produto"
          required
          class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
        />
      </div>

      <!-- Description -->
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
          Descrição
        </label>
        <textarea
          v-model="product.description"
          placeholder="Descrição do Produto"
          rows="4"
          required
          class="dark:bg-dark-900 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
        ></textarea>
      </div>

      <!-- Price -->
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
          Preço
        </label>
        <div class="relative">
          <span
            class="absolute left-0 top-1/2 flex h-11 w-[46px] -translate-y-1/2 items-center justify-center border-r border-gray-200 dark:border-gray-800"
          >
            $
          </span>
          <input
            type="number"
            step="0.01"
            min="0"
            v-model="product.price"
            placeholder="0.00"
            required
            class="dark:bg-dark-900 h-11 w-full appearance-none rounded-lg border border-gray-300 bg-transparent bg-none px-4 py-2.5 pl-[62px] text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
          />
        </div>
      </div>

      <!-- Quantity -->
      <div>
        <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
          Quantidade
        </label>
        <input
          type="number"
          min="0"
          v-model="product.quantity"
          placeholder="Enter stock quantity"
          required
          class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
        />
      </div>

      <!-- Submit Button -->
      <div class="flex justify-end space-x-4 pt-4">
        <button
          type="button"
          @click="router.push('/store/products')"
          class="px-6 py-2.5 text-sm font-medium text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-700"
        >
          Cancelar
        </button>
        <button
          type="submit"
          :disabled="isSubmitting"
          class="px-6 py-2.5 text-sm font-medium text-white bg-brand-500 rounded-lg hover:bg-brand-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ isSubmitting ? 'Criando...' : 'Criar Produto' }}
        </button>
      </div>
    </div>
  </form>
</template>


<style scoped>

</style>
