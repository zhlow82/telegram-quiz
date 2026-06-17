<template>
  <teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4" @keydown.escape="close">
      <div class="absolute inset-0 bg-black/40" />
      <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-md flex flex-col overflow-hidden" @click.stop>

        <!-- Header -->
        <div class="px-6 py-4 border-b border-slate-200 flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-primary flex items-center justify-center flex-shrink-0">
            <Users class="w-4 h-4 text-white" />
          </div>
          <div class="flex-1 min-w-0">
            <h2 class="text-base font-bold text-slate-900">{{ readonly ? 'Folder members' : 'Share folder' }}</h2>
            <p class="text-xs text-slate-500 truncate">{{ folderName }}</p>
          </div>
          <button
            class="absolute top-4 right-4 w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            @click="close"
          ><X class="w-4 h-4" /></button>
        </div>

        <!-- Invite form (managers only) -->
        <div v-if="!readonly" class="px-6 py-4 border-b border-slate-100">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">Invite member</p>
          <div class="flex gap-2">
            <!-- Username autocomplete -->
            <div class="flex-1 relative">
              <input
                v-model="inviteUsername"
                class="w-full border border-slate-200 rounded-lg px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Search username..."
                autocomplete="off"
                @input="onSearchInput"
                @keydown.escape="searchResults = []"
                @blur="onSearchBlur"
              />
              <div
                v-if="searchResults.length"
                class="absolute z-10 top-full mt-1 left-0 right-0 bg-white border border-slate-200 rounded-lg shadow-lg overflow-hidden"
              >
                <button
                  v-for="u in searchResults"
                  :key="u.username"
                  class="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-slate-50 cursor-pointer text-left transition-colors"
                  @mousedown.prevent="selectUser(u)"
                >
                  <div class="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-600 flex-shrink-0">
                    {{ u.username[0].toUpperCase() }}
                  </div>
                  <div>
                    <span class="font-medium text-slate-900">{{ u.username }}</span>
                    <span v-if="u.firstName || u.lastName" class="text-slate-400 ml-1 text-xs">
                      {{ [u.firstName, u.lastName].filter(Boolean).join(' ') }}
                    </span>
                  </div>
                </button>
              </div>
            </div>
            <select
              v-model="inviteRole"
              class="border border-slate-200 rounded-lg px-2 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-500 bg-white cursor-pointer"
            >
              <option value="CONTRIBUTOR">Contributor</option>
              <option value="CO_OWNER">Co-owner</option>
            </select>
            <button
              class="bg-primary hover:bg-primary-hover text-white text-sm font-semibold px-4 py-2 rounded-full transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="!inviteUsername.trim() || inviting"
              @click="submitInvite"
            >
              <span v-if="inviting" class="inline-block w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
              <span v-else>Invite</span>
            </button>
          </div>
          <p v-if="inviteError" class="text-xs text-red-600 mt-1.5">{{ inviteError }}</p>
        </div>

        <!-- Members list -->
        <div class="px-6 py-4 overflow-y-auto max-h-72">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">
            Members ({{ members.length + 1 }})
          </p>
          <div v-if="loadingMembers" class="text-sm text-slate-400 py-4 text-center">Loading...</div>
          <div v-else class="space-y-0">
            <!-- Owner row (always shown) -->
            <div class="flex items-center gap-2.5 py-2 border-b border-slate-100">
              <div class="w-7 h-7 rounded-full bg-amber-100 flex items-center justify-center text-xs font-bold text-amber-700 flex-shrink-0">
                {{ folderOwner ? folderOwner[0].toUpperCase() : '?' }}
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-slate-900 truncate">
                  {{ folderOwner }}
                  <span v-if="folderOwner === currentUsername" class="text-slate-400 font-normal">(You)</span>
                </p>
                <p class="text-[0.65rem] text-slate-400 leading-tight">Owner</p>
              </div>
              <span class="text-[0.65rem] font-semibold px-2 py-0.5 rounded-md whitespace-nowrap bg-amber-100 text-amber-700">Owner</span>
            </div>
            <!-- Invited members -->
            <div v-if="members.length === 0" class="text-sm text-slate-400 text-center py-4">
              {{ readonly ? 'No other members.' : 'No members yet. Invite someone above.' }}
            </div>
            <template v-else>
              <div
                v-for="m in members"
                :key="m.id"
                class="flex items-center gap-2.5 py-2 border-b border-slate-50 last:border-0"
              >
                <div class="w-7 h-7 rounded-full bg-slate-200 flex items-center justify-center text-xs font-bold text-slate-600 flex-shrink-0">
                  {{ m.username[0].toUpperCase() }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-slate-900 truncate">
                    {{ m.username }}
                    <span v-if="m.username === currentUsername" class="text-slate-400 font-normal">(You)</span>
                  </p>
                  <p class="text-[0.65rem] text-slate-400 leading-tight">
                    {{ m.status === 'PENDING' ? 'Pending acceptance' : 'Active member' }}
                  </p>
                </div>
                <template v-if="!readonly">
                  <select
                    v-if="m.status === 'ACCEPTED'"
                    :value="m.role"
                    class="text-xs border border-slate-200 rounded-md px-1.5 py-1 bg-white cursor-pointer outline-none focus:ring-1 focus:ring-blue-400"
                    @change="changeRole(m.username, ($event.target as HTMLSelectElement).value)"
                  >
                    <option value="CONTRIBUTOR">Contributor</option>
                    <option value="CO_OWNER">Co-owner</option>
                  </select>
                  <span
                    v-else
                    class="text-[0.65rem] text-slate-400 bg-slate-100 px-2 py-0.5 rounded-md whitespace-nowrap"
                  >
                    {{ m.role === 'CO_OWNER' ? 'Co-owner' : 'Contributor' }}
                  </span>
                  <button
                    class="w-6 h-6 rounded flex items-center justify-center text-slate-300 hover:text-red-500 hover:bg-red-50 transition cursor-pointer flex-shrink-0"
                    title="Remove member"
                    @click="removeMember(m.username)"
                  ><Trash2 class="w-3 h-3" /></button>
                </template>
                <span
                  v-else
                  class="text-[0.65rem] font-medium px-2 py-0.5 rounded-md whitespace-nowrap"
                  :class="m.role === 'CO_OWNER' ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-600'"
                >{{ m.role === 'CO_OWNER' ? 'Co-owner' : 'Contributor' }}</span>
              </div>
            </template>
          </div>
        </div>

      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Users, X, Trash2 } from '@lucide/vue'
