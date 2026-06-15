<template>
  <teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="fixed inset-0 bg-black/50" />
      <div class="relative z-10 bg-white rounded-2xl shadow-xl max-w-md w-full mx-4 p-6">
        <div class="flex items-center justify-between mb-5">
          <h2 class="text-lg font-bold text-slate-900">Export Questions</h2>
          <button
            class="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-400 hover:text-slate-600 transition cursor-pointer"
            @click="emit('close')"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-4">
          <div class="bg-blue-50 rounded-xl p-4 flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center flex-shrink-0">
              <FileJson class="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p class="text-sm font-semibold text-slate-900">{{ selectedCount }} question{{ selectedCount !== 1 ? 's' : '' }} selected</p>
              <p class="text-xs text-slate-500">Export as JSON file</p>
            </div>
          </div>

          <label class="flex items-center gap-3 p-3 rounded-xl border border-slate-200 hover:bg-slate-50 cursor-pointer transition">
            <input
              v-model="includeImages"
              type="checkbox"
              class="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
            />
            <div class="flex-1">
              <p class="text-sm font-medium text-slate-700">Include images</p>
              <p class="text-xs text-slate-500">Embed images as base64 (larger file size)</p>
            </div>
          </label>

          <div v-if="exporting" class="flex items-center gap-3 p-3">
            <Loader2 class="w-4 h-4 animate-spin text-blue-600" />
            <span class="text-sm text-slate-600">Preparing export...</span>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button
            class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition cursor-pointer"
            :disabled="exporting"
            @click="emit('close')"
          >
            Cancel
          </button>
          <button
            class="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="exporting"
            @click="handleExport"
          >
            <Download class="w-4 h-4" />
            Export
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { X, FileJson, Download, Loader2 } from '@lucide/vue'
import { questionsService } from '@/services/questionsService'
import { useToast } from '@/composables/useToast'
import type { ExportData } from '@/types/question'

const props = defineProps<{
  visible: boolean
  selectedCount: number
  questionIds: number[]
}>()

const emit = defineEmits<{
  close: []
  exported: []
}>()

const toast = useToast()
const includeImages = ref(true)
const exporting = ref(false)

async function handleExport() {
  exporting.value = true
  try {
    const data = await questionsService.exportQuestions(props.questionIds, includeImages.value)
    downloadJson(data, `quiz-export-${new Date().toISOString().slice(0, 10)}.json`)
    toast.success(`Exported ${props.selectedCount} question${props.selectedCount !== 1 ? 's' : ''}`)
    emit('exported')
    emit('close')
  } catch {
    toast.error('Export failed')
  } finally {
    exporting.value = false
  }
}

function downloadJson(data: ExportData, filename: string) {
  const json = JSON.stringify(data, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>
