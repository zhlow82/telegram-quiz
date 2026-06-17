<template>
  <div class="flex h-screen bg-slate-50 overflow-hidden">

    <!-- Skip navigation link -->
    <a
      href="#main-content"
      class="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-[100] focus:px-4 focus:py-2 focus:bg-blue-600 focus:text-white focus:rounded-lg focus:text-sm focus:font-semibold focus:outline-none"
    >
      Skip to content
    </a>

    <!-- Mobile backdrop -->
    <transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="drawerOpen"
        class="fixed inset-0 bg-black/50 z-30 md:hidden"
        @click="drawerOpen = false"
      />
    </transition>

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-40 flex-shrink-0 bg-slate-900 flex flex-col transition-all duration-200 ease-in-out',
        sidebarCollapsed ? 'w-16' : 'w-64',
        'md:static md:z-auto md:translate-x-0',
        drawerOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0',
      ]"
    >
      <!-- Brand -->
      <div class="flex items-center gap-3 px-4 py-5 flex-shrink-0">
        <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 overflow-hidden bg-blue-600">
          <img v-if="brandingStore.appLogoUrl" :src="brandingStore.appLogoUrl" class="w-full h-full object-cover" alt="" />
          <Zap v-else class="w-4 h-4 text-white" />
        </div>
        <span v-if="!sidebarCollapsed" class="text-white font-bold text-sm">{{ brandingStore.appName }}</span>
        <button
          v-if="!sidebarCollapsed"
          class="hidden md:flex ml-auto w-6 h-6 rounded-lg items-center justify-center text-slate-500 hover:text-white hover:bg-white/10 transition cursor-pointer"
          @click="sidebarCollapsed = true"
        >
          <ChevronLeft class="w-4 h-4" />
        </button>
      </div>

      <button
        v-if="sidebarCollapsed"
        class="mx-auto mb-3 w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:text-white hover:bg-white/10 transition cursor-pointer"
        @click="sidebarCollapsed = false"
        title="Expand sidebar"
      >
        <ChevronRight class="w-4 h-4" />
      </button>

      <div class="border-t border-white/5" />

      <!-- Nav -->
      <nav class="flex-1 px-3 pt-4 overflow-y-auto">
        <p
          v-if="!sidebarCollapsed"
          class="px-3 mb-2 text-xs font-semibold uppercase"
          style="color: rgba(148,163,184,0.6); letter-spacing: 0.08em"
        >Main</p>
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 px-3 py-2 rounded-lg mb-1 text-sm font-medium text-slate-400 hover:text-white hover:bg-white/5 transition-colors no-underline"
          :class="sidebarCollapsed ? 'justify-center' : ''"
          active-class="!text-white !bg-white/10"
          :title="sidebarCollapsed ? item.label : ''"
          @click="drawerOpen = false"
        >
          <component :is="item.icon" class="w-4 h-4 flex-shrink-0" />
          <span v-if="!sidebarCollapsed">{{ item.label }}</span>
        </router-link>

        <template v-if="authStore.isAdmin">
          <div class="border-t border-white/5 my-3" />
          <p
            v-if="!sidebarCollapsed"
            class="px-3 mb-2 text-xs font-semibold uppercase"
            style="color: rgba(148,163,184,0.6); letter-spacing: 0.08em"
          >Admin</p>
          <router-link
            v-for="item in adminNavItems"
            :key="item.to"
            :to="item.to"
            class="flex items-center gap-3 px-3 py-2 rounded-lg mb-1 text-sm font-medium text-slate-400 hover:text-white hover:bg-white/5 transition-colors no-underline"
            :class="sidebarCollapsed ? 'justify-center' : ''"
            active-class="!text-white !bg-white/10"
            :title="sidebarCollapsed ? item.label : ''"
            @click="drawerOpen = false"
          >
            <component :is="item.icon" class="w-4 h-4 flex-shrink-0" />
            <span v-if="!sidebarCollapsed">{{ item.label }}</span>
          </router-link>
        </template>
      </nav>

      <!-- User section -->
      <div class="flex-shrink-0 border-t border-white/5 p-3">
        <button
          class="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-xl mb-2 text-left transition-colors hover:bg-white/10 cursor-pointer"
          :class="sidebarCollapsed ? 'justify-center px-0' : ''"
          style="background: rgba(255,255,255,0.06)"
          title="My Profile"
          @click="openProfile"
        >
          <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
            {{ userInitial }}
          </div>
          <template v-if="!sidebarCollapsed">
            <div class="min-w-0 flex-1">
              <div class="text-white text-sm font-semibold truncate">{{ authStore.firstName || authStore.username }}</div>
              <div class="text-xs text-slate-400">{{ authStore.isAdmin ? 'Admin' : 'Member' }}</div>
            </div>
            <Pencil class="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
          </template>
        </button>
        <button
          class="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg text-sm text-slate-400 hover:text-white hover:bg-white/5 transition-colors disabled:opacity-50 cursor-pointer"
          :disabled="loading"
          @click="handleLogout"
        >
          <LogOut class="w-4 h-4" />
          <span v-if="!sidebarCollapsed">Sign out</span>
        </button>
      </div>
    </aside>

    <!-- My Profile Modal -->
    <teleport to="body">
      <div
        v-if="showProfile"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        @keydown.escape="closeProfile"
      >
        <div class="fixed inset-0 bg-black/50" />
      <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 overflow-y-auto max-h-[90vh]">
          <button
            type="button"
            class="absolute top-4 right-4 w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            aria-label="Close"
            @click="closeProfile"
          ><X class="w-4 h-4" /></button>
          <h2 class="text-lg font-black text-slate-900 mb-1">My Profile</h2>
          <p class="text-sm text-slate-500 mb-5">Update your name.</p>

          <div class="space-y-3">
            <div>
              <label class="block text-xs font-semibold text-slate-600 mb-1">First Name</label>
              <input
                v-model="profileForm.firstName"
                type="text"
                class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="First name"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 mb-1">Last Name</label>
              <input
                v-model="profileForm.lastName"
                type="text"
                class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Last name"
              />
            </div>
            <p v-if="profileError" class="text-xs text-red-500">{{ profileError }}</p>
            <p v-if="profileSuccess" class="text-xs text-green-600">Saved!</p>
          </div>
          <div class="flex gap-3 mt-4 mb-6">
            <button
              class="flex-1 py-2 border border-slate-200 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer"
              @click="closeProfile"
            >Cancel</button>
            <button
              class="flex-1 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-semibold transition-colors cursor-pointer disabled:opacity-50"
              :disabled="profileLoading"
              @click="saveProfile"
            >{{ profileLoading ? 'Saving…' : 'Save' }}</button>
          </div>

          <!-- Change Password (local accounts only) -->
          <template v-if="authStore.isLocalAccount">
            <div class="border-t border-slate-100 pt-5">
              <h3 class="text-sm font-bold text-slate-700 mb-4">Change Password</h3>
              <div class="space-y-3">
                <div>
                  <label class="block text-xs font-semibold text-slate-600 mb-1">Current Password</label>
                  <div class="relative">
                    <input
                      v-model="cpForm.current"
                      :type="cpShowCurrent ? 'text' : 'password'"
                      class="w-full px-3 py-2 pr-10 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Current password"
                    />
                    <button type="button" class="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-slate-600 transition cursor-pointer" @click="cpShowCurrent = !cpShowCurrent">
                      <EyeOff v-if="cpShowCurrent" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-600 mb-1">New Password</label>
                  <div class="relative">
                    <input
                      v-model="cpForm.newPwd"
                      :type="cpShowNew ? 'text' : 'password'"
                      class="w-full px-3 py-2 pr-10 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="At least 8 characters"
                    />
                    <button type="button" class="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-slate-600 transition cursor-pointer" @click="cpShowNew = !cpShowNew">
                      <EyeOff v-if="cpShowNew" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-600 mb-1">Confirm New Password</label>
                  <div class="relative">
                    <input
                      v-model="cpForm.confirm"
                      :type="cpShowConfirm ? 'text' : 'password'"
                      class="w-full px-3 py-2 pr-10 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="Repeat new password"
                      @keyup.enter="submitChangePassword"
                    />
                    <button type="button" class="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-slate-600 transition cursor-pointer" @click="cpShowConfirm = !cpShowConfirm">
                      <EyeOff v-if="cpShowConfirm" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                </div>
                <p v-if="cpError" class="text-xs text-red-500">{{ cpError }}</p>
                <p v-if="cpSuccess" class="text-xs text-green-600">Password changed!</p>
              </div>
              <button
                class="w-full mt-4 py-2 bg-slate-800 hover:bg-slate-900 text-white rounded-lg text-sm font-semibold transition-colors cursor-pointer disabled:opacity-50"
                :disabled="cpLoading"
                @click="submitChangePassword"
              >{{ cpLoading ? 'Saving…' : 'Change Password' }}</button>
            </div>
          </template>
        </div>
      </div>
    </teleport>

    <!-- Main area -->
    <div class="flex-1 flex flex-col min-w-0 overflow-hidden">

      <!-- Mobile top bar -->
      <header class="md:hidden flex items-center gap-3 px-4 h-14 bg-white border-b border-slate-200 flex-shrink-0">
        <button
          class="p-1.5 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
          @click="drawerOpen = true"
        >
          <Menu class="w-5 h-5 text-slate-600" />
        </button>
        <span class="font-bold text-slate-900 text-sm">{{ brandingStore.appName }}</span>
      </header>

      <!-- Scrollable page content -->
      <main id="main-content" class="flex-1 overflow-y-auto">
        <div class="max-w-6xl mx-auto w-full p-5 md:p-8 min-h-full">
          <slot />
        </div>
      </main>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Zap, Menu, LogOut, LayoutDashboard, BookOpen, Play, Users, KeyRound, Settings, Pencil, X, Eye, EyeOff, ChevronLeft, ChevronRight, ScrollText } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import { useBrandingStore } from '@/stores/branding'
