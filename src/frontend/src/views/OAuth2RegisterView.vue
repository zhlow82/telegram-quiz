<!-- Handles redirect from auth-service for a new Google user who needs an invitation code.
     URL: /oauth2/register?state=UUID -->
<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 w-full max-w-sm p-8">
      <!-- Logo -->
      <div class="flex justify-center mb-6">
        <div class="w-12 h-12 rounded-2xl bg-blue-600 flex items-center justify-center">
          <Zap class="w-6 h-6 text-white" />
        </div>
      </div>

      <h1 class="text-xl font-black text-slate-900 text-center mb-1">Almost there</h1>
      <p class="text-sm text-slate-500 text-center mb-6">
        Your Google account isn't registered yet.<br />Enter your invitation code to complete setup.
      </p>

      <div class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">Invitation Code</label>
          <input
            v-model="code"
            type="text"
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm font-mono uppercase tracking-widest focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="XXXXXXXXXXXX"
            @keyup.enter="submit"
          />
        </div>
        <p v-if="error" class="text-xs text-red-500">{{ error }}</p>
        <button
          class="w-full py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
          :disabled="loading"
          @click="submit"
        >
          {{ loading ? 'Verifying…' : 'Complete Sign Up' }}
        </button>
      </div>

      <p class="mt-4 text-xs text-center text-slate-400">
        Wrong account?
        <a href="/tg-quiz/login" class="text-blue-600 hover:underline">Go back</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Zap } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const code = ref('')
const loading = ref(false)
const error = ref('')
const state = ref('')

onMounted(() => {
  state.value = route.query.state as string ?? ''
  if (!state.value) router.replace('/login')
})

async function submit() {
  error.value = ''
  if (!code.value.trim()) {
    error.value = 'Please enter your invitation code.'
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/oauth2/complete', {
      state: state.value,
      invitationCode: code.value.trim().toUpperCase()
    })
    authStore.setTokenFromOAuth(res.data.accessToken)
    router.replace('/home')
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Invalid invitation code.'
  } finally {
    loading.value = false
  }
}
</script>
