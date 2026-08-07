<template>
  <AppLayout>
    <div>
      <!-- Header -->
      <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
            <ScrollText class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-2xl font-black text-slate-900 leading-tight">Application Logs</h1>
            <p class="text-sm text-slate-500 mt-0.5">Real-time log viewer (last {{ logs.length }} entries)</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            class="inline-flex items-center gap-1.5 px-3 py-2 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-50 transition cursor-pointer"
            :class="{ 'bg-blue-50 border-blue-200 text-blue-700': autoRefresh }"
            @click="autoRefresh = !autoRefresh"
          >
            <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': autoRefresh }" />
            {{ autoRefresh ? 'Auto-refresh ON' : 'Auto-refresh OFF' }}
          </button>
          <button
            class="inline-flex items-center gap-1.5 px-3 py-2 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-50 transition cursor-pointer"
            @click="loadLogs"
          >
            <RefreshCw class="w-4 h-4" />
            Refresh
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="flex items-center gap-3 mb-4 flex-wrap">
        <div class="flex items-center gap-2 px-3 py-2 bg-white border border-slate-200 rounded-lg w-56">
          <Search class="w-4 h-4 text-slate-400 flex-shrink-0" />
          <input
            v-model="search"
            type="text"
            placeholder="Search logs…"
            class="flex-1 text-sm text-slate-700 placeholder-slate-400 bg-transparent outline-none"
            @keyup.enter="loadLogs"
          />
        </div>
        <select
          v-model="levelFilter"
          class="px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm text-slate-700 outline-none focus:ring-2 focus:ring-primary"
          @change="loadLogs"
        >
          <option value="">All levels</option>
          <option value="ERROR">ERROR</option>
          <option value="WARN">WARN</option>
          <option value="INFO">INFO</option>
          <option value="DEBUG">DEBUG</option>
          <option value="TRACE">TRACE</option>
        </select>
        <span class="text-xs text-slate-400 ml-auto">{{ logs.length }} entries</span>
      </div>

      <!-- Log table -->
      <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div class="max-h-[calc(100vh-280px)] overflow-y-auto">
          <table class="w-full text-sm">
            <thead class="sticky top-0 bg-slate-50 border-b border-slate-200">
              <tr>
                <th class="text-left px-4 py-2 text-xs font-semibold text-slate-500 uppercase tracking-wide w-44">Timestamp</th>
                <th class="text-left px-4 py-2 text-xs font-semibold text-slate-500 uppercase tracking-wide w-20">Level</th>
                <th class="text-left px-4 py-2 text-xs font-semibold text-slate-500 uppercase tracking-wide w-48">Logger</th>
                <th class="text-left px-4 py-2 text-xs font-semibold text-slate-500 uppercase tracking-wide">Message</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="loading" class="animate-pulse">
                <td colspan="4" class="px-4 py-8 text-center text-slate-400">Loading logs…</td>
              </tr>
              <tr v-else-if="logs.length === 0">
                <td colspan="4" class="px-4 py-8 text-center text-slate-400">No logs found</td>
              </tr>
              <tr
                v-for="(log, idx) in logs"
                :key="idx"
                class="hover:bg-slate-50 transition-colors font-mono text-xs"
              >
                <td class="px-4 py-2 text-slate-500 whitespace-nowrap">{{ formatTimestamp(log.timestamp) }}</td>
                <td class="px-4 py-2">
                  <span
                    class="inline-flex items-center px-1.5 py-0.5 rounded text-[0.65rem] font-bold uppercase"
                    :class="levelClass(log.level)"
                  >{{ log.level }}</span>
                </td>
                <td class="px-4 py-2 text-slate-500 truncate max-w-48" :title="log.logger">{{ log.logger }}</td>
                <td class="px-4 py-2 text-slate-700 whitespace-pre-wrap break-words">{{ log.message }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ScrollText, Search, RefreshCw } from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { adminService, type LogEntry } from '@/services/adminService'

const logs = ref<LogEntry[]>([])
const loading = ref(false)
const search = ref('')
const levelFilter = ref('')
const autoRefresh = ref(true)
let refreshInterval: number | null = null

onMounted(() => {
  loadLogs()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

function startAutoRefresh() {
  stopAutoRefresh()
  if (autoRefresh.value) {
    refreshInterval = window.setInterval(loadLogs, 5000)
  }
}

function stopAutoRefresh() {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

async function loadLogs() {
  loading.value = true
  try {
    const params: Record<string, string | number> = { limit: 500 }
    if (levelFilter.value) params.level = levelFilter.value
    if (search.value.trim()) params.search = search.value.trim()

    const [authRes, mainRes] = await Promise.allSettled([
      adminService.getLogs(params),
      adminService.getMainServiceLogs(params),
    ])

    const allLogs: LogEntry[] = []
    if (authRes.status === 'fulfilled') allLogs.push(...authRes.value)
    if (mainRes.status === 'fulfilled') allLogs.push(...mainRes.value)

    allLogs.sort((a, b) => b.timestamp - a.timestamp)
    logs.value = allLogs.slice(0, 1000)
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function formatTimestamp(ts: number): string {
  return new Date(ts).toLocaleString(undefined, {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false,
  })
}

function levelClass(level: string): string {
  switch (level) {
    case 'ERROR': return 'bg-red-100 text-red-700'
    case 'WARN': return 'bg-amber-100 text-amber-700'
    case 'INFO': return 'bg-blue-100 text-blue-700'
    case 'DEBUG': return 'bg-slate-100 text-slate-600'
    case 'TRACE': return 'bg-slate-100 text-slate-500'
    default: return 'bg-slate-100 text-slate-600'
  }
}
</script>
