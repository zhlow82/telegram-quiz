<template>
  <AppLayout>
    <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">
      <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
        <KeyRound class="w-5 h-5 text-white" />
      </div>
      <div class="flex-1 min-w-0">
        <h1 class="text-2xl font-black text-slate-900 leading-tight">Invitation Codes</h1>
        <p class="text-sm text-slate-500 mt-0.5">Generate codes for new Google sign-ups</p>
      </div>
      <button
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
        :disabled="generating"
        @click="generate"
      >
        <Plus class="w-4 h-4" />
        Generate Code
      </button>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div v-if="loading" class="divide-y divide-slate-100">
        <div v-for="i in 4" :key="i" class="flex items-center gap-4 px-5 py-4">
          <div class="h-4 bg-slate-100 rounded animate-pulse w-40"></div>
          <div class="h-4 bg-slate-100 rounded animate-pulse w-24"></div>
          <div class="h-4 bg-slate-100 rounded animate-pulse w-32"></div>
          <div class="h-5 bg-slate-100 rounded-full animate-pulse w-16"></div>
          <div class="h-5 bg-slate-100 rounded-full animate-pulse w-10 ml-auto"></div>
          <div class="h-6 bg-slate-100 rounded animate-pulse w-8"></div>
        </div>
      </div>
      <div v-else-if="codes.length === 0" class="p-8 text-center text-slate-400">No invitation codes yet. Generate one above.</div>
      <table v-else class="w-full text-sm">
        <thead>
          <tr class="bg-white border-b-2 border-slate-200">
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">CODE</th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">CREATED BY</th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">CREATED AT</th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">STATUS</th>
            <th class="px-4 py-3 text-right text-xs font-semibold text-slate-600 uppercase tracking-wider">ACTIVE</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="c in codes"
            :key="c.id"
            class="border-b border-slate-50 hover:bg-slate-50 transition-colors"
          >
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <code class="font-mono font-bold text-slate-900 tracking-widest">{{ c.code }}</code>
                <button
                  class="text-slate-400 hover:text-blue-600 transition-colors cursor-pointer"
                  title="Copy"
                  @click="copy(c.code)"
                >
                  <Copy class="w-3.5 h-3.5" />
                </button>
              </div>
            </td>
            <td class="px-4 py-3 text-slate-600">{{ c.createdBy }}</td>
            <td class="px-4 py-3 text-slate-500">{{ formatDate(c.createdAt) }}</td>
            <td class="px-4 py-3">
              <span
                :class="[
                  'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold',
                  c.active ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500 line-through'
                ]"
              >{{ c.active ? 'Active' : 'Inactive' }}</span>
            </td>
            <td class="px-4 py-3 text-right">
              <button
                role="switch"
                :aria-checked="c.active"
                class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none"
                :class="c.active ? 'bg-blue-600' : 'bg-slate-200'"
                @click="toggleCode(c)"
              >
                <span
                  class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200"
                  :class="c.active ? 'translate-x-5' : 'translate-x-0'"
                />
              </button>
            </td>
            <td class="px-4 py-3 text-right">
              <button
                class="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition cursor-pointer"
                title="Delete permanently"
                @click="confirmDelete(c)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Delete confirmation dialog -->
    <AppDialog
      :visible="deleteDialogVisible"
      type="confirm"
      title="Delete Invitation Code"
      :message="`Permanently delete code ${deletingCode?.code}? This cannot be undone.`"
      @confirm="doDelete"
      @cancel="deleteDialogVisible = false"
    />
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { KeyRound, Plus, Copy, Trash2 } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import AppDialog from '@/components/AppDialog.vue'
import { adminService } from '@/services/adminService'
import { useToast } from '@/composables/useToast'
import type { InvitationCodeResponse } from '@/types/admin'

const toast = useToast()
const codes = ref<InvitationCodeResponse[]>([])
const loading = ref(false)
const generating = ref(false)
const deleteDialogVisible = ref(false)
const deletingCode = ref<InvitationCodeResponse | null>(null)

function confirmDelete(c: InvitationCodeResponse) {
  deletingCode.value = c
  deleteDialogVisible.value = true
}

async function doDelete() {
  if (!deletingCode.value) return
  deleteDialogVisible.value = false
  await adminService.deleteCode(deletingCode.value.id)
  codes.value = codes.value.filter(c => c.id !== deletingCode.value!.id)
  deletingCode.value = null
  toast.success('Invitation code deleted')
}

async function loadCodes() {
  loading.value = true
  try {
    codes.value = await adminService.listCodes()
  } finally {
    loading.value = false
  }
}

async function generate() {
  generating.value = true
  try {
    const newCode = await adminService.generateCode()
    codes.value.unshift(newCode)
    toast.success('Invitation code generated')
  } finally {
    generating.value = false
  }
}

async function toggleCode(c: InvitationCodeResponse) {
  if (c.active) {
    await adminService.deactivateCode(c.id)
    c.active = false
  } else {
    await adminService.activateCode(c.id)
    c.active = true
  }
}

function copy(code: string) {
  navigator.clipboard.writeText(code)
  toast.info('Copied to clipboard')
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleString()
}

onMounted(loadCodes)
</script>
