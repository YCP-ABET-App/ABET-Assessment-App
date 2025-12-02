<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import api from '@/api';
import { BaseCard, BaseSpinner, BaseButton, BaseSelect, BaseModal } from '@/components/ui';
import {type SummaryReportData, useSummaryReportData} from '@/composables/use-summary-report-data';
import { useToast } from '@/composables/use-toast';
import SummaryReportTemplate from "./SummaryReportTemplate.vue";

interface Props {
  programId: number;
  semesterId?: number; // Optional - if not provided, show selector
  showExportButton?: boolean;
  showSemesterSelector?: boolean; // Control whether to show selector
  effectiveReportOverride?: SummaryReportData | null;
}

const props = withDefaults(defineProps<Props>(), {
  showExportButton: true,
  showSemesterSelector: true,
  effectiveReportOverride: null
});

const { toast } = useToast();

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
  semesterId: activeSemesterId,
});

const effectiveReport = computed(() => {
  return props.effectiveReportOverride || reportData.value;
});

// Semester dropdown options
const semesterOptions = computed(() =>
  semesters.value.map(s => ({
    label: `${s.name} (${s.academicYear}-${s.academicYear + 1})`,
    value: s.id
  }))
);

const reportDataInternal = ref<any | null>(null);

// Import modal state
const showImportModal = ref(false);
const selectedFile = ref<File | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const importing = ref(false);
const importError = ref<string | null>(null);
const importSuccess = ref(false);

watch(
  () => effectiveReport.value,
  (newVal) => {
    reportDataInternal.value = newVal
      ? JSON.parse(JSON.stringify(newVal))
      : null;
  },
  { immediate: true }
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

    const current = semesters.value.find(s => s.isCurrent);

    if (current) {
      selectedSemesterId.value = current.id;
    } else if (semesters.value.length > 0) {
      selectedSemesterId.value = semesters.value[0].id;
    }

    if (selectedSemesterId.value && props.programId) {
      loadReportData();
    }

  } catch (err) {
    console.error('Error loading semesters:', err);
    semesterError.value = 'Failed to load semesters';
  } finally {
    loadingSemesters.value = false;
  }
}

// Save edited report data back to backend
async function saveReportToBackend(editedReport: any) {
  try {
    console.log('Saving report changes...', editedReport);

    let savedCount = 0;
    let failedCount = 0;

    // Iterate through outcomes, indicators, and measures to save changes
    for (const outcome of editedReport.outcomes) {
      for (const indicator of outcome.indicators) {
        for (const measure of indicator.measures) {
          // Convert string inputs back to numbers
          const met = Number(measure.studentsMet ?? measure.met ?? 0);
          const exceeded = Number(measure.studentsExceeded ?? measure.exceeded ?? 0);
          const below = Number(measure.studentsBelow ?? measure.below ?? 0);

          console.log(`Saving measure ${measure.measureId}:`, { met, exceeded, below });

          try {
            await api.put(`/measure/${measure.measureId}`, {
              id: measure.measureId,
              courseIndicatorId: measure.courseIndicatorId,

              // Editable text fields
              description: measure.description,
              observation: measure.note ?? null,
              recommendedAction: measure.recommendedAction ?? null,
              fcar: null,

              // Raw numeric fields - use the correct field names expected by backend
              studentsMet: met,
              studentsExceeded: exceeded,
              studentsBelow: below,

              // Status
              status: "Complete",

              active: true
            });
            savedCount++;
          } catch (err) {
            console.error(`Failed to save measure ${measure.measureId}:`, err);
            failedCount++;
          }
        }
      }
    }

    console.log(`Report saved: ${savedCount} successful, ${failedCount} failed`);

    // Show success toast
    toast({
      type: 'success',
      title: 'Changes Saved',
      message: `Successfully saved ${savedCount} measure${savedCount !== 1 ? 's' : ''}`,
      duration: 3000
    });

    if (failedCount > 0) {
      toast({
        type: 'warning',
        title: 'Some Changes Failed',
        message: `${failedCount} measure${failedCount !== 1 ? 's' : ''} could not be saved`,
        duration: 4000
      });
    }
  } catch (err) {
    console.error('Failed to save report:', err);
    error.value = 'Failed to save changes';

    toast({
      type: 'error',
      title: 'Save Failed',
      message: 'Could not save changes to the report',
      duration: 5000
    });

    throw err; // Re-throw so the template knows it failed
  }
}