import { foldersService } from '@/services/foldersService'
import { usersService, type UserSearchResult } from '@/services/usersService'
import type { FolderMember } from '@/types/folder'

const props = defineProps<{
  visible: boolean
  folderId: number | null
  folderName: string
  folderOwner: string
  currentUsername?: string
  readonly?: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

// ── Members ────────────────────────────────────────────────────────────────
const members = ref<FolderMember[]>([])
const loadingMembers = ref(false)

async function loadMembers() {
  if (!props.folderId) return
  loadingMembers.value = true
  try {
    members.value = await foldersService.getMembers(props.folderId)
  } catch {
    members.value = []
  } finally {
    loadingMembers.value = false
  }
}

watch(() => [props.visible, props.folderId], ([vis]) => {
  if (vis) {
    loadMembers()
    inviteUsername.value = ''
    inviteRole.value = 'CONTRIBUTOR'
    inviteError.value = ''
    searchResults.value = []
  }
}, { immediate: true })

// ── Invite ─────────────────────────────────────────────────────────────────
const inviteUsername = ref('')
const inviteRole = ref<'CO_OWNER' | 'CONTRIBUTOR'>('CONTRIBUTOR')
const inviting = ref(false)
const inviteError = ref('')

// Autocomplete
const searchResults = ref<UserSearchResult[]>([])
let searchTimeout: ReturnType<typeof setTimeout> | null = null

function onSearchInput() {
  inviteError.value = ''
  searchResults.value = []
  if (searchTimeout) clearTimeout(searchTimeout)
  const q = inviteUsername.value.trim()
  if (q.length < 1) return
  searchTimeout = setTimeout(async () => {
    try {
      searchResults.value = await usersService.search(q)
    } catch {
      searchResults.value = []
    }
  }, 300)
}

function onSearchBlur() {
  setTimeout(() => { searchResults.value = [] }, 150)
}

function selectUser(u: UserSearchResult) {
  inviteUsername.value = u.username
  searchResults.value = []
}

async function submitInvite() {
  const username = inviteUsername.value.trim()
  if (!username) return
  inviting.value = true
  inviteError.value = ''
  try {
    const member = await foldersService.inviteUser(props.folderId!, { username, role: inviteRole.value })
    members.value.push(member)
    inviteUsername.value = ''
  } catch (err: unknown) {
    const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message
    inviteError.value = msg ?? 'Failed to invite user.'
  } finally {
    inviting.value = false
  }
}

// ── Manage members ─────────────────────────────────────────────────────────
async function removeMember(username: string) {
  if (!props.folderId) return
  try {
    await foldersService.removeMember(props.folderId, username)
    members.value = members.value.filter(m => m.username !== username)
  } catch {
    // silently ignore
  }
}

async function changeRole(username: string, role: string) {
  if (!props.folderId) return
  try {
    const updated = await foldersService.updateMemberRole(props.folderId, username, role)
    const idx = members.value.findIndex(m => m.username === username)
    if (idx >= 0) members.value[idx] = updated
  } catch {
    // revert by reloading
    await loadMembers()
  }
}

function close() {
  emit('close')
}
</script>
