<template>
  <Transition
    @before-enter="beforeEnter"
    @enter="enter"
    @after-enter="afterEnter"

    @before-leave="beforeLeave"
    @leave="leave"
    @after-leave="afterLeave"
  >
    <div v-show="show" class="collapse-wrapper">
      <slot />
    </div>
  </Transition>
</template>

<script setup lang="ts">
const props = defineProps<{ show: boolean }>();

const DURATION = 250;

// ---- ENTER (expand) ----
function beforeEnter(el: HTMLElement) {
  el.style.height = "0";
  el.style.opacity = "0";
  el.style.overflow = "hidden";
}

function enter(el: HTMLElement) {
  const height = el.scrollHeight;

  el.style.transition = `height ${DURATION}ms ease, opacity ${DURATION}ms ease`;

  requestAnimationFrame(() => {
    el.style.height = height + "px";
    el.style.opacity = "1";
  });
}

function afterEnter(el: HTMLElement) {
  el.style.transition = "";
  el.style.height = "auto";
  el.style.opacity = "1";
  el.style.overflow = "visible";
}

// ---- LEAVE (collapse) ----
function beforeLeave(el: HTMLElement) {
  el.style.height = el.scrollHeight + "px";
  el.style.opacity = "1";
  el.style.overflow = "hidden";
}

function leave(el: HTMLElement) {
  el.style.transition = `height ${DURATION}ms ease, opacity ${DURATION}ms ease`;

  requestAnimationFrame(() => {
    el.style.height = "0px";
    el.style.opacity = "0";
  });
}

function afterLeave(el: HTMLElement) {
  el.style.transition = "";
  el.style.height = "";
  el.style.opacity = "";
  el.style.overflow = "";
}
</script>

<style scoped>
.collapse-wrapper {
  overflow: hidden;     /* keep this */
  transition: height 0.25s ease, opacity 0.25s ease;
}
</style>
