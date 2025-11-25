<template>
  <div>
    <div v-if="storeInfo" class="p-5 mb-6 border border-gray-200 rounded-2xl dark:border-gray-800 lg:p-6">
      <div class="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
        <div class="flex flex-col items-center w-full gap-6 xl:flex-row">
          <div
            class="w-20 h-20 overflow-hidden border border-gray-200 rounded-full dark:border-gray-800"
          >
            <img
              v-if="storeInfo?.image"
              :src="storeInfo.image.startsWith('data:') ? storeInfo.image : `data:image/png;base64,${storeInfo.image}`"
              alt="Store Logo"
              class="mb-4 w-24 h-24 rounded-full object-cover mx-auto xl:mx-0"
            />
          </div>
          <div class="order-3 xl:order-2">
            <h4
              class="mb-2 text-lg font-semibold text-center text-gray-800 dark:text-white/90 xl:text-left"
            >
              {{ storeInfo?.name }} 
            </h4>
            <p  class="mb-2 text-lg font-semibold text-center text-gray-800 dark:text-white/90 xl:text-left"> 
              Categoria: {{ storeInfo.category }}
            </p>
            <div
              class="flex flex-col items-center gap-1 text-center xl:flex-row xl:gap-3 xl:text-left"
            >
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ formatPhoneNumber(storeInfo?.telephone) }}</p>
              <div class="hidden h-3.5 w-px bg-gray-300 dark:bg-gray-700 xl:block"></div>
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ storeInfo.description }}</p>
              
            </div>
          </div>
        </div>
        <button @click="isProfileInfoModal = true" class="edit-button">
          <svg
            class="fill-current"
            width="18"
            height="18"
            viewBox="0 0 18 18"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fill-rule="evenodd"
              clip-rule="evenodd"
              d="M15.0911 2.78206C14.2125 1.90338 12.7878 1.90338 11.9092 2.78206L4.57524 10.116C4.26682 10.4244 4.0547 10.8158 3.96468 11.2426L3.31231 14.3352C3.25997 14.5833 3.33653 14.841 3.51583 15.0203C3.69512 15.1996 3.95286 15.2761 4.20096 15.2238L7.29355 14.5714C7.72031 14.4814 8.11172 14.2693 8.42013 13.9609L15.7541 6.62695C16.6327 5.74827 16.6327 4.32365 15.7541 3.44497L15.0911 2.78206ZM12.9698 3.84272C13.2627 3.54982 13.7376 3.54982 14.0305 3.84272L14.6934 4.50563C14.9863 4.79852 14.9863 5.2734 14.6934 5.56629L14.044 6.21573L12.3204 4.49215L12.9698 3.84272ZM11.2597 5.55281L5.6359 11.1766C5.53309 11.2794 5.46238 11.4099 5.43238 11.5522L5.01758 13.5185L6.98394 13.1037C7.1262 13.0737 7.25666 13.003 7.35947 12.9002L12.9833 7.27639L11.2597 5.55281Z"
              fill=""
            />
          </svg>
          Editar
        </button>
      </div>
    </div>
    <Modal v-if="isProfileInfoModal" @close="isProfileInfoModal = false">
        <template #body>
          <div
            class="no-scrollbar relative w-full max-w-[700px] overflow-y-auto rounded-3xl bg-white p-4 dark:bg-gray-900 lg:p-11"
          >
            <button
              @click="isProfileInfoModal = false"
              class="transition-color absolute right-5 top-5 z-999 flex h-11 w-11 items-center justify-center rounded-full bg-gray-100 text-gray-400 hover:bg-gray-200 hover:text-gray-600 dark:bg-gray-700 dark:bg-white/[0.05] dark:text-gray-400 dark:hover:bg-white/[0.07] dark:hover:text-gray-300"
            >
              <svg
                class="fill-current"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
              <path
                fill-rule="evenodd"
                clip-rule="evenodd"
                d="M6.04289 16.5418C5.65237 16.9323 5.65237 17.5655 6.04289 17.956C6.43342 18.3465 7.06658 18.3465 7.45711 17.956L11.9987 13.4144L16.5408 17.9565C16.9313 18.347 17.5645 18.347 17.955 17.9565C18.3455 17.566 18.3455 16.9328 17.955 16.5423L13.4129 12.0002L17.955 7.45808C18.3455 7.06756 18.3455 6.43439 17.955 6.04387C17.5645 5.65335 16.9313 5.65335 16.5408 6.04387L11.9987 10.586L7.45711 6.04439C7.06658 5.65386 6.43342 5.65386 6.04289 6.04439C5.65237 6.43491 5.65237 7.06808 6.04289 7.4586L10.5845 12.0002L6.04289 16.5418Z"
                fill=""
              />
            </svg>
          </button>
          <div class="px-2 pr-14">
            <h4 class="mb-2 text-2xl font-semibold text-gray-800 dark:text-white/90">
              Editar Informações da Loja
            </h4>
            <p class="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
              Atualize os detalhes da sua loja para manter seu perfil em dia.
            </p>
          </div>
          
          <form v-if="storeInfo" class="flex flex-col">
            <div class="px-2 custom-scrollbar max-h-[60vh] overflow-y-auto">
              <div class="mb-4.5 flex flex-col gap-6 xl:flex-row">
                <div class="w-full xl:w-1/2">
                  <label class="mb-2.5 block text-black dark:text-white">
                    Nome da Loja
                  </label>
                  <input
                    v-model="storeInfo.name"
                    type="text"
                    placeholder="Digite o nome da loja"
                    class="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-brand-500 active:border-brand-500 disabled:cursor-default disabled:bg-whiter dark:border-gray-700 dark:bg-gray-800 dark:text-white dark:focus:border-brand-500"
                  />
                </div>

                <div class="w-full xl:w-1/2">
                  <label class="mb-2.5 block text-black dark:text-white">
                    Categoria
                  </label>
                  <select
                    v-model="storeInfo.category"
                    class="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-brand-500 active:border-brand-500 disabled:cursor-default disabled:bg-whiter dark:border-gray-700 dark:bg-gray-800 dark:text-white dark:focus:border-brand-500"
                  >
                    <option value="VESTUARIO">Vestuário</option>
                    <option value="ELETRONICOS">Eletrônicos</option>
                    <option value="COSMETICOS">Cosméticos</option>
                    <option value="PETS">Pets</option>
                    <option value="LIVRARIA">Livraria</option>
                  </select>
                </div>
              </div>

              <div class="mb-4.5">
                <label class="mb-2.5 block text-black dark:text-white">
                  Telefone
                </label>
                <input
                  v-model="storeInfo.telephone"
                  v-maska
                  data-maska="['(##) ####-####', '(##) #####-####']"
                  type="tel"
                  placeholder="(00) 00000-0000"
                  class="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-brand-500 active:border-brand-500 disabled:cursor-default disabled:bg-whiter dark:border-gray-700 dark:bg-gray-800 dark:text-white dark:focus:border-brand-500"
                />
              </div>

              <div class="mb-4.5">
                <label class="mb-2.5 block text-black dark:text-white">
                  Imagem da Loja
                </label>
                <div class="flex flex-col gap-4">
                  <div v-if="storeInfo.image" class="h-20 w-20 overflow-hidden rounded-full border border-gray-200 dark:border-gray-700">
                    <img 
                      :src="storeInfo.image.startsWith('data:') ? storeInfo.image : `data:image/png;base64,${storeInfo.image}`" 
                      alt="Store Logo Preview" 
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

              <div class="mb-6">
                <label class="mb-2.5 block text-black dark:text-white">
                  Descrição
                </label>
                <textarea
                  v-model="storeInfo.description"
                  rows="6"
                  placeholder="Digite a descrição da loja"
                  class="w-full rounded border-[1.5px] border-stroke bg-transparent py-3 px-5 font-medium outline-none transition focus:border-brand-500 active:border-brand-500 disabled:cursor-default disabled:bg-whiter dark:border-gray-700 dark:bg-gray-800 dark:text-white dark:focus:border-brand-500"
                ></textarea>
              </div>
            </div>

            <div class="flex items-center gap-3 px-2 mt-6 lg:justify-end">
              <button
                @click="isProfileInfoModal = false"
                type="button"
                class="flex w-full justify-center rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] sm:w-auto"
              >
                Fechar
              </button>
              <button
                @click="saveProfile"
                type="button"
                class="flex w-full justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 sm:w-auto"
              >
                Salvar Alterações
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
    
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onMounted } from 'vue'
import { getToken } from '@/utils/auth'
import Modal from './Modal.vue'
import apiClient from '@/utils/axios'
import { vMaska } from 'maska/vue'

