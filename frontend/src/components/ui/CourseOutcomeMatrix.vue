<script setup lang="ts">
import { reactive } from 'vue';
import { useScheduleStore } from '@/stores/scheduleStore';
import { normalizePiIndexList, validatePiIndexList } from '@/utils/scheduleValidation';

const store = useScheduleStore();
const cellErrors = reactive<Record<string, string>>({});

function cellKey(course: string, outcome: number) {
  return `${course}:${outcome}`;
}

function piLimit(outcome: number) {
  return store.piCountForOutcome(outcome);
}

async function commitCell(course: string, outcome: number) {
  const raw = store.courseOutcomeMap[course]?.[outcome] ?? '';
  const error = validatePiIndexList(raw, piLimit(outcome));
  cellErrors[cellKey(course, outcome)] = error ?? '';
  if (!error && store.courseOutcomeMap[course]) {
    store.courseOutcomeMap[course][outcome] = normalizePiIndexList(raw);
    await store.syncCourseOutcomeIndicators(course, outcome);
  }
}

function onCellInput(course: string, outcome: number) {
  const raw = store.courseOutcomeMap[course]?.[outcome] ?? '';
  cellErrors[cellKey(course, outcome)] = validatePiIndexList(raw, piLimit(outcome)) ?? '';
}

function isUnmapped(course: string) {
  return store.unmappedCourses.includes(course);
}

function placeholder(outcome: number) {
  const limit = piLimit(outcome);
  return limit <= 0 ? '' : `1–${limit}`;
}
</script>

<template>
  <section class="mapping-section">
    <h2>Course / Outcome PI Mapping</h2>
    <p class="hint">Each cell lists PI numbers for that course and outcome. Numbers must match the PIs for that outcome in the table above (for example 1, 2).</p>

    <div v-if="store.unmappedCourses.length" class="empty-alert" role="status">
      <strong>Add at least one PI to these courses.</strong>
      A course with no mapped performance indicators does not belong in this table:
      <span>{{ store.unmappedCourses.join(', ') }}</span>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
        <tr>
          <th class="col-header">Course</th>
          <th class="col-header" v-for="outcome in store.outcomes" :key="outcome">
            {{ outcome }}
            <span class="pi-range">PIs 1-{{ piLimit(outcome) }}</span>
          </th>
        </tr>
        </thead>
        <tbody>
        <tr
          v-for="course in store.courses"
          :key="course"
          :class="{ 'unmapped-row': isUnmapped(course) }"
        >
          <th class="row-header">
            {{ course }}
            <span v-if="isUnmapped(course)" class="row-alert">Needs PIs</span>
          </th>
          <td v-for="outcome in store.outcomes" :key="outcome">
            <input
              v-if="store.courseOutcomeMap[course]"
              type="text"
              v-model="store.courseOutcomeMap[course][outcome]"
              class="cell-input"
              :class="{ invalid: cellErrors[cellKey(course, outcome)] }"
              :placeholder="placeholder(outcome)"
              :title="cellErrors[cellKey(course, outcome)] || `PI numbers for outcome ${outcome} (1–${piLimit(outcome)})`"
              @input="onCellInput(course, outcome)"
              @blur="commitCell(course, outcome)"
            />
          </td>
        </tr>
        </tbody>
      </table>
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

h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
  text-align: center;
}

.hint {
  margin: 0 0 1.25rem;
  text-align: center;
  color: #9ca3af;
  font-size: 0.85rem;
}

.empty-alert {
  background: rgba(245, 158, 11, 0.12);
  border: 1px solid #d97706;
  color: #fbbf24;
  border-radius: 6px;
  padding: 0.85rem 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  line-height: 1.45;
}

.empty-alert span {
  font-weight: 700;
  color: #fde68a;
}

.table-container {
  overflow-x: auto;
  border: 1px solid #333333;
  border-radius: 4px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #181818;
}

.data-table th,
.data-table td {
  border: 1px solid #333333;
  padding: 0.75rem 0.4rem;
  text-align: center;
  color: #ffffff;
}

.col-header,
.row-header {
  background: #242424;
  color: #e2e8f0;
  font-size: 0.875rem;
  font-weight: 600;
}

.pi-range {
  display: block;
  font-size: 0.7rem;
  font-weight: 500;
  color: #9ca3af;
  margin-top: 0.2rem;
}

.row-header {
  width: 90px;
  font-weight: 700;
  text-align: center;
}

.row-alert {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.68rem;
  font-weight: 700;
  color: #fbbf24;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.unmapped-row .row-header {
  background: #3a2a12;
}

.cell-input {
  width: 100%;
  border: none;
  background: transparent;
  text-align: center;
  color: #ffffff;
  font-size: 0.95rem;
  padding: 0.25rem;
}

.cell-input:focus {
  outline: 1px solid #22c55e;
  background: #121212;
}

.cell-input.invalid {
  outline: 1px solid #ef4444;
  color: #f87171;
}
</style>
