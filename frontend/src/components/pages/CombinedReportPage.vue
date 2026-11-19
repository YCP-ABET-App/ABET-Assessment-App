
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';


import SummaryReportTemplate from '@/components/SummaryReportTemplate.vue';
import { useSummaryReportData } from '@/composables/use-summary-report-data';
import { useMultiYearReportData } from '@/composables/use-multi-year-report-data';


const route = useRoute();
const programId = computed(() => Number(route.params.program_id));



interface Semester {
  id: number;
  name: string;
  code?: string;
  academicYear: string | number;
  startDate: string;
  endDate: string;
  isCurrent?: boolean;
}


const reportMode = ref<'single' | 'range'>('single');

const semesters = ref<Semester[]>([]);
const loadingSemesters = ref(false);
const semesterError = ref<string | null>(null);

const selectedSemesterId = ref<number | null>(null);
const selectedStartSemesterId = ref<number | null>(null);
const selectedEndSemesterId = ref<number | null>(null);

const reportDataInternal = ref<any>(null);
const reportError = ref<string | null>(null);
const generatingReport = ref(false);





function semesterLabel(semester: Semester): string {
  const year = semester.academicYear ?? '';
  return `${semester.name} ${year}`;
}

const selectedStartSemester = computed(() =>
  semesters.value.find(s => s.id === selectedStartSemesterId.value)
);

const selectedEndSemester = computed(() =>
  semesters.value.find(s => s.id === selectedEndSemesterId.value)
);

// Keep the range within six years, as in the existing multi-year page.
const endSemesterOptions = computed(() => {
  if (!selectedStartSemester.value) return [];

  const startDate = new Date(selectedStartSemester.value.startDate);
  const maxDate = new Date(startDate);
  maxDate.setFullYear(maxDate.getFullYear() + 6);

  return semesters.value.filter(semester => {
    const endDate = new Date(semester.endDate);
    return endDate >= startDate && endDate <= maxDate;
  });
});



// Single-semester report
const singleReport = useSummaryReportData({
  programId,
  semesterId: selectedSemesterId
});

// Multi-year report
const multiYearProps = computed(() => ({
  programId: programId.value,
  startDate: selectedStartSemester.value?.startDate ?? '',
  endDate: selectedEndSemester.value?.endDate ?? ''
}));

const multiYearReport = useMultiYearReportData(multiYearProps);



async function loadSemesters() {
  if (!programId.value || programId.value <= 0) {
    semesterError.value = 'Invalid program ID';
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

    console.log('Semester API response:', response.data);

    const payload = response.data?.data ?? response.data;

    let semesterData: Semester[] = [];

    if (Array.isArray(payload)) {
      semesterData = payload;
    } else if (Array.isArray(payload?.content)) {
      semesterData = payload.content;
    } else if (Array.isArray(payload?.semesters)) {
      semesterData = payload.semesters;
    }

    console.log('Parsed semesters:', semesterData);

    if (semesterData.length === 0) {
      console.error('Could not find semester array in API response:', payload);
      semesterError.value = 'No semesters were found or the semester response was unexpected.';
      return;
    }

    semesters.value = [...semesterData].sort(
      (a, b) =>
        new Date(a.startDate).getTime() -
        new Date(b.startDate).getTime()
    );

    if (semesters.value.length === 0) {
      semesterError.value = 'No semesters were found for this program.';
      return;
    }


    const currentSemester =
      semesters.value.find(s => s.isCurrent) ?? semesters.value[semesters.value.length - 1];

    selectedSemesterId.value = currentSemester.id;


    selectedStartSemesterId.value = semesters.value[0].id;
    selectedEndSemesterId.value = semesters.value[semesters.value.length - 1].id;


    const validEndSemesters = endSemesterOptions.value;
    if (
      !validEndSemesters.some(s => s.id === selectedEndSemesterId.value)
    ) {
      selectedEndSemesterId.value =
        validEndSemesters[validEndSemesters.length - 1]?.id ?? null;
    }
  } catch (error: any) {
    console.error('Failed to load semesters:', error);
    semesterError.value =
      error.response?.data?.message ?? 'Failed to load semesters.';
  } finally {
    loadingSemesters.value = false;
  }
}



async function generateReport() {

  console.log('========== GENERATE REPORT ==========');
  console.log('Program ID:', programId.value);
  console.log('Report mode:', reportMode.value);
  console.log('Selected semester:', selectedSemesterId.value);
  console.log('Start semester:', selectedStartSemester.value);
  console.log('End semester:', selectedEndSemester.value);

  reportError.value = null;
  reportDataInternal.value = null;

  if (reportMode.value === 'single') {
    if (!selectedSemesterId.value) {
      reportError.value = 'Please select a semester.';
      return;
    }

    generatingReport.value = true;

    try {
      await singleReport.loadReportData();

      console.log('Single report loading:', singleReport.loading.value);
      console.log('Single report error:', singleReport.error.value);
      console.log('Single report data:', singleReport.reportData.value);

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
      reportError.value =
        error.message ?? 'Failed to generate the report.';
    } finally {
      generatingReport.value = false;
    }

    return;
  }

  // Multi-year report
  if (!selectedStartSemesterId.value || !selectedEndSemesterId.value) {
    reportError.value = 'Please select both a starting and ending semester.';
    return;
  }

  if (
    new Date(selectedEndSemester.value!.endDate) <
    new Date(selectedStartSemester.value!.startDate)
  ) {
    reportError.value = 'The ending semester cannot be before the starting semester.';
    return;
  }

  generatingReport.value = true;

  try {
    await multiYearReport.loadReportData();

    console.log('Multi-year report loading:', multiYearReport.loading.value);
    console.log('Multi-year report error:', multiYearReport.error.value);
    console.log('Multi-year report data:', multiYearReport.reportData.value);

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
    console.error('Failed to generate semester-range report:', error);
    reportError.value =
      error.message ?? 'Failed to generate the report.';
  } finally {
    generatingReport.value = false;
  }
}


