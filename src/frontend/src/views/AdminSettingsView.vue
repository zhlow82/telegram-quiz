<template>
  <AppLayout>
    <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">
      <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
        <Settings class="w-5 h-5 text-white" />
      </div>
      <div class="flex-1 min-w-0">
        <h1 class="text-2xl font-black text-slate-900 leading-tight">Settings</h1>
        <p class="text-sm text-slate-500 mt-0.5">Configure application integrations and branding</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex items-center gap-1 mb-5 bg-slate-100 rounded-xl p-1 w-fit">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-colors cursor-pointer"
        :class="activeTab === tab.id
          ? 'bg-white text-slate-900 shadow-sm'
          : 'text-slate-500 hover:text-slate-700'"
        @click="activeTab = tab.id"
      >
        <component :is="tab.icon" class="w-4 h-4" />
        {{ tab.label }}
      </button>
    </div>

    <!-- Google Sign-In Tab -->
    <div v-if="activeTab === 'google'" class="bg-white rounded-xl border border-slate-200 p-6">
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
            :placeholder="configured ? '...' : 'GOCSPX-...'"
          />
        </div>
        <p v-if="googleError" class="text-xs text-red-500">{{ googleError }}</p>
        <p v-if="googleSaved" class="text-xs text-green-600">Settings saved successfully.</p>
        <div class="flex justify-end pt-1">
          <button
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
            :disabled="saving"
            @click="save"
          >
            {{ saving ? 'Saving...' : 'Save' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Branding Tab -->
    <div v-if="activeTab === 'branding'" class="bg-white rounded-xl border border-slate-200 p-6">
      <div class="flex items-center gap-3 mb-5">
        <div class="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center flex-shrink-0">
          <Paintbrush class="w-4 h-4 text-slate-600" />
        </div>
        <h2 class="text-base font-bold text-slate-900">Branding</h2>
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">App Name</label>
          <input
            v-model="branding.appName"
            type="text"
            maxlength="60"
            placeholder="Telegram Quiz"
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p class="text-xs text-slate-400 mt-1">Shown in the sidebar and login page</p>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">Login Welcome Text</label>
          <textarea
            v-model="branding.loginWelcomeText"
            rows="2"
            maxlength="200"
            placeholder="Create and manage engaging quiz games for your Telegram community."
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
          <p class="text-xs text-slate-400 mt-1">Subtitle shown on the left panel of the login page</p>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-2">Logo</label>
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-xl bg-slate-100 border border-slate-200 flex items-center justify-center overflow-hidden flex-shrink-0">
              <img v-if="branding.logoPreviewUrl" :src="branding.logoPreviewUrl" class="w-full h-full object-cover" alt="Logo preview" />
              <ImageIcon v-else class="w-6 h-6 text-slate-300" />
            </div>
            <div class="flex-1 space-y-2">
              <label class="inline-flex items-center gap-2 px-3 py-2 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-50 transition cursor-pointer">
                <Upload class="w-4 h-4" />
                {{ branding.uploading ? 'Uploading...' : 'Upload image' }}
                <input
                  type="file"
                  accept="image/*"
                  class="hidden"
                  :disabled="branding.uploading"
                  @change="uploadLogo"
                />
              </label>
              <button
                v-if="branding.logoPreviewUrl"
                type="button"
                class="block text-xs text-red-500 hover:text-red-700 cursor-pointer transition"
                @click="removeLogo"
              >Remove logo</button>
            </div>
          </div>
          <p class="text-xs text-slate-400 mt-2">PNG or JPG, shown in sidebar and login page. Falls back to icon if not set.</p>
        </div>

        <p v-if="brandingError" class="text-xs text-red-500">{{ brandingError }}</p>
        <p v-if="brandingSaved" class="text-xs text-green-600">Branding saved successfully.</p>
        <div class="flex justify-end pt-1">
          <button
            class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
            :disabled="branding.saving"
            @click="saveBranding"
          >
            {{ branding.saving ? 'Saving...' : 'Save Branding' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Settings, CheckCircle, AlertCircle, Paintbrush, Upload, Image as ImageIcon, KeyRound, Palette } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { adminService } from '@/services/adminService'
import { useBrandingStore } from '@/stores/branding'
import api from '@/services/api'

const brandingStore = useBrandingStore()

const tabs = [
  { id: 'google', label: 'Google Sign-In', icon: KeyRound },
  { id: 'branding', label: 'Branding', icon: Palette },
]
const activeTab = ref('google')

// ── Branding ─────────────────────────────────────────────────────────────────
const branding = ref({
  appName: '',
  loginWelcomeText: '',
  logoBlobId: null as string | null,
  logoPreviewUrl: null as string | null,
  saving: false,
  uploading: false,
})
const brandingError = ref('')
const brandingSaved = ref(false)

async function loadBranding() {
  const data = await adminService.getBrandingSettings()
  branding.value.appName = data.appName || ''
  branding.value.loginWelcomeText = data.loginWelcomeText || ''
  branding.value.logoPreviewUrl = data.appLogoUrl || null
  if (data.appLogoUrl) {
    branding.value.logoBlobId = data.appLogoUrl.split('/').pop() ?? null
  }
}

async function uploadLogo(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  branding.value.uploading = true
  brandingError.value = ''
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await api.post('/api/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    branding.value.logoBlobId = res.data.path
    branding.value.logoPreviewUrl = `/api/files/${res.data.path}`
  } catch {
    brandingError.value = 'Failed to upload image.'
  } finally {
    branding.value.uploading = false
  }
}

function removeLogo() {
  branding.value.logoBlobId = ''
  branding.value.logoPreviewUrl = null
}

async function saveBranding() {
  brandingError.value = ''
  brandingSaved.value = false
  branding.value.saving = true
  try {
    await adminService.saveBrandingSettings({
      appName: branding.value.appName.trim() || 'Telegram Quiz',
      loginWelcomeText: branding.value.loginWelcomeText.trim(),
      appLogoBlobId: branding.value.logoBlobId,
    })
    brandingSaved.value = true
    await brandingStore.refresh()
    setTimeout(() => { brandingSaved.value = false }, 2500)
  } catch {
    brandingError.value = 'Failed to save branding.'
  } finally {
    branding.value.saving = false
  }
}

// ── Google OAuth ──────────────────────────────────────────────────────────────
const configured = ref(false)
const saving = ref(false)
const googleSaved = ref(false)
const googleError = ref('')
const form = ref({ clientId: '', clientSecret: '' })

async function loadGoogle() {
  const data = await adminService.getGoogleSettings()
  configured.value = data.secretConfigured
  form.value.clientId = data.clientId ?? ''
}

async function save() {
  googleError.value = ''
  googleSaved.value = false
  if (!form.value.clientId.trim()) {
    googleError.value = 'Client ID is required.'
    return
  }
  if (!configured.value && !form.value.clientSecret.trim()) {
    googleError.value = 'Client Secret is required for initial setup.'
    return
  }
  saving.value = true
  try {
    await adminService.saveGoogleSettings(form.value.clientId, form.value.clientSecret)
    configured.value = true
    form.value.clientSecret = ''
    googleSaved.value = true
  } catch (e: any) {
    googleError.value = e.response?.data?.message ?? 'Failed to save settings.'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadBranding(), loadGoogle()])
})
</script>
