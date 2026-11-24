
<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';

import { BaseSpinner } from '@/components/ui';
import SummaryReportTemplate from '@/components/SummaryReportTemplate.vue';

import {
  useSummaryReportData,
  type SummaryReportData
} from '@/composables/use-summary-report-data';

import { useMultiYearReportData } from '@/composables/use-multi-year-report-data';

const route = useRoute();

const programId = computed(() => Number(route.params.program_id));

// --------------------------------------------------
// Types
// --------------------------------------------------

interface Semester {
  id: number;
  name: string;
  code?: string;
  academicYear: string | number;
  startDate: string;
  endDate: string;
  isCurrent?: boolean;
}

// --------------------------------------------------
// Page state
// --------------------------------------------------

const reportMode = ref<'single' | 'range'>('single');

const semesters = ref<Semester[]>([]);
const loadingSemesters = ref(false);
const semesterError = ref<string | null>(null);

const selectedSemesterId = ref<number | null>(null);
const selectedStartSemesterId = ref<number | null>(null);
const selectedEndSemesterId = ref<number | null>(null);

const reportDataInternal = ref<SummaryReportData | null>(null);
const reportError = ref<string | null>(null);

const generatingReport = ref(false);
const savingReport = ref(false);

// Used to prevent an older request from replacing newer report data.
let requestVersion = 0;

// --------------------------------------------------
// Semester selection
// --------------------------------------------------

function semesterLabel(semester: Semester): string {
  return `${semester.name} (${semester.academicYear})`;
}

const selectedStartSemester = computed(() =>
  semesters.value.find(s => s.id === selectedStartSemesterId.value)
);

const selectedEndSemester = computed(() =>
  semesters.value.find(s => s.id === selectedEndSemesterId.value)
);

// Only allow ending semesters within six years of the starting semester.
const endSemesterOptions = computed(() => {
  if (!selectedStartSemester.value) return [];

  const startDate = new Date(selectedStartSemester.value.startDate);

  const maxDate = new Date(startDate);
  maxDate.setFullYear(maxDate.getFullYear() + 6);

  return semesters.value.filter(semester => {
    const semesterStart = new Date(semester.startDate);
    const semesterEnd = new Date(semester.endDate);

    return semesterStart >= startDate && semesterEnd <= maxDate;
  });
});

// --------------------------------------------------
// Single-semester report composable
// --------------------------------------------------

const singleReport = useSummaryReportData({
  programId,
  semesterId: selectedSemesterId
});

// --------------------------------------------------
// Multi-year report composable
// --------------------------------------------------

const multiYearProps = computed(() => ({
  programId: programId.value,
  startDate: selectedStartSemester.value?.startDate ?? '',
  endDate: selectedEndSemester.value?.endDate ?? ''
}));

const multiYearReport = useMultiYearReportData(multiYearProps);

// --------------------------------------------------
// Load semesters
// --------------------------------------------------

async function loadSemesters() {
  if (!programId.value || programId.value <= 0) {
    semesterError.value = 'Invalid program ID.';
    return;
  }

  loadingSemesters.value = true;
  semesterError.value = null;

  try {
    const response = await api.get('/semesters', {
      params: {
        programId: programId.value,
        page: 0,
        size: 1000
      }
    });

    const payload = response.data?.data ?? response.data;

    let semesterData: Semester[] = [];

    if (Array.isArray(payload)) {
      semesterData = payload;
    } else if (Array.isArray(payload?.content)) {
      semesterData = payload.content;
    } else if (Array.isArray(payload?.semesters)) {
      semesterData = payload.semesters;
    }

    semesters.value = semesterData.sort(
      (a, b) =>
        new Date(a.startDate).getTime() -
        new Date(b.startDate).getTime()
    );

    if (semesters.value.length === 0) {
      semesterError.value = 'No semesters were found for this program.';
      return;
    }

    // Default single-semester selection: current semester, or latest.
    const currentSemester =
      semesters.value.find(s => s.isCurrent) ??
      semesters.value[semesters.value.length - 1];

    selectedSemesterId.value = currentSemester.id;

    // Default range: earliest semester through the latest semester
    // that falls within the six-year limit.
    selectedStartSemesterId.value = semesters.value[0].id;

    const validEndSemesters = endSemesterOptions.value;

    selectedEndSemesterId.value =
      validEndSemesters[validEndSemesters.length - 1]?.id ??
      semesters.value[0].id;

  } catch (error: any) {
    console.error('Failed to load semesters:', error);

    semesterError.value =
      error.response?.data?.message ?? 'Failed to load semesters.';
  } finally {
    loadingSemesters.value = false;
  }
}

