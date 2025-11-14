<script setup lang="ts">
import { ref } from 'vue';
import NewProduct from '@/components/products/NewProduct.vue';
import AdminLayout from '@/components/layout/AdminLayout.vue'

const currentPageTitle = ref('Form Elements')
</script>

<template>
  <AdminLayout>
    <PageBreadcrumb :pageTitle="currentPageTitle" />
    <div class="space-y-5 sm:space-y-6">
      <ComponentCard title="New Product" class="bg-white dark:bg-gray-900">
        <NewProduct />
      </ComponentCard>
    </div>
  </AdminLayout>
</template>

<style scoped>

</style>
