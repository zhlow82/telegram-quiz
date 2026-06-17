<template>
  <AppLayout>
    <div>

      <!-- Page heading -->
      <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
            <LayoutDashboard class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-2xl font-black text-slate-900 leading-tight">Dashboard</h1>
            <p class="text-sm text-slate-500 mt-0.5">Overview of your Telegram Quiz setup</p>
          </div>
        </div>
        <router-link
          to="/quizzes/new"
          class="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-white text-sm font-semibold px-4 py-2 rounded-full transition no-underline"
        >
          <Plus class="w-4 h-4" />
          Create Quiz
        </router-link>
      </div>

      <!-- Loading state -->
      <div v-if="statsLoading" class="space-y-5">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div v-for="i in 4" :key="i" class="bg-white rounded-xl border border-slate-200 p-5">
            <div class="h-3 bg-slate-100 rounded animate-pulse w-1/2 mb-2"></div>
            <div class="h-7 bg-slate-100 rounded animate-pulse w-1/3"></div>
          </div>
        </div>
        <div class="bg-white rounded-xl border border-slate-200 p-5">
          <div class="h-4 bg-slate-100 rounded animate-pulse w-1/4 mb-4"></div>
          <div v-for="i in 3" :key="i" class="h-12 bg-slate-50 rounded animate-pulse mb-2"></div>
        </div>
      </div>

      <template v-else>
        <!-- Stat cards -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
          <!-- Total Questions -->
          <router-link
            to="/questions"
            class="bg-white rounded-xl border border-slate-200 p-5 hover:border-blue-300 hover:shadow-sm transition-all cursor-pointer no-underline group"
          >
            <div class="flex items-center justify-between">
              <div>
                <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Questions</p>
                <p class="text-2xl font-black text-slate-900">{{ questionCount }}</p>
              </div>
              <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-blue-50 group-hover:bg-blue-100 transition-colors">
                <BookOpen class="w-5 h-5 text-blue-600" />
              </div>
            </div>
          </router-link>

          <!-- Active Quizzes -->
          <router-link
            to="/quizzes"
            class="bg-white rounded-xl border border-slate-200 p-5 hover:border-green-300 hover:shadow-sm transition-all cursor-pointer no-underline group"
          >
            <div class="flex items-center justify-between">
              <div>
                <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Active Quizzes</p>
                <p class="text-2xl font-black text-slate-900">{{ activeQuizCount }}</p>
              </div>
              <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-green-50 group-hover:bg-green-100 transition-colors">
                <Zap class="w-5 h-5 text-green-600" />
              </div>
            </div>
          </router-link>

          <!-- Total Participants -->
          <div class="bg-white rounded-xl border border-slate-200 p-5">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Participants</p>
                <p class="text-2xl font-black text-slate-900">{{ totalParticipants }}</p>
              </div>
              <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-amber-50">
                <Users class="w-5 h-5 text-amber-600" />
              </div>
            </div>
          </div>

          <!-- Pass Rate -->
          <div class="bg-white rounded-xl border border-slate-200 p-5">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-[0.6875rem] uppercase tracking-[0.07em] text-slate-500 font-semibold mb-1">Pass Rate</p>
                <p class="text-2xl font-black text-slate-900">{{ overallPassRate }}%</p>
              </div>
              <div class="w-[42px] h-[42px] min-w-[42px] rounded-xl flex items-center justify-center bg-purple-50">
                <Trophy class="w-5 h-5 text-purple-600" />
              </div>
            </div>
          </div>
        </div>

        <!-- Two column layout -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-5">
          <!-- Active Quizzes (2 cols) -->
          <div class="lg:col-span-2 bg-white rounded-xl border border-slate-200">
            <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-green-50 flex items-center justify-center">
                  <Zap class="w-4 h-4 text-green-600" />
                </div>
                <h3 class="text-sm font-bold text-slate-900">Active Quizzes</h3>
              </div>
              <router-link to="/quizzes" class="text-xs text-blue-600 hover:text-blue-700 font-medium no-underline">
                View all →
              </router-link>
            </div>
            <div v-if="activeQuizzes.length === 0" class="px-5 py-8 text-center text-slate-400 text-sm">
              No active quizzes. <router-link to="/quizzes/new" class="text-blue-600 hover:underline">Create one</router-link>
            </div>
            <div v-else class="divide-y divide-slate-100">
              <div
                v-for="quiz in activeQuizzes"
                :key="quiz.id"
                class="px-5 py-4 flex items-center gap-4 hover:bg-slate-50 transition-colors"
              >
                <div class="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center flex-shrink-0">
                  <Zap class="w-5 h-5 text-green-600" />
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-slate-900 truncate">{{ quiz.name }}</p>
                  <p class="text-xs text-slate-500 mt-0.5">
                    {{ getQuizParticipantCount(quiz.id) }} participants ·
                    {{ getQuizPassRate(quiz.id) }}% pass rate
                  </p>
                </div>
                <div class="flex items-center gap-2 flex-shrink-0">
                  <router-link
                    :to="`/quizzes/${quiz.id}/edit`"
                    class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer"
                    title="Edit"
                  >
                    <Pencil class="w-3.5 h-3.5" />
                  </router-link>
                  <button
                    class="w-8 h-8 rounded-lg bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-500 transition cursor-pointer"
                    title="Stop quiz"
                    @click="stopQuiz(quiz)"
                  >
                    <Square class="w-3.5 h-3.5 fill-red-500" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Quick Actions (1 col) -->
          <div class="bg-white rounded-xl border border-slate-200">
            <div class="flex items-center gap-3 px-5 py-4 border-b border-slate-100">
              <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
                <Lightbulb class="w-4 h-4 text-blue-600" />
              </div>
              <h3 class="text-sm font-bold text-slate-900">Quick Actions</h3>
            </div>
            <div class="p-4 space-y-2">
              <router-link
                to="/quizzes/new"
                class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-50 transition-colors no-underline group"
              >
                <div class="w-8 h-8 rounded-lg bg-green-50 flex items-center justify-center group-hover:bg-green-100 transition-colors">
                  <Plus class="w-4 h-4 text-green-600" />
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-900">Create Quiz</p>
                  <p class="text-xs text-slate-500">New quiz with Telegram bot</p>
                </div>
              </router-link>
              <router-link
                to="/questions"
                class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-50 transition-colors no-underline group"
              >
                <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center group-hover:bg-blue-100 transition-colors">
                  <BookOpen class="w-4 h-4 text-blue-600" />
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-900">Question Bank</p>
                  <p class="text-xs text-slate-500">Manage your questions</p>
                </div>
              </router-link>
              <router-link
                to="/quizzes"
                class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-slate-50 transition-colors no-underline group"
              >
                <div class="w-8 h-8 rounded-lg bg-purple-50 flex items-center justify-center group-hover:bg-purple-100 transition-colors">
                  <FolderIcon class="w-4 h-4 text-purple-600" />
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-900">All Quizzes</p>
                  <p class="text-xs text-slate-500">View draft, active & stopped</p>
                </div>
              </router-link>
            </div>
          </div>
        </div>

        <!-- Recent Participants -->
        <div class="bg-white rounded-xl border border-slate-200">
          <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg bg-amber-50 flex items-center justify-center">
                <Users class="w-4 h-4 text-amber-600" />
              </div>
              <h3 class="text-sm font-bold text-slate-900">Recent Participants</h3>
            </div>
            <span class="text-xs text-slate-400">{{ recentParticipants.length }} total</span>
          </div>
          <div v-if="recentParticipants.length === 0" class="px-5 py-8 text-center text-slate-400 text-sm">
            No participants yet. Start a quiz and share the bot link!
          </div>
          <div v-else class="divide-y divide-slate-100">
            <div
              v-for="p in recentParticipants"
              :key="p.sessionId"
              class="px-5 py-3 flex items-center gap-3"
            >
              <!-- Avatar -->
              <div class="w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0"
                :class="p.passed ? 'bg-green-100' : p.status === 'IN_PROGRESS' ? 'bg-blue-100' : 'bg-red-100'"
              >
                <span class="text-sm font-bold"
                  :class="p.passed ? 'text-green-700' : p.status === 'IN_PROGRESS' ? 'text-blue-700' : 'text-red-700'"
                >{{ (p.firstName || '?')[0].toUpperCase() }}</span>
              </div>
              <!-- Info -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-slate-900 truncate">
                  {{ p.firstName }}
                  <span v-if="p.username" class="text-slate-400 font-normal text-xs ml-1">@{{ p.username }}</span>
                </p>
                <p class="text-xs text-slate-400 mt-0.5">{{ p.quizName }} · {{ formatDate(p.startedAt) }}</p>
              </div>
              <!-- Result -->
              <div class="flex flex-col items-end gap-0.5 flex-shrink-0">
                <span
                  v-if="p.status === 'COMPLETED'"
                  class="inline-flex items-center gap-1 text-xs font-bold px-2 py-0.5 rounded-full"
                  :class="p.passed ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'"
                >
                  <CheckCircle v-if="p.passed" class="w-3 h-3" />
                  <XCircle v-else class="w-3 h-3" />
                  {{ p.passed ? 'PASS' : 'FAIL' }}
                </span>
                <span v-else-if="p.status === 'ABANDONED'" class="inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">
                  <Minus class="w-3 h-3" />
                  Left
                </span>
                <span v-else class="inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full bg-blue-50 text-blue-500">
                  <div class="w-2 h-2 rounded-full bg-blue-400 animate-pulse"></div>
                  Playing
                </span>
                <span class="text-xs text-slate-500">{{ p.score }}/{{ p.totalQuestions }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LayoutDashboard, BookOpen, Zap, Folder as FolderIcon, Users, Trophy, Plus, Pencil, Square, Lightbulb, CheckCircle, XCircle, Minus } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { quizService } from '@/services/quizService'
