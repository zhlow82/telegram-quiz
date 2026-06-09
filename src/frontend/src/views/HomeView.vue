<template>
  <AppLayout>
    <div>

      <!-- Page heading -->
      <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">
        <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
          <LayoutDashboard class="w-5 h-5 text-white" />
        </div>
        <div>
          <h1 class="text-2xl font-black text-slate-900 leading-tight">Dashboard</h1>
          <p class="text-sm text-slate-500 mt-0.5">Overview of your Telegram Quiz setup</p>
        </div>
      </div>

      <!-- Welcome banner -->
      <div
        class="rounded-xl p-6 mb-5"
        style="background: linear-gradient(145deg, #1e3a8a 0%, #2563eb 55%, #3b82f6 100%)"
      >
        <div class="flex items-center justify-between gap-4 flex-wrap">
          <div>
            <p class="text-sm mb-1" style="color: rgba(255,255,255,0.72)">Good to see you,</p>
            <h2 class="text-xl font-black text-white mb-1">{{ authStore.username }} 👋</h2>
            <p class="text-sm" style="color: rgba(255,255,255,0.72)">You're securely signed in via JWT.</p>
          </div>
          <div
            class="w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl font-extrabold flex-shrink-0 border-2"
            style="background: rgba(255,255,255,0.18); border-color: rgba(255,255,255,0.25); backdrop-filter: blur(8px)"
          >
            {{ userInitial }}
          </div>
        </div>
      </div>

      <!-- Stat cards -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5">
        <!-- Questions -->
        <router-link
          to="/questions"
          class="bg-white rounded-xl border border-slate-200 p-5 hover:border-blue-300 hover:shadow-sm transition-all cursor-pointer no-underline group"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Questions</p>
              <p v-if="statsLoading" class="h-6 w-12 bg-slate-100 rounded animate-pulse"></p>
              <p v-else class="text-2xl font-black text-slate-900">{{ questionCount }}</p>
            </div>
            <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-blue-50 group-hover:bg-blue-100 transition-colors">
              <BookOpen class="w-5 h-5 text-blue-600" />
            </div>
          </div>
        </router-link>

        <!-- Quizzes -->
        <router-link
          to="/quizzes"
          class="bg-white rounded-xl border border-slate-200 p-5 hover:border-green-300 hover:shadow-sm transition-all cursor-pointer no-underline group"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Active Quizzes</p>
              <p v-if="statsLoading" class="h-6 w-12 bg-slate-100 rounded animate-pulse"></p>
              <p v-else class="text-2xl font-black text-slate-900">{{ activeQuizCount }} <span class="text-sm font-medium text-slate-400">/ {{ quizCount }}</span></p>
            </div>
            <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-green-50 group-hover:bg-green-100 transition-colors">
              <Zap class="w-5 h-5 text-green-600" />
            </div>
          </div>
        </router-link>

        <!-- Folders -->
        <router-link
          to="/questions"
          class="bg-white rounded-xl border border-slate-200 p-5 hover:border-purple-300 hover:shadow-sm transition-all cursor-pointer no-underline group"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Folders</p>
              <p v-if="statsLoading" class="h-6 w-12 bg-slate-100 rounded animate-pulse"></p>
              <p v-else class="text-2xl font-black text-slate-900">{{ folderCount }}</p>
            </div>
            <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-purple-50 group-hover:bg-purple-100 transition-colors">
              <FolderIcon class="w-5 h-5 text-purple-600" />
            </div>
          </div>
        </router-link>
      </div>

      <!-- Server message -->
      <div class="bg-white rounded-xl border border-slate-200 p-5">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-blue-50">
            <Server class="w-5 h-5 text-blue-600" />
          </div>
          <span class="font-bold text-slate-900 text-sm">Server Message</span>
        </div>
        <p v-if="message" class="text-sm text-slate-500">{{ message }}</p>
        <div v-else class="space-y-2">
          <div class="h-3 bg-slate-100 rounded animate-pulse w-3/4"></div>
          <div class="h-3 bg-slate-100 rounded animate-pulse w-1/2"></div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { LayoutDashboard, BookOpen, Zap, Folder as FolderIcon, Server } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'
import { quizService } from '@/services/quizService'
import { questionsService } from '@/services/questionsService'
import { foldersService } from '@/services/foldersService'
import AppLayout from '@/components/AppLayout.vue'

const authStore = useAuthStore()
const message = ref('')
const statsLoading = ref(true)
const questionCount = ref(0)
const quizCount = ref(0)
const activeQuizCount = ref(0)
const folderCount = ref(0)

const userInitial = computed(() =>
  authStore.username ? authStore.username.charAt(0).toUpperCase() : '?'
)

onMounted(async () => {
  try {
    const [homeRes, questions, quizzes, folders] = await Promise.all([
      api.get('/api/home'),
      questionsService.list(),
      quizService.list(),
      foldersService.list(),
    ])
    message.value = homeRes.data.message
    questionCount.value = questions.length
    quizCount.value = quizzes.length
    activeQuizCount.value = quizzes.filter(q => q.status === 'ACTIVE').length
    folderCount.value = folders.length
  } catch {
    message.value = 'Failed to load data. Please try again.'
  } finally {
    statsLoading.value = false
  }
})
</script>
