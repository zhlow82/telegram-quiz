<template>
  <AppLayout>
    <!-- Header -->
    <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">
      <router-link
        to="/quizzes"
        class="w-9 h-9 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 flex items-center justify-center text-slate-500 hover:text-slate-700 transition no-underline flex-shrink-0"
      >
        <ChevronLeft class="w-4 h-4" />
      </router-link>
      <div>
        <h1 class="text-2xl font-black text-slate-900 leading-tight">Create Quiz</h1>
        <p class="text-sm text-slate-500 mt-0.5">Step {{ step }} of {{ TOTAL_STEPS }}</p>
      </div>
    </div>

    <!-- Step progress bar -->
    <div class="flex items-center gap-1.5 mb-8">
      <div
        v-for="s in TOTAL_STEPS"
        :key="s"
        class="h-1.5 rounded-full flex-1 transition-colors duration-300"
        :class="s <= step ? 'bg-blue-600' : 'bg-slate-200'"
      ></div>
    </div>

    <!-- ── Step 1: BotFather instructions ─────────────────────────────── -->
    <div v-if="step === 1" class="max-w-2xl space-y-5">
      <div class="bg-blue-50 border border-blue-200 rounded-2xl p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
            <Bot class="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 class="text-base font-bold text-slate-900">Create a Telegram Bot</h2>
            <p class="text-sm text-slate-500">You'll need a bot token from BotFather</p>
          </div>
        </div>
        <ol class="space-y-3 text-sm text-slate-700">
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-blue-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">1</span>
            <span>Open Telegram and search for <strong class="font-semibold">@BotFather</strong> (the official bot with a blue checkmark)</span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-blue-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">2</span>
            <span>Send the command <code class="bg-white border border-blue-200 px-1.5 py-0.5 rounded text-blue-700 font-mono text-xs">/newbot</code> to BotFather</span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-blue-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">3</span>
            <span>Follow the prompts — choose a name and a unique username ending in <code class="bg-white border border-blue-200 px-1.5 py-0.5 rounded text-blue-700 font-mono text-xs">bot</code></span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-blue-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">4</span>
            <span>BotFather will reply with your <strong class="font-semibold">bot token</strong> — a long string like <code class="bg-white border border-blue-200 px-1.5 py-0.5 rounded text-blue-700 font-mono text-xs">123456789:ABC...</code></span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-blue-600 text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">5</span>
            <span>Copy that token — you'll paste it in the next step</span>
          </li>
        </ol>
      </div>

      <div class="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 flex items-start gap-2.5 text-sm text-amber-800">
        <AlertTriangle class="w-4 h-4 flex-shrink-0 mt-0.5" />
        <span>Keep your bot token secret — treat it like a password. Never share it publicly.</span>
      </div>
    </div>

    <!-- ── Step 2: Bot token input ─────────────────────────────────────── -->
    <div v-else-if="step === 2" class="max-w-xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Enter your bot token</h2>
        <p class="text-sm text-slate-500">Paste the token you received from BotFather</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-slate-700">Bot Token</label>
        <div class="flex gap-2">
          <input
            v-model="botToken"
            type="password"
            placeholder="123456789:ABCDEFGHIJKLMNabcdefghijklmn"
            class="flex-1 px-3 py-2.5 text-sm border rounded-xl outline-none transition bg-white text-slate-900 placeholder-slate-400 font-mono"
            :class="tokenError ? 'border-red-400 focus:border-red-500 focus:ring-2 focus:ring-red-500/20' : 'border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20'"
            @input="botValidated = false; tokenError = ''"
            @keydown.enter="validateToken"
          />
          <button
            class="px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition disabled:opacity-60 cursor-pointer disabled:cursor-not-allowed flex items-center gap-2"
            :disabled="!botToken.trim() || validating"
            @click="validateToken"
          >
            <span v-if="validating" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
            {{ validating ? 'Checking…' : 'Validate' }}
          </button>
        </div>
        <p v-if="tokenError" class="text-xs text-red-600">{{ tokenError }}</p>
        <div v-if="botValidated" class="flex items-center gap-2 text-green-700 text-sm bg-green-50 border border-green-200 rounded-lg px-3 py-2">
          <CheckCircle class="w-4 h-4 flex-shrink-0" />
          Connected to <strong class="font-semibold">{{ botName }}</strong>
          <span class="text-green-500 text-xs">@{{ botUsername }}</span>
        </div>
      </div>
    </div>

    <!-- ── Step 3: Quiz config ─────────────────────────────────────────── -->
    <div v-else-if="step === 3" class="max-w-xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Quiz settings</h2>
        <p class="text-sm text-slate-500">Configure how your quiz will run</p>
      </div>

      <div class="space-y-4">
        <div class="space-y-1.5">
          <label class="block text-sm font-semibold text-slate-700">Quiz Name <span class="text-red-500">*</span></label>
          <input
            v-model="quizName"
            type="text"
            placeholder="e.g. Geography Round 1"
            class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-xl outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 bg-white text-slate-900 placeholder-slate-400 transition"
          />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1.5">
            <label class="block text-sm font-semibold text-slate-700">Time per Question</label>
            <div class="relative">
              <input
                v-model.number="timePerQuestion"
                type="number"
                min="5"
                max="300"
                class="w-full px-3 py-2.5 pr-10 text-sm border border-slate-200 rounded-xl outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 bg-white text-slate-900 transition"
              />
              <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400">sec</span>
            </div>
            <p class="text-xs text-slate-400">5 – 300 seconds</p>
          </div>

          <div class="space-y-1.5">
            <label class="block text-sm font-semibold text-slate-700">Pass Score</label>
            <div class="relative">
              <input
                v-model.number="passScorePercent"
                type="number"
                min="0"
                max="100"
                class="w-full px-3 py-2.5 pr-8 text-sm border border-slate-200 rounded-xl outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 bg-white text-slate-900 transition"
              />
              <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400">%</span>
            </div>
            <p class="text-xs text-slate-400">0 – 100%</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Step 4: Question selector ──────────────────────────────────── -->
    <div v-else-if="step === 4" class="max-w-3xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Select questions</h2>
        <p class="text-sm text-slate-500">Choose questions from your bank. Drag to reorder selected ones.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4" style="min-height: 300px">
        <!-- Available questions -->
        <div class="space-y-2">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide">Available ({{ unselectedQuestions.length }})</p>
          <div class="bg-white border border-slate-200 rounded-xl overflow-hidden max-h-96 overflow-y-auto">
            <div v-if="questionsLoading" class="px-4 py-6 text-center text-sm text-slate-400">Loading…</div>
            <div v-else-if="allQuestions.length === 0" class="px-4 py-6 text-center text-sm text-slate-400 space-y-1">
              <p>No accessible questions found.</p>
              <p class="text-xs text-slate-400">This account can only use questions you created or questions shared with you.</p>
            </div>
            <div v-else-if="unselectedQuestions.length === 0" class="px-4 py-6 text-center text-sm text-slate-400">
              All questions selected
            </div>
            <button
              v-for="q in unselectedQuestions"
              :key="q.id"
              type="button"
              class="w-full text-left px-4 py-3 border-b border-slate-100 last:border-0 hover:bg-blue-50 transition-colors cursor-pointer flex items-start gap-3"
              @click="selectQuestion(q)"
            >
              <Plus class="w-4 h-4 text-blue-500 flex-shrink-0 mt-0.5" />
              <span class="text-sm text-slate-700 line-clamp-2">{{ q.questionBlocks.find(b => b.type === 'text')?.content || '(no text)' }}</span>
            </button>
          </div>
        </div>

        <!-- Selected questions (draggable) -->
        <div class="space-y-2">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide">Selected ({{ selectedQuestions.length }})</p>
          <div class="bg-white border border-slate-200 rounded-xl overflow-hidden max-h-96 overflow-y-auto">
            <div v-if="selectedQuestions.length === 0" class="px-4 py-6 text-center text-sm text-slate-400">
              Click questions to add them
            </div>
            <VueDraggable
              v-else
              v-model="selectedQuestions"
              handle=".q-drag-handle"
              :animation="150"
            >
              <div
                v-for="(q, i) in selectedQuestions"
                :key="q.id"
                class="flex items-start gap-2 px-3 py-3 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors"
              >
                <button type="button" class="q-drag-handle cursor-grab mt-0.5 text-slate-300 hover:text-slate-400 active:cursor-grabbing">
                  <GripVertical class="w-4 h-4" />
                </button>
                <span class="text-xs font-bold text-slate-300 w-5 flex-shrink-0 mt-0.5">{{ i + 1 }}</span>
                <span class="flex-1 text-sm text-slate-700 line-clamp-2 min-w-0">{{ q.questionBlocks.find(b => b.type === 'text')?.content || '(no text)' }}</span>
                <button type="button" class="text-slate-300 hover:text-red-500 transition flex-shrink-0 mt-0.5 cursor-pointer" @click="deselectQuestion(q)">
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>
            </VueDraggable>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Step 5: Review ──────────────────────────────────────────────── -->
    <div v-else-if="step === 5" class="max-w-xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Review & Create</h2>
        <p class="text-sm text-slate-500">Everything look good? Hit Create to launch your quiz.</p>
      </div>

      <div class="bg-white border border-slate-200 rounded-2xl divide-y divide-slate-100">
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><Bot class="w-4 h-4" />Bot</span>
          <span class="text-sm font-semibold text-slate-900">{{ botName }} <span class="font-normal text-slate-500">@{{ botUsername }}</span></span>
        </div>
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><Zap class="w-4 h-4" />Quiz Name</span>
          <span class="text-sm font-semibold text-slate-900">{{ quizName }}</span>
        </div>
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><Clock class="w-4 h-4" />Time per Question</span>
          <span class="text-sm font-semibold text-slate-900">{{ timePerQuestion }}s</span>
        </div>
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><CheckCircle class="w-4 h-4" />Pass Score</span>
          <span class="text-sm font-semibold text-slate-900">{{ passScorePercent }}%</span>
        </div>
        <div class="px-5 py-4 flex items-start justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2 flex-shrink-0"><BookOpen class="w-4 h-4" />Questions</span>
          <div class="text-sm font-semibold text-slate-900 text-right">
            {{ selectedQuestions.length }} question{{ selectedQuestions.length !== 1 ? 's' : '' }}
            <ol class="mt-1 space-y-0.5 font-normal text-slate-500 text-xs text-left">
              <li v-for="(q, i) in selectedQuestions" :key="q.id">{{ i + 1 }}. {{ q.questionBlocks.find(b => b.type === 'text')?.content || '(no text)' }}</li>
            </ol>
          </div>
        </div>
      </div>

      <p v-if="submitError" class="text-sm text-red-600">{{ submitError }}</p>
    </div>

    <!-- Navigation -->
    <div class="flex items-center gap-3 mt-8">
      <button
        v-if="step > 1"
        type="button"
        class="px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
        @click="step--"
      >
        ← Back
      </button>
      <div
        v-if="step < TOTAL_STEPS"
        class="inline-flex"
        :class="canProceed ? 'cursor-pointer' : 'cursor-not-allowed'"
        role="button"
        :tabindex="canProceed ? 0 : -1"
        :aria-disabled="!canProceed"
        @click="nextStep"
        @keydown.enter.prevent="nextStep"
        @keydown.space.prevent="nextStep"
      >
        <button
          type="button"
          class="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition disabled:opacity-60 disabled:cursor-not-allowed"
          :class="canProceed ? 'cursor-pointer' : 'pointer-events-none'"
          :disabled="!canProceed"
        >
          Continue →
        </button>
      </div>
      <button
        v-if="step === TOTAL_STEPS"
        type="button"
        class="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed inline-flex items-center gap-2"
        :disabled="submitting"
        @click="createQuiz"
      >
        <span v-if="submitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
        {{ submitting ? 'Creating…' : 'Create Quiz' }}
      </button>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { VueDraggable } from 'vue-draggable-plus'
