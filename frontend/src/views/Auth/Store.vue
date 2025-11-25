<script setup lang="ts">
import FullScreenLayout from '@/components/layout/FullScreenLayout.vue'
import { ref } from 'vue'
import apiClient from '@/utils/axios.ts'
import { getToken } from '@/utils/auth.ts'
import router from '@/router'
import { handleImageChange } from '@/utils/image'

import { vMaska } from 'maska/vue' 


// Optional: If you need to debug or validate specifically
const handleMaska = (event: any) => {
  console.log('Masked:', event.detail.masked)
  console.log('Unmasked:', event.detail.unmasked)
}

type Category = 'VESTUARIO' | 'ELETRONICOS' | 'COSMETICOS' | 'PETS' | 'LIVRARIA'

type Store = {
  name: string
  image: string,
  description: string
  telephone: string
  category: Category
}

type RegisterResponse = {
  store_id: string
}

const name = ref('')
const image = ref('')
const description = ref('')
const telephone = ref('')
const category = ref<Category>('VESTUARIO')

const registerStore = async (
  name: string,
  image: string,
  description: string,
  telephone: string,
  category: Category,
) => {
  const store: Store = {
    name: name,
    image: image,
    description: description,
    telephone: telephone,
    category: category,
  }

  try {
    const token = getToken()
    const response = await apiClient.post<RegisterResponse>('/api/store', store, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (response.status === 200) {
      router.push('/dashboard')
    }
  } catch (error) {
    console.error('New Store failed:', error)
  }
}

const validatePhoneNumber = (event: Event) => {
  const input = event.target as HTMLInputElement
  const numericValue = input.value.replace(/\D/g, '')
  telephone.value = numericValue
}
</script>

<template>
  <FullScreenLayout>
    <div class="relative p-6 bg-white z-1 dark:bg-gray-900 sm:p-0">
      <div
        class="relative flex flex-col justify-center w-full h-screen lg:flex-row dark:bg-gray-900"
      >
        <div class="flex flex-col flex-1 w-full lg:w-1/2">
          <div class="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
            <div class="mb-5 sm:mb-8">
              <h1
                class="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md"
              >
                Criar Loja
              </h1>
              <p class="text-sm text-gray-500 dark:text-gray-400">
                Insira os dados para criar uma loja
              </p>
            </div>
            <div>
              <form @submit.prevent="registerStore(name, image, description, telephone, category)">
                <div class="space-y-5">
                  <div class="sm:col-span-1">
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
                      @change="async (e) => image = await handleImageChange(e) || ''"
                      />

                      <label
                      for="fotoproduto"
                      class="flex items-center cursor-pointer h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm shadow-theme-xs focus-within:border-brand-300 focus-within:ring-3 focus-within:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:focus-within:border-brand-800"
                      >
                      Foto do Produto
                      </label>
                    </div>

                    <div v-if="image" class="image-preview">
                      <img :src="image" alt="Preview" />
                    </div>
                    <label
                      for="sname"
                      class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                    >
                      Nome da Loja<span class="text-error-500">*</span>
                    </label>
                    <input
                      v-model="name"
                      type="text"
                      id="sname"
                      name="sname"
                      placeholder="Insira nome de usuário"
                      class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                    />
                  </div>
                  <div>
                    <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                      Description
                    </label>
                    <textarea
                      v-model="description"
                      placeholder="Enter a description..."
                      rows="6"
                      class="dark:bg-dark-900 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                    ></textarea>
                  </div>
                  <div class="space-y-5">
                    <div class="sm:col-span-1">
                      <label
                        for="telnumber"
                        class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                      >
                        Telefone<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="telephone"
                        v-maska
                        data-maska="['(##) ####-####', '(##) #####-####']"
                        @maska="handleMaska"
                        type="tel"
                        id="telnumber"
                        placeholder="(99) 99999-9999"
                        class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      /> 
                    </div>
                    <div class="space-y-5">
                      <div class="sm:col-span-1">
                        <label
                          for="store-category"
                          class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                        >
                          Categoria<span class="text-error-500">*</span>
                        </label>
                        <select
                          v-model="category"
                          id="store-category"
                          name="store-category"
                          class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        >
                          <option value="VESTUARIO">Vestuário</option>
                          <option value="ELETRONICOS">Eletrônicos</option>
                          <option value="COSMETICOS">Cosméticos</option>
                          <option value="PETS">Pets</option>
                          <option value="LIVRARIA">Livraria</option>
                        </select>
                      </div>
                    </div>
                    <div>
                      <button
                        type="submit"
                        class="flex items-center justify-center w-full px-4 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 shadow-theme-xs hover:bg-brand-600"
                      >
                        Criar Loja
                      </button>
                    </div>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </FullScreenLayout>
</template>

<style scoped></style>