// Handle save event from template - save then reload
async function handleSave(editedReport: any) {
  try {
    await saveReportToBackend(editedReport);
    // Wait a moment for backend to process, then reload
    await new Promise(resolve => setTimeout(resolve, 300));
    await loadReportData();
  } catch (err) {
    console.error('Save and reload failed:', err);
  }
}

// Import functionality
function handleImport() {
  console.log('handleImport called');
  showImportModal.value = true;
  importError.value = null;
  importSuccess.value = false;
  selectedFile.value = null;
  console.log('showImportModal set to:', showImportModal.value);
}

function closeImportModal() {
  showImportModal.value = false;
  selectedFile.value = null;
  importError.value = null;
  importSuccess.value = false;
}

function triggerFileInput() {
  fileInputRef.value?.click();
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
    importError.value = null;
  }
}

async function processImport() {
  if (!selectedFile.value) {
    importError.value = 'Please select a file to import';
    return;
  }

  if (!activeSemesterId.value) {
    importError.value = 'Please select a semester first';
    return;
  }

  importing.value = true;
  importError.value = null;
  importSuccess.value = false;

  try {
    // Parse the file based on type
    const fileExtension = selectedFile.value.name.split('.').pop()?.toLowerCase();

    let parsedData: any[] = [];

    if (fileExtension === 'txt') {
      parsedData = await importFromText(selectedFile.value);
    } else if (fileExtension === 'pdf') {
      parsedData = await importFromPDF(selectedFile.value);
    } else if (fileExtension === 'csv') {
      parsedData = await importFromCSV(selectedFile.value);
    } else if (fileExtension === 'xlsx' || fileExtension === 'xls') {
      parsedData = await importFromExcel(selectedFile.value);
    } else {
      throw new Error('Unsupported file format. Please use PDF, TXT, CSV, or Excel files.');
    }

    // Send directly to backend import endpoint
    await sendImportToBackend(parsedData);

    importSuccess.value = true;

    // Show success toast
    toast({
      type: 'success',
      title: 'Import Successful',
      message: `Successfully imported ${parsedData.length} measures`,
      duration: 3000
    });

    // Wait a moment to show success message, then close and reload
    await new Promise(resolve => setTimeout(resolve, 1500));
    closeImportModal();
    await loadReportData();

  } catch (err: any) {
    console.error('Import failed:', err);
    importError.value = err.message || 'Failed to import file';

    // Show error toast
    toast({
      type: 'error',
      title: 'Import Failed',
      message: err.message || 'Failed to import file',
      duration: 5000
    });
  } finally {
    importing.value = false;
  }
}

async function sendImportToBackend(measureUpdates: any[]) {
  // Group measures by outcome and indicator
  const outcomeMap = new Map<number, any>();

  for (const update of measureUpdates) {
    const outcomeNum = parseInt(update.indicatorNumber.split('.')[0]);

    if (!outcomeMap.has(outcomeNum)) {
      outcomeMap.set(outcomeNum, {
        number: outcomeNum,  // Changed from outcomeNumber to number
        status: null,
        indicators: []
      });
    }

    const outcome = outcomeMap.get(outcomeNum);

    // Parse the full indicator number as a double (e.g., "1.1" -> 1.1)
    const indicatorNumberDouble = parseFloat(update.indicatorNumber);

    // Find or create indicator in outcome
    let indicator = outcome.indicators.find((ind: any) => ind.number === indicatorNumberDouble);

    if (!indicator) {
      indicator = {
        number: indicatorNumberDouble,  // Use number (double), not indicatorNumber (string)
        courses: []
      };
      outcome.indicators.push(indicator);
    }

    // Find or create course in indicator
    let course = indicator.courses.find((c: any) => c.courseCode === update.courseCode);

    if (!course) {
      course = {
        courseCode: update.courseCode,
        measures: []
      };
      indicator.courses.push(course);
    }

    // Add measure to course - use metPercentage and recommendedActions as array
    course.measures.push({
      description: update.description,
      metPercentage: update.percentage,  // Send percentage, not counts
      status: update.status,
      recommendedActions: update.recommendedAction ? [update.recommendedAction] : []  // Send as array
    });
  }

  // Convert map to array
  const outcomes = Array.from(outcomeMap.values());

  const payload = {
    semesterId: activeSemesterId.value,
    outcomes: outcomes
  };

  console.log('Sending import to backend:', JSON.stringify(payload, null, 2));

  // Send to backend
  const response = await api.post('/import/summary', payload);

  console.log('Import response:', response.data);

  return response.data;
}