import {
  ChevronLeft, Plus, X, GripVertical, Bot, Zap, BookOpen, Clock,
  CheckCircle, AlertTriangle
} from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { quizService } from '@/services/quizService'
import { questionsService } from '@/services/questionsService'
import type { Question } from '@/types/question'

const router = useRouter()

const TOTAL_STEPS = 5
const step = ref(1)

// Step 2 — bot token
const botToken = ref('')
const botValidated = ref(false)
const botName = ref('')
const botUsername = ref('')
const validating = ref(false)
const tokenError = ref('')

// Step 3 — quiz config
const quizName = ref('')
const timePerQuestion = ref(30)
const passScorePercent = ref(60)

// Step 4 — questions
const allQuestions = ref<Question[]>([])
const selectedQuestions = ref<Question[]>([])
const questionsLoading = ref(false)

// Step 5
const submitting = ref(false)
const submitError = ref('')

const unselectedQuestions = computed(() => {
  const selectedIds = new Set(selectedQuestions.value.map(q => q.id))
  return allQuestions.value.filter(q => !selectedIds.has(q.id))
})

const isQuizConfigValid = computed(() => {
  const hasName = quizName.value.trim().length > 0
  const hasValidTime = Number.isFinite(timePerQuestion.value)
    && timePerQuestion.value >= 5
    && timePerQuestion.value <= 300
  const hasValidPassScore = Number.isFinite(passScorePercent.value)
    && passScorePercent.value >= 0
    && passScorePercent.value <= 100

  return hasName && hasValidTime && hasValidPassScore
})

