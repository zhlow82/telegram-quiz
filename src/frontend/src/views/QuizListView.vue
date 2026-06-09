<template>
  <AppLayout>
    <!-- Header -->
    <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
      <div class="flex items-center gap-4">
        <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
          <Zap class="w-5 h-5 text-white" />
        </div>
        <div>
          <h1 class="text-2xl font-black text-slate-900 leading-tight">Quizzes</h1>
          <p class="text-sm text-slate-500 mt-0.5">
            {{ quizzes.length }} quiz{{ quizzes.length !== 1 ? 'zes' : '' }}
          </p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2 px-3 py-2 bg-white border border-slate-200 rounded-lg w-56">
          <Search class="w-4 h-4 text-slate-400 flex-shrink-0" />
          <input
            v-model="search"
            type="text"
            placeholder="Search quizzes…"
            class="flex-1 text-sm text-slate-700 placeholder-slate-400 bg-transparent outline-none"
          />
        </div>
        <router-link
          to="/quizzes/new"
          class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition no-underline"
        >
          <Plus class="w-4 h-4" />
          Create Quiz
        </router-link>
      </div>
    </div>

    <!-- Error -->
    <div v-if="loadError" class="flex items-center gap-3 mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm">
      <AlertCircle class="w-4 h-4 flex-shrink-0" />
      Failed to load quizzes. Please refresh.
    </div>

    <!-- Loading skeleton -->
    <div v-else-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl border border-slate-200 px-5 py-4 flex items-center gap-4">
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-slate-100 rounded animate-pulse w-1/3"></div>
          <div class="h-3 bg-slate-100 rounded animate-pulse w-1/2"></div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-else-if="quizzes.length === 0"
      class="bg-white rounded-xl border border-slate-200 flex flex-col items-center gap-4 py-14 px-6 text-center"
    >
      <div class="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center">
        <Zap class="w-7 h-7 text-blue-600" />
      </div>
      <div>
        <p class="font-semibold text-slate-900 mb-1">No quizzes yet</p>
        <p class="text-sm text-slate-500">Create your first quiz to get started</p>
      </div>
      <router-link
        to="/quizzes/new"
        class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition no-underline"
      >
        <Plus class="w-4 h-4" />
        Create Quiz
      </router-link>
    </div>

    <!-- Quiz list -->
    <div v-else class="space-y-3">
      <div v-if="filteredQuizzes.length === 0" class="bg-white rounded-xl border border-slate-200 p-8 text-center text-slate-400">
        No quizzes match your search.
      </div>
      <div
        v-for="quiz in filteredQuizzes"
        :key="quiz.id"
        class="bg-white rounded-xl border border-slate-200 px-5 py-4 flex items-center gap-4 hover:border-slate-300 transition-colors"
      >
        <!-- Status dot -->
        <div
          class="w-2.5 h-2.5 rounded-full flex-shrink-0"
          :class="{
            'bg-slate-300': quiz.status === 'DRAFT',
            'bg-green-500': quiz.status === 'ACTIVE',
            'bg-amber-400': quiz.status === 'STOPPED',
          }"
        ></div>

        <!-- Info (clickable → participant details) -->
        <div
          class="flex-1 min-w-0 cursor-pointer rounded-lg px-2 py-1 -mx-2 -my-1 hover:bg-slate-50 transition-colors"
          @click="openDetails(quiz)"
        >
          <div class="flex items-center gap-2 flex-wrap">
            <span class="text-sm font-semibold text-slate-900 truncate">{{ quiz.name }}</span>
            <span
              class="text-[0.65rem] font-bold px-1.5 py-0.5 rounded-md uppercase tracking-wide"
              :class="{
                'bg-slate-100 text-slate-500': quiz.status === 'DRAFT',
                'bg-green-100 text-green-700': quiz.status === 'ACTIVE',
                'bg-amber-100 text-amber-700': quiz.status === 'STOPPED',
              }"
            >{{ quiz.status }}</span>
          </div>
          <div class="flex gap-3 mt-1.5 flex-wrap">
            <span class="inline-flex items-center gap-1 text-xs text-slate-500">
              <BookOpen class="w-3 h-3" />{{ quiz.questionCount }} question{{ quiz.questionCount !== 1 ? 's' : '' }}
            </span>
            <span class="inline-flex items-center gap-1 text-xs text-slate-500">
              <Clock class="w-3 h-3" />{{ quiz.timePerQuestionSeconds }}s per question
            </span>
            <span class="inline-flex items-center gap-1 text-xs text-slate-500">
              <CheckCircle class="w-3 h-3" />{{ quiz.passScorePercent }}% to pass
            </span>
            <span class="inline-flex items-center gap-1 text-xs text-slate-400">
              <Users class="w-3 h-3" />View participants
            </span>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-1.5 flex-shrink-0">
          <!-- Start button (DRAFT or STOPPED) -->
          <button
            v-if="quiz.status !== 'ACTIVE'"
            class="h-8 px-3 rounded-lg bg-green-50 hover:bg-green-100 flex items-center gap-1.5 text-green-700 text-xs font-semibold transition cursor-pointer disabled:opacity-50"
            title="Start quiz"
            :disabled="togglingId === quiz.id"
            @click="startQuiz(quiz)"
          >
            <Play class="w-3.5 h-3.5" />
            Start
          </button>

          <!-- Stop button + QR code button (ACTIVE) -->
          <template v-if="quiz.status === 'ACTIVE'">
            <button
              v-if="quiz.botUsername"
              class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer"
              title="Show QR code"
              @click="openQr(quiz)"
            >
              <QrCode class="w-4 h-4" />
            </button>
            <button
              class="h-8 px-3 rounded-lg bg-red-50 hover:bg-red-100 flex items-center gap-1.5 text-red-600 text-xs font-semibold transition cursor-pointer disabled:opacity-50"
              title="Stop quiz"
              :disabled="togglingId === quiz.id"
              @click="stopQuiz(quiz)"
            >
              <Square class="w-3.5 h-3.5 fill-red-500" />
              Stop
            </button>
          </template>

          <button
            class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer"
            title="Edit"
            @click="router.push(`/quizzes/${quiz.id}/edit`)"
          >
            <Pencil class="w-3.5 h-3.5" />
          </button>
          <button
            class="w-8 h-8 rounded-lg bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-500 transition cursor-pointer"
            title="Delete"
            @click="confirmDelete(quiz)"
          >
            <Trash2 class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>

    <!-- Delete confirmation dialog -->
    <teleport to="body">
      <div v-if="deleteTarget" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="deleteTarget = null">
        <div class="absolute inset-0 bg-black/50" @click="deleteTarget = null" />
        <div class="relative bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm">
          <h3 class="text-base font-bold text-slate-900 mb-2">Delete quiz?</h3>
          <p class="text-sm text-slate-500 mb-5">
            "<span class="font-medium text-slate-700">{{ deleteTarget.name }}</span>" will be permanently deleted.
          </p>
          <div class="flex gap-2 justify-end">
            <button
              class="px-4 py-2 rounded-xl border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
              @click="deleteTarget = null"
            >Cancel</button>
            <button
              class="px-4 py-2 rounded-xl bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition cursor-pointer disabled:opacity-60"
              :disabled="deleting"
              @click="doDelete"
            >Delete</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- QR Code modal -->
    <teleport to="body">
      <div v-if="qrQuiz" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="qrQuiz = null">
        <div class="absolute inset-0 bg-black/50" @click="qrQuiz = null" />
        <div class="relative bg-white rounded-2xl shadow-2xl p-6 w-full max-w-xs text-center">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-bold text-slate-900">Scan to join</h3>
            <button class="text-slate-400 hover:text-slate-600 cursor-pointer" @click="qrQuiz = null">
              <X class="w-5 h-5" />
            </button>
          </div>
          <p class="text-sm text-slate-500 mb-4">
            Share this with participants to start <span class="font-semibold text-slate-700">{{ qrQuiz.name }}</span>
          </p>
          <div class="flex justify-center mb-4">
            <canvas ref="qrCanvas" class="rounded-lg" />
          </div>
          <a
            :href="`https://t.me/${qrQuiz.botUsername}`"
            target="_blank"
            rel="noopener"
            class="block w-full text-center bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2.5 rounded-xl transition no-underline mb-2"
          >
            Open in Telegram
          </a>
          <button
            class="block w-full text-center border border-slate-200 text-slate-600 text-sm font-medium px-4 py-2.5 rounded-xl transition hover:bg-slate-50 cursor-pointer"
            @click="copyLink"
          >
            {{ copied ? 'Copied!' : 'Copy link' }}
          </button>
        </div>
      </div>
    </teleport>
    <!-- Participant details modal -->
    <teleport to="body">
      <div v-if="detailsQuiz" class="fixed inset-0 z-50 flex items-start justify-center p-4 pt-12" @keydown.escape="detailsQuiz = null">
        <div class="absolute inset-0 bg-black/50" @click="detailsQuiz = null" />
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-xl flex flex-col max-h-[80vh]">
          <!-- Modal header -->
          <div class="flex items-start justify-between gap-4 px-6 pt-5 pb-4 border-b border-slate-100">
            <div class="min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <h3 class="text-base font-bold text-slate-900 truncate">{{ detailsQuiz.name }}</h3>
                <span
                  class="text-[0.65rem] font-bold px-1.5 py-0.5 rounded-md uppercase tracking-wide flex-shrink-0"
                  :class="{
                    'bg-slate-100 text-slate-500': detailsQuiz.status === 'DRAFT',
                    'bg-green-100 text-green-700': detailsQuiz.status === 'ACTIVE',
                    'bg-amber-100 text-amber-700': detailsQuiz.status === 'STOPPED',
                  }"
                >{{ detailsQuiz.status }}</span>
              </div>
              <p class="text-xs text-slate-500 mt-0.5">
                <template v-if="detailsQuiz.botUsername">@{{ detailsQuiz.botUsername }} · </template>
                {{ detailsQuiz.questionCount }} question{{ detailsQuiz.questionCount !== 1 ? 's' : '' }} ·
                {{ detailsQuiz.passScorePercent }}% to pass
              </p>
            </div>
            <button class="text-slate-400 hover:text-slate-600 cursor-pointer flex-shrink-0 mt-0.5" @click="detailsQuiz = null">
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Stats bar -->
          <div v-if="!sessionsLoading && sessions.length > 0" class="flex items-center gap-6 px-6 py-3 bg-slate-50 border-b border-slate-100 text-xs text-slate-500">
            <span><span class="font-bold text-slate-800 text-sm">{{ sessions.length }}</span> attempt{{ sessions.length !== 1 ? 's' : '' }}</span>
            <span><span class="font-bold text-slate-800 text-sm">{{ completedCount }}</span> completed</span>
            <span><span class="font-bold text-slate-800 text-sm">{{ passRate }}%</span> pass rate</span>
          </div>

          <!-- Body: loading / empty / list -->
          <div class="overflow-y-auto flex-1 px-2 py-2">
            <!-- Loading -->
            <div v-if="sessionsLoading" class="flex items-center justify-center py-12 text-slate-400 text-sm gap-2">
              <div class="w-4 h-4 rounded-full border-2 border-blue-400 border-t-transparent animate-spin"></div>
              Loading participants…
            </div>

            <!-- Empty -->
            <div v-else-if="sessions.length === 0" class="flex flex-col items-center gap-3 py-12 text-center">
              <div class="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center">
                <Users class="w-6 h-6 text-slate-400" />
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-700">No participants yet</p>
                <p class="text-xs text-slate-400 mt-0.5">Participants will appear here when they start the bot</p>
              </div>
            </div>

            <!-- Participant rows -->
            <div v-else class="divide-y divide-slate-100">
              <div
                v-for="s in sessions"
                :key="s.id"
                class="flex items-center gap-3 px-3 py-3"
              >
                <!-- Avatar -->
                <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                  <span class="text-xs font-bold text-blue-700">{{ (s.telegramFirstName || '?')[0].toUpperCase() }}</span>
                </div>
                <!-- Name -->
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-slate-900 truncate">
                    {{ s.telegramFirstName }}
                    <span v-if="s.telegramUsername" class="text-slate-400 font-normal text-xs ml-1">@{{ s.telegramUsername }}</span>
                  </p>
                  <p class="text-xs text-slate-400 mt-0.5">{{ formatDate(s.startedAt) }}</p>
                </div>
                <!-- Score / result -->
                <div class="flex flex-col items-end gap-1 flex-shrink-0">
                  <span
                    v-if="s.status === 'COMPLETED'"
                    class="inline-flex items-center gap-1 text-xs font-bold px-2 py-0.5 rounded-full"
                    :class="s.passed ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'"
                  >
                    <CheckCircle v-if="s.passed" class="w-3 h-3" />
                    <XCircle v-else class="w-3 h-3" />
                    {{ s.passed ? 'PASS' : 'FAIL' }}
                  </span>
                  <span v-else class="inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-500">
                    <div class="w-2 h-2 rounded-full bg-blue-400 animate-pulse"></div>
                    In progress
                  </span>
                  <span class="text-xs text-slate-500">
                    {{ s.status === 'COMPLETED' ? `${s.score} / ${s.totalQuestions}` : '…' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer: refresh -->
          <div class="px-6 py-3 border-t border-slate-100 flex justify-end">
            <button
              class="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-700 cursor-pointer transition"
              @click="refreshDetails"
            >
              <RefreshCw class="w-3.5 h-3.5" />
              Refresh
            </button>
          </div>
        </div>
      </div>
    </teleport>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { Plus, AlertCircle, Zap, BookOpen, Clock, CheckCircle, Trash2, Play, Square, QrCode, X, Users, XCircle, RefreshCw, Search, Pencil } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { quizService } from '@/services/quizService'
import { useToast } from '@/composables/useToast'
import type { QuizSummary, QuizSessionSummary } from '@/types/quiz'

const toast = useToast()
const router = useRouter()
const quizzes = ref<QuizSummary[]>([])
const loading = ref(true)
const loadError = ref(false)
const deleteTarget = ref<QuizSummary | null>(null)
const deleting = ref(false)
const togglingId = ref<number | null>(null)
const search = ref('')

const filteredQuizzes = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return quizzes.value
  return quizzes.value.filter(quiz =>
    quiz.name.toLowerCase().includes(q) ||
    (quiz.botUsername && quiz.botUsername.toLowerCase().includes(q))
  )
})

