<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { BaseCard, BaseSpinner, BaseButton } from '@/components/ui';
import { useMultiYearReportData } from '@/composables/use-multi-year-report-data';
import type { SummaryReportData } from '@/composables/use-summary-report-data';
import MultiYearReportTemplate from './MultiYearReportTemplate.vue';
import api from '@/api';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface Props {
  programId: number;
}

interface Semester {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
}

const props = defineProps<Props>();

// ── Semester data ────────────────────────────────────────────────────────────
const allSemesters = ref<Semester[]>([]);
const startSemesterId = ref<number | null>(null);
const endSemesterId = ref<number | null>(null);
const dateError = ref<string | null>(null);
const loadingUI = ref(false);
const exporting = ref(false);

// ── Load available semesters ─────────────────────────────────────────────────
onMounted(async () => {
  try {
    loadingUI.value = true;
    const res = await api.get('/semesters', {
      params: { academicYear: null }
    });
    allSemesters.value = (res.data.data ?? [])
      .sort((a: Semester, b: Semester) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime());

    if (allSemesters.value.length > 0) {
      startSemesterId.value = allSemesters.value[0].id;
      endSemesterId.value = allSemesters.value[allSemesters.value.length - 1].id;
    }
  } catch (err) {
    console.error('Failed to load semesters:', err);
  } finally {
    loadingUI.value = false;
  }
});

// ── Get semester details ─────────────────────────────────────────────────────
const selectedStartSemester = computed(() =>
  allSemesters.value.find(s => s.id === startSemesterId.value)
);

const selectedEndSemester = computed(() =>
  allSemesters.value.find(s => s.id === endSemesterId.value)
);

// ── Filtered end semester options ────────────────────────────────────────────
const availableEndSemesters = computed(() => {
  if (!selectedStartSemester.value) return allSemesters.value;
  return allSemesters.value.filter(s =>
    new Date(s.startDate) >= new Date(selectedStartSemester.value!.startDate)
  );
});

function validateDates(): boolean {
  dateError.value = null;

  if (!startSemesterId.value || !endSemesterId.value) {
    dateError.value = 'Please select both start and end semesters';
    return false;
  }

  if (!selectedStartSemester.value || !selectedEndSemester.value) {
    dateError.value = 'Invalid semester selection';
    return false;
  }

  if (new Date(selectedEndSemester.value.startDate) < new Date(selectedStartSemester.value.startDate)) {
    dateError.value = 'End semester cannot be before start semester';
    return false;
  }

  return true;
}

// ── Composable ───────────────────────────────────────────────────────────────
const reportProps = computed(() => ({
  programId: props.programId,
  startDate: selectedStartSemester.value?.startDate ?? '',
  endDate:   selectedEndSemester.value?.endDate ?? ''
}));

const { loading, error, reportData, loadReportData } = useMultiYearReportData(reportProps);

function generate() {
  if (!validateDates()) return;
  loadReportData();
}

// ── Local editable copy (array of per-year reports) ──────────────────────────
const reportDataInternal = ref<SummaryReportData[] | null>(null);

watch(
  () => reportData.value,
  (newVal) => {
    reportDataInternal.value = newVal ? JSON.parse(JSON.stringify(newVal)) : null;
  },
  { immediate: true }
);

function updateYearReport(index: number, updated: SummaryReportData) {
  if (!reportDataInternal.value) return;
  reportDataInternal.value[index] = updated;
}

// ── Save ───────────────────────────────────────────────────────────────────
async function saveReportToBackend(updatedReport: SummaryReportData) {
  for (const outcome of updatedReport.outcomes) {
    for (const indicator of outcome.indicators) {
      for (const measure of indicator.measures) {
        await api.put(`/measure/${measure.measureId}`, {
          id: measure.measureId,
          courseIndicatorId: measure.courseIndicatorId,
          description: measure.description,
          observation: measure.note ?? null,
          recAction: measure.recommendedAction ?? null,
          met: measure.studentsMet ?? 0,
          exceeded: measure.studentsExceeded ?? 0,
          below: measure.studentsBelow ?? 0,
          metPercentage: measure.metPercentage ?? 0,
          status: 'Complete',
          active: true
        });
      }
    }
  }
  await loadReportData();
}

