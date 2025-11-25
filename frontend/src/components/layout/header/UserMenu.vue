<template>
  <div class="relative" ref="dropdownRef">
    <button
      class="flex items-center text-gray-700 dark:text-gray-400"
      @click.prevent="toggleDropdown"
    >
      <span class="mr-3 overflow-hidden rounded-full h-11 w-11">
        <img
              v-if="storeInfo?.image"
              :src="`data:image/png;base64,${storeInfo.image}`"
              alt="Store Logo"
              class="mb-4 w-24 h-24 rounded-full object-cover mx-auto xl:mx-0"
            />
      </span>

      <span class="block mr-1 font-medium text-theme-sm">{{ storeInfo?.name }}</span>

      <ChevronDownIcon :class="{ 'rotate-180': dropdownOpen }" />
    </button>

    <!-- Dropdown Start -->
    <div
      v-if="dropdownOpen"
      class="absolute right-0 mt-[17px] flex w-[260px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-theme-lg dark:border-gray-800 dark:bg-gray-dark"
    >
      <div>
        <span class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400">
          {{ storeInfo?.name }}
        </span>
      </div>

      <ul class="flex flex-col gap-1 pt-4 pb-3 border-b border-gray-200 dark:border-gray-800">
        <li v-for="item in menuItems" :key="item.href">
          <router-link
            :to="item.href"
            class="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
          >
            <!-- SVG icon would go here -->
            <component
              :is="item.icon"
              class="text-gray-500 group-hover:text-gray-700 dark:group-hover:text-gray-300"
            />
            {{ item.text }}
          </router-link>
        </li>
      </ul>
      <button
        @click="signOut"
        class="flex items-center gap-3 px-3 py-2 mt-3 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300 w-full text-left"
      >
        <LogoutIcon
          class="text-gray-500 group-hover:text-gray-700 dark:group-hover:text-gray-300"
        />
        Sign out
      </button>
    </div>
    <!-- Dropdown End -->
  </div>
</template>

<script setup lang="ts">
import { UserCircleIcon, ChevronDownIcon, LogoutIcon, SettingsIcon, InfoCircleIcon } from '@/icons'
import { RouterLink, useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'
import { getToken, removeToken } from '@/utils/auth'
import Modal from './Modal.vue'
import apiClient from '@/utils/axios'

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

onMounted(() => {
  getStoreOrder()
})



const dropdownOpen = ref(false)
const dropdownRef = ref(null)

const menuItems = [
  { href: '/profile', icon: UserCircleIcon, text: 'Edit profile' },
]

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}

const closeDropdown = () => {
  dropdownOpen.value = false
}

const router = useRouter()

const signOut = () => {
  removeToken()
  closeDropdown()
  router.push('/')
}

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
