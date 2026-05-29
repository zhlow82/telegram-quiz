<template>
  <div class="min-h-screen flex">

    <!-- Brand panel (desktop only) -->
    <div
      class="hidden md:flex flex-col items-center justify-center flex-[0_0_42%] relative overflow-hidden px-12 bg-slate-900"
    >
      <!-- Decorative circles -->
      <div class="absolute w-96 h-96 rounded-full -top-36 -right-36" style="background: rgba(255,255,255,0.03)"></div>
      <div class="absolute w-72 h-72 rounded-full -bottom-24 -left-24" style="background: rgba(255,255,255,0.03)"></div>

      <div class="relative z-10 max-w-sm w-full">
        <!-- Logo -->
        <div
          class="w-16 h-16 rounded-2xl flex items-center justify-center mb-6 bg-blue-600"
        >
          <Zap class="w-8 h-8 text-white" />
        </div>

        <h1 class="text-3xl font-black text-white mb-3">Telegram Quiz</h1>
        <p class="text-base mb-8 text-slate-400">
          Create and manage engaging quiz games for your Telegram community.
        </p>

        <!-- Feature list -->
        <div class="flex flex-col gap-3">
          <div v-for="f in features" :key="f" class="flex items-center gap-2">
            <CheckCircle class="w-4 h-4 flex-shrink-0 text-blue-500" />
            <span class="text-sm text-slate-300">{{ f }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Form panel -->
    <div class="flex-1 bg-white flex items-center justify-center px-6 py-10">
      <div class="w-full max-w-sm">

        <!-- Mobile logo -->
        <div class="flex md:hidden items-center gap-2 mb-8">
          <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
            <Zap class="w-4 h-4 text-white" />
          </div>
          <span class="font-bold text-slate-900 text-sm">Telegram Quiz</span>
        </div>

        <h2 class="text-xl font-black text-slate-900 mb-1">Welcome back</h2>
        <p class="text-sm text-slate-500 mb-7">Sign in to your account to continue</p>

        <form @submit.prevent="handleLogin">
          <!-- Username -->
          <div class="relative mb-4">
            <User class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <input
              v-model="form.username"
              type="text"
              placeholder="Username"
              autocomplete="username"
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
            />
          </div>

          <!-- Password -->
          <div class="relative mb-3">
            <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <input
              v-model="form.password"
              type="password"
              placeholder="Password"
              autocomplete="current-password"
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
            />
          </div>

          <!-- Remember me / forgot -->
          <div class="flex items-center justify-between mb-5">
            <label class="flex items-center gap-2 text-sm text-slate-600 cursor-pointer select-none">
              <input v-model="remember" type="checkbox" class="rounded border-slate-300 text-blue-600 focus:ring-blue-500" />
              Remember me
            </label>
            <a href="#" class="text-sm text-blue-600 font-medium hover:underline">Forgot password?</a>
          </div>

          <!-- Error alert -->
          <div
            v-if="error"
            class="flex items-center gap-2 mb-5 px-4 py-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm"
          >
            <AlertCircle class="w-4 h-4 flex-shrink-0" />
            {{ error }}
          </div>

          <!-- Submit -->
          <button
            type="submit"
            class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-xl text-sm transition mb-3 disabled:opacity-60 cursor-pointer disabled:cursor-not-allowed"
            :disabled="loading"
          >
            <span v-if="loading" class="inline-flex items-center gap-2">
              <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
              Signing in…
            </span>
            <span v-else>Sign in</span>
          </button>

          <!-- Sign up note -->
          <p class="text-center text-sm text-slate-500">
            Don't have an account?&nbsp;
            <a href="#" class="text-blue-600 font-medium hover:underline">Sign up</a>
          </p>
        </form>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Zap, User, Lock, CheckCircle, AlertCircle } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)
const remember = ref(false)

const features = [
  'Create multi-round quiz games',
  'Photo answer & poll question support',
  'Real-time results & leaderboards',
  'Drag-and-drop question ordering',
]

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(form.value.username, form.value.password)
    router.push('/home')
  } catch {
    error.value = 'Invalid username or password'
  } finally {
    loading.value = false
  }
}
</script>
