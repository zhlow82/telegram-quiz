<template>
  <AppLayout>
    <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200">
      <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
        <Users class="w-5 h-5 text-white" />
      </div>
      <div class="flex-1 min-w-0">
        <h1 class="text-2xl font-black text-slate-900 leading-tight">Users</h1>
        <p class="text-sm text-slate-500 mt-0.5">Manage accounts and roles</p>
      </div>
      <!-- Search -->
      <div class="flex items-center gap-2 px-3 py-2 bg-white border border-slate-200 rounded-lg w-64">
        <Search class="w-4 h-4 text-slate-400 flex-shrink-0" />
        <input
          v-model="search"
          type="text"
          placeholder="Search users…"
          class="flex-1 text-sm text-slate-700 placeholder-slate-400 bg-transparent outline-none"
        />
      </div>
      <button
        class="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer"
        @click="showCreate = true"
      >
        <Plus class="w-4 h-4" />
        New User
      </button>
    </div>

    <!-- User table -->
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-slate-400">Loading...</div>
      <div v-else-if="sortedFilteredUsers.length === 0" class="p-8 text-center text-slate-400">{{ search ? 'No users match your search.' : 'No users found.' }}</div>
      <table v-else class="w-full text-sm">
        <thead>
          <tr class="bg-white border-b-2 border-slate-200">
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">
              <button
                class="flex items-center gap-1 transition-colors cursor-pointer hover:text-slate-900"
                :class="sortKey === 'firstName' ? 'text-blue-600' : ''"
                @click="setSort('firstName')"
              >
                FIRST NAME
                <component :is="sortKey === 'firstName' ? (sortDir === 'asc' ? ArrowUp : ArrowDown) : ArrowUpDown" class="w-3 h-3" :class="sortKey === 'firstName' ? 'opacity-100' : 'opacity-40'" />
              </button>
            </th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">
              <button
                class="flex items-center gap-1 transition-colors cursor-pointer hover:text-slate-900"
                :class="sortKey === 'lastName' ? 'text-blue-600' : ''"
                @click="setSort('lastName')"
              >
                LAST NAME
                <component :is="sortKey === 'lastName' ? (sortDir === 'asc' ? ArrowUp : ArrowDown) : ArrowUpDown" class="w-3 h-3" :class="sortKey === 'lastName' ? 'opacity-100' : 'opacity-40'" />
              </button>
            </th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">
              <button
                class="flex items-center gap-1 transition-colors cursor-pointer hover:text-slate-900"
                :class="sortKey === 'username' ? 'text-blue-600' : ''"
                @click="setSort('username')"
              >
                USERNAME / EMAIL
                <component :is="sortKey === 'username' ? (sortDir === 'asc' ? ArrowUp : ArrowDown) : ArrowUpDown" class="w-3 h-3" :class="sortKey === 'username' ? 'opacity-100' : 'opacity-40'" />
              </button>
            </th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">
              <button
                class="flex items-center gap-1 transition-colors cursor-pointer hover:text-slate-900"
                :class="sortKey === 'provider' ? 'text-blue-600' : ''"
                @click="setSort('provider')"
              >
                TYPE
                <component :is="sortKey === 'provider' ? (sortDir === 'asc' ? ArrowUp : ArrowDown) : ArrowUpDown" class="w-3 h-3" :class="sortKey === 'provider' ? 'opacity-100' : 'opacity-40'" />
              </button>
            </th>
            <th class="text-left px-4 py-3 text-xs font-semibold text-slate-600 uppercase tracking-wider">
              <button
                class="flex items-center gap-1 transition-colors cursor-pointer hover:text-slate-900"
                :class="sortKey === 'role' ? 'text-blue-600' : ''"
                @click="setSort('role')"
              >
                ROLE
                <component :is="sortKey === 'role' ? (sortDir === 'asc' ? ArrowUp : ArrowDown) : ArrowUpDown" class="w-3 h-3" :class="sortKey === 'role' ? 'opacity-100' : 'opacity-40'" />
              </button>
            </th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-slate-600 uppercase tracking-wider">ACTIVE</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="user in pagedUsers"
            :key="user.id"
            :class="user.username === authStore.username
              ? 'border-b border-blue-100 last:border-0 bg-blue-50/60 hover:bg-blue-50 transition-colors'
              : 'border-b border-slate-50 last:border-0 hover:bg-slate-50/70 transition-colors'"
          >
            <!-- First Name column -->
            <td class="px-4 py-3 font-medium text-slate-900">{{ user.firstName || '—' }}</td>
            <!-- Last Name column -->
            <td class="px-4 py-3 font-medium text-slate-900">{{ user.lastName || '—' }}</td>
            <!-- Username / Email column -->
            <td class="px-4 py-3 text-sm text-slate-500">
              {{ user.provider === 'google' ? user.email : user.username }}
              <span v-if="user.username === authStore.username" class="ml-1.5 text-xs font-semibold text-blue-500">(you)</span>
            </td>
            <!-- Provider / Type column -->
            <td class="px-4 py-3">
              <span
                v-if="user.provider === 'google'"
                class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-semibold bg-white border border-slate-200"
                title="Google account"
              >
                <!-- Google coloured "G" logo -->
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="w-3.5 h-3.5 flex-shrink-0" aria-hidden="true">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                <span class="text-slate-600">Google</span>
              </span>
              <span
                v-else
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200"
              >Local</span>
            </td>
            <td class="px-4 py-3">
              <button
                :disabled="user.username === authStore.username"
                :class="[
                  'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold transition-colors',
                  user.role === 'ROLE_ADMIN'
                    ? 'bg-purple-100 text-purple-700 hover:bg-purple-200'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
                  user.username === authStore.username ? 'cursor-default' : 'cursor-pointer'
                ]"
                :title="user.username !== authStore.username ? (user.role === 'ROLE_ADMIN' ? 'Click to demote to Member' : 'Click to promote to Admin') : ''"
                @click="user.username !== authStore.username && toggleRole(user)"
              >{{ user.role === 'ROLE_ADMIN' ? 'Admin' : 'Member' }}</button>
            </td>
            <td class="px-4 py-3 text-center">
              <button
                v-if="user.username !== authStore.username"
                role="switch"
                :aria-checked="user.active"
                class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none"
                :class="user.active ? 'bg-blue-600' : 'bg-slate-200'"
                @click="toggleUserActive(user)"
              >
                <span
                  class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200"
                  :class="user.active ? 'translate-x-5' : 'translate-x-0'"
                />
              </button>
            </td>
            <td class="px-4 py-3 text-right">
              <div class="flex items-center justify-end gap-1">
                <button
                  class="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition cursor-pointer"
                  title="Edit profile"
                  @click="openEdit(user)"
                >
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button
                  v-if="user.username !== authStore.username"
                  class="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition cursor-pointer"
                  title="Delete user"
                  @click="confirmDeleteUser(user)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div v-if="!loading" class="flex items-center justify-between px-4 py-3 border-t border-slate-100">
        <span class="text-xs text-slate-500">
          Showing {{ pageStart }}–{{ pageEnd }} of {{ sortedFilteredUsers.length }} record{{ sortedFilteredUsers.length !== 1 ? 's' : '' }}
        </span>
        <div v-if="totalPages > 1" class="flex items-center gap-1">
          <button
            class="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-100 disabled:opacity-30 disabled:cursor-not-allowed transition cursor-pointer"
            :disabled="currentPage === 1"
            @click="currentPage--"
          >← Prev</button>
          <button
            class="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-100 disabled:opacity-30 disabled:cursor-not-allowed transition cursor-pointer"
            :disabled="currentPage === totalPages"
            @click="currentPage++"
          >Next →</button>
        </div>
      </div>
    </div>

    <!-- Edit user modal -->
    <teleport to="body">
      <div
        v-if="showEdit"
        class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        @click.self="closeEdit"
      >
        <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-sm p-6">
          <button
            type="button"
            class="absolute top-4 right-4 w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            aria-label="Close"
            @click="closeEdit"
          ><X class="w-4 h-4" /></button>
          <h2 class="text-lg font-bold text-slate-900 mb-1">Edit User</h2>
          <p class="text-sm text-slate-500 mb-4">{{ editingUser?.username || editingUser?.email }}</p>
          <div class="space-y-3">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-semibold text-slate-600 mb-1">First Name</label>
                <input
                  v-model="editForm.firstName"
                  type="text"
                  class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="First name"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold text-slate-600 mb-1">Last Name</label>
                <input
                  v-model="editForm.lastName"
                  type="text"
                  class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Last name"
                />
              </div>
            </div>
            <!-- Password reset — local accounts only -->
            <template v-if="editingUser?.provider !== 'google'">
              <div class="pt-1 border-t border-slate-100">
                <label class="block text-xs font-semibold text-slate-600 mb-1">New Password <span class="font-normal text-slate-400">(leave blank to keep)</span></label>
                <input
                  v-model="editForm.newPassword"
                  type="password"
                  class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="New password"
                  autocomplete="new-password"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold text-slate-600 mb-1">Confirm Password</label>
                <input
                  v-model="editForm.confirmPassword"
                  type="password"
                  class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  :class="editForm.confirmPassword && editForm.newPassword !== editForm.confirmPassword ? 'border-red-400 focus:ring-red-400' : ''"
                  placeholder="Repeat password"
                  autocomplete="new-password"
                />
              </div>
            </template>
            <p v-if="editError" class="text-xs text-red-500">{{ editError }}</p>
            <p v-if="editSuccess" class="text-xs text-green-600">{{ editSuccess }}</p>
          </div>
          <div class="flex gap-2 mt-5">
            <button
              class="flex-1 px-4 py-2 border border-slate-200 rounded-lg text-sm text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer"
              @click="closeEdit"
            >Cancel</button>
            <button
              class="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
              :disabled="saving"
              @click="saveEdit"
            >Save</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- Create user modal -->
    <teleport to="body">
      <div
        v-if="showCreate"
        class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        @click.self="showCreate = false"
      >
        <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-sm p-6">
          <button
            type="button"
            class="absolute top-4 right-4 w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            aria-label="Close"
            @click="showCreate = false"
          ><X class="w-4 h-4" /></button>
          <h2 class="text-lg font-bold text-slate-900 mb-4">Create Local Account</h2>
          <div class="space-y-3">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-semibold text-slate-600 mb-1">First Name</label>
                <input
                  v-model="form.firstName"
                  type="text"
                  class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="First name"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold text-slate-600 mb-1">Last Name</label>
                <input
                  v-model="form.lastName"
                  type="text"
                  class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Last name"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 mb-1">Username</label>
              <input
                v-model="form.username"
                type="text"
                class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="e.g. johndoe"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 mb-1">Password</label>
              <input
                v-model="form.password"
                type="password"
                class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Temporary password"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-600 mb-1">Confirm Password</label>
              <input
                v-model="form.confirmPassword"
                type="password"
                class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                :class="form.confirmPassword && form.password !== form.confirmPassword ? 'border-red-400 focus:ring-red-400' : ''"
                placeholder="Repeat password"
              />
            </div>
            <p v-if="createError" class="text-xs text-red-500">{{ createError }}</p>
          </div>
          <div class="flex gap-2 mt-5">
            <button
              class="flex-1 px-4 py-2 border border-slate-200 rounded-lg text-sm text-slate-600 hover:bg-slate-50 transition-colors cursor-pointer"
              @click="showCreate = false"
            >Cancel</button>
            <button
              class="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition-colors cursor-pointer disabled:opacity-50"
              :disabled="creating"
              @click="createUser"
            >Create</button>
          </div>
        </div>
      </div>
    </teleport>

    <!-- App dialog (alert) -->
    <AppDialog
      :visible="dialogVisible"
      type="alert"
      :title="dialogTitle"
      :message="dialogMessage"
      @confirm="dialogVisible = false"
      @cancel="dialogVisible = false"
    />

    <!-- Delete user confirmation -->
    <AppDialog
      :visible="deleteUserDialogVisible"
      type="confirm"
      title="Delete User"
      :message="`Permanently delete ${deletingUser?.firstName || deletingUser?.username}? This cannot be undone.`"
      @confirm="doDeleteUser"
      @cancel="deleteUserDialogVisible = false"
    />
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Users, Plus, Pencil, Search, Trash2, ArrowUp, ArrowDown, ArrowUpDown, X } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import AppDialog from '@/components/AppDialog.vue'
import { adminService } from '@/services/adminService'
import { useAuthStore } from '@/stores/auth'
import type { UserResponse } from '@/types/admin'

