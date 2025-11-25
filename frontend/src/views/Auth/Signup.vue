<template>
  <FullScreenLayout>
    <div class="relative p-6 bg-white z-1 dark:bg-gray-900 sm:p-0">
      <div
        class="relative flex flex-col justify-center w-full h-screen lg:flex-row dark:bg-gray-900"
      >
        <div class="flex flex-col flex-1 w-full lg:w-1/2">
          <!-- Form -->
          <div class="flex flex-col justify-center flex-1 w-full max-w-md mx-auto">
            <div class="mb-5 sm:mb-8">
              <h1
                class="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md"
              >
                Sign Up
              </h1>
              <p class="text-sm text-gray-500 dark:text-gray-400">
                Enter your email and password to sign up!
              </p>
            </div>
            <div>
              <form
                @submit.prevent="
                  registerUser(email, username, password, number, street, neighborhood, cep)
                "
              >
                <div class="space-y-5">
                  <!-- Username -->
                  <div class="sm:col-span-1">
                    <label
                      for="fname"
                      class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                    >
                      Nome de usuário<span class="text-error-500">*</span>
                    </label>
                    <input
                      v-model="username"
                      type="text"
                      id="fname"
                      name="fname"
                      placeholder="Insira nome de usuário"
                      class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                    />
                  </div>
                  <!-- Email -->
                  <div>
                    <label
                      class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                    >
                      Email
                    </label>
                    <div class="relative">
                      <span
                        class="absolute left-0 top-1/2 -translate-y-1/2 border-r border-gray-200 px-3.5 py-3 text-gray-500 dark:border-gray-800 dark:text-gray-400"
                      >
                        <svg
                          width="20"
                          height="20"
                          viewBox="0 0 20 20"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            fill-rule="evenodd"
                            clip-rule="evenodd"
                            d="M3.04175 7.06206V14.375C3.04175 14.6511 3.26561 14.875 3.54175 14.875H16.4584C16.7346 14.875 16.9584 14.6511 16.9584 14.375V7.06245L11.1443 11.1168C10.457 11.5961 9.54373 11.5961 8.85638 11.1168L3.04175 7.06206ZM16.9584 5.19262C16.9584 5.19341 16.9584 5.1942 16.9584 5.19498V5.20026C16.9572 5.22216 16.946 5.24239 16.9279 5.25501L10.2864 9.88638C10.1145 10.0062 9.8862 10.0062 9.71437 9.88638L3.07255 5.25485C3.05342 5.24151 3.04202 5.21967 3.04202 5.19636C3.042 5.15695 3.07394 5.125 3.11335 5.125H16.8871C16.9253 5.125 16.9564 5.15494 16.9584 5.19262ZM18.4584 5.21428V14.375C18.4584 15.4796 17.563 16.375 16.4584 16.375H3.54175C2.43718 16.375 1.54175 15.4796 1.54175 14.375V5.19498C1.54175 5.1852 1.54194 5.17546 1.54231 5.16577C1.55858 4.31209 2.25571 3.625 3.11335 3.625H16.8871C17.7549 3.625 18.4584 4.32843 18.4585 5.19622C18.4585 5.20225 18.4585 5.20826 18.4584 5.21428Z"
                            fill="#667085"
                          />
                        </svg>
                      </span>
                      <input
                        v-model="email"
                        type="text"
                        placeholder="info@gmail.com"
                        class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 pl-[62px] text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      />
                    </div>
                  </div>
                  <!-- Password -->
                  <div>
                    <label
                      for="password"
                      class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                    >
                      Senha<span class="text-error-500">*</span>
                    </label>
                    <div class="relative">
                      <input
                        v-model="password"
                        :type="showPassword ? 'text' : 'password'"
                        id="password"
                        placeholder="Insira senha"
                        class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent py-2.5 pl-4 pr-11 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      />
                      <span
                        @click="togglePasswordVisibility"
                        class="absolute z-30 text-gray-500 -translate-y-1/2 cursor-pointer right-4 top-1/2 dark:text-gray-400"
                      >
                        <svg
                          v-if="!showPassword"
                          class="fill-current"
                          width="20"
                          height="20"
                          viewBox="0 0 20 20"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            fill-rule="evenodd"
                            clip-rule="evenodd"
                            d="M10.0002 13.8619C7.23361 13.8619 4.86803 12.1372 3.92328 9.70241C4.86804 7.26761 7.23361 5.54297 10.0002 5.54297C12.7667 5.54297 15.1323 7.26762 16.0771 9.70243C15.1323 12.1372 12.7667 13.8619 10.0002 13.8619ZM10.0002 4.04297C6.48191 4.04297 3.49489 6.30917 2.4155 9.4593C2.3615 9.61687 2.3615 9.78794 2.41549 9.94552C3.49488 13.0957 6.48191 15.3619 10.0002 15.3619C13.5184 15.3619 16.5055 13.0957 17.5849 9.94555C17.6389 9.78797 17.6389 9.6169 17.5849 9.45932C16.5055 6.30919 13.5184 4.04297 10.0002 4.04297ZM9.99151 7.84413C8.96527 7.84413 8.13333 8.67606 8.13333 9.70231C8.13333 10.7286 8.96527 11.5605 9.99151 11.5605H10.0064C11.0326 11.5605 11.8646 10.7286 11.8646 9.70231C11.8646 8.67606 11.0326 7.84413 10.0064 7.84413H9.99151Z"
                            fill="#98A2B3"
                          />
                        </svg>
                        <svg
                          v-else
                          class="fill-current"
                          width="20"
                          height="20"
                          viewBox="0 0 20 20"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            fill-rule="evenodd"
                            clip-rule="evenodd"
                            d="M4.63803 3.57709C4.34513 3.2842 3.87026 3.2842 3.57737 3.57709C3.28447 3.86999 3.28447 4.34486 3.57737 4.63775L4.85323 5.91362C3.74609 6.84199 2.89363 8.06395 2.4155 9.45936C2.3615 9.61694 2.3615 9.78801 2.41549 9.94558C3.49488 13.0957 6.48191 15.3619 10.0002 15.3619C11.255 15.3619 12.4422 15.0737 13.4994 14.5598L15.3625 16.4229C15.6554 16.7158 16.1302 16.7158 16.4231 16.4229C16.716 16.13 16.716 15.6551 16.4231 15.3622L4.63803 3.57709ZM12.3608 13.4212L10.4475 11.5079C10.3061 11.5423 10.1584 11.5606 10.0064 11.5606H9.99151C8.96527 11.5606 8.13333 10.7286 8.13333 9.70237C8.13333 9.5461 8.15262 9.39434 8.18895 9.24933L5.91885 6.97923C5.03505 7.69015 4.34057 8.62704 3.92328 9.70247C4.86803 12.1373 7.23361 13.8619 10.0002 13.8619C10.8326 13.8619 11.6287 13.7058 12.3608 13.4212ZM16.0771 9.70249C15.7843 10.4569 15.3552 11.1432 14.8199 11.7311L15.8813 12.7925C16.6329 11.9813 17.2187 11.0143 17.5849 9.94561C17.6389 9.78803 17.6389 9.61696 17.5849 9.45938C16.5055 6.30925 13.5184 4.04303 10.0002 4.04303C9.13525 4.04303 8.30244 4.17999 7.52218 4.43338L8.75139 5.66259C9.1556 5.58413 9.57311 5.54303 10.0002 5.54303C12.7667 5.54303 15.1323 7.26768 16.0771 9.70249Z"
                            fill="#98A2B3"
                          />
                        </svg>
                      </span>
                    </div>
                  </div>
                  <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
                    <!-- Street -->
                    <div class="sm:col-span-1">
                      <label
                        for="fname"
                        class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                      >
                        Rua<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="street"
                        type="text"
                        id="fname"
                        name="fname"
                        placeholder="Insira rua"
                        class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      />
                    </div>
                    <!-- Number -->
                    <div class="sm:col-span-1">
                      <label
                        for="lname"
                        class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                      >
                        Número<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="number"
                        type="text"
                        id="lname"
                        name="lname"
                        placeholder="Insira número"
                        class="dark:bg-dark-900 ah-11 w-full drounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      />
                    </div>
                    <!-- Neighborhood -->
                    <div class="sm:col-span-1">
                      <label
                        for="lname"
                        class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                      >
                        Bairro<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="neighborhood"
                        type="text"
                        id="lname"
                        name="lname"
                        placeholder="Insira bairro"
                        class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      />
                    </div>
                    <!-- CEP -->
                    <div class="sm:col-span-1">
                      <label
                        for="lname"
                        class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400"
                      >
                        CEP<span class="text-error-500">*</span>
                      </label>
                      <input
                        v-model="cep"
                        type="text"
                        id="lname"
                        name="lname"
                        placeholder="Insira CEP"
                        v-maska data-maska="#####-###"
                        class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800"
                      />
                    </div>
                  </div>
                  <!-- Button -->
                  <div>
                    <button
                      type="submit"
                      class="flex items-center justify-center w-full px-4 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 shadow-theme-xs hover:bg-brand-600"
                    >
                      Sign Up
                    </button>
                  </div>
                </div>
              </form>
              <div class="mt-5">
                <p
                  class="text-sm font-normal text-center text-gray-700 dark:text-gray-400 sm:text-start"
                >
                  Already have an account?
                  <router-link
                    to="/signin"
                    class="text-brand-500 hover:text-brand-600 dark:text-brand-400"
                    >Sign In
                  </router-link>
                </p>
              </div>
            </div>
          </div>
        </div>
        <div
          class="relative items-center hidden w-full h-full lg:w-1/2 bg-brand-950 dark:bg-white/5 lg:grid"
        >
          <div class="flex items-center justify-center z-1">
            <common-grid-shape />
            <div class="flex flex-col items-center max-w-xs">
              <router-link to="/" class="block mb-4">
                <img width="{231}" height="{48}" src="/images/logo/auth-logo.svg" alt="Logo" />
              </router-link>
              <p class="text-center text-gray-400 dark:text-white/60">
                Free and Open-Source Tailwind CSS Admin Dashboard Template
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </FullScreenLayout>
</template>