import { questionsService } from '@/services/questionsService'
import { foldersService } from '@/services/foldersService'
import { useToast } from '@/composables/useToast'
import type { QuizSummary, QuizSessionSummary } from '@/types/quiz'

const toast = useToast()

const statsLoading = ref(true)
const questionCount = ref(0)
const quizCount = ref(0)
const activeQuizCount = ref(0)
const folderCount = ref(0)
const totalParticipants = ref(0)
const overallPassRate = ref(0)
const activeQuizzes = ref<QuizSummary[]>([])
const recentParticipants = ref<Array<{
  sessionId: number
  firstName: string
  username: string | null
  quizName: string
  score: number
  totalQuestions: number
  passed: boolean
  status: string
  startedAt: string
}>>([])

const quizSessionsMap = ref<Map<number, QuizSessionSummary[]>>(new Map())

function getQuizParticipantCount(quizId: number): number {
  return quizSessionsMap.value.get(quizId)?.length ?? 0
}

function getQuizPassRate(quizId: number): number {
  const sessions = quizSessionsMap.value.get(quizId) ?? []
  const completed = sessions.filter(s => s.status === 'COMPLETED')
  if (completed.length === 0) return 0
  const passed = completed.filter(s => s.passed).length
  return Math.round((passed / completed.length) * 100)
}

