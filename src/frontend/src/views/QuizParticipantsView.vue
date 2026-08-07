<template>
  <AppLayout>
    <!-- Header -->
    <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
      <div class="flex items-center gap-4">
        <button
          class="w-9 h-9 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 flex items-center justify-center text-slate-500 hover:text-slate-700 transition cursor-pointer flex-shrink-0"
          @click="router.push('/quizzes')"
        >
          <ArrowLeft class="w-4 h-4" />
        </button>
        <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
          <Users class="w-5 h-5 text-white" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 flex-wrap">
            <h1 class="text-2xl font-black text-slate-900 leading-tight truncate">{{ quiz?.name || 'Loading...' }}</h1>
            <span
              v-if="quiz"
              class="text-[0.65rem] font-bold px-1.5 py-0.5 rounded-md uppercase tracking-wide flex-shrink-0"
              :class="{
                'bg-slate-100 text-slate-500': quiz.status === 'DRAFT',
                'bg-green-100 text-green-700': quiz.status === 'ACTIVE',
                'bg-amber-100 text-amber-700': quiz.status === 'STOPPED',
              }"
            >{{ quiz.status }}</span>
          </div>
          <p v-if="quiz" class="text-sm text-slate-500 mt-0.5">
            <template v-if="quiz.botUsername">@{{ quiz.botUsername }} · </template>
            {{ quiz.questions.filter(q => !q.isBriefing && !q.expectsTextInput).length }} question{{ quiz.questions.filter(q => !q.isBriefing && !q.expectsTextInput).length !== 1 ? 's' : '' }} ·
            {{ quiz.passScorePercent }}% to pass
          </p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <button
          class="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-700 cursor-pointer transition px-3 py-2 rounded-lg hover:bg-slate-100 border border-slate-200"
          @click="refresh"
        >
          <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': sessionsLoading }" />
          Refresh
        </button>
        <label class="inline-flex items-center gap-2 cursor-pointer select-none" title="Auto-refresh every 5 seconds">
          <span class="text-xs text-slate-500">Live</span>
          <div class="relative w-8 h-4">
            <input type="checkbox" class="sr-only peer" :checked="isLive" @change="toggleLive" />
            <div class="absolute inset-0 bg-slate-200 rounded-full peer-checked:bg-green-500 transition-colors"></div>
            <div class="absolute top-0.5 left-0.5 w-3 h-3 bg-white rounded-full shadow transition-transform peer-checked:translate-x-4"></div>
          </div>
          <div v-if="isLive" class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
        </label>
      </div>
    </div>

    <!-- Stats cards -->
    <div v-if="!sessionsLoading && sessions.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-4 mb-5">
      <div class="bg-white rounded-xl border border-slate-200 p-5" title="Total number of times someone has started this quiz">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Attempts</p>
            <p class="text-2xl font-black text-slate-900">{{ sessions.length }}</p>
          </div>
          <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-blue-50">
            <Users class="w-5 h-5 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5" title="Sessions where the participant is currently answering questions">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">In Progress</p>
            <p class="text-2xl font-black text-blue-600">{{ inProgressCount }}</p>
          </div>
          <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-blue-50">
            <Clock class="w-5 h-5 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5" title="Sessions where the participant finished all questions">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Completed</p>
            <p class="text-2xl font-black text-green-600">{{ completedCount }}</p>
          </div>
          <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-green-50">
            <CheckCircle class="w-5 h-5 text-green-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5" title="Percentage of completed sessions that passed">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Pass Rate</p>
            <p class="text-2xl font-black text-purple-600">{{ passRate }}%</p>
          </div>
          <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-purple-50">
            <Trophy class="w-5 h-5 text-purple-600" />
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5" title="Sessions where the participant started but never finished">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Abandoned</p>
            <p class="text-2xl font-black text-slate-400">{{ abandonedCount }}</p>
          </div>
          <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-slate-100">
            <XCircle class="w-5 h-5 text-slate-400" />
          </div>
        </div>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="sessionsLoading" class="space-y-3">
      <div v-for="i in 5" :key="i" class="bg-white rounded-xl border border-slate-200 px-5 py-4 flex items-center gap-4">
        <div class="w-10 h-10 rounded-full bg-slate-100 animate-pulse flex-shrink-0"></div>
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-slate-100 rounded animate-pulse w-1/3"></div>
          <div class="h-3 bg-slate-100 rounded animate-pulse w-1/2"></div>
        </div>
        <div class="h-6 bg-slate-100 rounded animate-pulse w-20"></div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="sessions.length === 0" class="bg-white rounded-xl border border-slate-200">
      <AppEmptyState
        icon="users"
        variant="slate"
        title="No participants yet"
        description="Participants will appear here when they start the bot"
      />
    </div>

    <!-- Session list -->
    <div v-else>
      <!-- Search box -->
      <div v-if="sessions.length > 0" class="flex items-center gap-2 px-3 py-2 bg-white border border-slate-200 rounded-lg w-full sm:w-80 mb-4">
        <Search class="w-4 h-4 text-slate-400 flex-shrink-0" />
        <input
          v-model="sessionSearch"
          type="text"
          placeholder="Search participants…"
          class="flex-1 text-sm text-slate-700 placeholder-slate-400 bg-transparent outline-none"
        />
      </div>

      <div class="space-y-2">
      <div
        v-for="s in paginatedSessions"
        :key="s.id"
        class="bg-white rounded-xl border border-slate-200 overflow-hidden"
      >
        <!-- Session row (clickable to expand) -->
        <div
          class="flex items-center gap-4 px-5 py-4 cursor-pointer hover:bg-slate-50 transition-colors"
          :class="{ 'bg-slate-50 border-b border-slate-100': expandedSessionId === s.id }"
          @click="toggleSession(s)"
        >
          <!-- Avatar -->
          <div
            class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
            :class="{
              'bg-green-100': s.status === 'COMPLETED' && s.passed,
              'bg-red-100': s.status === 'COMPLETED' && !s.passed,
              'bg-blue-100': s.status === 'IN_PROGRESS',
              'bg-slate-100': s.status === 'ABANDONED',
            }"
          >
            <span
              class="text-sm font-bold"
              :class="{
                'text-green-700': s.status === 'COMPLETED' && s.passed,
                'text-red-700': s.status === 'COMPLETED' && !s.passed,
                'text-blue-700': s.status === 'IN_PROGRESS',
                'text-slate-500': s.status === 'ABANDONED',
              }"
            >{{ (s.telegramFirstName || '?')[0].toUpperCase() }}</span>
          </div>
          <!-- Name & meta -->
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-slate-900 truncate">
              {{ s.telegramFirstName }}
              <span v-if="s.telegramUsername" class="text-slate-400 font-normal text-xs ml-1">@{{ s.telegramUsername }}</span>
            </p>
            <p class="text-xs text-slate-400 mt-0.5">
              {{ formatDate(s.startedAt) }}
              <span v-if="s.teamName" class="ml-1">· Team: {{ s.teamName }}</span>
            </p>
          </div>
          <!-- Score / result -->
          <div class="flex items-center gap-3 flex-shrink-0">
            <span
              v-if="s.status === 'COMPLETED'"
              class="inline-flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-full"
              :class="s.passed ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'"
            >
              <CheckCircle v-if="s.passed" class="w-3.5 h-3.5" />
              <XCircle v-else class="w-3.5 h-3.5" />
              {{ s.passed ? 'PASS' : 'FAIL' }}
            </span>
            <span v-else-if="s.status === 'ABANDONED'" class="inline-flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full bg-slate-100 text-slate-500">
              <Minus class="w-3.5 h-3.5" />
              Abandoned
            </span>
            <span v-else class="inline-flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full bg-blue-50 text-blue-500">
              <div class="w-2 h-2 rounded-full bg-blue-400 animate-pulse"></div>
              In progress
            </span>
            <div class="flex flex-col items-end gap-1 w-20">
              <span class="text-sm font-bold text-slate-700">
                {{ s.status === 'COMPLETED' ? `${s.score} / ${s.totalQuestions}` : `Q${s.currentQuestionIndex + 1} / ${s.totalQuestions}` }}
              </span>
              <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  class="h-full transition-all"
                  :class="{
                    'bg-green-500': s.status === 'COMPLETED' && s.passed,
                    'bg-red-400': s.status === 'COMPLETED' && !s.passed,
                    'bg-blue-400': s.status === 'IN_PROGRESS',
                    'bg-slate-300': s.status === 'ABANDONED',
                  }"
                  :style="{ width: `${(s.score / s.totalQuestions) * 100}%` }"
                ></div>
              </div>
            </div>
            <ChevronDown
              class="w-4 h-4 text-slate-400 transition-transform flex-shrink-0"
              :class="{ 'rotate-180': expandedSessionId === s.id }"
            />
          </div>
        </div>

        <!-- Expanded answers -->
        <div v-if="expandedSessionId === s.id" class="px-5 py-4 bg-slate-50/50">
          <div v-if="answersLoading" class="flex items-center justify-center py-8 text-slate-400 text-sm gap-2">
            <div class="w-4 h-4 rounded-full border-2 border-blue-400 border-t-transparent animate-spin"></div>
            Loading answers…
          </div>
          <div v-else-if="answers.length === 0 && s.status !== 'IN_PROGRESS'" class="flex flex-col items-center gap-3 py-8 text-center">
            <FileText class="w-8 h-8 text-slate-300" />
            <p class="text-sm text-slate-500">No answers recorded yet</p>
          </div>
          <div v-else>
            <!-- Summary bar -->
            <div v-if="answers.length > 0" class="bg-white rounded-xl border border-slate-200 p-4 mb-3">
              <div class="flex items-center justify-between gap-4 mb-3 flex-wrap">
                <div class="flex items-center gap-6">
                  <div class="flex items-center gap-2">
                    <CheckCircle class="w-4 h-4 text-slate-400" />
                    <div>
                      <p class="text-xs text-slate-500 mb-0.5">Score</p>
                      <p class="text-lg font-bold text-slate-900">{{ answerStats.correct }}<span class="text-slate-400 font-normal">/{{ answerStats.total }}</span></p>
                    </div>
                  </div>
                  <div class="flex items-center gap-2">
                    <Trophy class="w-4 h-4 text-slate-400" />
                    <div>
                      <p class="text-xs text-slate-500 mb-0.5">Pass Rate</p>
                      <p class="text-lg font-bold" :class="answerStats.passRate >= 70 ? 'text-green-600' : answerStats.passRate >= 50 ? 'text-amber-600' : 'text-red-600'">{{ answerStats.passRate }}%</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-2">
                    <Clock class="w-4 h-4 text-slate-400" />
                    <div>
                      <p class="text-xs text-slate-500 mb-0.5">Avg Time</p>
                      <p class="text-lg font-bold text-slate-900">{{ formatResponseTime(answerStats.avgTime) }}</p>
                    </div>
                  </div>
                </div>
                <button
                  class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition cursor-pointer"
                  :class="showAllAnswers ? 'bg-slate-100 text-slate-700 hover:bg-slate-200' : 'bg-blue-50 text-blue-600 hover:bg-blue-100'"
                  @click.stop="showAllAnswers = !showAllAnswers"
                >
                  <Eye class="w-3.5 h-3.5" />
                  {{ showAllAnswers ? 'Hide answers' : 'View all answers' }}
                </button>
              </div>
              <!-- Progress bar -->
              <div class="h-2 bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full flex">
                  <div class="bg-green-500 transition-all" :style="{ width: `${answerStats.passRate}%` }"></div>
                  <div class="bg-red-400 transition-all" :style="{ width: `${100 - answerStats.passRate}%` }"></div>
                </div>
              </div>
            </div>

            <!-- Answer grid (2 columns) -->
            <div v-if="showAllAnswers && answers.length > 0" class="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <div
                v-for="(a, idx) in answers"
                :key="a.id"
                class="rounded-lg border px-3 py-2 bg-white cursor-pointer hover:shadow-sm transition-shadow"
                :class="{
                  'border-green-200 hover:border-green-300': a.isCorrect === true,
                  'border-red-200 hover:border-red-300': a.isCorrect === false,
                  'border-slate-200 hover:border-slate-300': a.isCorrect === null,
                }"
                @click.stop="a.photoFileId ? openPhotoModal(s, a) : null"
              >
                <div class="flex items-start justify-between gap-2 mb-1">
                  <span class="text-xs font-bold text-slate-700">Q{{ idx + 1 }}</span>
                  <div class="flex items-center gap-1.5">
                    <span v-if="a.responseTimeMs" class="text-[10px] text-slate-400">{{ formatResponseTime(a.responseTimeMs) }}</span>
                    <CheckCircle v-if="a.isCorrect === true" class="w-3.5 h-3.5 text-green-600" />
                    <XCircle v-else-if="a.isCorrect === false" class="w-3.5 h-3.5 text-red-500" />
                    <Clock v-else class="w-3.5 h-3.5 text-slate-400" />
                  </div>
                </div>
                <div class="text-xs text-slate-600 line-clamp-2 min-h-[2rem] mb-1">
                  {{ questionText(a.questionId) }}
                </div>
                <div class="text-xs text-slate-600 line-clamp-2 min-h-[2rem]">
                  <template v-if="a.photoFileId">
                    <span class="inline-flex items-center gap-1 text-blue-600">
                      <Camera class="w-3 h-3" />
                      Photo response
                    </span>
                  </template>
                  <template v-else-if="a.selectedAnswer">
                    {{ a.selectedAnswer }}
                  </template>
                  <template v-else>
                    <span class="text-slate-400 italic">No answer</span>
                  </template>
                </div>
              </div>
            </div>

            <!-- Waiting for first answer (in-progress) -->
            <div v-if="s.status === 'IN_PROGRESS' && answers.length === 0" class="flex flex-col items-center gap-2 py-6 text-center">
              <div class="w-3 h-3 rounded-full bg-blue-500 animate-pulse"></div>
              <p class="text-sm text-slate-500">Waiting for first response…</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-between gap-4 pt-4">
        <span class="text-sm text-slate-500">
          Showing <span class="font-bold text-slate-700">{{ paginatedStart }}</span>–<span class="font-bold text-slate-700">{{ paginatedEnd }}</span> of <span class="font-bold text-slate-700">{{ filteredSessions.length }}</span>
        </span>
        <div class="flex items-center gap-2">
          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center transition cursor-pointer disabled:opacity-30 disabled:cursor-default"
            :class="currentPage > 1 ? 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50' : 'bg-slate-50 text-slate-300'"
            :disabled="currentPage <= 1"
            @click="currentPage--"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          <span class="text-sm text-slate-500 px-2">
            Page <span class="font-bold text-slate-700">{{ currentPage }}</span> of <span class="font-bold text-slate-700">{{ totalPages }}</span>
          </span>
          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center transition cursor-pointer disabled:opacity-30 disabled:cursor-default"
            :class="currentPage < totalPages ? 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50' : 'bg-slate-50 text-slate-300'"
            :disabled="currentPage >= totalPages"
            @click="currentPage++"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
      </div>
    </div>

    <!-- Photo viewer modal -->
    <teleport to="body">
      <transition
        enter-active-class="transition-opacity duration-150"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition-opacity duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
      <div v-if="photoModalVisible" class="fixed inset-0 z-[70] flex items-center justify-center p-4" @keydown.escape="closePhotoModal">
        <div class="absolute inset-0 bg-black/70" @click="closePhotoModal" />
        <div class="relative max-w-3xl max-h-[85vh] flex flex-col items-center">
          <button class="absolute -top-10 right-0 text-white/80 hover:text-white cursor-pointer" @click="closePhotoModal">
            <X class="w-6 h-6" />
          </button>
          <img
            v-if="photoUrl"
            :src="photoUrl"
            class="max-w-full max-h-[80vh] rounded-xl shadow-2xl object-contain"
            alt="Participant photo"
          />
          <div v-else-if="photoError" class="bg-white rounded-xl px-6 py-8 flex flex-col items-center gap-3">
            <AlertCircle class="w-8 h-8 text-red-500" />
            <span class="text-sm text-slate-700 font-semibold">Failed to load photo</span>
          </div>
          <div v-else class="bg-white rounded-xl px-6 py-8 flex flex-col items-center gap-3">
            <div class="w-5 h-5 rounded-full border-2 border-blue-400 border-t-transparent animate-spin"></div>
            <span class="text-sm text-slate-500">Loading photo...</span>
          </div>
        </div>
      </div>
      </transition>
    </teleport>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Users, CheckCircle, XCircle, Minus, Clock, RefreshCw, FileText, Camera, X, AlertCircle, ChevronDown, ChevronLeft, ChevronRight, Search, Trophy, Eye } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import AppEmptyState from '@/components/AppEmptyState.vue'
