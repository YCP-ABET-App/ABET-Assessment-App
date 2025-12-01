<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import api from '@/api';
import { BaseCard, BaseSpinner, BaseButton, BaseSelect } from '@/components/ui';
import { useSummaryReportData } from '@/composables/use-summary-report-data';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface Props {
  programId: number;
  semesterId?: number; // Optional - if not provided, show selector
  showExportButton?: boolean;
  showSemesterSelector?: boolean; // Control whether to show selector
}

const props = withDefaults(defineProps<Props>(), {
  showExportButton: true,
  showSemesterSelector: true
});

// Semester selection
interface Semester {
  id: number;
  name: string;
  code: string;
  academicYear: number;
  isCurrent: boolean;
}

const semesters = ref<Semester[]>([]);
const selectedSemesterId = ref<number | ''>('');
const loadingSemesters = ref(false);
const semesterError = ref<string | null>(null);

// Use provided semesterId or selected one
const activeSemesterId = computed(() => {
  if (props.semesterId) return props.semesterId;
  return selectedSemesterId.value ? Number(selectedSemesterId.value) : null;
});

// Load report data using composable - pass the computed ref
const {
  loading,
  error,
  reportData,
  loadReportData
} = useSummaryReportData({
  programId: props.programId,
  semesterId: activeSemesterId
});

// Semester dropdown options
const semesterOptions = computed(() =>
  semesters.value.map(s => ({
    label: `${s.name} (${s.academicYear}-${s.academicYear + 1})`,
    value: s.id
  }))
);

// Load semesters for selector
async function loadSemesters() {
  if (!props.showSemesterSelector || props.semesterId) return;

  loadingSemesters.value = true;
  semesterError.value = null;

  try {
    const res = await api.get('/semesters', {
      params: {
        programId: props.programId,
        page: 0,
        size: 100,
        sort: 'academicYear',
        direction: 'desc'
      }
    });

    semesters.value = res.data.content || [];

    // Auto-select current semester if available
    const current = semesters.value.find(s => s.isCurrent);
    if (current) {
      selectedSemesterId.value = current.id;
    } else if (semesters.value.length > 0) {
      selectedSemesterId.value = semesters.value[0].id;
    }
  } catch (err) {
    console.error('Error loading semesters:', err);
    semesterError.value = 'Failed to load semesters';
  } finally {
    loadingSemesters.value = false;
  }
}

// Format measure display
function formatMeasure(measure: any): string {
  return `${measure.courseCode}: ${measure.description}, ${measure.status} (${measure.metPercentage}%)`;
}