import { useToast } from '@/composables/useToast'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()
const brandingStore = useBrandingStore()
const toast = useToast()

const drawerOpen = ref(false)
const loading = ref(false)
const sidebarCollapsed = ref(false)
const showProfile = ref(false)
const profileLoading = ref(false)
const profileError = ref('')
const profileSuccess = ref(false)
const profileForm = ref({ firstName: '', lastName: '' })
const cpLoading = ref(false)
const cpError = ref('')
const cpSuccess = ref(false)
const cpForm = ref({ current: '', newPwd: '', confirm: '' })
const cpShowCurrent = ref(false)
const cpShowNew = ref(false)
const cpShowConfirm = ref(false)

function openProfile() {
  profileForm.value.firstName = authStore.firstName || ''
  profileForm.value.lastName = authStore.lastName || ''
  profileError.value = ''
  profileSuccess.value = false
  cpError.value = ''
  cpSuccess.value = false
  cpForm.value = { current: '', newPwd: '', confirm: '' }
  showProfile.value = true
}

function closeProfile() {
  showProfile.value = false
}

async function saveProfile() {
  profileError.value = ''
  profileSuccess.value = false
  profileLoading.value = true
  try {
    await authStore.updateProfile(profileForm.value.firstName, profileForm.value.lastName)
    profileSuccess.value = true
    toast.success('Profile updated')
    setTimeout(() => { profileSuccess.value = false }, 2000)
  } catch {
    profileError.value = 'Failed to save.'
  } finally {
    profileLoading.value = false
  }
}

