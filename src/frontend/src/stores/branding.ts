import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'

export const useBrandingStore = defineStore('branding', () => {
  const appName = ref('Telegram Quiz')
  const loginWelcomeText = ref('')
  const appLogoUrl = ref<string | null>(null)
  const loaded = ref(false)

  async function load() {
    if (loaded.value) return
    try {
      const res = await api.get('/auth/settings/branding')
      appName.value = res.data.appName || 'Telegram Quiz'
      loginWelcomeText.value = res.data.loginWelcomeText || ''
      appLogoUrl.value = res.data.appLogoUrl || null
      loaded.value = true
    } catch {
      // use defaults silently
    }
  }

  async function refresh() {
    loaded.value = false
    await load()
  }

  return { appName, loginWelcomeText, appLogoUrl, load, refresh }
})
