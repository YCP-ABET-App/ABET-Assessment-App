<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { useReportCollapse } from "@/composables/use-report-collapse";
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

import { useGoogleDrive } from '@/composables/use-google-drive';

import ReportOutcome from "@/components/report/ReportOutcome.vue";
import { BaseButton, BaseCard } from "@/components/ui";

const props = defineProps<{
  report: any;
  hideExport?: boolean;
  isEditing?: boolean;
  saving?: boolean;
}>();

const emit = defineEmits(["update:report", "import", "reload", "start-editing", "save", "cancel"]);

const localReport = reactive(JSON.parse(JSON.stringify(props.report)));

watch(
  () => props.report,
  (newVal) => Object.assign(localReport, JSON.parse(JSON.stringify(newVal))),
  { deep: true }
);

const exporting = ref(false);

const exportingDrive = ref(false);
const { openDriveFolderPickerAndUpload } = useGoogleDrive();

function handleImport() { emit("import"); }

// Export functionality
async function exportToPDF() {
  exporting.value = true;

  try {

    const { pdf, filename } = await generateSummaryReportPDF();
    pdf.save(filename);

  } catch (error) {
    console.error('Export failed:', error);
    alert('Failed to export PDF. Please try again.');
  } finally {
    exporting.value = false;
  }
}