// Export report as text
function exportAsText() {
  if (!reportData.value) return;

  const lines: string[] = [];
  lines.push(`Summary results ${reportData.value.academicYear}`);
  lines.push(`${reportData.value.generatedDate} - ${reportData.value.generatedBy.join(', ')}`);
  lines.push('');

  reportData.value.outcomes.forEach(outcome => {
    lines.push(`Outcome (${outcome.outcomeNumber}) - ${outcome.overallStatus}`);

    outcome.indicators.forEach(indicator => {
      lines.push(`(${indicator.indicatorNumber}) - ${indicator.courseCode}`);

      indicator.measures.forEach(measure => {
        lines.push(`${indicator.indicatorNumber}-${measure.courseCode.toLowerCase()}: ${measure.description}, ${measure.status} (${measure.metPercentage}%)`);
        if (measure.note) {
          lines.push(`    ${measure.note}`);
        }
      });
    });

    if (outcome.recommendedActions.length > 0) {
      lines.push('Recommended actions:');
      outcome.recommendedActions.forEach(action => {
        lines.push(`â— ${action}`);
      });
    }

    lines.push('');
  });

  const text = lines.join('\n');
  const blob = new Blob([text], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `ABET_Summary_${reportData.value.academicYear}.txt`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

async function exportAsPDF() {
  const data = reportData.value;
  if (!data) return;

  const doc = new jsPDF('p', 'pt', 'letter');
  const left = 40;
  const topMargin = 40;
  let cursorY = topMargin;

  // ------------------------------
  // Normalize values from report
  // ------------------------------
  const semester = String(data.semesterName ?? '');
  const academicYear = String(data.academicYear ?? '');
  const generatedDate = String(data.generatedDate ?? '');

  const generatedBy = Array.isArray(data.generatedBy)
    ? data.generatedBy.join(', ')
    : String(data.generatedBy ?? '');

  // ============================
  //  TITLE PAGE
  // ============================
  doc.setFont('Times', 'bold');
  doc.setFontSize(24);
  doc.text('ABET Assessment Summary Report', left, cursorY);

  cursorY += 40;

  doc.setFont('Times', 'normal');
  doc.setFontSize(14);

  doc.text(`Program ID: ${String(props.programId)}`, left, cursorY);
  cursorY += 24;

  if (semester) {
    doc.text(`Semester: ${semester}`, left, cursorY);
    cursorY += 20;
  }

  if (academicYear) {
    doc.text(`Academic Year: ${academicYear}`, left, cursorY);
    cursorY += 20;
  }

  if (generatedDate) {
    doc.text(`Generated On: ${generatedDate}`, left, cursorY);
    cursorY += 20;
  }

  if (generatedBy) {
    doc.text(`Generated By: ${generatedBy}`, left, cursorY);
  }

  // ============================
  //  OUTCOME SUMMARY PAGE
  // ============================
  doc.addPage();
  cursorY = topMargin;

  doc.setFont('Times', 'bold');
  doc.setFontSize(18);
  doc.text('Outcome Summary', left, cursorY);
  cursorY += 20;

  if (data.outcomes && data.outcomes.length > 0) {
    const outcomeSummary = data.outcomes.map((o: any) => [
      `Outcome ${String(o.outcomeNumber ?? '')}`,
      String(o.overallStatus ?? ''),
      String(o.outcomeDescription ?? ''),
    ]);

    autoTable(doc, {
      head: [['Outcome', 'Status', 'Description']],
      body: outcomeSummary,
      startY: cursorY,
      margin: { left },
      styles: { font: 'Times', fontSize: 11, cellPadding: 4 },
      headStyles: { fillColor: [50, 50, 50], textColor: 255 },
    });
  } else {
    doc.setFont('Times', 'normal');
    doc.setFontSize(12);
    doc.text('No outcome data available.', left, cursorY + 10);
  }

  // ============================
  //  OUTCOME → INDICATOR TABLES
  // ============================
  if (data.outcomes && data.outcomes.length > 0) {
    data.outcomes.forEach((outcome: any) => {
      // New page per outcome
      doc.addPage();
      cursorY = topMargin;

      doc.setFont('Times', 'bold');
      doc.setFontSize(18);
      doc.text(
        `Outcome ${String(outcome.outcomeNumber ?? '')} - ${String(
          outcome.overallStatus ?? ''
        )}`,
        left,
        cursorY
      );
      cursorY += 20;

      if (outcome.outcomeDescription) {
        doc.setFont('Times', 'italic');
        doc.setFontSize(12);
        doc.text(String(outcome.outcomeDescription), left, cursorY);
        cursorY += 30;
      } else {
        cursorY += 10;
      }

      const indicators = outcome.indicators ?? [];

      indicators.forEach((indicator: any) => {
        // Indicator header
        doc.setFont('Times', 'bold');
        doc.setFontSize(14);

        const indicatorNumber = String(indicator.indicatorNumber ?? '');
        const courseCode = indicator.courseCode
          ? `(${String(indicator.courseCode)})`
          : '';
        const indicatorTitle = `Indicator ${indicatorNumber} ${courseCode}`.trim();

        doc.text(indicatorTitle, left, cursorY);
        cursorY += 16;

        const measures = indicator.measures ?? [];

        if (measures.length === 0) {
          doc.setFont('Times', 'normal');
          doc.setFontSize(11);
          doc.text('No measures recorded for this indicator.', left, cursorY);
          cursorY += 20;
          return;
        }

        const rows = measures.map((m: any) => [
          String(m.description ?? ''),
          String(m.status ?? ''),
          `${m.metPercentage ?? ''}%`,
          String(m.note ?? ''),
        ]);

        autoTable(doc, {
          head: [['Measure', 'Status', 'Met %', 'Notes']],
          body: rows,
          startY: cursorY,
          margin: { left },
          styles: { font: 'Times', fontSize: 10, cellPadding: 4 },
          headStyles: { fillColor: [70, 70, 70], textColor: 255 },
        });

        // Safely read lastAutoTable.finalY
        const last = (doc as any).lastAutoTable;
        const finalY =
          last && typeof last.finalY === 'number' && !isNaN(last.finalY)
            ? last.finalY
            : cursorY;

        cursorY = finalY + 25;
      });
    });
  }

  // ============================
  //  RECOMMENDED ACTIONS PAGE
  // ============================
  doc.addPage();
  cursorY = topMargin;

  doc.setFont('Times', 'bold');
  doc.setFontSize(18);
  doc.text('Recommended Actions', left, cursorY);
  cursorY += 20;

  const allActions =
    data.outcomes?.flatMap((o: any) => o.recommendedActions ?? []) ?? [];

  if (allActions.length === 0) {
    doc.setFont('Times', 'normal');
    doc.setFontSize(12);
    doc.text(
      'No recommended actions recorded for this assessment cycle.',
      left,
      cursorY
    );
  } else {
    const recRows = allActions.map((a: any) => [`• ${String(a)}`]);

    autoTable(doc, {
      head: [['Action Items']],
      body: recRows,
      margin: { left },
      startY: cursorY,
      styles: { font: 'Times', fontSize: 11, cellPadding: 4 },
      headStyles: { fillColor: [90, 90, 90], textColor: 255 },
    });
  }

  // ============================
  //  FOOTERS (PAGE X of Y)
  // ============================
  const pageCount =
    typeof (doc as any).getNumberOfPages === 'function'
      ? (doc as any).getNumberOfPages()
      : 1;

  for (let i = 1; i <= pageCount; i++) {
    doc.setPage(i);
    doc.setFont('Times', 'normal');
    doc.setFontSize(10);

    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();

    doc.text(
      `Page ${i} of ${pageCount}`,
      pageWidth - 70,
      pageHeight - 20
    );
  }

  // ============================
  //  SAVE
  // ============================
  doc.save(`ABET_Summary_Report_${academicYear || 'Report'}.pdf`);
}

// Lifecycle
watch(activeSemesterId, (newId) => {
  console.log('Semester changed to:', newId);
  if (newId && props.programId) {
    loadReportData();
  }
});

watch(() => props.programId, () => {
  if (props.programId && activeSemesterId.value) {
    loadReportData();
  }
});

onMounted(() => {
  loadSemesters();

  // Load report if we have both programId and semesterId
  if (props.programId && activeSemesterId.value) {
    loadReportData();
  }
});
</script>

<template>
  <div class="summary-report">
    <!-- Semester Selector -->
    <BaseCard
      v-if="showSemesterSelector && !semesterId"
      class="semester-selector-card"
    >
      <div class="selector-content">
        <BaseSelect
          v-model="selectedSemesterId"
          :options="semesterOptions"
          label="Select Academic Year"
          placeholder="Choose a semester..."
          :disabled="loadingSemesters || semesters.length === 0"
        />

        <div v-if="loadingSemesters" class="loading-text">
          Loading semesters...
        </div>

        <div v-else-if="semesterError" class="error-text">
          {{ semesterError }}
        </div>

        <div v-else-if="semesters.length === 0" class="empty-text">
          No semesters available for this program
        </div>
      </div>
    </BaseCard>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <BaseSpinner size="lg" text="Generating summary report..." />
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <BaseButton variant="primary" @click="loadReportData">
        Retry
      </BaseButton>
    </div>

    <!-- Report Content -->
    <div v-else-if="reportData">
      <div class="pdf-export-wrapper">
      <!-- Header -->
      <BaseCard class="report-header">
        <div class="header-content">
          <h1>Summary Results {{ reportData.academicYear }}</h1>
          <p class="header-meta">
            {{ reportData.generatedDate }} - {{ reportData.generatedBy.join(', ') }}
          </p>
        </div>

        <div v-if="showExportButton" class="header-actions">
          <BaseButton
            variant="primary"
            size="sm"
            @click="exportAsText"
          >
            Export as Text
          </BaseButton>

          <BaseButton
            variant="primary"
            size="sm"
            @click="exportAsPDF"
          >
            Export as PDF
          </BaseButton>
        </div>
      </BaseCard>

      <!-- Outcomes -->
      <div class="outcomes-container">
        <BaseCard
          v-for="outcome in reportData.outcomes"
          :key="`outcome-${outcome.outcomeNumber}`"
          class="outcome-card"
        >
          <!-- Outcome Header -->
          <div class="outcome-header">
            <h2 class="outcome-title">
              Outcome ({{ outcome.outcomeNumber }}) -
              <span
                class="outcome-status"
                :class="{
                  'status-met': outcome.overallStatus === 'MET',
                  'status-partial': outcome.overallStatus === 'Partially Met',
                  'status-not-met': outcome.overallStatus === 'Not Met'
                }"
              >
                {{ outcome.overallStatus }}
              </span>
            </h2>

            <p class="outcome-description">{{ outcome.outcomeDescription }}</p>
          </div>

          <!-- Indicators -->
          <div class="indicators-container">
            <div
              v-for="indicator in outcome.indicators"
              :key="indicator.id"
              class="indicator-section"
            >
              <!-- Indicator Header -->
              <div class="indicator-header">
      <span class="indicator-number">
        {{ indicator.indicatorNumber }}
      </span>

                <span class="course-code">
        {{ indicator.courseCode }}
      </span>
              </div>

              <!-- Measures -->
              <div class="measures-list">
                <div
                  v-for="measure in indicator.measures"
                  :key="measure.measureId"
                  class="measure-item"
                >
                  <div class="measure-main">
          <span class="measure-description">
            {{ measure.description }},
          </span>

                    <span
                      class="measure-status"
                      :class="{
              'status-met-comfortably': measure.status === 'Met comfortably',
              'status-met': measure.status === 'Met',
              'status-barely-not-met': measure.status === 'Barely not met',
              'status-not-met': measure.status === 'Not met'
            }"
                    >
            {{ measure.status }}
          </span>

                    <span class="measure-percentage">
            ({{ measure.metPercentage }}%)
          </span>
                  </div>

                  <!-- Existing Notes -->
                  <div v-if="measure.note" class="measure-note">
                    {{ measure.note }}
                  </div>

                  <div
                    v-if="measure.recommendedAction"
                    class="measure-note"
                    style="background: var(--color-bg-tertiary); color: var(--color-text-primary);"
                  >
                    <strong>Recommended Action:</strong><br />
                    {{ measure.recommendedAction }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </BaseCard>
      </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <p>
        {{ showSemesterSelector && !semesterId
        ? 'Select a semester to view the report'
        : 'No report data available'
        }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.summary-report {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Semester Selector */
.semester-selector-card {
  margin-bottom: 1rem;
}

.selector-content {
  max-width: 500px;
}

.loading-text,
.error-text,
.empty-text {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.error-text {
  color: var(--color-error);
}

/* Loading/Error States */
.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 1rem;
}

.error-state {
  color: var(--color-error);
}

/* Report Header */
.report-header {
  margin-bottom: 2rem;
}

.header-content h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.header-meta {
  margin: 0;
  font-size: 0.95rem;
  color: var(--color-text-secondary);
}

.header-actions {
  margin-top: 1rem;
  gap: 0.5rem;
}

/* Outcomes */
.outcomes-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.outcome-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--color-border-light);
}

.outcome-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.outcome-status {
  font-weight: 700;
}

.status-met {
  color: var(--color-success);
}

.status-partial {
  color: var(--color-warning);
}

.status-not-met {
  color: var(--color-error);
}

.outcome-description {
  margin: 0;
  font-size: 0.95rem;
  color: var(--color-text-secondary);
  font-style: italic;
}

/* Indicators */
.indicators-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.indicator-section {
  padding: 1rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.5rem;
}

.indicator-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 600;
}