const canProceed = computed(() => {
  if (step.value === 1) return true
  if (step.value === 2) return botValidated.value
  if (step.value === 3) return isQuizConfigValid.value
  if (step.value === 4) return selectedQuestions.value.length > 0
  return true
})

onMounted(loadQuestions)

async function loadQuestions() {
  questionsLoading.value = true
  try {
    allQuestions.value = await questionsService.list()
  } finally {
    questionsLoading.value = false
  }
}

async function validateToken() {
  if (!botToken.value.trim() || validating.value) return
  validating.value = true
  tokenError.value = ''
  botValidated.value = false
  try {
    const res = await quizService.validateToken({ token: botToken.value.trim() })
    if (res.valid) {
      botValidated.value = true
      botName.value = res.botName ?? ''
      botUsername.value = res.username ?? ''
    } else {
      tokenError.value = 'Invalid token. Please check and try again.'
    }
  } catch {
    tokenError.value = 'Could not validate token. Check your connection.'
  } finally {
    validating.value = false
  }
}

function selectQuestion(q: Question) {
  if (!selectedQuestions.value.find(s => s.id === q.id)) {
    selectedQuestions.value.push(q)
  }
}

function deselectQuestion(q: Question) {
  selectedQuestions.value = selectedQuestions.value.filter(s => s.id !== q.id)
}

function nextStep() {
  if (!canProceed.value) return
  step.value++
}

async function createQuiz() {
  submitting.value = true
  submitError.value = ''
  try {
    await quizService.create({
      name: quizName.value.trim(),
      botToken: botToken.value.trim(),
      botUsername: botUsername.value.trim() || undefined,
      timePerQuestionSeconds: timePerQuestion.value,
      passScorePercent: passScorePercent.value,
      questionIds: selectedQuestions.value.map(q => q.id),
    })
    router.push('/quizzes')
  } catch {
    submitError.value = 'Failed to create quiz. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>
