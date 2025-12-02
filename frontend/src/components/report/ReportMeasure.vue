<template>
  <div class="measure-item">

    <!-- MAIN LINE -->
    <div class="measure-main">
      <span class="measure-description">{{ measure.description }}</span>

      <div class="measure-status-group">
        <span class="measure-status" :class="statusClass">
          {{ status }}
        </span>
        <span class="measure-percentage">
          ({{ metPercentage }}%)
        </span>
      </div>
    </div>

    <!-- NOTE -->
    <div v-if="measure.note" class="measure-note">
      {{ measure.note }}
    </div>

    <!-- STAT GRID -->
    <div class="measure-stats">
      <!-- EXCEEDED -->
      <div class="stat-block">
        <strong>Exceeded:</strong>
        <template v-if="editable">
          <input
            type="number"
            min="0"
            v-model.number="local.studentsExceeded"
            @input="emitUpdate"
            class="edit-input"
          />
        </template>
        <span v-else>{{ local.studentsExceeded }}</span>
      </div>

      <!-- MET -->
      <div class="stat-block">
        <strong>Met:</strong>
        <template v-if="editable">
          <input
            type="number"
            min="0"
            v-model.number="local.studentsMet"
            @input="emitUpdate"
            class="edit-input"
          />
        </template>
        <span v-else>{{ local.studentsMet }}</span>
      </div>

      <!-- BELOW -->
      <div class="stat-block">
        <strong>Below:</strong>
        <template v-if="editable">
          <input
            type="number"
            min="0"
            v-model.number="local.studentsBelow"
            @input="emitUpdate"
            class="edit-input"
          />
        </template>
        <span v-else>{{ local.studentsBelow }}</span>
      </div>
    </div>

    <!-- RECOMMENDED ACTION -->
    <div v-if="measure.recommendedAction" class="measure-note recommended-block">
      <strong>Recommended Action:</strong><br />
      {{ measure.recommendedAction }}
    </div>

  </div>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from "vue";

const props = defineProps<{ measure: any; editable: boolean }>();
const emit = defineEmits(["update:measure"]);

const local = reactive({
  studentsExceeded: props.measure.studentsExceeded ?? 0,
  studentsMet: props.measure.studentsMet ?? 0,
  studentsBelow: props.measure.studentsBelow ?? 0
});

// Sync when parent replaces measure
watch(
  () => props.measure,
  newVal => {
    local.studentsExceeded = newVal.studentsExceeded ?? 0;
    local.studentsMet = newVal.studentsMet ?? 0;
    local.studentsBelow = newVal.studentsBelow ?? 0;
  },
  { deep: true }
);

// Percent calculation
const metPercentage = computed(() => {
  const total = local.studentsExceeded + local.studentsMet + local.studentsBelow;
  return total === 0 ? 0 : Math.round(((local.studentsExceeded + local.studentsMet) / total) * 100);
});

// UI only classification
const status = computed(() => {
  const pct = metPercentage.value;
  if (pct >= 85) return "Met comfortably";
  if (pct >= 70) return "Met";
  if (pct >= 60) return "Barely not met";
  return "Not met";
});

const statusClass = computed(() => ({
  "status-met-comfortably": status.value === "Met comfortably",
  "status-met": status.value === "Met",
  "status-barely-not-met": status.value === "Barely not met",
  "status-not-met": status.value === "Not met"
}));

function emitUpdate() {
  emit("update:measure", {
    ...props.measure,                     // preserve backend fields
    courseIndicatorId: props.measure.courseIndicatorId,

    studentsExceeded: local.studentsExceeded,
    studentsMet: local.studentsMet,
    studentsBelow: local.studentsBelow,

    metPercentage: metPercentage.value    // saved
  });
}
</script>

<style scoped>
.measure-item {
  background: var(--color-bg-secondary);
  border-radius: .375rem;
  padding: .75rem;
}

.measure-main {
  font-size: .9rem;
  display: flex;
  justify-content: space-between;
}

.measure-status-group {
  display: flex;
  gap: .25rem;
  align-items: center;
}

.measure-stats {
  margin-top: .75rem;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: .75rem;
  font-size: .85rem;
}

.stat-block {
  background: var(--color-bg-tertiary);
  padding: .5rem;
  border-radius: .25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-input {
  width: 60px;
  padding: .35rem;
  border: 1px solid var(--color-border-light);
  border-radius: .25rem;
}

.measure-note {
  margin-top: .5rem;
  padding: .5rem;
  background: var(--color-bg-tertiary);
  border-radius: .25rem;
  font-style: italic;
}

.status-met-comfortably { color: var(--color-success-dark); }
.status-met            { color: var(--color-info); }
.status-barely-not-met { color: var(--color-warning); }
.status-not-met        { color: var(--color-error); }
</style>