<script setup lang="ts">
import FullScreenLayout from '@/components/layout/FullScreenLayout.vue'
import CommonGridShape from '@/components/common/CommonGridShape.vue'
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import apiClient from '@/utils/axios.ts'
import { setToken } from '@/utils/auth.ts'
import router from '@/router'



type Address = {
  number: string
  street: string
  neighborhood: string
  cep: string
}

type User = {
  email: string
  username: string
  password: string
  address: Address[]
}

type RegisterResponse = {
  token: string
}

const username = ref('')
const email = ref('')
const password = ref('')
const showPassword = ref(false)

const street = ref('')
const number = ref('')
const neighborhood = ref('')
const cep = ref('')

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const registerUser = async (
  email: string,
  username: string,
  password: string,
  number: string,
  street: string,
  neighborhood: string,
  cep: string,
) => {
  const address: Address = {
    number: number,
    street: street,
    neighborhood: neighborhood,
    cep: cep.replace(/\D/g, ''),
  }

  const user: User = {
    email: email,
    username: username,
    password: password,
    address: [address],
  }

  try {
    const response = await apiClient.post<RegisterResponse>('/api/account/register', user)
    if (response.data.token != null) {
      setToken(response.data.token)
      await router.push('/store/new')
    }
  } catch (error) {
    console.error('Registration failed:', error)
  }
}

import { watch } from 'vue'

watch(cep, async (newCep) => {
  const cleanCep = newCep.replace(/\D/g, '')
  if (cleanCep.length === 8) {
    try {
      const response = await fetch(`https://viacep.com.br/ws/${cleanCep}/json/`)
      const data = await response.json()
      if (!data.erro) {
        street.value = data.logradouro
        neighborhood.value = data.bairro
      }
    } catch (error) {
      console.error('Error fetching CEP:', error)
    }
  }
})
</script>
