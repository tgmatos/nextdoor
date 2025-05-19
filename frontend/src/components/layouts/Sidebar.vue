<script setup lang="ts">
import { ref } from 'vue'
import {useRoute, useRouter} from 'vue-router'

const router = useRouter()
const route = useRoute()
const menuItems = ref([
  { title: 'Dashboard', path: '/dashboard', icon: 'dashboard' },
  { title: 'Produtos', path: '/dashboard/products', icon: 'shopping-bag' },
  { title: 'Pedidos', path: '/dashboard/orders', icon: 'shopping-cart' },
])

const isActive = (path: string) => {
  return route.path === path
}
</script>

<template>
  <nav class="sidebar">
    <ul class="menu">
      <li v-for="item in menuItems"
          :key="item.title"
          @click="router.push(item.path)"
          :class="{ active: router.currentRoute.value.path === item.path }">
        <i :class="item.icon"></i>
        {{ item.title }}
      </li>
    </ul>
  </nav>
</template>

<style scoped>
.sidebar {
  height: 100%;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu li {
  padding: 15px;
  cursor: pointer;
}

.menu li:hover {
  background-color: #e9ecef;
}

.menu li.active {
  background-color: #e9ecef;
  font-weight: bold;
}
</style>
