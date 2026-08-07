<template>
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" />
      <div class="relative z-10 bg-white rounded-2xl shadow-xl max-w-lg w-full mx-4 p-6 max-h-[85vh] flex flex-col">
        <div class="flex items-center justify-between mb-5">
          <div class="flex items-center gap-3">
            <h2 class="text-lg font-bold text-slate-900">Import Questions</h2>
            <span v-if="step === 2" class="text-xs font-medium px-2 py-0.5 rounded-full bg-blue-100 text-blue-700">Step 2 of 2</span>
          </div>
          <button
            class="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-400 hover:text-slate-600 transition cursor-pointer"
            @click="handleClose"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <div class="flex-1 overflow-y-auto">
          <template v-if="step === 1">
            <div
              class="border-2 border-dashed rounded-xl p-8 text-center transition-colors"
              :class="isDragging ? 'border-blue-400 bg-blue-50' : 'border-slate-300 hover:border-slate-400'"
              @dragover.prevent.stop="isDragging = true"
              @dragleave.stop="isDragging = false"
              @drop.prevent.stop="onDrop"
            >
              <div class="w-14 h-14 mx-auto rounded-2xl bg-slate-100 flex items-center justify-center mb-4">
                <Upload class="w-7 h-7 text-slate-400" />
              </div>
              <p class="text-sm font-medium text-slate-700 mb-1">Drag & drop a JSON file here</p>
              <p class="text-xs text-slate-500 mb-4">or</p>
              <button
                class="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-lg transition cursor-pointer"
                @click="triggerFileInput"
              >
                <FolderOpen class="w-4 h-4" />
                Browse Files
              </button>
              <input
                ref="fileInput"
                type="file"
                accept=".json,application/json"
                class="hidden"
                @change="onFileSelect"
              />
            </div>

            <div v-if="parseError" class="mt-4 p-3 rounded-xl bg-red-50 border border-red-200 flex items-start gap-2">
              <AlertCircle class="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5" />
              <p class="text-sm text-red-700">{{ parseError }}</p>
            </div>
          </template>

          <template v-else-if="step === 2 && parsedData">
            <div class="space-y-4">
              <div class="p-3 rounded-xl bg-amber-50 border border-amber-200 flex items-start gap-2">
                <Inbox class="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
                <p class="text-sm text-amber-700">All imported questions will be placed in the <span class="font-semibold">Unfiled</span> folder. You can move them afterward.</p>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="bg-blue-50 rounded-xl p-3 text-center">
                  <p class="text-2xl font-bold text-blue-600">{{ parsedData.questions.length }}</p>
                  <p class="text-xs text-slate-600">Questions</p>
                </div>
                <div class="bg-purple-50 rounded-xl p-3 text-center">
                  <p class="text-2xl font-bold text-purple-600">{{ imageCount }}</p>
                  <p class="text-xs text-slate-600">Images</p>
                </div>
              </div>

              <div>
                <p class="text-xs font-medium text-slate-500 uppercase tracking-wide mb-2">Preview</p>
                <div class="space-y-2 max-h-60 overflow-y-auto">
                  <div
                    v-for="(q, i) in parsedData.questions"
                    :key="i"
                    class="flex items-center gap-3 p-2.5 rounded-lg bg-slate-50 border border-slate-200"
                  >
                    <div v-if="hasImage(q)" class="relative w-10 h-10 rounded-lg bg-slate-200 flex items-center justify-center flex-shrink-0">
                      <img
                        :src="getImageSrc(q)"
                        class="w-10 h-10 rounded-lg object-cover"
                        alt=""
                        @error="onImageError($event)"
                      />
                      <span
                        v-if="imageCountForQuestion(q) > 1"
                        class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
                      >+{{ imageCountForQuestion(q) - 1 }}</span>
                    </div>
                    <div v-else class="w-10 h-10 rounded-lg bg-slate-200 flex items-center justify-center flex-shrink-0">
                      <FileText class="w-5 h-5 text-slate-400" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm text-slate-700 truncate">{{ getQuestionText(q) }}</p>
                      <p class="text-xs text-slate-500">
                        {{ q.options?.length || 0 }} options
                        <template v-if="q.mark"> · {{ q.mark }} pts</template>
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="importing" class="space-y-2">
                <div class="flex items-center gap-2">
                  <Loader2 class="w-4 h-4 animate-spin text-blue-600" />
                  <span class="text-sm text-slate-600">Importing questions...</span>
                </div>
              </div>

              <div v-if="importResult" class="space-y-2">
                <div v-if="importResult.imported > 0" class="p-3 rounded-xl bg-green-50 border border-green-200 flex items-center gap-2">
                  <CheckCircle2 class="w-4 h-4 text-green-600" />
                  <span class="text-sm text-green-700">Successfully imported {{ importResult.imported }} question{{ importResult.imported !== 1 ? 's' : '' }}</span>
                </div>
                <div v-if="importResult.errors.length > 0" class="p-3 rounded-xl bg-amber-50 border border-amber-200">
                  <div class="flex items-center gap-2 mb-1">
                    <AlertTriangle class="w-4 h-4 text-amber-600" />
                    <span class="text-sm font-medium text-amber-700">{{ importResult.errors.length }} error{{ importResult.errors.length !== 1 ? 's' : '' }}</span>
                  </div>
                  <ul class="text-xs text-amber-600 space-y-0.5 ml-6">
                    <li v-for="(err, i) in importResult.errors" :key="i">{{ err }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="flex justify-between gap-3 mt-5 pt-4 border-t border-slate-200">
          <button
            v-if="step === 2 && !importing"
            class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition cursor-pointer"
            @click="step = 1; resetImport()"
          >
            Back
          </button>
          <div v-else />
          <div class="flex gap-3">
            <button
              class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition cursor-pointer"
              :disabled="importing"
              @click="handleClose"
            >
              {{ importResult ? 'Close' : 'Cancel' }}
            </button>
            <button
              v-if="step === 2 && !importResult"
              class="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold text-white bg-primary hover:bg-primary-hover rounded-full transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="importing"
              @click="handleImport"
            >
              <Upload class="w-4 h-4" />
              Import
            </button>
          </div>
        </div>
      </div>
    </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  X, Upload, FolderOpen, AlertCircle, Loader2,
  CheckCircle2, AlertTriangle, FileText, Inbox
} from '@lucide/vue'
import { questionsService } from '@/services/questionsService'
import { useToast } from '@/composables/useToast'
import type { ExportData, ExportedQuestion, ImportResult } from '@/types/question'

