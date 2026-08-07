<template>
  <!-- Header -->
  <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
    <div class="flex items-center gap-4">
      <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
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
        class="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-white text-sm font-semibold px-4 py-2 rounded-full transition no-underline"
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
    class="bg-white rounded-xl border border-slate-200"
  >
    <AppEmptyState
      icon="zap"
      variant="blue"
      title="No quizzes yet"
      description="Create your first quiz to get started"
      action-label="Create Quiz"
      @action="router.push('/quizzes/new')"
    />
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
          @click="startTarget = quiz"
        >
          <Loader2 v-if="togglingId === quiz.id" class="w-3.5 h-3.5 animate-spin" />
          <Play v-else class="w-3.5 h-3.5" />
          {{ togglingId === quiz.id ? 'Starting...' : 'Start' }}
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
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
    <div v-if="deleteTarget" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="deleteTarget = null">
      <div class="absolute inset-0 bg-black/50" />
      <div class="relative bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm">
        <h3 class="text-base font-bold text-slate-900 mb-2">Delete quiz?</h3>
        <p class="text-sm text-slate-500 mb-5">
          "<span class="font-medium text-slate-700">{{ deleteTarget.name }}</span>" will be permanently deleted.
        </p>
        <div class="flex gap-2 justify-end">
          <button
            class="px-4 py-2 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            @click="deleteTarget = null"
          >Cancel</button>
          <button
            class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold transition cursor-pointer disabled:opacity-60"
            :disabled="deleting"
            @click="doDelete"
          >Delete</button>
        </div>
      </div>
    </div>
    </transition>
  </teleport>

  <!-- Start confirmation dialog -->
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
    <div v-if="startTarget" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="startTarget = null">
      <div class="absolute inset-0 bg-black/50" />
      <div class="relative bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-green-100 flex items-center justify-center flex-shrink-0">
            <Play class="w-5 h-5 text-green-600" />
          </div>
          <h3 class="text-base font-bold text-slate-900">Start quiz?</h3>
        </div>
        <p class="text-sm text-slate-500 mb-5">
          "<span class="font-medium text-slate-700">{{ startTarget.name }}</span>" will go live. Participants can join via the bot and start answering questions.
        </p>
        <div class="flex gap-2 justify-end">
          <button
            class="px-4 py-2 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            @click="startTarget = null"
          >Cancel</button>
          <button
            class="px-4 py-2 rounded-lg bg-green-600 hover:bg-green-700 text-white text-sm font-semibold transition cursor-pointer disabled:opacity-60 inline-flex items-center gap-2"
            :disabled="togglingId === startTarget?.id"
            @click="confirmStart"
          >
            <Loader2 v-if="togglingId === startTarget?.id" class="w-4 h-4 animate-spin" />
            {{ togglingId === startTarget?.id ? 'Starting...' : 'Start Quiz' }}
          </button>
        </div>
      </div>
    </div>
    </transition>
  </teleport>

  <!-- Start error dialog -->
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
    <div v-if="startError" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="startError = null">
      <div class="absolute inset-0 bg-black/50" />
      <div class="relative bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-red-100 flex items-center justify-center flex-shrink-0">
            <AlertCircle class="w-5 h-5 text-red-600" />
          </div>
          <h3 class="text-base font-bold text-slate-900">Failed to start quiz</h3>
        </div>
        <p class="text-sm text-slate-500 mb-5">
          {{ startError }}
        </p>
        <div class="flex gap-2 justify-end">
          <button
            class="px-4 py-2 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            @click="startError = null"
          >Close</button>
        </div>
      </div>
    </div>
    </transition>
  </teleport>

  <!-- QR Code modal -->
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
    <div v-if="qrQuiz" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="qrQuiz = null">
      <div class="absolute inset-0 bg-black/50" />
      <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm overflow-hidden">
        <!-- Header -->
        <div class="bg-gradient-to-br from-green-50 to-blue-50 px-6 pt-6 pb-5">
          <div class="flex items-start justify-between mb-3">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-green-500 flex items-center justify-center flex-shrink-0">
                <Zap class="w-5 h-5 text-white" />
              </div>
              <div>
                <h3 class="text-base font-bold text-slate-900 leading-tight">{{ qrQuiz.name }}</h3>
                <div class="flex items-center gap-1.5 mt-0.5">
                  <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
                  <span class="text-xs font-semibold text-green-700 uppercase tracking-wide">Live</span>
                </div>
              </div>
            </div>
            <button class="text-slate-400 hover:text-slate-600 cursor-pointer p-1 -mr-1 -mt-1" @click="qrQuiz = null">
              <X class="w-5 h-5" />
            </button>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-600">
            <Send class="w-4 h-4 text-blue-500 flex-shrink-0" />
            <span class="font-medium text-slate-700">@{{ qrQuiz.botUsername }}</span>
            <span class="text-slate-400">·</span>
            <span class="text-xs text-slate-500">Ready for participants</span>
          </div>
        </div>

        <!-- QR Code -->
        <div class="px-6 py-5 flex flex-col items-center">
          <div class="bg-white p-3 rounded-xl border border-slate-200 shadow-sm mb-4">
            <canvas ref="qrCanvas" class="rounded-lg" />
          </div>
          <p class="text-xs text-slate-400 mb-4">Scan with Telegram to join</p>

          <!-- Actions -->
          <div class="w-full space-y-2">
            <a
              :href="`https://t.me/${qrQuiz.botUsername}`"
              target="_blank"
              rel="noopener"
              class="block w-full text-center bg-primary hover:bg-primary-hover text-white text-sm font-semibold px-4 py-2.5 rounded-full transition no-underline"
            >
              Open in Telegram
            </a>
            <button
              class="block w-full text-center bg-green-50 hover:bg-green-100 text-green-700 text-sm font-semibold px-4 py-2.5 rounded-full transition cursor-pointer"
              @click="goToParticipants"
            >
              View Participants
            </button>
            <button
              class="block w-full text-center border border-slate-200 text-slate-600 text-sm font-medium px-4 py-2.5 rounded-lg transition hover:bg-slate-50 cursor-pointer"
              @click="copyLink"
            >
              {{ copied ? 'Copied!' : 'Copy link' }}
            </button>
          </div>
        </div>

        <!-- Footer -->
        <div class="bg-slate-50 px-6 py-3 border-t border-slate-100">
          <p class="text-xs text-slate-400 text-center">Quiz remains active until you stop it</p>
        </div>
      </div>
    </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { Plus, AlertCircle, Zap, BookOpen, Clock, CheckCircle, Trash2, Play, Square, QrCode, X, Users, Search, Pencil, Loader2, Send } from '@lucide/vue'
