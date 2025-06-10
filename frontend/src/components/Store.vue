<script setup lang="ts">
import {ref} from "vue";
import apiClient from "@/router/axios.ts";
import {getToken} from "@/utils/auth.ts";
import router from "@/router";

type Category = 'VESTUARIO' | 'ELETRONICOS' | 'COSMETICOS' | 'PETS' | 'LIVRARIA';

type Store = {
  name: String,
  description: String,
  telephone: String,
  category: Category
}

type RegisterResponse  = {
  store_id: string;
}

const name = ref('');
const description = ref('');
const telephone = ref('');
const category = ref<Category>('VESTUARIO');

const registerStore = async (name: String, description: String, telephone: String, category: Category) => {
  let store: Store = {
    name: name,
    description: description,
    telephone: telephone,
    category: category,
  };

  try {
    const token = getToken();
    const response = await apiClient.post<RegisterResponse>("/api/store", store,
      {
        headers:
          {
            'Authorization': `Bearer ${token}`
          }
      });
    if (response.data.store_id != null) {
      router.push("/about")
    }
  } catch (error) {
    console.error("New Store failed:", error)
  }
}
</script>

<template>
  <form @submit.prevent="registerStore(name, description, telephone, category)">
    <input v-model="name" type="text" placeholder="Nome da Loja" required>
    <input v-model="description" type="text" placeholder="Descrição" required>
    <input v-model="telephone" type="tel" placeholder="Telefone" required>
    <select v-model="category" required>
      <option value="VESTUARIO">Vestuário</option>
      <option value="ELETRONICOS">Eletrônicos</option>
      <option value="COSMETICOS">Cosméticos</option>
      <option value="PETS">Pets</option>
      <option value="LIVRARIA">Livraria</option>
    </select>
    <button type="submit">Criar</button>
  </form>
</template>

<style scoped>
form {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  background-color: #f9f9f9;
}

input, select {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}

input:focus, select:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 5px rgba(0, 123, 255, 0.5);
}

button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

button:active {
  background-color: #004080;
}
</style>