// --------------------------------------------------
// Single-semester report
// --------------------------------------------------

async function loadSingleReport() {
  if (!selectedSemesterId.value) {
    reportError.value = 'Please select a semester.';
    return;
  }

  const currentRequest = ++requestVersion;

  generatingReport.value = true;
  reportError.value = null;
  reportDataInternal.value = null;

  try {
    await singleReport.loadReportData();

    // Ignore results from outdated requests or another report mode.
    if (
      currentRequest !== requestVersion ||
      reportMode.value !== 'single'
    ) {
      return;
    }

    if (singleReport.error.value) {
      reportError.value = singleReport.error.value;
      return;
    }

    if (!singleReport.reportData.value) {
      reportError.value = 'No report data was returned.';
      return;
    }

    reportDataInternal.value = JSON.parse(
      JSON.stringify(singleReport.reportData.value)
    );

  } catch (error: any) {
    console.error('Failed to generate single-semester report:', error);

    if (currentRequest === requestVersion) {
      reportError.value =
        error.message ?? 'Failed to generate the summary report.';
    }
  } finally {
    if (currentRequest === requestVersion) {
      generatingReport.value = false;
    }
  }
}

// --------------------------------------------------
// Multi-year report
// --------------------------------------------------

function validateRange(): boolean {
  reportError.value = null;

  if (!selectedStartSemesterId.value || !selectedEndSemesterId.value) {
    reportError.value = 'Please select both a starting and ending semester.';
    return false;
  }

  if (!selectedStartSemester.value || !selectedEndSemester.value) {
    reportError.value = 'Invalid semester selection.';
    return false;
  }

  const startDate = new Date(selectedStartSemester.value.startDate);
  const endDate = new Date(selectedEndSemester.value.endDate);

  const maxDate = new Date(startDate);
  maxDate.setFullYear(maxDate.getFullYear() + 6);

  if (new Date(selectedEndSemester.value.startDate) < startDate) {
    reportError.value =
      'The ending semester cannot be before the starting semester.';
    return false;
  }

  if (endDate > maxDate) {
    reportError.value = 'The selected range cannot exceed six years.';
    return false;
  }

  return true;
}

async function loadMultiYearReport() {
  if (!validateRange()) return;

  const currentRequest = ++requestVersion;

  generatingReport.value = true;
  reportError.value = null;
  reportDataInternal.value = null;

  try {
    console.log('Multi-year report parameters:', multiYearProps.value);

    await multiYearReport.loadReportData();

    if (
      currentRequest !== requestVersion ||
      reportMode.value !== 'range'
    ) {
      return;
    }

    if (multiYearReport.error.value) {
      reportError.value = multiYearReport.error.value;
      return;
    }

    if (!multiYearReport.reportData.value) {
      reportError.value = 'No report data was returned.';
      return;
    }

    reportDataInternal.value = JSON.parse(
      JSON.stringify(multiYearReport.reportData.value)
    );

  } catch (error: any) {
    console.error('Failed to generate multi-year report:', error);

    if (currentRequest === requestVersion) {
      reportError.value =
        error.message ?? 'Failed to generate the multi-year report.';
    }
  } finally {
    if (currentRequest === requestVersion) {
      generatingReport.value = false;
    }
  }
}

// --------------------------------------------------
// Generate report
// --------------------------------------------------

async function generateReport() {
  if (generatingReport.value || savingReport.value) return;

  if (reportMode.value === 'single') {
    await loadSingleReport();
  } else {
    await loadMultiYearReport();
  }
}

// --------------------------------------------------
// Report mode and selection changes
// --------------------------------------------------

function changeReportMode(mode: 'single' | 'range') {
  if (reportMode.value === mode) return;

  // Invalidate any request that belongs to the previous mode.
  requestVersion++;

  reportMode.value = mode;
  reportDataInternal.value = null;
  reportError.value = null;
  generatingReport.value = false;
}