async function importFromText(file: File): Promise<any[]> {
  const text = await file.text();
  return parseNarrativeFormat(text);
}

async function importFromPDF(file: File): Promise<any[]> {
  // Read PDF as text using browser's File API
  // Note: This requires the PDF to be text-based, not scanned images
  const arrayBuffer = await file.arrayBuffer();

  // For basic text extraction from PDF, we'll need to use a library
  // For now, show a helpful error with instructions
  throw new Error('PDF import requires text extraction. Please convert your PDF to TXT format first, or we can add PDF parsing support with a library.');
}

async function parseNarrativeFormat(text: string): Promise<any[]> {
  // Parse the narrative format like the example:
  // "(1.1) - CS 101"
  // "1.1-cs101: Assignment 3 MS1, Dominoes Initialization, Met comfortably (86.4%)"
  // "Recommended actions:"
  // "● Action text"

  const lines = text.split('\n').map(line => line.trim()).filter(line => line);
  const measureUpdates: any[] = [];
  let currentRecommendedAction: string | null = null;
  let lastMeasure: any = null;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    // Check if this is a recommended action line
    if (line.toLowerCase().includes('recommended action')) {
      // Next line(s) might contain the action
      currentRecommendedAction = '';
      continue;
    }

    // Check if this is a bullet point action
    if (currentRecommendedAction !== null && (line.startsWith('●') || line.startsWith('•') || line.startsWith('-'))) {
      const actionText = line.replace(/^[●•\-]\s*/, '').trim();
      if (actionText) {
        currentRecommendedAction = actionText;
        // Apply to last measure if available
        if (lastMeasure) {
          lastMeasure.recommendedAction = currentRecommendedAction;
        }
        currentRecommendedAction = null;
      }
      continue;
    }

    // Look for measure lines with format: "X.Y-courseCode: description, status (percentage%)"
    const measureMatch = line.match(/^(\d+\.\d+)-([a-zA-Z0-9]+):\s*(.+?),\s*(Met comfortably|Met|Barely not met|Not met)\s*\((\d+\.?\d*)%\)/);

    if (measureMatch) {
      const [, indicatorNum, courseCode, description, status, percentage] = measureMatch;

      // Calculate students met/exceeded/below based on percentage and status
      const pct = parseFloat(percentage);

      lastMeasure = {
        indicatorNumber: indicatorNum,
        courseCode: courseCode.toUpperCase(),
        description: description.trim(),
        status,
        percentage: pct,
        recommendedAction: null
      };

      measureUpdates.push(lastMeasure);
      currentRecommendedAction = null;
    }
  }

  if (measureUpdates.length === 0) {
    throw new Error('No valid measure data found in file. Expected format: "1.1-cs101: Description, Status (XX.X%)"');
  }

  console.log('Found measures to import:', measureUpdates);
  console.log('Sample measures:', measureUpdates.slice(0, 5).map(m => ({
    indicatorNumber: m.indicatorNumber,
    courseCode: m.courseCode,
    percentage: m.percentage,
    status: m.status
  })));
  return measureUpdates;
}

