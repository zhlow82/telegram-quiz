<template>
  <div class="home-page" :class="{ 'sidebar-open': sidebarOpen }">

    <!-- Mobile top bar -->
    <header class="mobile-topbar">
      <button class="hamburger" @click="sidebarOpen = !sidebarOpen" aria-label="Toggle menu">
        <span></span><span></span><span></span>
      </button>
      <div class="mobile-brand">
        <div class="brand-dot">ZH</div>
        <span>ZH Template</span>
      </div>
    </header>

    <!-- Sidebar overlay (mobile) -->
    <div class="overlay" @click="sidebarOpen = false"></div>

    <!-- â”€â”€ Left sidebar â”€â”€ -->
    <aside class="sidebar">
      <!-- Brand -->
      <div class="sidebar-brand">
        <div class="brand-dot">ZH</div>
        <span class="brand-name">ZH Template</span>
      </div>

      <!-- User profile -->
      <div class="sidebar-profile">
        <div class="profile-avatar">{{ userInitial }}</div>
        <div class="profile-info">
          <span class="profile-name">{{ authStore.username }}</span>
          <span class="profile-role">Member</span>
        </div>
      </div>

      <div class="sidebar-divider"></div>

      <!-- Nav links -->
      <nav class="sidebar-nav">
        <a href="#" class="nav-item active">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          Home
        </a>
        <a href="#" class="nav-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Profile
        </a>
        <a href="#" class="nav-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93A10 10 0 0 0 4.93 19.07M4.93 4.93A10 10 0 0 0 19.07 19.07"/></svg>
          Settings
        </a>
      </nav>

      <div class="sidebar-spacer"></div>

      <!-- Logout -->
      <button @click="handleLogout" :disabled="loading" class="btn-logout">
        <svg v-if="!loading" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        <span class="spinner-sm" v-else></span>
        {{ loading ? 'Logging out…' : 'Log out' }}
      </button>
    </aside>

    <!-- â”€â”€ Main content â”€â”€ -->
    <main class="content">
      <div class="feed">

        <!-- Welcome card -->
        <div class="card welcome-card">
          <div class="welcome-header">
            <div class="welcome-avatar">{{ userInitial }}</div>
            <div>
              <h2 class="welcome-title">Welcome back, <span class="highlight">{{ authStore.username }}</span> 👋</h2>
              <p class="welcome-sub">You're securely signed in.</p>
            </div>
          </div>
        </div>

        <!-- Server message card -->
        <div class="card message-card">
          <h3 class="card-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#1877f2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            Server Message
          </h3>
          <p v-if="message" class="message-text">{{ message }}</p>
          <div v-else class="skeleton">
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>

        <!-- Stats row -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-icon green">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            </div>
            <div>
              <div class="stat-label">Status</div>
              <div class="stat-value">Active</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon blue">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            </div>
            <div>
              <div class="stat-label">Auth</div>
              <div class="stat-value">JWT Secured</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon purple">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
            </div>
            <div>
              <div class="stat-label">Database</div>
              <div class="stat-value">PostgreSQL</div>
            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

const message = ref('')
const loading = ref(false)
const sidebarOpen = ref(false)

const userInitial = computed(() =>
  authStore.username ? authStore.username.charAt(0).toUpperCase() : '?'
)

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
* { box-sizing: border-box; }

/* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
   Layout: sidebar + main area
â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */
.home-page {
  display: flex;
  min-height: 100vh;
  background: #f3f4f6;
}

/* â”€â”€ Mobile top bar (hidden on desktop) â”€â”€ */
.mobile-topbar {
  display: none;
}

/* â”€â”€ Sidebar â”€â”€ */
.sidebar {
  width: 260px;
  min-height: 100vh;
  background: #1e293b;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  position: sticky;
  top: 0;
  flex-shrink: 0;
}

/* Brand */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0 0.5rem 1.5rem;
}

.brand-dot {
  width: 36px;
  height: 36px;
  background: #2563eb;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 900;
  font-size: 0.875rem;
  flex-shrink: 0;
}

.brand-name {
  color: #fff;
  font-size: 1.0625rem;
  font-weight: 700;
}

/* Profile */
.sidebar-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: rgba(255,255,255,0.07);
  border-radius: 10px;
  padding: 0.75rem 0.875rem;
  margin-bottom: 1rem;
}