// Helper to generate the PDF and return the jsPDF instance and filename
async function generateSummaryReportPDF(): Promise<{ pdf: jsPDF, filename: string }> {
  const pdf = new jsPDF('p', 'mm', 'a4');
  const pageWidth = pdf.internal.pageSize.getWidth();
  const pageHeight = pdf.internal.pageSize.getHeight();
  const margin = 14;

  // ===== TITLE PAGE =====
  let yPosition = pageHeight / 3; 

  // Main title 
  pdf.setFontSize(24);
  pdf.setFont('helvetica', 'bold');
  const title = `Assessment Summary Report`;
  const titleWidth = pdf.getTextWidth(title);
  pdf.text(title, (pageWidth - titleWidth) / 2, yPosition);
  yPosition += 15;

  // Academic year
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'normal');
  const academicYear = localReport.academicYear;
  const yearWidth = pdf.getTextWidth(academicYear);
  pdf.text(academicYear, (pageWidth - yearWidth) / 2, yPosition);
  yPosition += 20;

  // Metadata
  pdf.setFontSize(11);
  pdf.setTextColor(100, 100, 100);
  const dateText = `Generated: ${localReport.generatedDate}`;
  const dateWidth = pdf.getTextWidth(dateText);
  pdf.text(dateText, (pageWidth - dateWidth) / 2, yPosition);
  yPosition += 7;

  const byText = `By: ${localReport.generatedBy.join(", ")}`;
  const byWidth = pdf.getTextWidth(byText);
  pdf.text(byText, (pageWidth - byWidth) / 2, yPosition);

  // Reset text color
  pdf.setTextColor(0, 0, 0);

  // ===== TABLE OF CONTENTS =====
  pdf.addPage();
  yPosition = margin;

  pdf.setFontSize(16);
  pdf.setFont('helvetica', 'bold');
  pdf.text('Table of Contents', margin, yPosition);
  yPosition += 10;

  // Draw a line under the heading
  pdf.setDrawColor(66, 139, 202);
  pdf.setLineWidth(0.5);
  pdf.line(margin, yPosition, pageWidth - margin, yPosition);
  yPosition += 8;

  // List all outcomes
  pdf.setFontSize(11);
  pdf.setFont('helvetica', 'normal');
  for (const outcome of localReport.outcomes) {
    const outcomeStatus = outcome.overallStatus || 'Unknown';
    const statusColor = getStatusColor(outcomeStatus);

    // Outcome number and status
    pdf.setFont('helvetica', 'bold');
    pdf.text(`Outcome ${outcome.outcomeNumber}`, margin + 5, yPosition);

    // Status badge
    pdf.setFont('helvetica', 'normal');
    pdf.setTextColor(...statusColor);
    pdf.text(`[${outcomeStatus.toUpperCase()}]`, margin + 35, yPosition);
    pdf.setTextColor(0, 0, 0);

    yPosition += 7;

    // Indicators count
    pdf.setFontSize(9);
    pdf.setTextColor(100, 100, 100);
    const indicatorCount = outcome.indicators.length;
    const measureCount = outcome.indicators.reduce((sum: number, ind: any) => sum + ind.measures.length, 0);
    pdf.text(`${indicatorCount} indicator(s), ${measureCount} measure(s)`, margin + 10, yPosition);
    pdf.setTextColor(0, 0, 0);
    pdf.setFontSize(11);

    yPosition += 8;
  }

  // ===== OUTCOMES =====
  for (const outcome of localReport.outcomes) {
    // Start each outcome on a new page
    pdf.addPage();
    yPosition = margin;

      // Academic year header 
      pdf.setFontSize(10);
      pdf.setFont('helvetica', 'normal');
      pdf.setTextColor(120, 120, 120);
      pdf.text(`Academic Year: ${localReport.academicYear}`, pageWidth / 2, yPosition, { align: 'center' });
      pdf.setTextColor(0, 0, 0);
      yPosition += 8;

    // Outcome header with status
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    const outcomeStatus = outcome.overallStatus || 'Unknown';
    pdf.text(`Outcome ${outcome.outcomeNumber}`, margin, yPosition);
    yPosition += 8;

    // Status badge
    pdf.setFontSize(11);
    const statusColor = getStatusColor(outcomeStatus);
    pdf.setTextColor(...statusColor);
    pdf.text(`Status: ${outcomeStatus.toUpperCase()}`, margin, yPosition);
    pdf.setTextColor(0, 0, 0);
    yPosition += 10;

    // Collect all measures for this outcome grouped by indicator
    const tableData: any[] = [];

    for (const indicator of outcome.indicators) {
      for (const measure of indicator.measures) {
        const metPercentage = measure.metPercentage?.toFixed(1) || '0.0';
        const status = measure.status || 'Not assessed';

        // Course and Indicator column: (1.1) - CS 101
        const courseIndicator = `(${indicator.indicatorNumber}) - ${measure.courseCode}`;

        // Details column: 1.1-cs101: Description, Status (XX.X%)
        const measureId = `${indicator.indicatorNumber}-${measure.courseCode.toLowerCase()}`;
        const details = `${measureId}: ${measure.description}, ${status} (${metPercentage}%)`;

        // Recommended actions column
        let actions = '';
        if (measure.recommendedAction && measure.recommendedAction.trim()) {
          actions = measure.recommendedAction;
        }

        // Notes column
        let notes = '';
        if (measure.note && measure.note.trim()) {
          notes = measure.note;
        }

        tableData.push([
          courseIndicator,
          details,
          actions,
          notes
        ]);
      }
    }

    // Create a table for this outcome
    autoTable(pdf, {
      startY: yPosition,
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
      bodyStyles: {
        fontSize: 8,
        cellPadding: 3,
        valign: 'top'
      },
      columnStyles: {
        0: { cellWidth: 30 },   // Course/Indicator
        1: { cellWidth: 70 },   // Measure Details
        2: { cellWidth: 50 },   // Recommended Actions
        3: { cellWidth: 32 }    // Notes
      },
      margin: { left: margin, right: margin },
      didDrawCell: (data) => {
        // Add bullet points for recommended actions
        if (data.column.index === 2 && data.section === 'body' && data.cell.text.length > 0) {
          const text = data.cell.text;
          if (text.length > 0 && text[0] !== '●' && text[0] !== '•') {
            data.cell.text = ['● ' + text[0], ...text.slice(1)];
          }
        }
      }
    });
  }

  const filename = `Summary_Report_${localReport.academicYear}_${new Date().toISOString().split('T')[0]}.pdf`;
  return { pdf, filename };
}

// Export to Google Drive
async function exportToDrive() {
  exportingDrive.value = true;
  try {
    const { pdf, filename } = await generateSummaryReportPDF();
    // Get PDF as Blob
    const pdfBlob = pdf.output('blob');
    await openDriveFolderPickerAndUpload(pdfBlob, filename);
    alert('Exported to Google Drive successfully!');
  } catch (error) {
    console.error('Export to Drive failed:', error);
    alert('Failed to export to Google Drive. Please try again.');
  } finally {
    exportingDrive.value = false;
  }
}

