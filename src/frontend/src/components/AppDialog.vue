<template>
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/50" />

        <!-- Dialog panel -->
        <div
          class="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 flex flex-col gap-4"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="'dialog-title-' + _uid"
          :aria-describedby="message ? 'dialog-desc-' + _uid : undefined"
        >
          <button
            type="button"
            class="absolute top-4 right-4 w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
            aria-label="Close"
            @click="cancel"
          ><X class="w-4 h-4" /></button>
          <!-- Icon + title -->
          <div class="flex items-start gap-3">
            <div
              class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0"
              :class="type === 'confirm' ? 'bg-amber-100' : 'bg-red-100'"
            >
              <AlertTriangle v-if="type === 'confirm'" class="w-5 h-5 text-amber-600" />
              <AlertCircle v-else class="w-5 h-5 text-red-600" />
            </div>
            <div class="flex-1 min-w-0">
              <h3 :id="'dialog-title-' + _uid" class="text-base font-bold text-slate-900 leading-snug">{{ title }}</h3>
              <p v-if="message" :id="'dialog-desc-' + _uid" class="mt-1 text-sm text-slate-600 leading-relaxed">{{ message }}</p>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-3 justify-end pt-1">
            <button
              v-if="type === 'confirm'"
              type="button"
              class="px-4 py-2 text-sm font-semibold rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50 transition cursor-pointer"
              @click="cancel"
            >
              Cancel
            </button>
            <button
              type="button"
              class="px-4 py-2 text-sm font-semibold rounded-lg transition cursor-pointer"
              :class="type === 'confirm'
                ? 'bg-red-600 hover:bg-red-700 text-white'
                : 'bg-slate-900 hover:bg-slate-700 text-white'"
              @click="confirm"
            >
              {{ type === 'confirm' ? confirmLabel : 'OK' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { watch, onUnmounted, getCurrentInstance } from 'vue'
import { AlertTriangle, AlertCircle, X } from '@lucide/vue'

const _uid = getCurrentInstance()?.uid ?? 0

const props = withDefaults(defineProps<{
  visible: boolean
  type?: 'confirm' | 'alert'
  title: string
  message?: string
  confirmLabel?: string
}>(), {
  type: 'alert',
  message: '',
  confirmLabel: 'Delete'
})

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

function confirm() {
  emit('confirm')
}

function cancel() {
  emit('cancel')
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.visible) {
    cancel()
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    document.addEventListener('keydown', onKeydown)
  } else {
    document.removeEventListener('keydown', onKeydown)
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>
