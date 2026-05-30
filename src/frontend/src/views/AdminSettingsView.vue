<template>
  <AppLayout>
    <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">
      <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
        <Settings class="w-5 h-5 text-white" />
      </div>
      <div class="flex-1 min-w-0">
        <h1 class="text-2xl font-black text-slate-900 leading-tight">Settings</h1>
        <p class="text-sm text-slate-500 mt-0.5">Configure application integrations</p>
      </div>
    </div>

    <!-- Google OAuth section -->
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <div class="flex items-center gap-3 mb-5">
        <img src="https://www.google.com/favicon.ico" class="w-5 h-5" alt="Google" />
        <h2 class="text-base font-bold text-slate-900">Google Sign-In</h2>
        <span
          :class="[
            'ml-auto inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold',
            configured ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
          ]"
        >
          <component :is="configured ? CheckCircle : AlertCircle" class="w-3 h-3" />
          {{ configured ? 'Configured' : 'Not configured' }}
        </span>
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">Client ID</label>
          <input
            v-model="form.clientId"
            type="text"
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="xxxx.apps.googleusercontent.com"
          />
        </div>
        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">
            Client Secret
            <span v-if="configured && !form.clientSecret" class="ml-1 font-normal text-slate-400">(leave blank to keep existing)</span>
          </label>
          <input
            v-model="form.clientSecret"
            type="password"
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-blue-500"
            :placeholder="configured ? '••••••••••••••••' : 'GOCSPX-...'"
          />
        </div>
        <p v-if="error" class="text-xs text-red-500">{{ error }}</p>
        <p v-if="saved" class="text-xs text-green-600">Settings saved successfully.</p>
        <div class="flex justify-end pt-1">
          <button
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
            :disabled="saving"
            @click="save"
          >
            {{ saving ? 'Saving…' : 'Save' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Settings, CheckCircle, AlertCircle } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { adminService } from '@/services/adminService'

const configured = ref(false)
const saving = ref(false)
const saved = ref(false)
const error = ref('')
const form = ref({ clientId: '', clientSecret: '' })

async function load() {
  const res = await adminService.getGoogleSettings()
  configured.value = res.data.secretConfigured
  form.value.clientId = res.data.clientId ?? ''
}

async function save() {
  error.value = ''
  saved.value = false
  if (!form.value.clientId.trim()) {
    error.value = 'Client ID is required.'
    return
  }
  if (!configured.value && !form.value.clientSecret.trim()) {
    error.value = 'Client Secret is required for initial setup.'
    return
  }
  saving.value = true
  try {
    await adminService.saveGoogleSettings(form.value.clientId, form.value.clientSecret)
    configured.value = true
    form.value.clientSecret = ''
    saved.value = true
  } catch (e: any) {
    error.value = e.response?.data?.message ?? 'Failed to save settings.'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
