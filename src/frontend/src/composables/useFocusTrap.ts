import { watch, isRef, type Ref, type ComputedRef } from 'vue'

export function useFocusTrap(isActive: Ref<boolean> | ComputedRef<boolean> | (() => boolean), containerRef: Ref<HTMLElement | null>) {
  function getFocusableElements(container: HTMLElement): HTMLElement[] {
    const selectors = [
      'button:not([disabled])',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
      'a[href]'
    ]
    return Array.from(container.querySelectorAll(selectors.join(',')))
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key !== 'Tab' || !containerRef.value) return

    const focusable = getFocusableElements(containerRef.value)
    if (focusable.length === 0) return

    const first = focusable[0]
    const last = focusable[focusable.length - 1]

    if (e.shiftKey) {
      if (document.activeElement === first) {
        e.preventDefault()
        last.focus()
      }
    } else {
      if (document.activeElement === last) {
        e.preventDefault()
        first.focus()
      }
    }
  }

  const getter = isRef(isActive) ? isActive : (typeof isActive === 'function' ? isActive : () => false)

  watch(getter, (active) => {
    if (active && containerRef.value) {
      const focusable = getFocusableElements(containerRef.value)
      if (focusable.length > 0) {
        setTimeout(() => focusable[0].focus(), 50)
      }
      document.addEventListener('keydown', handleKeydown)
    } else {
      document.removeEventListener('keydown', handleKeydown)
    }
  }, { immediate: true })
}
