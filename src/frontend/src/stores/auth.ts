import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'
import { decodeJwtPayload } from '@/utils/jwt'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const username = ref<string | null>(localStorage.getItem('username'))
  const role = ref<string | null>(localStorage.getItem('userRole'))
  const provider = ref<string | null>(localStorage.getItem('userProvider'))
  const userId = ref<number | null>(
    localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null
  )
  const firstName = ref<string | null>(localStorage.getItem('firstName') || null)
  const lastName = ref<string | null>(localStorage.getItem('lastName') || null)

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => role.value === 'ROLE_ADMIN')
  const isLocalAccount = computed(() => provider.value === 'local')
  const displayName = computed(() => {
    const full = [firstName.value, lastName.value].filter(Boolean).join(' ')
    return full || username.value || ''
  })

  async function login(usernameInput: string, password: string) {
    const response = await api.post('/auth/login', {
      username: usernameInput,
      password
    })
    const { accessToken: token, refreshToken } = response.data
    const payload = decodeJwtPayload(token)
    accessToken.value = token
    username.value = usernameInput
    role.value = (payload.role as string) ?? 'ROLE_MEMBER'
    provider.value = (payload.provider as string) ?? 'local'
    userId.value = payload.userId as number ?? null

    localStorage.setItem('accessToken', token)
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('username', usernameInput)
    localStorage.setItem('userRole', role.value)
    localStorage.setItem('userProvider', provider.value)
    if (userId.value != null) localStorage.setItem('userId', String(userId.value))
    await fetchProfile()
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } finally {
      accessToken.value = null
      username.value = null
      role.value = null
      provider.value = null
      userId.value = null
      firstName.value = null
      lastName.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userProvider')
      localStorage.removeItem('userId')
      localStorage.removeItem('firstName')
      localStorage.removeItem('lastName')
    }
  }

  /** Called after OAuth2 redirect — token already issued by auth-service */
  async function setTokenFromOAuth(token: string) {
    const payload = decodeJwtPayload(token)
    accessToken.value = token
    username.value = (payload.sub as string) ?? ''
    role.value = (payload.role as string) ?? 'ROLE_MEMBER'
    provider.value = (payload.provider as string) ?? 'google'
    userId.value = payload.userId as number ?? null

    localStorage.setItem('accessToken', token)
    localStorage.setItem('username', username.value)
    localStorage.setItem('userRole', role.value!)
    localStorage.setItem('userProvider', provider.value!)
    if (userId.value != null) localStorage.setItem('userId', String(userId.value))
    await fetchProfile()
  }

  async function fetchProfile() {
    try {
      const res = await api.get('/auth/me')
      firstName.value = res.data.firstName || null
      lastName.value = res.data.lastName || null
      localStorage.setItem('firstName', res.data.firstName || '')
      localStorage.setItem('lastName', res.data.lastName || '')
    } catch { /* ignore */ }
  }

  async function updateProfile(first: string, last: string) {
    await api.patch('/auth/profile', { firstName: first, lastName: last })
    firstName.value = first || null
    lastName.value = last || null
    localStorage.setItem('firstName', first)
    localStorage.setItem('lastName', last)
  }

  return { accessToken, username, firstName, lastName, displayName, role, provider, userId, isAuthenticated, isAdmin, isLocalAccount, login, logout, setTokenFromOAuth, fetchProfile, updateProfile }
})