import { quizService } from '@/services/quizService'
import type { Quiz, QuizSessionSummary, QuizSessionAnswer } from '@/types/quiz'

const router = useRouter()
const route = useRoute()

const quizId = computed(() => Number(route.params.id))

const SESSIONS_PER_PAGE = 20
const currentPage = ref(1)
const sessionSearch = ref('')

const filteredSessions = computed(() => {
  const q = sessionSearch.value.trim().toLowerCase()
  if (!q) return sessions.value
  return sessions.value.filter(s =>
    s.telegramFirstName.toLowerCase().includes(q) ||
    (s.telegramUsername && s.telegramUsername.toLowerCase().includes(q)) ||
    (s.teamName && s.teamName.toLowerCase().includes(q))
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredSessions.value.length / SESSIONS_PER_PAGE)))

const paginatedSessions = computed(() => {
  const start = (currentPage.value - 1) * SESSIONS_PER_PAGE
  return filteredSessions.value.slice(start, start + SESSIONS_PER_PAGE)
})

const paginatedStart = computed(() => {
  if (filteredSessions.value.length === 0) return 0
  return (currentPage.value - 1) * SESSIONS_PER_PAGE + 1
})

const paginatedEnd = computed(() => {
  const end = currentPage.value * SESSIONS_PER_PAGE
  return Math.min(end, filteredSessions.value.length)
})

