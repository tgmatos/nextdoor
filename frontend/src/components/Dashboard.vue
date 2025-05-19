<script setup lang="ts">
import apiClient from "@/router/axios.ts";
import {getToken} from "@/utils/auth.ts";

type Category = 'VESTUARIO' | 'ELETRONICOS' | 'COSMETICOS' | 'PETS' | 'LIVRARIA';

type Store = {
  id: string,
  name: string,
  address: string,
  description: string,
  category: Category,
  inserted_at: string,
  updated_at: string,
  telephone: string,
};

</script>

<template>

</template>

<style scoped>

</style>

<!--
{
"store": {
"id": "a9397a1e-3e52-4a3f-b9cc-3bb0fe1e4387",
"name": "Loja Teste2",
"address": "Av. Frei Gaspar2",
"description": "Loja de Teste2",
"category": "VESTUARIO",
"inserted_at": "2025-04-11T00:08:48",
"updated_at": "2025-04-11T00:08:48",
"telephone": "33998646163"
}
}
->
