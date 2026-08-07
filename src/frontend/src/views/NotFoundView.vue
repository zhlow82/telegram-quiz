<template>
  <div class="flex flex-col items-center justify-center py-24 text-center">
    <div class="w-20 h-20 bg-slate-100 rounded-3xl flex items-center justify-center mb-6">
      <FileQuestion class="w-10 h-10 text-slate-400" />
    </div>
    <h1 class="text-4xl font-black text-slate-900 mb-2">404</h1>
    <p class="text-lg text-slate-500 mb-8">The page you're looking for doesn't exist.</p>
    <router-link
      to="/home"
      class="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-white text-sm font-semibold px-5 py-2.5 rounded-full transition no-underline"
    >
      <ArrowLeft class="w-4 h-4" />
      Back to Dashboard
    </router-link>
  </div>
</template>

<script setup lang="ts">
import { FileQuestion, ArrowLeft } from '@lucide/vue'
</script>