const quiz = ref<Quiz | null>(null)
const sessions = ref<QuizSessionSummary[]>([])
const sessionsLoading = ref(true)
const loadError = ref(false)

const completedCount = computed(() => sessions.value.filter(s => s.status === 'COMPLETED').length)
const abandonedCount = computed(() => sessions.value.filter(s => s.status === 'ABANDONED').length)
const inProgressCount = computed(() => sessions.value.filter(s => s.status === 'IN_PROGRESS').length)
const passRate = computed(() => {
  const done = completedCount.value
  if (done === 0) return 0
  const passed = sessions.value.filter(s => s.status === 'COMPLETED' && s.passed).length
  return Math.round((passed / done) * 100)
})

const expandedSessionId = ref<number | null>(null)
const showAllAnswers = ref(false)
const answers = ref<QuizSessionAnswer[]>([])
const answersLoading = ref(false)

const answerStats = computed(() => {
  if (answers.value.length === 0) return { correct: 0, total: 0, avgTime: 0, passRate: 0 }
  const correct = answers.value.filter(a => a.isCorrect === true).length
  const total = answers.value.length
  const avgTime = Math.round(
    answers.value.reduce((sum, a) => sum + (a.responseTimeMs || 0), 0) / total
  )
  const passRate = Math.round((correct / total) * 100)
  return { correct, total, avgTime, passRate }
})

