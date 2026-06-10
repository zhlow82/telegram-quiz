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
          class="w-16 h-16 rounded-2xl flex items-center justify-center mb-6 overflow-hidden bg-blue-600"
        >
          <img v-if="brandingStore.appLogoUrl" :src="brandingStore.appLogoUrl" class="w-full h-full object-cover" alt="" />
          <Zap v-else class="w-8 h-8 text-white" />
        </div>

        <h1 class="text-3xl font-black text-white mb-3">{{ brandingStore.appName }}</h1>
        <p class="text-base mb-8 text-slate-400">
          {{ brandingStore.loginWelcomeText || 'Create and manage engaging quiz games for your Telegram community.' }}
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
          <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden bg-blue-600">
            <img v-if="brandingStore.appLogoUrl" :src="brandingStore.appLogoUrl" class="w-full h-full object-cover" alt="" />
            <Zap v-else class="w-4 h-4 text-white" />
          </div>
          <span class="font-bold text-slate-900 text-sm">{{ brandingStore.appName }}</span>
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
              :type="showPassword ? 'text' : 'password'"
              placeholder="Password"
              autocomplete="current-password"
              class="w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 p-0.5 text-slate-400 hover:text-slate-600 transition cursor-pointer"
              @click="showPassword = !showPassword"
            >
              <EyeOff v-if="showPassword" class="w-4 h-4" />
              <Eye v-else class="w-4 h-4" />
            </button>
          </div>

          <!-- Remember me / forgot -->
          <div class="flex items-center mb-5">
            <label class="flex items-center gap-2 text-sm text-slate-600 cursor-pointer select-none">
              <input v-model="remember" type="checkbox" class="rounded border-slate-300 text-blue-600 focus:ring-blue-500" />
              Remember me
            </label>
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

          <!-- Google sign-in -->
          <div v-if="googleConfigured" class="flex items-center gap-3 my-4">
            <div class="flex-1 h-px bg-slate-200"></div>
            <span class="text-xs text-slate-400">or</span>
            <div class="flex-1 h-px bg-slate-200"></div>
          </div>
          <a
            v-if="googleConfigured"
            href="http://localhost:8081/oauth2/authorization/google"
            class="w-full flex items-center justify-center gap-2.5 border border-slate-200 rounded-xl py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50 transition cursor-pointer no-underline"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Sign in with Google
          </a>


        </form>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Zap, User, Lock, CheckCircle, AlertCircle, Eye, EyeOff } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import { useBrandingStore } from '@/stores/branding'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()
const brandingStore = useBrandingStore()

const form = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)
const remember = ref(false)
const googleConfigured = ref(false)
const showPassword = ref(false)

const features = [
  'Create multi-round quiz games',
  'Photo answer & poll question support',
  'Real-time results & leaderboards',
  'Drag-and-drop question ordering',
]

onMounted(async () => {
  try {
    const res = await api.get('/auth/oauth2/configured')
    googleConfigured.value = res.data.configured
  } catch {
    // not configured or unreachable — hide button
  }
})

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