.indicator-number {
  color: var(--color-text-primary);
}

.course-code {
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  padding: 0.25rem 0.75rem;
  border-radius: 0.375rem;
  font-weight: 600;
  font-size: 0.9rem;
}

/* Measures */
.measures-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.measure-item {
  padding: 0.75rem;
  background: var(--color-bg-secondary);
  border-radius: 0.375rem;
}

.measure-main {
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--color-text-primary);
}

.measure-id {
  font-weight: 600;
  color: var(--color-text-primary);
  font-family: var(--font-family-mono),monospace;
}

.measure-description {
  margin: 0 0.25rem;
}

.measure-status {
  font-weight: 600;
  margin: 0 0.25rem;
}

.status-met-comfortably {
  color: var(--color-success-dark);
}

.status-met {
  color: var(--color-info);
}

.status-barely-not-met {
  color: var(--color-warning);
}

.status-not-met {
  color: var(--color-error);
}

.measure-percentage {
  color: var(--color-text-secondary);
  font-style: italic;
}

.measure-note {
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.25rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  font-style: italic;
}

/* Recommended Actions */
.recommended-actions {
  margin-top: 1.5rem;
  padding: 1rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.5rem;
  text-align: left;
}

.recommended-actions h3 {
  margin: 0 0 0.75rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.recommended-actions ul {
  margin: 0;
  padding-left: 1.5rem;
  list-style-type: disc;
}

.recommended-actions li {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--color-text-primary);
}

/* Responsive */
@media (max-width: 768px) {
  .selector-content {
    max-width: 100%;
  }

  .outcome-title {
    font-size: 1.25rem;
  }

  .indicator-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .measure-main {
    font-size: 0.85rem;
  }
}

.pdf-export-wrapper {
  padding: 1rem;
}

.pdf-export-wrapper * {
  break-inside: avoid;
  page-break-inside: avoid;
}

@media print {
  .header-actions,
  .semester-selector-card {
    display: none !important;
  }

  .pdf-export-wrapper {
    padding: 0;
  }
}
.summary-report {
  gap: 1rem;
}

.outcome-card {
  page-break-inside: avoid;
  margin-bottom: 2rem;
}
</style>