type Store = {
  id: string;
  name: string;
  description: string;
  image: string;
  category: string;
  telephone: string;
}

const isProfileInfoModal = ref(false)

const storeInfo = ref <Store | null>(null)

const getStoreOrder = async () => {
  try {
    const token = getToken()
    const response = await apiClient.get<Store>('/api/store', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    if (response.data) {
      storeInfo.value = response.data
    }
  } catch (error) {
    console.log('Error fetching store order:', error)
  }
}

const saveProfile = async () => {
  if (!storeInfo.value) return

  try {
    const token = getToken()

    const storeData = {
      name: storeInfo.value.name,
      description: storeInfo.value.description,
      telephone: storeInfo.value.telephone,
      category: storeInfo.value.category,
      image: storeInfo.value.image 
    }

    // Phoenix often expects the params to be wrapped in the resource name
    const payload = { store: storeData }

    await apiClient.patch('/api/store', payload, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    // Update local state immediately to reflect changes
    storeInfo.value = { ...storeInfo.value, ...storeData }

    isProfileInfoModal.value = false
    // await getStoreOrder() // Removed to prevent overwriting with potentially stale data if backend is eventually consistent
  } catch {
    // Handle error silently or show a user notification if needed
  }
}

const formatPhoneNumber = (phone: string | undefined) => {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return cleaned.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3')
  } else if (cleaned.length === 10) {
    return cleaned.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3')
  }
  return phone
}

const handleImageUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    const reader = new FileReader()
    
    reader.onload = (e) => {
      const result = e.target?.result as string
      // Remove the data URL prefix to store just the base64 string, 
      // as the backend seems to expect that based on existing code.
      // However, the display logic now handles both.
      const base64String = result.replace(/^data:image\/[a-z]+;base64,/, '')
      
      if (storeInfo.value) {
        storeInfo.value.image = base64String
      }
    }
    
    reader.readAsDataURL(file)
  }
}

onMounted(() => {
  getStoreOrder()
})


</script>
