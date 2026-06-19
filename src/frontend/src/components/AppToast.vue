<template>
  <teleport to="body">
    <div class="fixed bottom-6 left-1/2 -translate-x-1/2 z-[100] flex flex-col items-center gap-2 pointer-events-none">
      <transition-group
        enter-active-class="transition-all duration-200 ease-out"
        enter-from-class="opacity-0 translate-y-2 scale-95"
        enter-to-class="opacity-100 translate-y-0 scale-100"
        leave-active-class="transition-all duration-150 ease-in"
        leave-from-class="opacity-100 translate-y-0 scale-100"
        leave-to-class="opacity-0 translate-y-2 scale-95"
      >
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="pointer-events-auto px-4 py-2.5 rounded-xl shadow-lg text-sm font-medium flex items-center gap-2"
          :class="{
            'bg-slate-900 text-white': toast.type === 'info',
            'bg-green-600 text-white': toast.type === 'success',
            'bg-red-600 text-white': toast.type === 'error',
          }"
        >
          <CheckCircle v-if="toast.type === 'success'" class="w-4 h-4 flex-shrink-0" />
          <AlertCircle v-else-if="toast.type === 'error'" class="w-4 h-4 flex-shrink-0" />
          <Info v-else class="w-4 h-4 flex-shrink-0" />
          <span>{{ toast.message }}</span>
          <button
            v-if="toast.action"
            class="ml-2 px-2 py-0.5 rounded-md text-xs font-bold uppercase tracking-wide bg-white/20 hover:bg-white/30 text-white transition cursor-pointer"
            @click="toast.action.onClick()"
          >
            {{ toast.action.label }}
          </button>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { CheckCircle, AlertCircle, Info } from '@lucide/vue'
import { useToast } from '@/composables/useToast'

const { toasts } = useToast()
</script>