// QR code
const qrQuiz = ref<QuizSummary | null>(null)
const qrCanvas = ref<HTMLCanvasElement | null>(null)
const copied = ref(false)

// Participant details
const detailsQuiz = ref<QuizSummary | null>(null)
const sessions = ref<QuizSessionSummary[]>([])
const sessionsLoading = ref(false)

const completedCount = computed(() => sessions.value.filter(s => s.status === 'COMPLETED').length)
const passRate = computed(() => {
  const done = completedCount.value
  if (done === 0) return 0
  const passed = sessions.value.filter(s => s.status === 'COMPLETED' && s.passed).length
  return Math.round((passed / done) * 100)
})

onMounted(loadQuizzes)

async function loadQuizzes() {
  loading.value = true
  loadError.value = false
  try {
    quizzes.value = await quizService.list()
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function confirmDelete(quiz: QuizSummary) {
  deleteTarget.value = quiz
}

async function doDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await quizService.delete(deleteTarget.value.id)
    quizzes.value = quizzes.value.filter(q => q.id !== deleteTarget.value!.id)
    deleteTarget.value = null
    toast.success('Quiz deleted')
  } finally {
    deleting.value = false
  }
}

async function startQuiz(quiz: QuizSummary) {
  togglingId.value = quiz.id
  try {
    const updated = await quizService.activate(quiz.id)
    const idx = quizzes.value.findIndex(q => q.id === quiz.id)
    if (idx !== -1) quizzes.value[idx] = { ...quizzes.value[idx], status: updated.status }
    toast.success(`${quiz.name} started`)
    if (updated.botUsername) {
      await openQr({ ...quizzes.value[idx] })
    }
  } finally {
    togglingId.value = null
  }
}

