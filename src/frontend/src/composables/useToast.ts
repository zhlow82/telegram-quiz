import { ref } from 'vue'

interface ToastAction {
  label: string
  onClick: () => void
}

interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
  action?: ToastAction
}

const toasts = ref<Toast[]>([])
let nextId = 0

function addToast(message: string, type: Toast['type'] = 'info', duration = 3000, action?: ToastAction) {
  const id = nextId++
  toasts.value.push({ id, message, type, action })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, duration)
}

export function useToast() {
  return {
    toasts,
    success: (message: string, duration?: number, action?: ToastAction) => addToast(message, 'success', duration, action),
    error: (message: string, duration?: number, action?: ToastAction) => addToast(message, 'error', duration, action),
    info: (message: string, duration?: number, action?: ToastAction) => addToast(message, 'info', duration, action),
  }
}