async function submitChangePassword() {
  cpError.value = ''
  cpSuccess.value = false
  if (!cpForm.value.current || !cpForm.value.newPwd || !cpForm.value.confirm) {
    cpError.value = 'All fields are required.'
    return
  }
  if (cpForm.value.newPwd.length < 8) {
    cpError.value = 'New password must be at least 8 characters.'
    return
  }
  if (cpForm.value.newPwd !== cpForm.value.confirm) {
    cpError.value = 'Passwords do not match.'
    return
  }
  cpLoading.value = true
  try {
    await api.post('/auth/change-password', {
      currentPassword: cpForm.value.current,
      newPassword: cpForm.value.newPwd
    })
    cpSuccess.value = true
    cpForm.value = { current: '', newPwd: '', confirm: '' }
    toast.success('Password changed')
    setTimeout(() => { cpSuccess.value = false }, 2000)
  } catch (e: any) {
    cpError.value = e.response?.data?.message ?? 'Failed to change password.'
  } finally {
    cpLoading.value = false
  }
}

const navItems = [
  { to: '/home', label: 'Home', icon: LayoutDashboard },
  { to: '/questions', label: 'Question Bank', icon: BookOpen },
  { to: '/quizzes', label: 'Quizzes', icon: Play },
]

const adminNavItems = [
  { to: '/admin/users', label: 'Users', icon: Users },
  { to: '/admin/invitation-codes', label: 'Invitation Codes', icon: KeyRound },
  { to: '/admin/logs', label: 'Logs', icon: ScrollText },
  { to: '/admin/settings', label: 'Settings', icon: Settings },
]

const userInitial = computed(() => {
  if (authStore.firstName) return authStore.firstName.charAt(0).toUpperCase()
  if (authStore.username) return authStore.username.charAt(0).toUpperCase()
  return '?'
})

async function handleLogout() {
  loading.value = true
  try {
    await authStore.logout()
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>