function getStatusColor(status: string): [number, number, number] {
  const statusLower = status.toLowerCase();
  if (statusLower.includes('met') || statusLower.includes('exceeds')) {
    return [34, 139, 34]; // Green
  } else if (statusLower.includes('not met') || statusLower.includes('below')) {
    return [220, 20, 60]; // Red
  } else if (statusLower.includes('partially')) {
    return [255, 140, 0]; // Orange
  }
  return [100, 100, 100]; // Gray for unknown
}

const { expandAll, collapseAll } = useReportCollapse();

function collapseIdsForAllIndicators() {
  return localReport.outcomes.flatMap((o: any) =>
    o.indicators.map((_: any, idx: number) => o.outcomeNumber * 1000 + idx)
  );
}

function expandAllOutcomes() {
  const outcomeIds = localReport.outcomes.map((o: any) => o.outcomeNumber);
  expandAll([...outcomeIds, ...collapseIdsForAllIndicators()]);
}

function collapseAllOutcomes() {
  const outcomeIds = localReport.outcomes.map((o: any) => o.outcomeNumber);
  collapseAll([...outcomeIds, ...collapseIdsForAllIndicators()]);
}

function updateOutcome(outcomeNumber: number, updated: any) {
  localReport.outcomes = localReport.outcomes.map((o: any) =>
    o.outcomeNumber === outcomeNumber ? updated : o
  );
}


</script>

<template>
  <div class="unified-summary-report">

    <!-- Toolbar -->
    <div class="toolbar">
      <div class="toolbar-left">
        <template v-if="!isEditing">
          <BaseButton variant="primary" @click="emit('start-editing')">Complete One Year Report</BaseButton>
        </template>
        <template v-else>
          <BaseButton variant="primary" :disabled="saving" @click="emit('save', localReport)">
            {{ saving ? 'Saving...' : 'Save' }}
          </BaseButton>
          <BaseButton variant="secondary" :disabled="saving" @click="emit('cancel')">Cancel</BaseButton>
        </template>
      </div>

      <div class="toolbar-right">
        <BaseButton
          v-if="!hideExport"
          variant="primary"
          @click="exportToPDF"
          :disabled="exporting"
        >
          {{ exporting ? 'Downloading...' : 'Downloading PDF' }}
        </BaseButton>


        <BaseButton
          variant="primary"
          @click="exportToDrive"
          :disabled="exportingDrive"
        >
          {{ exportingDrive ? 'Exporting...' : 'Export to Google Drive' }}
        </BaseButton>

        <BaseButton variant="primary" @click="handleImport">Import Summary</BaseButton>
      </div>
    </div>

    <!-- Collapse Controls -->
    <div class="collapse-controls">
      <BaseButton variant="primary" size="sm" @click="expandAllOutcomes">Expand All</BaseButton>
      <BaseButton variant="primary" size="sm" @click="collapseAllOutcomes">Collapse All</BaseButton>
    </div>

    <!-- Header -->
    <BaseCard>
      <h2>Summary Results {{ localReport.academicYear }}</h2>
      <p class="header-meta">
        {{ localReport.generatedDate }} |
        {{ localReport.generatedBy.join(", ") }}
      </p>
    </BaseCard>

    <!-- Outcomes -->
    <div v-if="localReport.outcomes && localReport.outcomes.length > 0" class="outcomes-container">
      <ReportOutcome
        v-for="o in localReport.outcomes"
        :key="o.outcomeNumber"
        :outcome="o"
        :editable="false"
        :is-editing="isEditing ?? false"
        @update:outcome="updateOutcome(o.outcomeNumber, $event)"
      />
    </div>

    <!-- No outcomes message -->
    <BaseCard v-else>
      <p>No outcome data available in this report.</p>
    </BaseCard>

  </div>
</template>

<style scoped>
.unified-summary-report {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-bottom: 2rem;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  background: var(--color-bg-tertiary);
  padding: .75rem 1rem;
  border: 1px solid var(--color-border-light);
  border-radius: .5rem;
}

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 0.5rem;
}

.toolbar-divider {
  width: 1px;
  height: 1.5rem;
}

.collapse-controls {
  display: flex;
  justify-content: flex-end;
  gap: .75rem;
}

.outcomes-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.header-meta {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

@media print {
  .toolbar,
  .collapse-controls {
    display: none !important;
  }
}
</style>