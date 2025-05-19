<script setup lang="ts">
// No additional imports needed as the layout is handled by the router
</script>

<template>
  <div class="dashboard-home">
    <h1>Dashboard</h1>
    <div class="dashboard-stats">
      <div class="stat-card">
        <h3>Total Products</h3>
        <p>0</p>
      </div>
      <div class="stat-card">
        <h3>Total Orders</h3>
        <p>0</p>
      </div>
      <div class="stat-card">
        <h3>Revenue</h3>
        <p>$0.00</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-home {
  padding: 20px;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 16px;
}

.stat-card p {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
</style>
