<script setup lang="ts">
import {ref} from "vue";
import apiClient from "../router/axios.ts"
import  {setToken} from "@/utils/auth.ts";
import router from "@/router";

type User = {
  email: String,
  password: String,
}

const email = ref('');
const password = ref('');

type LoginResponse  = {
  token: string;
}

const loginUser = async (email: String, password: String) => {
  let user: User = {
    email: email,
    password: password,
  }

  try {
    const response = await apiClient.post<LoginResponse>("/api/account/login", user);
    if(response.data.token != null) {
      setToken(response.data.token);
      router.push('/dashboard')
    }
  } catch (error) {
    console.error("Login failed:", error);
  }
}
</script>

<template>
  <form @submit.prevent="loginUser(email, password)">
    <input v-model="email" type="email" placeholder="Email" required/>
    <input v-model="password" type="password" placeholder="Password" required/>
    <button type="submit">Login</button>
  </form>
</template>

<style scoped>
.login-container input {
  width: 100%;
  padding: 10px;
  margin: 5px 0;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 16px;
}
.login-container input:focus {
  border-color: #007bff;
  outline: none;
}

.login-container button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.login-container button:hover {
  background-color: #0056b3;
}
</style>
