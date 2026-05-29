<template>
  <div class="flex h-screen bg-slate-50 overflow-hidden">

    <!-- Mobile backdrop -->
    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="drawerOpen"
        class="fixed inset-0 bg-black/50 z-30 md:hidden"
        @click="drawerOpen = false"
      />
    </transition>

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 w-64 flex-shrink-0 bg-slate-900 flex flex-col',
        'md:static md:z-auto md:translate-x-0',
        'transition-transform duration-200 ease-in-out',
        drawerOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
    >
      <!-- Brand -->
      <div class="flex items-center gap-3 px-4 py-5 flex-shrink-0">
        <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
          <Zap class="w-4 h-4 text-white" />
        </div>
        <span class="text-white font-bold text-sm">Telegram Quiz</span>
      </div>

      <div class="border-t border-white/5" />

      <!-- Nav -->
      <nav class="flex-1 px-3 pt-4 overflow-y-auto">
        <p
          class="px-3 mb-2 text-xs font-semibold uppercase"
          style="color: rgba(148,163,184,0.6); letter-spacing: 0.08em"
        >Main</p>
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 px-3 py-2 rounded-lg mb-1 text-sm font-medium text-slate-400 hover:text-white hover:bg-white/5 transition-colors no-underline"
          active-class="!text-white !bg-white/10"
          @click="drawerOpen = false"
        >
          <component :is="item.icon" class="w-4 h-4 flex-shrink-0" />
          {{ item.label }}
        </router-link>
      </nav>

      <!-- User section -->
      <div class="flex-shrink-0 border-t border-white/5 p-3">
        <div class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl mb-2" style="background: rgba(255,255,255,0.06)">
          <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
            {{ userInitial }}
          </div>
          <div class="min-w-0">
            <div class="text-white text-sm font-semibold truncate">{{ authStore.username }}</div>
            <div class="text-xs text-slate-400">Member</div>
          </div>
        </div>
        <button
          class="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-sm text-slate-400 hover:text-white hover:bg-white/5 transition-colors disabled:opacity-50 cursor-pointer"
          :disabled="loading"
          @click="handleLogout"
        >
          <LogOut class="w-4 h-4" />
          Sign out
        </button>
      </div>
    </aside>

    <!-- Main area -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">

      <!-- Mobile top bar -->
      <header class="md:hidden flex items-center gap-3 px-4 h-14 bg-white border-b border-slate-200 flex-shrink-0">
        <button
          class="p-1.5 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
          @click="drawerOpen = true"
        >
          <Menu class="w-5 h-5 text-slate-600" />
        </button>
        <span class="font-bold text-slate-900 text-sm">Telegram Quiz</span>
      </header>

      <!-- Scrollable page content -->
      <main class="flex-1 overflow-y-auto">
        <div class="max-w-6xl mx-auto w-full p-5 md:p-8">
          <slot />
        </div>
      </main>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Zap, Menu, LogOut, LayoutDashboard, BookOpen } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const drawerOpen = ref(false)
const loading = ref(false)

const navItems = [
  { to: '/home', label: 'Home', icon: LayoutDashboard },
  { to: '/questions', label: 'Question Bank', icon: BookOpen },
]

const userInitial = computed(() =>
  authStore.username ? authStore.username.charAt(0).toUpperCase() : '?'
)

async function handleLogout() {
  loading.value = true
  try {
    await authStore.logout()
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>