const photoModalVisible = ref(false)
const photoUrl = ref<string | null>(null)
const photoError = ref(false)
const photoSessionId = ref<number | null>(null)
const isLive = ref(true)

let refreshInterval: ReturnType<typeof setInterval> | null = null

function startAutoRefresh() {
  if (refreshInterval) clearInterval(refreshInterval)
  refreshInterval = setInterval(async () => {
    try {
      const updated = await quizService.getSessions(quizId.value)
      sessions.value = updated

      if (expandedSessionId.value !== null) {
        const expandedAnswers = await quizService.getSessionAnswers(expandedSessionId.value)
        answers.value = expandedAnswers
      }
    } catch {
      // ignore refresh errors
    }
  }, 5000)
}

function stopAutoRefresh() {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

function toggleLive() {
  isLive.value = !isLive.value
  if (isLive.value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

onMounted(async () => {
  try {
    quiz.value = await quizService.get(quizId.value)
    sessions.value = await quizService.getSessions(quizId.value)
    currentPage.value = 1
  } catch {
    loadError.value = true
  } finally {
    sessionsLoading.value = false
  }

  if (isLive.value) startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

async function refresh() {
  sessionsLoading.value = true
  try {
    sessions.value = await quizService.getSessions(quizId.value)
    if (expandedSessionId.value !== null) {
      answers.value = await quizService.getSessionAnswers(expandedSessionId.value)
    }
  } catch {
    // ignore
  } finally {
    sessionsLoading.value = false
  }
}

async function toggleSession(s: QuizSessionSummary) {
  if (expandedSessionId.value === s.id) {
    expandedSessionId.value = null
    showAllAnswers.value = false
    answers.value = []
    return
  }
  expandedSessionId.value = s.id
  showAllAnswers.value = false
  answersLoading.value = true
  answers.value = []
  try {
    answers.value = await quizService.getSessionAnswers(s.id)
  } catch {
    // show empty state on error
  } finally {
    answersLoading.value = false
  }
}

async function openPhotoModal(session: QuizSessionSummary, answer: QuizSessionAnswer) {
  if (!answer.photoFileId) return
  photoModalVisible.value = true
  photoUrl.value = null
  photoError.value = false
  photoSessionId.value = session.id
  try {
    const encodedFileId = encodeURIComponent(answer.photoFileId)
    const res = await fetch(`/api/quizzes/sessions/${session.id}/photos?fileId=${encodedFileId}&t=${Date.now()}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` },
    })
    if (!res.ok) throw new Error(`Failed to load photo: ${res.status}`)
    const blob = await res.blob()
    if (blob.size < 100) throw new Error('Photo response too small')
    photoUrl.value = URL.createObjectURL(blob)
  } catch {
    photoError.value = true
  }
}

function closePhotoModal() {
  photoModalVisible.value = false
  if (photoUrl.value) {
    URL.revokeObjectURL(photoUrl.value)
    photoUrl.value = null
  }
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function formatResponseTime(ms: number): string {
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(1)}s`
}

function questionText(questionId: number): string {
  if (!quiz.value) return ''
  const q = quiz.value.questions.find(q => q.id === questionId)
  if (!q) return ''
  const textBlock = q.questionBlocks.find(b => b.type === 'text')
  if (!textBlock) return ''
  return textBlock.content.length > 80 ? textBlock.content.substring(0, 80) + '...' : textBlock.content
}

watch(sessionSearch, () => {
  currentPage.value = 1
})
</script>
