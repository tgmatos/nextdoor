<script setup lang="ts">
import Sidebar from './Sidebar.vue';
</script>

<template>
  <div class="dashboard-layout">
    <Sidebar class="sidebar" />
    <div class="main-content">
      <div class="content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background-color: #f8f9fa;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e9ecef;
}

.content {
  flex: 1;
  padding: 20px;
}
</style>
