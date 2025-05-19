<script setup lang="ts">
import {ref} from "vue";
import apiClient from "../router/axios.ts"
import {setToken} from "@/utils/auth.ts";
import router from "@/router";

type User = {
  email: String,
  username: String,
  password: String,
}

const email = ref('');
const username = ref('');
const password = ref('');

type RegisterResponse  = {
  token: string;
}

const registerUser = async (email: String, username: String, password: String) => {
  let user: User = {
    email: email,
    username: username,
    password: password,
  }

  try {
    const response = await apiClient.post<RegisterResponse>("/api/account/register", user);
    if(response.data.token != null) {
      setToken(response.data.token);
      router.push('/store/new')
    }

  } catch (error) {
    console.error("Registration failed:", error);
  }
}
</script>

<template>
  <form @submit.prevent="registerUser(email, username, password)">
    <input v-model="email" type="email" placeholder="Email" required/>
    <input v-model="username" type="text" placeholder="Username" required />
    <input v-model="password" type="password" placeholder="Password" required/>
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