// ── PDF export ───────────────────────────────────────────────────────────────
function getStatusColor(status: string): [number, number, number] {
  const s = status.toLowerCase();
  if (s.includes('not met')) return [220, 20, 60];
  if (s.includes('partially')) return [255, 140, 0];
  if (s.includes('met')) return [34, 139, 34];
  return [100, 100, 100];
}

async function exportAllYearsToPDF() {
  if (!reportDataInternal.value || reportDataInternal.value.length === 0) return;

  exporting.value = true;
  try {
    const pdf = new jsPDF('p', 'mm', 'a4');
    const pageWidth = pdf.internal.pageSize.getWidth();
    const pageHeight = pdf.internal.pageSize.getHeight();
    const margin = 14;

    const years = reportDataInternal.value;
    const firstYear = years[0].academicYear;
    const lastYear = years[years.length - 1].academicYear;
    const rangeLabel = firstYear === lastYear ? firstYear : `${firstYear} \u2013 ${lastYear}`;

    // ===== COVER PAGE =====
    let yPos = pageHeight / 3;

    pdf.setFontSize(24);
    pdf.setFont('helvetica', 'bold');
    pdf.text('Multi-Year Assessment Summary Report', pageWidth / 2, yPos, { align: 'center' });
    yPos += 15;

    pdf.setFontSize(18);
    pdf.setFont('helvetica', 'normal');
    pdf.text(rangeLabel, pageWidth / 2, yPos, { align: 'center' });
    yPos += 20;

    pdf.setFontSize(11);
    pdf.setTextColor(100, 100, 100);
    pdf.text(`Generated: ${years[0].generatedDate}`, pageWidth / 2, yPos, { align: 'center' });
    yPos += 7;
    if (years[0].generatedBy?.length) {
      pdf.text(`By: ${years[0].generatedBy.join(', ')}`, pageWidth / 2, yPos, { align: 'center' });
    }
    pdf.setTextColor(0, 0, 0);

    // ===== PER-YEAR SECTIONS =====
    for (const yearReport of years) {
      // Year divider page
      pdf.addPage();
      yPos = pageHeight / 2 - 15;

      pdf.setFontSize(22);
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(0, 0, 0);
      pdf.text(`Academic Year ${yearReport.academicYear}`, pageWidth / 2, yPos, { align: 'center' });

      const hasData = yearReport.outcomes && yearReport.outcomes.length > 0;

      if (!hasData) {
        yPos += 14;
        pdf.setFontSize(12);
        pdf.setFont('helvetica', 'italic');
        pdf.setTextColor(120, 120, 120);
        pdf.text(`No assessment data available for ${yearReport.academicYear}.`, pageWidth / 2, yPos, { align: 'center' });
        pdf.setTextColor(0, 0, 0);
        continue;
      }

      // Table of Contents
      pdf.addPage();
      yPos = margin;

      pdf.setFontSize(16);
      pdf.setFont('helvetica', 'bold');
      pdf.setTextColor(0, 0, 0);
      pdf.text('Table of Contents', margin, yPos);
      yPos += 10;

      pdf.setDrawColor(66, 139, 202);
      pdf.setLineWidth(0.5);
      pdf.line(margin, yPos, pageWidth - margin, yPos);
      yPos += 8;

      pdf.setFontSize(11);
      for (const outcome of yearReport.outcomes) {
        const outcomeStatus = outcome.overallStatus || 'Unknown';
        const statusColor = getStatusColor(outcomeStatus);

        pdf.setFont('helvetica', 'bold');
        pdf.setTextColor(0, 0, 0);
        pdf.text(`Outcome ${outcome.outcomeNumber}`, margin + 5, yPos);

        pdf.setFont('helvetica', 'normal');
        pdf.setTextColor(...statusColor);
        pdf.text(`[${outcomeStatus.toUpperCase()}]`, margin + 38, yPos);
        pdf.setTextColor(0, 0, 0);
        yPos += 7;

        pdf.setFontSize(9);
        pdf.setTextColor(100, 100, 100);
        const indicatorCount = outcome.indicators.length;
        const measureCount = outcome.indicators.reduce((sum: number, ind: any) => sum + ind.measures.length, 0);
        pdf.text(`${indicatorCount} indicator(s), ${measureCount} measure(s)`, margin + 10, yPos);
        pdf.setTextColor(0, 0, 0);
        pdf.setFontSize(11);
        yPos += 8;
      }

      // Outcome pages
      for (const outcome of yearReport.outcomes) {
        pdf.addPage();
        yPos = margin;

        // Academic year label 
        pdf.setFontSize(10);
        pdf.setFont('helvetica', 'normal');
        pdf.setTextColor(120, 120, 120);
        pdf.text(`Academic Year: ${yearReport.academicYear}`, pageWidth / 2, yPos, { align: 'center' });
        pdf.setTextColor(0, 0, 0);
        yPos += 8;

        const outcomeStatus = outcome.overallStatus || 'Unknown';
        const statusColor = getStatusColor(outcomeStatus);

        pdf.setFontSize(14);
        pdf.setFont('helvetica', 'bold');
        pdf.setTextColor(0, 0, 0);
        pdf.text(`Outcome ${outcome.outcomeNumber}`, margin, yPos);
        yPos += 8;

        pdf.setFontSize(11);
        pdf.setTextColor(...statusColor);
        pdf.text(`Status: ${outcomeStatus.toUpperCase()}`, margin, yPos);
        pdf.setTextColor(0, 0, 0);
        yPos += 10;

        const tableData: any[] = [];
        for (const indicator of outcome.indicators) {
          for (const measure of indicator.measures) {
            const metPct = measure.metPercentage?.toFixed(1) ?? '0.0';
            const status = measure.status || 'Not assessed';
            const courseIndicator = `(${indicator.indicatorNumber}) - ${measure.courseCode}`;
            const measureId = `${indicator.indicatorNumber}-${measure.courseCode.toLowerCase()}`;
            const details = `${measureId}: ${measure.description}, ${status} (${metPct}%)`;
            tableData.push([
              courseIndicator,
              details,
              measure.recommendedAction?.trim() || '',
              measure.note?.trim() || ''
            ]);
          }
        }

        autoTable(pdf, {
          startY: yPos,
          head: [['Course/Indicator', 'Measure Details', 'Recommended Actions', 'Notes']],
          body: tableData,
          theme: 'grid',
          headStyles: {
            fillColor: [66, 139, 202],
            textColor: 255,
            fontStyle: 'bold',
            fontSize: 9,
            halign: 'left'
          },
          bodyStyles: { fontSize: 8, cellPadding: 3, valign: 'top' },
          columnStyles: {
            0: { cellWidth: 30 },
            1: { cellWidth: 70 },
            2: { cellWidth: 50 },
            3: { cellWidth: 32 }
          },
          margin: { left: margin, right: margin }
        });
      }
    }

    const safeRange = rangeLabel.replace(/\u2013/g, '-').replace(/\s/g, '_');
    pdf.save(`Multi_Year_Report_${safeRange}_${new Date().toISOString().split('T')[0]}.pdf`);

  } catch (err) {
    console.error('Export failed:', err);
    alert('Failed to export PDF. Please try again.');
  } finally {
    exporting.value = false;
  }
}
</script>

