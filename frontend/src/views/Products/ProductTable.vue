<template>
  <AdminLayout>
    <div class="space-y-5 sm:space-y-6">
      <ComponentCard title="Tabela de Produtos">
        <ProductTableOne/>
      </ComponentCard>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import AdminLayout from "@/components/layout/AdminLayout.vue";
import ComponentCard from "@/components/common/ComponentCard.vue";
import ProductTableOne from '@/components/products/ProductTableOne.vue'
</script>