async function importFromCSV(file: File): Promise<any[]> {
  // Read the CSV file
  const text = await file.text();
  const lines = text.split('\n').filter(line => line.trim());

  if (lines.length < 2) {
    throw new Error('CSV file is empty or has no data rows');
  }

  // Parse CSV (simple implementation - assumes comma-separated)
  const headers = lines[0].split(',').map(h => h.trim());
  const data: any[] = [];

  for (let i = 1; i < lines.length; i++) {
    const values = lines[i].split(',').map(v => v.trim());
    const row: any = {};
    headers.forEach((header, index) => {
      row[header] = values[index];
    });
    data.push(row);
  }

  // Transform CSV data to the same format as narrative parser
  const measureUpdates: any[] = [];

  for (const row of data) {
    const indicatorNum = row.indicatorNumber || row.indicator;
    const courseCode = (row.courseCode || row.course || '').toUpperCase();
    const percentage = parseFloat(row.percentage || row.metPercentage || '0');

    if (indicatorNum && courseCode && !isNaN(percentage)) {
      measureUpdates.push({
        indicatorNumber: indicatorNum,
        courseCode: courseCode,
        description: row.description || '',
        status: row.status || 'Met',
        percentage: percentage,
        recommendedAction: row.recommendedAction || null
      });
    }
  }

  if (measureUpdates.length === 0) {
    throw new Error('No valid measure data found in CSV');
  }

  return measureUpdates;
}

async function importFromExcel(file: File): Promise<any[]> {
  // For Excel files, we'd need a library like xlsx
  // For now, show a message that this feature is coming soon
  throw new Error('Excel import is not yet implemented. Please use CSV or TXT format.');
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

    <!-- Unified Renderer -->
    <div v-else-if="reportDataInternal">
      <SummaryReportTemplate
        v-model:report="reportDataInternal"
        @save="handleSave"
        @import="handleImport"
        @regenerate="loadReportData"
        @reload="loadReportData"
      />
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

    <!-- Import Modal (outside conditional chain) -->
    <BaseModal
      :isOpen="showImportModal"
      title="Import Summary Data"
      @close="closeImportModal"
    >
      <div class="import-modal-content">
        <p>Upload a file containing summary report data. Supported formats: PDF, TXT, CSV, Excel.</p>

        <div class="file-upload-section">
          <input
            ref="fileInputRef"
            type="file"
            accept=".csv,.xlsx,.xls,.pdf,.txt"
            @change="handleFileSelect"
            style="display: none"
          />

          <BaseButton variant="primary" @click="triggerFileInput">
            Choose File
          </BaseButton>

          <span v-if="selectedFile" class="selected-file-name">
            {{ selectedFile.name }}
          </span>
        </div>

        <div v-if="importError" class="import-error">
          {{ importError }}
        </div>

        <div v-if="importSuccess" class="import-success">
          Summary data imported successfully! The page will reload to show the updated data.
        </div>
      </div>

      <template #footer>
        <div class="modal-footer-buttons">
          <BaseButton variant="secondary" @click="closeImportModal">
            Cancel
          </BaseButton>
          <BaseButton
            variant="primary"
            @click="processImport"
            :disabled="!selectedFile || importing"
          >
            {{ importing ? 'Importing...' : 'Import' }}
          </BaseButton>
        </div>
      </template>
    </BaseModal>
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
.header-content h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

/* Recommended Actions */
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

/* Import Modal Styles */
.import-modal-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.file-upload-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px dashed var(--color-border-light);
  border-radius: 0.5rem;
  background: var(--color-bg-secondary);
}

.selected-file-name {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.import-error {
  padding: 0.75rem;
  background: var(--color-error-light, #fee);
  color: var(--color-error);
  border-radius: 0.375rem;
  font-size: 0.875rem;
}

.import-success {
  padding: 0.75rem;
  background: var(--color-success-light, #efe);
  color: var(--color-success, #0a0);
  border-radius: 0.375rem;
  font-size: 0.875rem;
}

.modal-footer-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}
</style>