function handleStartSemesterChange() {
  const validOptions = endSemesterOptions.value;

  if (!validOptions.some(s => s.id === selectedEndSemesterId.value)) {
    selectedEndSemesterId.value =
      validOptions[validOptions.length - 1]?.id ?? null;
  }

  reportDataInternal.value = null;
  reportError.value = null;
}

// Automatically load the single-semester report when its semester changes
// or when the user switches back to Single Semester.
watch(
  [reportMode, selectedSemesterId, programId],
  ([mode, semesterId]) => {
    if (mode === 'single' && semesterId && programId.value > 0) {
      loadSingleReport();
    }
  }
);

// Reload semesters if the program changes.
watch(programId, () => {
  requestVersion++;
  reportDataInternal.value = null;
  selectedSemesterId.value = null;
  selectedStartSemesterId.value = null;
  selectedEndSemesterId.value = null;

  loadSemesters();
});

// --------------------------------------------------
// Save report
// --------------------------------------------------

async function saveReport() {
  if (!reportDataInternal.value || savingReport.value) return;

  savingReport.value = true;
  reportError.value = null;

  try {
    for (const outcome of reportDataInternal.value.outcomes ?? []) {
      for (const indicator of outcome.indicators ?? []) {
        for (const measure of indicator.measures ?? []) {
          const m = measure as any;

          if (!m.measureId) continue;

          await api.put(`/measure/${m.measureId}`, {
            id: m.measureId,
            courseIndicatorId: m.courseIndicatorId,
            description: m.description,
            observation: m.observation ?? m.note ?? '',
            recAction:
              m.recAction ?? m.recommendedAction ?? '',
            met: m.met ?? m.studentsMet ?? 0,
            exceeded: m.exceeded ?? m.studentsExceeded ?? 0,
            below: m.below ?? m.studentsBelow ?? 0,
            metPercentage: m.metPercentage ?? 0,
            status: 'Complete',
            active: true
          });
        }
      }
    }

    console.log('Report saved successfully.');

    // Reload whichever report mode is currently active.
    if (reportMode.value === 'single') {
      await loadSingleReport();
    } else {
      await loadMultiYearReport();
    }

  } catch (error: any) {
    console.error('Failed to save report:', error);

    reportError.value =
      error.response?.data?.message ?? 'Failed to save the report.';
  } finally {
    savingReport.value = false;
  }
}

// --------------------------------------------------
// Other template actions
// --------------------------------------------------

function handleImport() {
  console.log('Import report requested');
}

async function handleRegenerate() {
  await generateReport();
}

async function handleReload() {
  await generateReport();
}

// --------------------------------------------------
// Lifecycle
// --------------------------------------------------

onMounted(() => {
  loadSemesters();
});
</script>

