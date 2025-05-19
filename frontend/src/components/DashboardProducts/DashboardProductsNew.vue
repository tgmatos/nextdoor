<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

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

    await axios.post('/product/create', product.value)
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
    <h1>Create New Product</h1>

    <form @submit.prevent="createProduct" class="product-form">
      <div class="form-group">
        <label for="name">Product Name</label>
        <input
          id="name"
          v-model="product.name"
          type="text"
          required
          placeholder="Enter product name"
        >
      </div>

      <div class="form-group">
        <label for="description">Description</label>
        <textarea
          id="description"
          v-model="product.description"
          rows="4"
          placeholder="Enter product description"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="price">Price</label>
        <input
          id="price"
          v-model.number="product.price"
          type="number"
          required
          min="0"
          step="0.01"
          placeholder="Enter price"
        >
      </div>

      <div class="form-group">
        <label for="stock">Stock</label>
        <input
          id="stock"
          v-model.number="product.stock"
          type="number"
          required
          min="0"
          placeholder="Enter stock quantity"
        >
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Creating...' : 'Create Product' }}
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
