import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const username = ref<string | null>(localStorage.getItem('username'))

  const isAuthenticated = computed(() => !!accessToken.value)

  async function login(usernameInput: string, password: string) {
    const response = await api.post('/auth/login', {
      username: usernameInput,
      password
    })
    const { accessToken: token } = response.data
    accessToken.value = token
    username.value = usernameInput
    localStorage.setItem('accessToken', token)
    localStorage.setItem('username', usernameInput)
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } finally {
      accessToken.value = null
      username.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('username')
    }
  }

  return { accessToken, username, isAuthenticated, login, logout }
})
