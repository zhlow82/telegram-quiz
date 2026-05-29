<template>
  <div class="login-page">

    <!-- Top-left logo -->
    <div class="page-logo">
      <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect width="32" height="32" rx="8" fill="#2563eb"/>
        <path d="M8 16L14 10L20 16L14 22L8 16Z" fill="white" opacity="0.9"/>
        <path d="M14 10L20 16L26 10L20 4L14 10Z" fill="white"/>
      </svg>
      <span class="logo-text">Telegram Quiz</span>
    </div>

    <!-- Centered card -->
    <div class="login-card">
      <p class="card-sub">Please enter your details</p>
      <h1 class="card-title">Welcome back</h1>

      <form @submit.prevent="handleLogin">
        <div class="field-wrap">
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="Username"
            required
            autocomplete="username"
            class="field"
          />
        </div>

        <div class="field-wrap">
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Password"
            required
            autocomplete="current-password"
            class="field"
          />
        </div>

        <div class="form-row">
          <label class="remember-label">
            <input type="checkbox" v-model="remember" class="checkbox" />
            Remember for 30 days
          </label>
          <a href="#" class="forgot-link">Forgot password</a>
        </div>

        <transition name="fade">
          <p v-if="error" class="error">{{ error }}</p>
        </transition>

        <button type="submit" :disabled="loading" class="btn-primary">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? 'Signing inâ€¦' : 'Sign in' }}
        </button>

        <button type="button" class="btn-google">
          <!-- Google "G" SVG -->
          <svg width="20" height="20" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
            <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
            <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
            <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
            <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
            <path fill="none" d="M0 0h48v48H0z"/>
          </svg>
          Sign in with Google
        </button>

        <p class="signup-row">
          Don't have an account?&nbsp;<a href="#" class="signup-link">Sign up</a>
        </p>
      </form>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)
const remember = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(form.value.username, form.value.password)
    router.push('/home')
  } catch {
    error.value = 'Invalid username or password'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }

/* â”€â”€ Page â”€â”€ */
.login-page {
  min-height: 100vh;
  background: #f3f4f6;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem 1rem 3rem;
}

/* â”€â”€ Top-left logo â”€â”€ */
.page-logo {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 2.5rem;
}

.logo-text {
  font-size: 1.125rem;
  font-weight: 700;
  color: #111827;
}

/* â”€â”€ Card â”€â”€ */
.login-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08), 0 4px 20px rgba(0,0,0,0.06);
  padding: 2.25rem 2rem 2rem;
  width: 100%;
  max-width: 460px;
}

.card-sub {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.35rem;
}

.card-title {
  font-size: 1.875rem;
  font-weight: 800;
  color: #111827;
  margin: 0 0 1.75rem;
  line-height: 1.2;
}

/* â”€â”€ Fields â”€â”€ */
.field-wrap {
  margin-bottom: 1rem;
}

.field {
  display: block;
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1.5px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.9375rem;
  color: #111827;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
  font-family: inherit;
}

.field::placeholder {
  color: #9ca3af;
}

.field:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

/* â”€â”€ Remember + forgot row â”€â”€ */
.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #374151;
  cursor: pointer;
  user-select: none;
}

.checkbox {
  width: 15px;
  height: 15px;
  accent-color: #2563eb;
  cursor: pointer;
  flex-shrink: 0;
}

.forgot-link {
  font-size: 0.875rem;
  font-weight: 600;
  color: #2563eb;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

/* â”€â”€ Buttons â”€â”€ */
.btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.8rem;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.9375rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 0.875rem;
  font-family: inherit;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-google {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  width: 100%;
  padding: 0.75rem;
  background: #fff;
  color: #374151;
  border: 1.5px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  font-family: inherit;
  margin-bottom: 1.5rem;
}

.btn-google:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

/* â”€â”€ Spinner â”€â”€ */
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.45);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* â”€â”€ Error â”€â”€ */
.error {
  color: #dc2626;
  font-size: 0.875rem;
  margin: 0 0 0.875rem;
  padding: 0.5rem 0.75rem;
  background: #fef2f2;
  border-radius: 6px;
  border: 1px solid #fecaca;
}

/* â”€â”€ Sign-up row â”€â”€ */
.signup-row {
  text-align: center;
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.signup-link {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

.signup-link:hover {
  text-decoration: underline;
}

/* â”€â”€ Fade transition â”€â”€ */
.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

/* â”€â”€ Mobile â”€â”€ */
@media (max-width: 520px) {
  .login-page {
    padding: 1.25rem 0.75rem 2rem;
  }

  .login-card {
    padding: 1.75rem 1.25rem 1.5rem;
    border-radius: 10px;
  }

  .card-title {
    font-size: 1.5rem;
  }
}
</style>

