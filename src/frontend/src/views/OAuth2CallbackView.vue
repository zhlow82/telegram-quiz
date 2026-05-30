<!-- Handles redirect from auth-service after successful Google sign-in of a known user.
     URL: /oauth2/callback?token=JWT -->
<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center">
    <div class="text-slate-500 text-sm">Signing you in…</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  const token = route.query.token as string | undefined
  if (!token) {
    router.replace('/login')
    return
  }
  authStore.setTokenFromOAuth(token)
  router.replace('/home')
})
</script>