const authStore = useAuthStore()
const users = ref<UserResponse[]>([])
const loading = ref(false)
const search = ref('')
const currentPage = ref(1)
const PAGE_SIZE = 10

type SortKey = 'firstName' | 'lastName' | 'username' | 'provider' | 'role'
const sortKey = ref<SortKey>('firstName')
const sortDir = ref<'asc' | 'desc'>('asc')

function setSort(key: SortKey) {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortDir.value = 'asc'
  }
  currentPage.value = 1
}

watch(search, () => { currentPage.value = 1 })

const sortedFilteredUsers = computed(() => {
  const q = search.value.trim().toLowerCase()
  const list = q
    ? users.value.filter(u => {
        const hay = [u.firstName, u.lastName, u.username, u.email].filter(Boolean).join(' ').toLowerCase()
        return hay.includes(q)
      })
    : [...users.value]
  return list.sort((a, b) => {
    if (a.username === authStore.username) return -1
    if (b.username === authStore.username) return 1
    let aVal: string
    let bVal: string
    if (sortKey.value === 'firstName') {
      aVal = (a.firstName || '').toLowerCase()
      bVal = (b.firstName || '').toLowerCase()
    } else if (sortKey.value === 'lastName') {
      aVal = (a.lastName || '').toLowerCase()
      bVal = (b.lastName || '').toLowerCase()
    } else if (sortKey.value === 'username') {
      aVal = (a.provider === 'google' ? (a.email || '') : (a.username || '')).toLowerCase()
      bVal = (b.provider === 'google' ? (b.email || '') : (b.username || '')).toLowerCase()
    } else {
      aVal = sortKey.value === 'provider' ? a.provider : a.role
      bVal = sortKey.value === 'provider' ? b.provider : b.role
    }
    const cmp = aVal.localeCompare(bVal)
    return sortDir.value === 'asc' ? cmp : -cmp
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(sortedFilteredUsers.value.length / PAGE_SIZE)))

const pageStart = computed(() => sortedFilteredUsers.value.length === 0 ? 0 : (currentPage.value - 1) * PAGE_SIZE + 1)
const pageEnd = computed(() => Math.min(currentPage.value * PAGE_SIZE, sortedFilteredUsers.value.length))

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return sortedFilteredUsers.value.slice(start, start + PAGE_SIZE)
})

