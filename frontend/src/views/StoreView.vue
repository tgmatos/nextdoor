<script setup lang="ts">
import Store from "@/components/Store.vue";
</script>

<template>
  <div class="store-view">
    <h1>Register a New Store</h1>
    <Store />
  </div>
</template>

<style scoped>
.store-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}

h1 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}
</style>
