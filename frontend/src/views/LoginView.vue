<script setup lang="ts">
import Login from "@/components/Login.vue";
</script>

<template>
  <div class="login-container">
    <login/>
  </div>
</template>

<style scoped>
.login-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  width: 450px;
}
</style>