.profile-avatar {
  width: 38px;
  height: 38px;
  background: #2563eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 1rem;
  flex-shrink: 0;
}

.profile-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.profile-name {
  color: #f1f5f9;
  font-size: 0.875rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.profile-role {
  color: #94a3b8;
  font-size: 0.75rem;
}

/* Divider */
.sidebar-divider {
  height: 1px;
  background: rgba(255,255,255,0.08);
  margin: 0.25rem 0 0.75rem;
}

/* Nav links */
.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0.875rem;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 0.9375rem;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.07);
  color: #f1f5f9;
}

.nav-item.active {
  background: rgba(37,99,235,0.2);
  color: #60a5fa;
}

/* Spacer pushes logout to bottom */
.sidebar-spacer {
  flex: 1;
}

/* Logout */
.btn-logout {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  width: 100%;
  padding: 0.65rem 0.875rem;
  background: rgba(239,68,68,0.12);
  color: #fca5a5;
  border: 1px solid rgba(239,68,68,0.2);
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  font-family: inherit;
}

.btn-logout:hover:not(:disabled) {
  background: rgba(239,68,68,0.22);
}

.btn-logout:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.spinner-sm {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(252,165,165,0.4);
  border-top-color: #fca5a5;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* â”€â”€ Main content â”€â”€ */
.content {
  flex: 1;
  padding: 2rem 1.5rem;
  min-width: 0;
  overflow-y: auto;
}

.feed {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* â”€â”€ Cards â”€â”€ */
.card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.07), 0 1px 8px rgba(0,0,0,0.04);
  padding: 1.5rem;
}

/* Welcome card */
.welcome-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.welcome-avatar {
  width: 52px;
  height: 52px;
  background: #2563eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.375rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(37,99,235,0.3);
}

.welcome-title {
  margin: 0 0 0.2rem;
  font-size: 1.125rem;
  color: #111827;
  font-weight: 600;
}

.highlight { color: #2563eb; }

.welcome-sub {
  margin: 0;
  font-size: 0.875rem;
  color: #6b7280;
}

/* Message card */
.message-card .card-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 0.875rem;
  font-size: 0.9375rem;
  color: #111827;
  font-weight: 600;
}

.message-text {
  margin: 0;
  color: #374151;
  font-size: 0.9375rem;
  line-height: 1.6;
}

/* Skeleton */
.skeleton { display: flex; flex-direction: column; gap: 0.5rem; }

.skeleton-line {
  height: 14px;
  background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.4s ease infinite;
}

.skeleton-line.short { width: 55%; }

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Stats row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.875rem;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.07);
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.875rem;
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.green  { background: #dcfce7; color: #16a34a; }
.stat-icon.blue   { background: #dbeafe; color: #2563eb; }
.stat-icon.purple { background: #ede9fe; color: #7c3aed; }

.stat-label {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  font-size: 0.9375rem;
  color: #111827;
  font-weight: 600;
  margin-top: 0.1rem;
}

/* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
   Mobile: drawer sidebar
â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */
@media (max-width: 768px) {
  /* Show mobile topbar */
  .mobile-topbar {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 200;
    height: 52px;
    background: #1e293b;
    padding: 0 1rem;
    box-shadow: 0 2px 8px rgba(0,0,0,0.25);
  }

  .mobile-brand {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #fff;
    font-size: 1rem;
    font-weight: 700;
  }

  /* Hamburger */
  .hamburger {
    display: flex;
    flex-direction: column;
    gap: 4px;
    background: none;
    border: none;
    cursor: pointer;
    padding: 4px;
  }

  .hamburger span {
    display: block;
    width: 22px;
    height: 2px;
    background: #fff;
    border-radius: 2px;
    transition: all 0.2s;
  }

  /* Overlay */
  .overlay {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.45);
    z-index: 150;
  }

  .sidebar-open .overlay {
    display: block;
  }

  /* Sidebar slides in */
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100%;
    z-index: 160;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    min-height: 100%;
    padding-top: 1.25rem;
  }

  .sidebar-open .sidebar {
    transform: translateX(0);
  }

  /* Content gets top padding for the mobile bar */
  .content {
    padding-top: calc(52px + 1.25rem);
  }

  /* Stats stack vertically */
  .stats-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .content {
    padding: calc(52px + 1rem) 0.875rem 1.5rem;
  }

  .card {
    padding: 1.125rem 1rem;
  }
}
</style>

