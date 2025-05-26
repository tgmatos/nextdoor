<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from "@/router/axios.ts";
import {getToken} from "@/utils/auth.ts";

const router = useRouter()
const product = ref({
  name: '',
  description: '',
  price: 0,
  stock: 0
})

const error = ref('')
const isSubmitting = ref(false)

const createProduct = async () => {
  try {
    isSubmitting.value = true
    error.value = ''
    const token = getToken();

    const productData = {
      product: {
        name: product.value.name,
        description: product.value.description,
        price: product.value.price,
        stock: product.value.stock
      }
    }

    await apiClient.post('api/store/product', productData, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    router.push('/dashboard/products')
  } catch (e) {
    error.value = 'Failed to create product. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="products-new">
    <h1>Adicionar Produto</h1>

    <form @submit.prevent="createProduct" class="product-form">
      <div class="form-group">
        <label for="name">Nome</label>
        <input
          id="name"
          v-model="product.name"
          type="text"
          required
          placeholder="Nome do Produto"
        >
      </div>

      <div class="form-group">
        <label for="description">Descrição</label>
        <textarea
          id="description"
          v-model="product.description"
          rows="4"
          placeholder="Descrição do Produto"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="price">Preço</label>
        <input
          id="price"
          v-model.number="product.price"
          type="number"
          required
          min="0"
          step="0.01"
          placeholder="Preço do Produto"
        >
      </div>

      <div class="form-group">
        <label for="stock">Quantidade</label>
        <input
          id="stock"
          v-model.number="product.stock"
          type="number"
          required
          min="0"
          placeholder="Quantidade de Produtos"
        >
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Criando Produto...' : 'Criar Produto' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.products-new {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.product-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label {
  font-weight: bold;
}

input, textarea {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  resize: vertical;
}

button {
  padding: 12px 24px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  font-size: 14px;
  margin-bottom: 10px;
}
</style>