<template>
  <div class="summary-report-page">

    <!-- Page heading -->
    <div class="page-header">
      <h1>Summary Reports</h1>
      <p>
        Select a single semester or a semester range to generate a report.
      </p>
    </div>

    <!-- Report selection -->
    <div class="report-selection">

      <div class="report-mode">
        <button
          type="button"
          :class="{ active: reportMode === 'single' }"
          @click="changeReportMode('single')"
        >
          Single Semester
        </button>

        <button
          type="button"
          :class="{ active: reportMode === 'range' }"
          @click="changeReportMode('range')"
        >
          Semester Range
        </button>
      </div>

      <!-- Single semester selector -->
      <div
        v-if="reportMode === 'single'"
        class="semester-selection"
      >
        <label for="single-semester">Select Semester</label>

        <select
          id="single-semester"
          v-model.number="selectedSemesterId"
          :disabled="loadingSemesters || semesters.length === 0"
        >
          <option :value="null" disabled>
            Select a semester
          </option>

          <option
            v-for="semester in semesters"
            :key="semester.id"
            :value="semester.id"
          >
            {{ semesterLabel(semester) }}
          </option>
        </select>
      </div>

      <!-- Semester range selectors -->
      <div
        v-else
        class="semester-range-selection"
      >
        <div class="semester-selection">
          <label for="start-semester">Starting Semester</label>

          <select
            id="start-semester"
            v-model.number="selectedStartSemesterId"
            :disabled="loadingSemesters || semesters.length === 0"
            @change="handleStartSemesterChange"
          >
            <option :value="null" disabled>
              Select a starting semester
            </option>

            <option
              v-for="semester in semesters"
              :key="semester.id"
              :value="semester.id"
            >
              {{ semesterLabel(semester) }}
            </option>
          </select>
        </div>

        <div class="semester-selection">
          <label for="end-semester">Ending Semester</label>

          <select
            id="end-semester"
            v-model.number="selectedEndSemesterId"
            :disabled="!selectedStartSemesterId || loadingSemesters"
          >
            <option :value="null" disabled>
              Select an ending semester
            </option>

            <option
              v-for="semester in endSemesterOptions"
              :key="semester.id"
              :value="semester.id"
            >
              {{ semesterLabel(semester) }}
            </option>
          </select>
        </div>
      </div>

      <!-- Generate button -->
      <button
        type="button"
        class="generate-button"
        :disabled="loadingSemesters || generatingReport || savingReport"
        @click="generateReport"
      >
        {{ generatingReport ? 'Generating...' : 'Generate Report' }}
      </button>

    </div>

    <!-- Semester loading -->
    <div v-if="loadingSemesters" class="status-message">
      Loading semesters...
    </div>

    <!-- Semester error -->
    <div v-if="semesterError" class="error-message">
      {{ semesterError }}
    </div>

    <!-- Report loading -->
    <div v-if="generatingReport" class="loading-state">
      <BaseSpinner
        size="lg"
        :text="reportMode === 'single'
          ? 'Generating summary report...'
          : 'Generating multi-year report...'"
      />
    </div>

    <!-- Report error -->
    <div
      v-else-if="reportError"
      class="error-state"
    >
      <p>{{ reportError }}</p>

      <button
        type="button"
        class="generate-button"
        @click="generateReport"
      >
        Retry
      </button>
    </div>

    <!-- Shared report template -->
    <div
      v-else-if="reportDataInternal"
      class="report-content"
    >
      <SummaryReportTemplate
        v-model:report="reportDataInternal"
        student-count-label="Students in Course:"
        @save="saveReport"
        @import="handleImport"
        @regenerate="handleRegenerate"
        @reload="handleReload"
      />
    </div>

    <!-- Empty state -->
    <div
      v-else-if="!loadingSemesters && !semesterError"
      class="empty-state"
    >
      <p v-if="reportMode === 'single'">
        Select a semester to view its summary report.
      </p>

      <p v-else>
        Select a starting and ending semester, then generate your report.
      </p>
    </div>

  </div>
</template>

<style scoped>
.summary-report-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  margin-bottom: 0.5rem;
}

.page-header h1 {
  margin-bottom: 0.4rem;
}

.page-header p {
  color: var(--text-secondary, #666);
}

.report-selection {
  padding: 1.5rem;
  border: 1px solid var(--border-color, #444);
  border-radius: 8px;
  background: var(--card-background, #1e1e1e);
  color: var(--text-primary, #ffffff);
}

.report-mode {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.report-mode button {
  padding: 0.65rem 1rem;
  border: 1px solid var(--border-color, #555);
  border-radius: 6px;
  background: var(--input-background, #2d2d2d);
  color: var(--text-primary, #ffffff);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.report-mode button.active {
  background: var(--primary-color, #2563eb);
  border-color: var(--primary-color, #2563eb);
  color: #ffffff;
}

.semester-selection {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.semester-selection label {
  font-weight: 600;
  color: var(--text-primary, #ffffff);
}

.semester-selection select {
  width: 100%;
  min-width: 200px;
  padding: 0.7rem;
  border: 1px solid var(--border-color, #555);
  border-radius: 6px;
  background: var(--input-background, #2d2d2d);
  color: var(--text-primary, #ffffff);
}

.semester-range-selection {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.generate-button {
  padding: 0.75rem 1.25rem;
  border: none;
  border-radius: 6px;
  background: var(--primary-color, #2563eb);
  color: white;
  cursor: pointer;
}

.generate-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status-message {
  margin: 1rem 0;
}

.error-message,
.error-state {
  color: var(--color-error, #dc2626);
}

.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 1rem;
}

.report-content {
  margin-top: 1rem;
}

@media (max-width: 650px) {
  .semester-range-selection {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .report-mode {
    flex-direction: column;
  }
}
</style>
