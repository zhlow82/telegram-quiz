<template>
  <div class="home-container">
    <header class="header">
      <h1>ZH Template</h1>
      <div class="user-info">
        <span class="welcome-text">Welcome, <strong>{{ authStore.username }}</strong></span>
        <button @click="handleLogout" :disabled="loading" class="logout-btn">
          {{ loading ? 'Logging out...' : 'Logout' }}
        </button>
      </div>
    </header>

    <main class="content">
      <div class="welcome-card">
        <h2>Home</h2>
        <p v-if="message">{{ message }}</p>
        <p v-else class="loading-text">Loading...</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const message = ref('')
const loading = ref(false)

onMounted(async () => {
  try {
    const response = await api.get('/api/home')
    message.value = response.data.message
  } catch {
    message.value = 'Failed to load data. Please try again.'
  }
})

async function handleLogout() {
  loading.value = true
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f0f2f5;
}

.header {
  background: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header h1 {
  margin: 0;
  color: #1a1a2e;
  font-size: 1.5rem;
  font-weight: 700;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.welcome-text {
  color: #555;
  font-size: 0.925rem;
}

.logout-btn {
  padding: 0.45rem 1rem;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover:not(:disabled) {
  background: #dc2626;
}

.logout-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.content {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.welcome-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.welcome-card h2 {
  margin-top: 0;
  color: #1a1a2e;
}

.welcome-card p {
  color: #555;
  font-size: 1rem;
  margin-bottom: 0;
}

.loading-text {
  color: #999;
}
</style>