const showCreate = ref(false)
const creating = ref(false)
const createError = ref('')
const form = ref({ firstName: '', lastName: '', username: '', password: '', confirmPassword: '' })

// AppDialog state
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')

function showAlert(title: string, message?: string) {
  dialogTitle.value = title
  dialogMessage.value = message ?? ''
  dialogVisible.value = true
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminService.listUsers()
    users.value = res.data
  } finally {
    loading.value = false
  }
}

async function toggleRole(user: UserResponse) {
  const newRole = user.role === 'ROLE_ADMIN' ? 'ROLE_MEMBER' : 'ROLE_ADMIN'
  try {
    const res = await adminService.updateRole(user.id, newRole)
    const idx = users.value.findIndex(u => u.id === user.id)
    if (idx !== -1) users.value[idx] = res.data
  } catch (e: any) {
    showAlert('Role update failed', e.response?.data?.message ?? `Failed to update role: ${e.message}`)
  }
}

async function toggleUserActive(user: UserResponse) {
  if (user.active) {
    await adminService.deactivateUser(user.id)
    user.active = false
  } else {
    await adminService.activateUser(user.id)
    user.active = true
  }
}

async function createUser() {
  createError.value = ''
  if (!form.value.username.trim() || !form.value.password.trim()) {
    createError.value = 'Username and password are required.'
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    createError.value = 'Passwords do not match.'
    return
  }
  creating.value = true
  try {
    const res = await adminService.createUser(form.value)
    users.value.push(res.data)
    showCreate.value = false
    form.value = { firstName: '', lastName: '', username: '', password: '', confirmPassword: '' }
  } catch (e: any) {
    createError.value = e.response?.data?.message ?? 'Failed to create user.'
  } finally {
    creating.value = false
  }
}

