<script setup lang="ts">
import apiClient from "@/router/axios.ts";
import {getToken} from "@/utils/auth.ts";
import {onMounted, ref, computed} from "vue";
import {useRouter} from "vue-router";

const router = useRouter();

type Product = {
  id: string,
  name: string,
  quantity: number,
  price: number,
  description: string,
  inserted_at: string,
  updated_at: string
}

const products = ref<Product[]>([])
const isLoading = ref(true);
const searchQuery = ref('')

const getProducts = async () => {
  try {
    const token = getToken();
    const response = await  apiClient.get<{products: Product[]}>("/api/store/product",
      {
        headers:
          {
            'Authorization': `Bearer ${token}`
          }
      })
    if(response.data.products != null) {
      products.value = response.data.products
    }
  } catch (error) {
    console.log('Error fetching products:', error)
  }
}

const filteredProducts = computed(() => {
  const query = searchQuery.value.toLowerCase()
  return products.value.filter(product =>
    product.name.toLowerCase().includes(query) ||
    product.description.toLowerCase().includes(query)
  )
})

const goToNewProduct = () => {
  router.push('/dashboard/products/new');
}

onMounted(() => {
  getProducts()
})
</script>

<template>
  <div class="products-container">
    <div class="header-container">
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Pesquisar"
          class="search-input"
        />
      </div>
      <button class="new-product-button" @click="goToNewProduct">
        New Product
      </button>
    </div>
    <table class="products-table" v-if="filteredProducts.length">
      <thead>
      <tr>
        <th>Nome</th>
        <th>Descrição</th>
        <th>Preço</th>
        <th>Estoque</th>
        <th>Adicionado</th>
        <th>Modificado</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="product in filteredProducts" :key="product.id">
        <td>{{ product.name }}</td>
        <td>{{ product.description }}</td>
        <td>{{ product.price }}</td>
        <td>{{ product.quantity }}</td>
        <td>{{ new Date(product.inserted_at).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }) }}</td>
        <td>{{ new Date(product.updated_at).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }) }}</td>
      </tr>
      </tbody>
    </table>
    <p v-else>Nenhum produto encontrado</p>
  </div>
</template>

<style scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.search-container {
  flex-grow: 1;
}

.new-product-button {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  white-space: nowrap;
}

.new-product-button:hover {
  background-color: #45a049;
}

.products-container {
  width: 100%;
  overflow-x: auto;
}

.search-input {
  width: 100%;
  padding: 10px 15px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  outline: none;
  transition: border-color 0.2s ease;
}

.search-input:focus {
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}


.products-table {
  width: 100%;
  min-width: max-content; /* Ensures table doesn't shrink below content width */
  border-collapse: collapse;
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.products-table th,
.products-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
  white-space: nowrap; /* Prevents text from wrapping */
}

.products-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.products-table tr:hover {
  background-color: #f5f5f5;
}

.products-table td {
  color: #666;
}
</style>