async function stopQuiz(quiz: QuizSummary) {
  togglingId.value = quiz.id
  try {
    const updated = await quizService.stop(quiz.id)
    const idx = quizzes.value.findIndex(q => q.id === quiz.id)
    if (idx !== -1) quizzes.value[idx] = { ...quizzes.value[idx], status: updated.status }
    toast.success(`${quiz.name} stopped`)
  } finally {
    togglingId.value = null
  }
}

async function openQr(quiz: QuizSummary) {
  if (!quiz.botUsername) return
  qrQuiz.value = quiz
  copied.value = false
  await nextTick()
  if (qrCanvas.value) {
    await QRCode.toCanvas(qrCanvas.value, `https://t.me/${quiz.botUsername}`, {
      width: 220,
      margin: 2,
      color: { dark: '#0f172a', light: '#ffffff' },
    })
  }
}

async function copyLink() {
  if (!qrQuiz.value?.botUsername) return
  await navigator.clipboard.writeText(`https://t.me/${qrQuiz.value.botUsername}`)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

async function openDetails(quiz: QuizSummary) {
  detailsQuiz.value = quiz
  sessionsLoading.value = true
  sessions.value = []
  try {
    sessions.value = await quizService.getSessions(quiz.id)
  } catch {
    // show empty state on error
  } finally {
    sessionsLoading.value = false
  }
}

async function refreshDetails() {
  if (!detailsQuiz.value) return
  sessionsLoading.value = true
  try {
    sessions.value = await quizService.getSessions(detailsQuiz.value.id)
  } catch {
    // ignore
  } finally {
    sessionsLoading.value = false
  }
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>