const props = defineProps<{
  visible: boolean
  initialFile?: File | null
}>()

const emit = defineEmits<{
  close: []
  imported: []
}>()

const toast = useToast()
const fileInput = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const parseError = ref('')
const parsedData = ref<ExportData | null>(null)
const step = ref(1)
const importing = ref(false)
const importResult = ref<ImportResult | null>(null)

const imageCount = computed(() => {
  if (!parsedData.value) return 0
  return parsedData.value.questions.reduce((count, q) => {
    const qImages = q.questionBlocks?.filter(b => b.type === 'image').length || 0
    const hImages = q.hintBlocks?.filter(b => b.type === 'image').length || 0
    const eImages = q.explanationBlocks?.filter(b => b.type === 'image').length || 0
    return count + qImages + hImages + eImages
  }, 0)
})

watch(() => props.visible, (val) => {
  if (!val) {
    step.value = 1
    resetImport()
  }
})

watch(() => props.initialFile, (file) => {
  if (file && props.visible) {
    handleFile(file)
  }
})

function triggerFileInput() {
  fileInput.value?.click()
}

function onFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.length) {
    handleFile(input.files[0])
    input.value = ''
  }
}

function onDrop(e: DragEvent) {
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files?.length) {
    handleFile(files[0])
  }
}

function handleFile(file: File) {
  parseError.value = ''
  if (!file.name.endsWith('.json') && file.type !== 'application/json') {
    parseError.value = 'Please select a JSON file'
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = JSON.parse(e.target?.result as string)
      if (!validateExportData(data)) {
        parseError.value = 'Invalid export format. Expected "questions" array.'
        return
      }
      parsedData.value = data
      step.value = 2
    } catch {
      parseError.value = 'Failed to parse JSON file'
    }
  }
  reader.onerror = () => {
    parseError.value = 'Failed to read file'
  }
  reader.readAsText(file)
}

function validateExportData(data: unknown): data is ExportData {
  if (typeof data !== 'object' || data === null) return false
  const obj = data as Record<string, unknown>
  if (!Array.isArray(obj.questions)) return false
  return obj.questions.length > 0
}

function hasImage(q: ExportedQuestion): boolean {
  return q.questionBlocks?.some(b => b.type === 'image') || false
}

function getImageBlock(q: ExportedQuestion): { type: string; content: string } | undefined {
  return q.questionBlocks?.find(b => b.type === 'image')
}

function imageCountForQuestion(q: ExportedQuestion): number {
  return q.questionBlocks?.filter(b => b.type === 'image').length || 0
}

function getImageSrc(q: ExportedQuestion): string {
  const block = getImageBlock(q)
  if (!block) return ''
  if (block.content.startsWith('data:')) return block.content
  return `/api/files/${block.content}`
}

function onImageError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

function getQuestionText(q: ExportedQuestion): string {
  const textBlock = q.questionBlocks?.find(b => b.type === 'text')
  if (!textBlock) return '(no text)'
  return textBlock.content.replace(/<[^>]*>/g, '').trim() || '(no text)'
}

async function handleImport() {
  if (!parsedData.value) return
  importing.value = true
  try {
    const result = await questionsService.importQuestions(parsedData.value)
    importResult.value = result
    if (result.imported > 0) {
      emit('imported')
    }
  } catch {
    toast.error('Import failed')
  } finally {
    importing.value = false
  }
}

function handleClose() {
  emit('close')
}

function resetImport() {
  parsedData.value = null
  parseError.value = ''
  importResult.value = null
  importing.value = false
}
</script>