async function saveReport() {
  if (!reportDataInternal.value) return;

  try {
    const outcomes = reportDataInternal.value.outcomes ?? [];

    for (const outcome of outcomes) {
      for (const indicator of outcome.indicators ?? []) {
        for (const measure of indicator.measures ?? []) {
          if (!measure.measureId) continue;

          await api.put(`/measure/${measure.measureId}`, {
            id: measure.measureId,
            courseIndicatorId: measure.courseIndicatorId,
            description: measure.description,
            observation: measure.observation ?? measure.note ?? '',
            recAction:
              measure.recAction ??
              measure.recommendedAction ??
              '',
            met: measure.met ?? measure.studentsMet ?? 0,
            exceeded: measure.exceeded ?? measure.studentsExceeded ?? 0,
            below: measure.below ?? measure.studentsBelow ?? 0,
            metPercentage: measure.metPercentage ?? 0,
            status: 'Complete',
            active: true
          });
        }
      }
    }

    await generateReport();
  } catch (error: any) {
    console.error('Failed to save report:', error);
    reportError.value =
      error.response?.data?.message ?? 'Failed to save the report.';
  }
}





function handleImport() {
  console.log('Import report requested');
}

async function handleRegenerate() {
  await generateReport();
}

async function handleReload() {
  await generateReport();
}



onMounted(() => {
  loadSemesters();
});
</script>

<template>
  <div class="summary-report-page">
    <div class="page-header">
      <h1>Summary Reports</h1>
      <p>Select the type of report and the semester or semester range.</p>
    </div>

    <div class="report-selection">
      <div class="report-mode">
        <button
          type="button"
          :class="{ active: reportMode === 'single' }"
          @click="reportMode = 'single'; reportDataInternal = null; reportError = null"
        >
          Single Semester
        </button>

        <button
          type="button"
          :class="{ active: reportMode === 'range' }"
          @click="reportMode = 'range'; reportDataInternal = null; reportError = null"
        >
          Semester Range
        </button>
      </div>

      <div v-if="reportMode === 'single'" class="semester-selection">
        <label for="single-semester">Semester</label>

        <select
          id="single-semester"
          v-model.number="selectedSemesterId"
          :disabled="loadingSemesters || semesters.length === 0"
        >
          <option :value="null" disabled>Select a semester</option>
          <option
            v-for="semester in semesters"
            :key="semester.id"
            :value="semester.id"
          >
            {{ semesterLabel(semester) }}
          </option>
        </select>
      </div>


      <div v-else class="semester-range-selection">
        <div class="semester-selection">
          <label for="start-semester">Starting Semester</label>

          <select
            id="start-semester"
            v-model.number="selectedStartSemesterId"
            :disabled="loadingSemesters || semesters.length === 0"
            @change="
              selectedEndSemesterId = endSemesterOptions.some(
                s => s.id === selectedEndSemesterId
              )
                ? selectedEndSemesterId
                : (endSemesterOptions[endSemesterOptions.length - 1]?.id ?? null)
            "
          >
            <option :value="null" disabled>Select a starting semester</option>
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
            :disabled="!selectedStartSemesterId"
          >
            <option :value="null" disabled>Select an ending semester</option>
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

      <button
        type="button"
        class="generate-button"
        :disabled="loadingSemesters || generatingReport"
        @click="generateReport"
      >
        {{ generatingReport ? 'Generating...' : 'Generate Report' }}
      </button>
    </div>


    <div v-if="loadingSemesters" class="status-message">
      Loading semesters...
    </div>

    <div v-if="semesterError" class="error-message">
      {{ semesterError }}
    </div>

    <div v-if="reportError" class="error-message">
      {{ reportError }}
    </div>


    <div v-if="reportDataInternal" class="report-content">
      <SummaryReportTemplate
        v-model:report="reportDataInternal"
        student-count-label="Students in Course:"
        @save="saveReport"
        @import="handleImport"
        @regenerate="handleRegenerate"
        @reload="handleReload"
      />
    </div>
  </div>
</template>

<style scoped>
.summary-report-page {
  width: 100%;
}

.page-header {
  margin-bottom: 1.5rem;
}

.page-header h1 {
  margin-bottom: 0.4rem;
}

.page-header p {
  color: var(--text-secondary, #666);
}

.report-selection {
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 8px;
  background: var(--card-background, white);
}

.report-mode {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.report-mode button {
  padding: 0.65rem 1rem;
  border: 1px solid var(--border-color, #ccc);
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
}

.report-mode button.active {
  background: var(--primary-color, #2563eb);
  border-color: var(--primary-color, #2563eb);
  color: white;
}

.semester-range-selection {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.semester-selection {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.semester-selection label {
  font-weight: 600;
}

.semester-selection select {
  width: 100%;
  padding: 0.7rem;
  border: 1px solid var(--border-color, #ccc);
  border-radius: 6px;
  background: var(--card-background, white);
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

.error-message {
  margin: 1rem 0;
  color: #dc2626;
}

.report-content {
  margin-top: 2rem;
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