// Edit user state
const showEdit = ref(false)
const editingUser = ref<UserResponse | null>(null)
const editForm = ref({ firstName: '', lastName: '', newPassword: '', confirmPassword: '' })
const editError = ref('')
const editSuccess = ref('')
const saving = ref(false)

function openEdit(user: UserResponse) {
  editingUser.value = user
  editForm.value = { firstName: user.firstName || '', lastName: user.lastName || '', newPassword: '', confirmPassword: '' }
  editError.value = ''
  editSuccess.value = ''
  showEdit.value = true
}

function closeEdit() {
  showEdit.value = false
  editingUser.value = null
}

async function saveEdit() {
  if (!editingUser.value) return
  editError.value = ''
  editSuccess.value = ''
  saving.value = true
  if (editForm.value.newPassword && editForm.value.newPassword !== editForm.value.confirmPassword) {
    editError.value = 'Passwords do not match.'
    saving.value = false
    return
  }
  try {
    const res = await adminService.updateProfile(editingUser.value.id, editForm.value.firstName, editForm.value.lastName)
    const idx = users.value.findIndex(u => u.id === editingUser.value!.id)
    if (idx !== -1) users.value[idx] = res.data
    editingUser.value = res.data
    if (editForm.value.newPassword.trim()) {
      await adminService.resetPassword(editingUser.value.id, editForm.value.newPassword)
      editForm.value.newPassword = ''
    }
    editSuccess.value = 'Saved successfully.'
    setTimeout(() => { editSuccess.value = '' }, 2000)
  } catch (e: any) {
    editError.value = e.response?.data?.message ?? 'Failed to save changes.'
  } finally {
    saving.value = false
  }
}

