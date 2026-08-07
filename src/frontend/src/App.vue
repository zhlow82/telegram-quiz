<template>
  <router-view v-slot="{ Component, route }">
    <AppLayout v-if="route.meta.requiresAuth || route.name === 'not-found'">
      <transition name="page" mode="out-in">
        <div :key="route.path">
          <component :is="Component" />
        </div>
      </transition>
    </AppLayout>
    <template v-else>
      <transition name="page" mode="out-in">
        <component :is="Component" />
      </transition>
    </template>
  </router-view>
  <AppToast />
</template>

<script setup lang="ts">
import AppLayout from '@/components/AppLayout.vue'
import AppToast from '@/components/AppToast.vue'
</script>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
