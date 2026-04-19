<template>
  <div class="measure-item">

    <!-- MAIN LINE -->
    <div class="measure-main">
      <router-link v-if="measure.measureId" :to="{ name: 'FCAR', params: { measure_id: measure.measureId } }" class="fcar-link">
        <span class="measure-description">{{ measure.description }}</span>
      </router-link>

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
    <div class="recommended-row">
      <div v-if="measure.recommendedAction" class="measure-note recommended-block">
        <strong>Recommended Action:</strong><br />
        {{ measure.recommendedAction }}
      </div>

      <button
        v-if="isEditing && !measure.recommendedAction"
        class="add-action-btn"
        @click="openModal"
      >
        + Add Recommended Action
      </button>

      <button
        v-if="isEditing && measure.recommendedAction"
        class="pencil-btn"
        title="Edit recommended action"
        @click="openModal"
      >
        <img src="@/assets/icons/edit-pencil.svg" alt="Edit" class="pencil-icon" />
      </button>
    </div>

    <!-- RECOMMENDED ACTION MODAL -->
    <BaseModal
      :is-open="modalOpen"
      title="Recommended Action"
      size="md"
      @close="closeModal"
    >
      <textarea
        v-model="modalText"
        class="action-textarea"
        rows="5"
        placeholder="Enter recommended action..."
      />
      <template #footer>
        <div class="modal-actions">
          <BaseButton variant="primary" @click="saveModal">Save</BaseButton>
          <BaseButton variant="secondary" @click="closeModal">Cancel</BaseButton>
        </div>
      </template>
    </BaseModal>

  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch, computed } from "vue";
import { BaseModal, BaseButton } from "@/components/ui";

const props = defineProps<{ measure: any; editable: boolean; isEditing: boolean }>();
const emit = defineEmits(["update:measure"]);

const modalOpen = ref(false);
const modalText = ref('');

function openModal() {
  modalText.value = props.measure.recommendedAction ?? '';
  modalOpen.value = true;
}

function closeModal() {
  modalOpen.value = false;
}

function saveModal() {
  emit("update:measure", {
    ...props.measure,
    recommendedAction: modalText.value.trim() || null
  });
  modalOpen.value = false;
}

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

.fcar-link{
  text-decoration: none;
  color: white;
}

.fcar-link:hover{
  color: green;
}

.status-met-comfortably { color: var(--color-success-dark); }
.status-met            { color: var(--color-info); }
.status-barely-not-met { color: var(--color-warning); }
.status-not-met        { color: var(--color-error); }

.recommended-row {
  display: flex;
  align-items: flex-start;
  gap: .5rem;
  margin-top: .5rem;
}

.recommended-block {
  flex: 1;
  margin-top: 0;
}

.add-action-btn {
  background: none;
  border: 1px dashed var(--color-border-light);
  border-radius: .25rem;
  color: var(--color-primary);
  cursor: pointer;
  font-size: .85rem;
  padding: .35rem .75rem;
}

.add-action-btn:hover {
  background: var(--color-bg-tertiary);
}

.pencil-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: .25rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.pencil-icon {
  width: 18px;
  height: 18px;
  filter: invert(1);
}

.action-textarea {
  width: 100%;
  padding: .5rem;
  border: 1px solid var(--color-border-light);
  border-radius: .25rem;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-size: .9rem;
  resize: vertical;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  gap: .5rem;
  justify-content: flex-end;
}
</style>
