<script setup lang="ts">
import {ref} from "vue";
import apiClient from "../router/axios.ts"
import {setToken} from "@/utils/auth.ts";
import router from "@/router";

type Address = {
  number: String,
  street: String,
  neighborhood: String,
  cep: String
}

type User = {
  email: String,
  username: String,
  password: String,
  address: Address
}

const email = ref('');
const username = ref('');
const password = ref('');

const number = ref('')
const street = ref('')
const neighborhood = ref('')
const cep = ref('')

type RegisterResponse  = {
  token: string;
}

const registerUser = async (email: String, username: String, password: String, number: String, street: String, neighborhood: String, cep: String) => {
  let address: Address = {
    number: number,
    street: street,
    neighborhood: neighborhood,
    cep: cep
  }

  let user: User = {
    email: email,
    username: username,
    password: password,
    address: address
  }

  try {
    const response = await apiClient.post<RegisterResponse>("/api/account/register", user);
    if(response.data.token != null) {
      setToken(response.data.token);
      await router.push('/store/new')
    }
  } catch (error) {
    console.error("Registration failed:", error);
  }
}
</script>

<template>
  <form @submit.prevent="registerUser(email, username, password, number, street, neighborhood, cep)">
    <h3>User</h3>
    <input v-model="email" type="email" placeholder="Email" required/>
    <input v-model="username" type="text" placeholder="Username" required />
    <input v-model="password" type="password" placeholder="Password" required/>
    <h3>Address</h3>
    <input v-model="street" type="text" placeholder="street" required/>
    <input v-model="number" type="text" placeholder="number" required />
    <input v-model="neighborhood" type="text" placeholder="neighborhood" required/>
    <input v-model="cep" type="text" placeholder="cep" required/>
    <button type="submit">Registrar</button>
  </form>
</template>

<style scoped>
.register-container input {
  width: 100%;
  padding: 10px;
  margin: 5px 0;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 16px;
}
.register-container input:focus {
  border-color: #007bff;
  outline: none;
}

.register-container button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.register-container button:hover {
  background-color: #0056b3;
}
</style>
