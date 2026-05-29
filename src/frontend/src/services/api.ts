import axios from 'axios'

const BASE = import.meta.env.BASE_URL // '/tg-quiz/'

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

function clearSession() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('username')
  window.location.href = `${BASE}login`
}

const api = axios.create({
  baseURL: '/',
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    if (isTokenExpired(token)) {
      clearSession()
      return Promise.reject(new axios.Cancel('Token expired'))
    }
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isCancel(error)) return Promise.reject(error)
    if (error.response?.status === 401) {
      clearSession()
    }
    return Promise.reject(error)
  }
)

export default api