onMounted(async () => {
  statsLoading.value = true
  try {
    const [questions, quizzes, folders] = await Promise.all([
      questionsService.list(),
      quizService.list(),
      foldersService.list(),
    ])

    questionCount.value = questions.length
    quizCount.value = quizzes.length
    activeQuizCount.value = quizzes.filter(q => q.status === 'ACTIVE').length
    folderCount.value = folders.length
    activeQuizzes.value = quizzes.filter(q => q.status === 'ACTIVE')

    // Fetch sessions for all active quizzes
    const sessionPromises = activeQuizzes.value.map(q =>
      quizService.getSessions(q.id).then(sessions => ({ quizId: q.id, quizName: q.name, sessions }))
    )
    const sessionResults = await Promise.allSettled(sessionPromises)

    let allParticipants = 0
    let allCompleted = 0
    let allPassed = 0
    const participants: typeof recentParticipants.value = []

    for (const result of sessionResults) {
      if (result.status === 'fulfilled') {
        const { quizId, quizName, sessions } = result.value
        quizSessionsMap.value.set(quizId, sessions)
        allParticipants += sessions.length
        const completed = sessions.filter(s => s.status === 'COMPLETED')
        allCompleted += completed.length
        allPassed += completed.filter(s => s.passed).length

        for (const s of sessions) {
          participants.push({
            sessionId: s.id,
            firstName: s.telegramFirstName,
            username: s.telegramUsername,
            quizName,
            score: s.score,
            totalQuestions: s.totalQuestions,
            passed: s.passed,
            status: s.status,
            startedAt: s.startedAt,
          })
        }
      }
    }

    totalParticipants.value = allParticipants
    overallPassRate.value = allCompleted > 0 ? Math.round((allPassed / allCompleted) * 100) : 0

    // Sort by startedAt desc, take top 20
    participants.sort((a, b) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime())
    recentParticipants.value = participants.slice(0, 20)
  } catch {
    // show empty state on error
  } finally {
    statsLoading.value = false
  }
})

async function stopQuiz(quiz: QuizSummary) {
  try {
    await quizService.stop(quiz.id)
    toast.success(`${quiz.name} stopped`)
    // Reload data
    const quizzes = await quizService.list()
    activeQuizzes.value = quizzes.filter(q => q.status === 'ACTIVE')
    activeQuizCount.value = activeQuizzes.value.length
  } catch {
    toast.error('Failed to stop quiz')
  }
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}
</script>
