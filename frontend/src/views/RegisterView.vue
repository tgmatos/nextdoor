<script setup lang="ts">
import Register from "../components/Register.vue";
</script>

<template>
  <div class="register-container">
    <register/>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f4;
}

.register-container :deep(form) {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  width: 450px;
}
</style>
