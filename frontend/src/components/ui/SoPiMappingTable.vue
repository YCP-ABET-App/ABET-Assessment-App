<script setup lang="ts">
import { computed, ref } from 'vue';
import { useScheduleStore } from '@/stores/scheduleStore';
import { useScheduleModals } from '@/composables/useScheduleModals';
import type { OutcomeMapping, PerformanceIndicatorDetail } from '@/types/schedule';

const store = useScheduleStore();
const {
  openRubricModal,
  pendingDelete,
  deleteStep,
  DELETE_PI_STEPS,
  beginDeletePi,
  closeDeletePi,
  backDeletePi,
  continueDeletePi,
} = useScheduleModals();
const tableError = ref('');

const maxPiCount = computed(() =>
  Math.max(1, ...store.soPiMap.map(so => so.pis.length)),
);

const currentDeleteStep = computed(() => DELETE_PI_STEPS[deleteStep.value]);

function addOutcome() {
  tableError.value = '';
  const so = store.addStudentOutcome();
  if (so.pis[0]) openRubricModal(so, so.pis[0]);
}

function addPi(so: OutcomeMapping) {
  tableError.value = '';
  const pi = store.addPerformanceIndicator(so.id);
  if (pi) openRubricModal(so, pi);
}

function requestRemovePi(so: OutcomeMapping, pi: PerformanceIndicatorDetail, event: Event) {
  event.stopPropagation();
  if (so.pis.length <= 1) {
    tableError.value = 'Each student outcome needs at least one performance indicator.';
    return;
  }
  tableError.value = '';
  beginDeletePi(so.id, pi.name);
}

function confirmRemovePi() {
  tableError.value = continueDeletePi() ?? '';
}

function openPi(so: OutcomeMapping, pi: PerformanceIndicatorDetail) {
  openRubricModal(so, pi);
}
</script>

<template>
  <section class="mapping-section">
    <details class="so-pi-dropdown">
      <summary>SO-PI Desc. Map Dropdown</summary>
      <div class="dropdown-content">
        <div class="table-toolbar">
          <p class="hint">Open a performance indicator to edit the student outcome text. Student outcomes cannot be removed from this table.</p>
          <button class="btn-save btn-small" type="button" @click="addOutcome">+ Add SO</button>
        </div>

        <p v-if="tableError" class="field-error">{{ tableError }}</p>

        <table class="data-table">
          <thead>
          <tr>
            <th class="col-so-num">SO #</th>
            <th :colspan="maxPiCount">Performance Indicators</th>
            <th class="col-actions"> </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="so in store.soPiMap" :key="so.id">
            <td class="so-num-cell">
              <div>{{ so.id }}</div>
              <p class="so-snippet" :class="{ missing: !so.description }">
                {{ so.description || 'No description yet — open a PI to add one.' }}
              </p>
            </td>
            <td
              v-for="index in maxPiCount"
              :key="`${so.id}-${index}`"
              class="pi-cell"
              :class="{ empty: !so.pis[index - 1] }"
              @click="so.pis[index - 1] && openPi(so, so.pis[index - 1])"
            >
              <template v-if="so.pis[index - 1]">
                <span :title="so.pis[index - 1].definition || 'Needs a PI definition'">
                  {{ so.pis[index - 1].name }}
                </span>
                <button
                  class="chip-remove"
                  type="button"
                  title="Remove PI"
                  @click="requestRemovePi(so, so.pis[index - 1], $event)"
                >×</button>
              </template>
            </td>
            <td class="col-actions">
              <button class="btn-save btn-small" type="button" @click="addPi(so)">+ PI</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </details>

    <div v-if="pendingDelete && currentDeleteStep" class="modal-overlay" @click.self="closeDeletePi">
      <div class="modal-content">
        <h3>{{ currentDeleteStep.title }}</h3>
        <div class="warning-wrapper">
          <div class="warning-icon">⚠️</div>
          <p>Removing <strong>{{ pendingDelete.piName }}</strong> from Outcome {{ pendingDelete.outcomeId }}.</p>
          <p>{{ currentDeleteStep.body }}</p>
          <p class="step-count">Warning {{ deleteStep + 1 }} of {{ DELETE_PI_STEPS.length }}</p>
        </div>
        <p v-if="tableError" class="field-error">{{ tableError }}</p>
        <div class="modal-actions">
          <button class="btn-cancel" type="button" @click="backDeletePi">
            {{ deleteStep === 0 ? 'Cancel' : 'Back' }}
          </button>
          <button class="btn-danger" type="button" @click="confirmRemovePi">
            {{ deleteStep < DELETE_PI_STEPS.length - 1 ? 'Continue' : 'Permanently Delete' }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.mapping-section {
  background: #282828;
  padding: 1.5rem;
  border-radius: 6px;
  border: 1px solid #383838;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  margin-bottom: 2rem;
}

details.so-pi-dropdown summary {
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  padding: 0.85rem 1rem;
  background: #2d2d2d;
  border: 1px solid #4a4a4a;
  border-radius: 4px;
  color: #ffffff;
  text-align: center;
}

details.so-pi-dropdown summary:hover {
  background: #353535;
}

.dropdown-content {
  margin-top: 1.25rem;
  overflow-x: auto;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.85rem;
}

.hint {
  margin: 0;
  color: #9ca3af;
  font-size: 0.85rem;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #181818;
}

.data-table th,
.data-table td {
  border: 1px solid #333333;
  padding: 0.85rem 1rem;
  color: #ffffff;
  font-size: 0.9rem;
}

.col-so-num { width: 220px; }
.col-actions { width: 90px; text-align: center; }
.so-num-cell { font-weight: 700; text-align: center; }

.so-snippet {
  margin: 0.4rem 0 0;
  font-weight: 400;
  font-size: 0.78rem;
  color: #9ca3af;
  line-height: 1.35;
}

.so-snippet.missing {
  color: #fbbf24;
}

.pi-cell {
  cursor: pointer;
  color: #e2e8f0;
  text-align: center;
  transition: background 0.15s ease;
}

.pi-cell.empty {
  cursor: default;
}

.pi-cell:not(.empty):hover {
  background: #2a2a2a;
  color: #4ade80;
  text-decoration: underline;
}

.chip-remove {
  background: transparent;
  border: none;
  color: #ef4444;
  font-size: 0.95rem;
  cursor: pointer;
  margin-left: 0.35rem;
  font-weight: bold;
}

.chip-remove:hover { color: #f87171; }

.field-error {
  margin: 0.4rem 0 0;
  color: #f87171;
  font-size: 0.8rem;
}

.btn-cancel,
.btn-save,
.btn-danger {
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  color: #ffffff;
}

.btn-save { background: #22c55e; }
.btn-save:hover { background: #16a34a; }
.btn-cancel { background: #4b5563; }
.btn-cancel:hover { background: #374151; }
.btn-danger { background: #dc2626; }
.btn-danger:hover { background: #b91c1c; }
.btn-small { padding: 0.35rem 0.65rem; font-size: 0.8rem; margin: 0.15rem; }

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: #2c2c2c;
  color: #ffffff;
  padding: 2rem;
  border-radius: 8px;
  width: 480px;
  max-width: 90vw;
  border: 1px solid #383838;
}

.modal-content h3 {
  text-align: center;
  margin: 0 0 1rem;
}

.warning-wrapper {
  text-align: center;
}

.warning-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.warning-wrapper p {
  color: #d1d5db;
  line-height: 1.5;
}

.step-count {
  font-size: 0.8rem;
  color: #9ca3af !important;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.modal-actions button {
  padding: 0.5rem 1.15rem;
}
</style>