import AppEmptyState from '@/components/AppEmptyState.vue'
import { quizService } from '@/services/quizService'
import { useToast } from '@/composables/useToast'
import type { QuizSummary } from '@/types/quiz'

const toast = useToast()
const router = useRouter()
const quizzes = ref<QuizSummary[]>([])
const loading = ref(true)
const loadError = ref(false)
const deleteTarget = ref<QuizSummary | null>(null)
const deleting = ref(false)
const togglingId = ref<number | null>(null)
const search = ref('')
const startTarget = ref<QuizSummary | null>(null)
const startError = ref<string | null>(null)

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

async function confirmStart() {
  if (!startTarget.value) return
  const quiz = startTarget.value
  togglingId.value = quiz.id
  startError.value = null
  try {
    const updated = await quizService.activate(quiz.id)
    const idx = quizzes.value.findIndex(q => q.id === quiz.id)
    if (idx !== -1) quizzes.value[idx] = { ...quizzes.value[idx], status: updated.status }
    startTarget.value = null
    toast.success(`${quiz.name} started`)
    if (updated.botUsername) {
      await openQr({ ...quizzes.value[idx] })
    }
  } catch (e: any) {
    startTarget.value = null
    const msg = e?.response?.data?.message || e?.message || 'Unknown error'
    startError.value = msg
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

function openDetails(quiz: QuizSummary) {
  router.push(`/quizzes/${quiz.id}/participants`)
}

function goToParticipants() {
  if (!qrQuiz.value) return
  const id = qrQuiz.value.id
  qrQuiz.value = null
  router.push(`/quizzes/${id}/participants`)
}
</script>