// Avatar helpers
const AVATAR_COLORS = [
  'bg-blue-100 text-blue-700',
  'bg-emerald-100 text-emerald-700',
  'bg-violet-100 text-violet-700',
  'bg-amber-100 text-amber-700',
  'bg-rose-100 text-rose-700',
  'bg-cyan-100 text-cyan-700',
]
function initials(user: UserResponse): string {
  const f = (user.firstName || '').charAt(0).toUpperCase()
  const l = (user.lastName || '').charAt(0).toUpperCase()
  if (f && l) return f + l
  return f || l || (user.username || user.email || '?').charAt(0).toUpperCase()
}
function avatarColor(user: UserResponse): string {
  const seed = (user.username || user.email || '').charCodeAt(0) || 0
  return AVATAR_COLORS[seed % AVATAR_COLORS.length]
}

onMounted(loadUsers)

// Delete user
const deleteUserDialogVisible = ref(false)
const deletingUser = ref<UserResponse | null>(null)

function confirmDeleteUser(user: UserResponse) {
  deletingUser.value = user
  deleteUserDialogVisible.value = true
}

async function doDeleteUser() {
  if (!deletingUser.value) return
  deleteUserDialogVisible.value = false
  try {
    await adminService.deleteUser(deletingUser.value.id)
    users.value = users.value.filter(u => u.id !== deletingUser.value!.id)
  } catch (e: any) {
    showAlert('Delete failed', e.response?.data?.message ?? 'Failed to delete user.')
  } finally {
    deletingUser.value = null
  }
}
</script>