<template>
  <div class="multi-year-report">

    <!-- Semester Range Selector -->
    <BaseCard class="date-selector-card">
      <h3>Select Semester Range</h3>
      <div class="date-inputs">
        <div class="date-field">
          <label for="start-semester">Start Semester</label>
          <select
            id="start-semester"
            v-model.number="startSemesterId"
            :disabled="loadingUI || allSemesters.length === 0"
          >
            <option :value="null" disabled>-- Select start semester --</option>
            <option
              v-for="sem in allSemesters"
              :key="sem.id"
              :value="sem.id"
            >
              {{ sem.name }} ({{ sem.startDate }})
            </option>
          </select>
        </div>

        <div class="date-field">
          <label for="end-semester">End Semester</label>
          <select
            id="end-semester"
            v-model.number="endSemesterId"
            :disabled="!startSemesterId || loadingUI || availableEndSemesters.length === 0"
          >
            <option :value="null" disabled>-- Select end semester --</option>
            <option
              v-for="sem in availableEndSemesters"
              :key="sem.id"
              :value="sem.id"
            >
              {{ sem.name }} ({{ sem.endDate }})
            </option>
          </select>
        </div>

        <BaseButton variant="primary" @click="generate" :disabled="loading || loadingUI">
          Generate Report
        </BaseButton>
      </div>

      <p v-if="dateError" class="error-text">{{ dateError }}</p>
    </BaseCard>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <BaseSpinner size="lg" text="Generating multi-year report..." />
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <BaseButton variant="primary" @click="generate">Retry</BaseButton>
    </div>

    <!-- Report loaded -->
    <template v-else-if="reportDataInternal && reportDataInternal.length > 0">

      <!-- Export toolbar -->
      <div class="export-toolbar">
        <BaseButton variant="primary" @click="exportAllYearsToPDF" :disabled="exporting">
          {{ exporting ? 'Exporting...' : 'Export PDF' }}
        </BaseButton>
        <BaseButton variant="primary" @click="() => console.log('import!')">Import Summary</BaseButton>
      </div>

      <!-- Per-year report sections -->
      <div
        v-for="(yearReport, idx) in reportDataInternal"
        :key="yearReport.academicYear"
        class="year-section"
      >
        <div class="year-heading">
          <h2>Academic Year {{ yearReport.academicYear }}</h2>
        </div>

        <!-- Year has data -->
        <MultiYearReportTemplate
          v-if="yearReport.outcomes && yearReport.outcomes.length > 0"
          :report="yearReport"
          :hide-export="true"
          @update:report="updateYearReport(idx, $event)"
          @reload="generate"
        />

        <!-- Year has no data -->
        <BaseCard v-else class="no-data-card">
          <p class="no-data-text">No assessment data available for {{ yearReport.academicYear }}.</p>
        </BaseCard>
      </div>

    </template>

    <!-- Empty (nothing generated yet) -->
    <div v-else-if="startSemesterId && endSemesterId" class="empty-state">
      <p>No report data available for the selected semester range.</p>
    </div>

  </div>
</template>

<style scoped>
.multi-year-report {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.date-selector-card h3 {
  margin: 0 0 1rem;
  font-size: 1rem;
  font-weight: 600;
}

.date-inputs {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  flex-wrap: wrap;
}

.date-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.date-field label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.date-field select {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border-light);
  border-radius: 0.375rem;
  font-size: 0.9rem;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  min-width: 200px;
}

.date-field select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-text {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-error);
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

.error-state {
  color: var(--color-error);
}

.export-toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.year-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.year-heading {
  padding: 0.75rem 1rem;
  background: var(--color-bg-secondary, var(--color-bg-tertiary));
  border-radius: 0.25rem;
}

.year-heading h2 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.no-data-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80px;
}

.no-data-text {
  margin: 0;
  color: var(--color-text-secondary);
  font-style: italic;
}

@media (max-width: 640px) {
  .date-inputs {
    flex-direction: column;
    align-items: stretch;
  }

  .date-field select {
    min-width: unset;
  }
}
</style>
