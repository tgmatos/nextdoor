<script setup lang="ts">
import { ref } from 'vue'
</script>

<template>
  <header class="topbar">
    <div class="search">
      <input type="text" placeholder="Search...">
    </div>
  </header>
</template>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.search input {
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}
</style>
