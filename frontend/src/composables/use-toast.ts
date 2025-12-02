// composables/useToast.ts
import { ref, readonly, inject } from 'vue'
import type { ToastProps } from '@/components/ui/BaseToast.vue'

export type ToastPosition =
  | 'top-left'
  | 'top-right'
  | 'bottom-left'
  | 'bottom-right'
  | 'top-center'
  | 'bottom-center'

export interface Toast extends ToastProps {
  id: string
  position?: ToastPosition
}

const _toasts = ref<Toast[]>([])
let counter = 0

// Deduplication: do not add identical messages within X ms
let lastMessage = ''
let lastTime = 0
const DEDUPE_MS = 700

export function createInternalToastAPI() {
  function add(toast: Omit<Toast, 'id'>) {
    const now = Date.now()

    if (toast.message === lastMessage && now - lastTime < DEDUPE_MS) {
      lastTime = now
      return null
    }

    lastMessage = toast.message
    lastTime = now

    // Generate ID
    const id = `toast-${now}-${counter++}`

    // Batching animation: push then re-sort by position
    _toasts.value.push({ ...toast, id })
    sortToasts()

    return id
  }

  function remove(id: string) {
    const index = _toasts.value.findIndex((t) => t.id === id)
    if (index >= 0) _toasts.value.splice(index, 1)
  }

  function clear() {
    _toasts.value = []
  }

  function sortToasts() {
    // Group positions so animations don’t conflict
    _toasts.value = [..._toasts.value].sort((a, b) =>
      (a.position ?? 'bottom-right').localeCompare(b.position ?? 'bottom-right')
    )
  }

  const api = {
    toasts: readonly(_toasts),
    add,
    remove,
    clear,

    // Helper shortcuts
    success(message: string, title?: string, duration?: number) {
      return add({ type: 'success', message, title, duration })
    },

    error(message: string, title?: string, duration?: number) {
      return add({ type: 'error', message, title, duration })
    },

    warning(message: string, title?: string, duration?: number) {
      return add({ type: 'warning', message, title, duration })
    },

    info(message: string, title?: string, duration?: number) {
      return add({ type: 'info', message, title, duration })
    },

    toast(options: Omit<Toast, 'id'>) {
      return add(options)
    }
  }

  return api
}

// ===================
//  VueUse-style export
// ===================
export function useToast() {
  const api = inject('toast')
  if (!api) {
    console.error('❌ useToast() called before provider is registered.')
    return {
      toast: () => null,
      success: () => null,
      error: () => null,
      warning: () => null,
      info: () => null,
      remove: () => null,
      clear: () => null,
      toasts: readonly([])
    }
  }
  return api
}
