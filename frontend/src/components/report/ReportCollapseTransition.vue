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
function beforeEnter(el: Element) {
  const element = el as HTMLElement;
  element.style.height = "0";
  element.style.opacity = "0";
  element.style.overflow = "hidden";
}

function enter(el: Element) {
  const element = el as HTMLElement;
  const height = element.scrollHeight;

  element.style.transition = `height ${DURATION}ms ease, opacity ${DURATION}ms ease`;

  requestAnimationFrame(() => {
    element.style.height = height + "px";
    element.style.opacity = "1";
  });
}

function afterEnter(el: Element) {
  const element = el as HTMLElement;
  element.style.transition = "";
  element.style.height = "auto";
  element.style.opacity = "1";
  element.style.overflow = "visible";
}

// ---- LEAVE (collapse) ----
function beforeLeave(el: Element) {
  const element = el as HTMLElement;
  element.style.height = element.scrollHeight + "px";
  element.style.opacity = "1";
  element.style.overflow = "hidden";
}

function leave(el: Element) {
  const element = el as HTMLElement;
  element.style.transition = `height ${DURATION}ms ease, opacity ${DURATION}ms ease`;

  requestAnimationFrame(() => {
    element.style.height = "0px";
    element.style.opacity = "0";
  });
}

function afterLeave(el: Element) {
  const element = el as HTMLElement;
  element.style.transition = "";
  element.style.height = "";
  element.style.opacity = "";
  element.style.overflow = "";
}
</script>

<style scoped>
.collapse-wrapper {
  overflow: hidden;     /* keep this */
  transition: height 0.25s ease, opacity 0.25s ease;
}
</style>
