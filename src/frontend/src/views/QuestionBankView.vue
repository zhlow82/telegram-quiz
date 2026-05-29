<template>
  <AppLayout>
    <div>

      <!-- Page header -->
      <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
            <BookOpen class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-2xl font-black text-slate-900 leading-tight">Question Bank</h1>
            <p class="text-sm text-slate-500 mt-0.5">
              {{ questions.length }} question{{ questions.length !== 1 ? 's' : '' }} · drag to reorder
            </p>
          </div>
        </div>
        <button
          class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition cursor-pointer"
          @click="openCreate"
        >
          <Plus class="w-4 h-4" />
          Add Question
        </button>
      </div>

      <!-- Error state -->
      <div
        v-if="loadError"
        class="flex items-center gap-3 mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm"
      >
        <AlertCircle class="w-4 h-4 flex-shrink-0" />
        Failed to load questions. Please refresh.
      </div>

      <!-- Loading skeleton -->
      <div v-else-if="loading" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div v-for="i in 4" :key="i" class="flex items-center gap-4 px-5 py-4 border-b border-slate-100 last:border-0">
          <div class="w-4 h-8 bg-slate-200 rounded animate-pulse flex-shrink-0"></div>
          <div class="flex-1 flex flex-col gap-2">
            <div class="h-3.5 bg-slate-100 rounded animate-pulse" style="animation-delay: 0.05s"></div>
            <div class="h-3 bg-slate-100 rounded animate-pulse w-2/5" style="animation-delay: 0.1s"></div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div
        v-else-if="questions.length === 0"
        class="bg-white rounded-xl border border-slate-200 flex flex-col items-center gap-4 py-14 px-6 text-center"
      >
        <div class="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center">
          <BookOpen class="w-7 h-7 text-blue-600" />
        </div>
        <div>
          <p class="font-semibold text-slate-900 mb-1">No questions yet</p>
          <p class="text-sm text-slate-500">Add your first question to get started</p>
        </div>
        <button
          class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition cursor-pointer"
          @click="openCreate"
        >
          <Plus class="w-4 h-4" />
          Add Question
        </button>
      </div>

      <!-- Question list -->
      <div v-else class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <VueDraggable
          v-model="questions"
          handle=".drag-handle"
          :animation="200"
          ghost-class="drag-ghost"
          @end="persistReorder"
        >
            <div v-for="(q, i) in questions" :key="q.id"
              class="flex items-center gap-3 px-5 py-3.5 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors"
            >
              <button
                class="bg-transparent border-0 cursor-grab p-1 rounded flex shrink-0 text-slate-300 hover:text-slate-400 active:cursor-grabbing transition-colors"
                title="Drag to reorder" aria-label="Drag"
              >
                <GripVertical class="w-4 h-4" />
              </button>

              <span class="text-[0.8125rem] font-bold text-slate-300 w-[22px] shrink-0 text-right">{{ i + 1 }}</span>

              <div class="flex-1 min-w-0">
                <span class="block text-sm font-medium text-slate-900 truncate sm:whitespace-normal">{{ q.questionText }}</span>
                <div class="flex gap-1.5 mt-1 flex-wrap">
                  <span v-if="q.expectPhoto" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-amber-100 text-amber-800">📷 photo</span>
                  <span v-if="q.options.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-blue-100 text-blue-800">{{ q.options.length }} options</span>
                  <span v-if="q.questionImagePaths?.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-green-100 text-green-800">🖼 image</span>
                </div>
              </div>

              <div class="flex items-center gap-1.5 flex-shrink-0">
                <button
                  class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer"
                  title="Edit"
                  @click="openEdit(q)"
                >
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button
                  class="w-8 h-8 rounded-lg bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-500 transition cursor-pointer"
                  title="Delete"
                  @click="confirmDelete(q)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
        </VueDraggable>
      </div>

    </div>

    <!-- Form modal -->
    <QuestionFormModal
      :visible="modalVisible"
      :question="editingQuestion"
      @close="modalVisible = false"
      @saved="onSaved"
    />
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import { Plus, AlertCircle, BookOpen, GripVertical, Pencil, Trash2 } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import QuestionFormModal from '@/components/QuestionFormModal.vue'
import { questionsService } from '@/services/questionsService'
import type { Question } from '@/types/question'

const questions = ref<Question[]>([])
const loading = ref(true)
const loadError = ref(false)

const modalVisible = ref(false)
const editingQuestion = ref<Question | null>(null)

onMounted(loadQuestions)

async function loadQuestions() {
  loading.value = true
  loadError.value = false
  try {
    questions.value = await questionsService.list()
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingQuestion.value = null
  modalVisible.value = true
}

function openEdit(q: Question) {
  editingQuestion.value = q
  modalVisible.value = true
}

function onSaved(q: Question) {
  const idx = questions.value.findIndex(x => x.id === q.id)
  if (idx >= 0) {
    questions.value.splice(idx, 1, q)
  } else {
    questions.value.push(q)
  }
  modalVisible.value = false
}

async function confirmDelete(q: Question) {
  if (!confirm(`Delete "${q.questionText}"?`)) return
  try {
    await questionsService.delete(q.id)
    questions.value = questions.value.filter(x => x.id !== q.id)
  } catch {
    alert('Failed to delete question.')
  }
}

async function persistReorder() {
  const orderedIds = questions.value.map(q => q.id)
  try {
    await questionsService.reorder(orderedIds)
  } catch {
    await loadQuestions()
  }
}
</script>

<style scoped>
/* ghost element while dragging — required by VueDraggable ghost-class prop */
.drag-ghost { opacity: 0.4; background: #dbeafe !important; }
</style>
