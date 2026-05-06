<!-- components/ui/GlobalToast.vue -->
<template>
  <Teleport to="body">
    <div
      v-for="pos in positions"
      :key="pos"
      class="toast-zone"
      :class="'pos-' + pos"
    >
      <transition-group name="toast-fade" tag="div">
        <BaseToast
          v-for="t in toasts.filter((x: Toast) => (x.position ?? 'bottom-right') === pos)"
          :key="t.id"
          v-bind="t"
          @close="remove(t.id)"
        />
      </transition-group>
    </div>
  </Teleport>
</template>

<script lang="ts" setup>
import BaseToast from './BaseToast.vue'
import { useToast, type Toast } from '@/composables/use-toast'

const { toasts, remove } = useToast()

const positions = [
  'top-left',
  'top-right',
  'bottom-left',
  'bottom-right',
  'top-center',
  'bottom-center'
] as const
</script>

<style scoped>
.toast-zone {
  position: fixed;
  z-index: 9999;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.toast-zone > * {
  pointer-events: auto;
}

/* bottom-right default */
.pos-bottom-right { bottom: 1rem; right: 1rem; }
.pos-bottom-left { bottom: 1rem; left: 1rem; }
.pos-bottom-center { bottom: 1rem; left: 50%; transform: translateX(-50%); }

.pos-top-right { top: 1rem; right: 1rem; }
.pos-top-left { top: 1rem; left: 1rem; }
.pos-top-center { top: 1rem; left: 50%; transform: translateX(-50%); }

/* Animations */
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.25s ease;
}

.toast-fade-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}
</style>
