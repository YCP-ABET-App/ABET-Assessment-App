<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import api from '@/api';
import { BaseCard, BaseButton, BaseModal, BaseInput, BaseSpinner } from '@/components/ui';
import { useToast } from '@/composables/use-toast';
import { useAssessmentScheduleData } from '@/composables/use-assessment-schedule-data';

interface Props {
  programId: number | null;
  semesterId: number | null;
  startYear?: number;
  yearCount?: number;
  editable?: boolean;
  showToolbar?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  editable: true,
  showToolbar: true
});

const toast = useToast();
const { success, error: showError, warning, info } = toast;

// Use composable for shared data loading logic
const {
  loading,
  error,
  courses,
  outcomes,
  indicators,
  scheduleData,
  courseIndicatorMappings,
  academicYears,
  loadData,
  getCourseCodesForCell,
  getCourseNamesForCell,
  getIndicatorTooltip
} = useAssessmentScheduleData(props);

// Computed property to show data summary
const dataSummary = computed(() => {
  if (loading.value || error.value) return null;

  return {
    courseCount: courses.value.length,
    outcomeCount: outcomes.value.length,
    indicatorCount: indicators.value.length,
    mappedCourses: courseIndicatorMappings.value.filter(m =>
      Object.keys(m.indicatorMappings).length > 0
    ).length,
    totalMappings: courseIndicatorMappings.value.reduce((sum, m) =>
      sum + Object.keys(m.indicatorMappings).length, 0
    )
  };
});

// Editing state (only needed when editable)
const saving = ref(false);
const selectedRows = ref<Set<number>>(new Set());
const selectedCell = ref<{ indicatorId: number; year: string } | null>(null);

// Course selection modal
const showCourseSelector = ref(false);
const selectorIndicatorId = ref<number | null>(null);
const selectorYear = ref<string | null>(null);
const selectedCourses = ref<Set<number>>(new Set());
const courseSearchTerm = ref('');

// Filtered courses for modal
const filteredCourses = computed(() => {
  if (!courseSearchTerm.value) return courses.value;

  const term = courseSearchTerm.value.toLowerCase();
  return courses.value.filter(c =>
    c.courseCode.toLowerCase().includes(term) ||
    c.courseName.toLowerCase().includes(term)
  );
});

// Cell state helpers
function isCellSelected(indicatorId: number, year: string): boolean {
  return selectedCell.value?.indicatorId === indicatorId &&
    selectedCell.value?.year === year;
}

function isRowSelected(indicatorId: number): boolean {
  return selectedRows.value.has(indicatorId);
}

function getCellClass(year: string): string {
  const currentYear = new Date().getFullYear();
  const yearNum = parseInt(year.split('-')[0]);

  if (yearNum === currentYear || yearNum === currentYear + 1) {
    return 'highlight-cell';
  }
  return '';
}

// Selection handlers (only for editable mode)
function handleCellClick(event: MouseEvent, indicatorId: number, year: string) {
  if (!props.editable) return;

  if (event.ctrlKey || event.metaKey) {
    toggleRowSelection(indicatorId);
  } else {
    selectCell(indicatorId, year);
  }
}

function selectCell(indicatorId: number, year: string) {
  selectedCell.value = { indicatorId, year };
  selectedRows.value.clear();
}

function toggleRowSelection(indicatorId: number) {
  if (selectedRows.value.has(indicatorId)) {
    selectedRows.value.delete(indicatorId);
  } else {
    selectedRows.value.add(indicatorId);
  }
  selectedCell.value = null;
}

function selectAllRows() {
  selectedRows.value.clear();
  scheduleData.value.forEach(row => {
    selectedRows.value.add(row.indicatorId);
  });
  selectedCell.value = null;
}

function clearSelection() {
  selectedRows.value.clear();
  selectedCell.value = null;
}

// Editing handlers
function handleCellDoubleClick(indicatorId: number, year: string) {
  if (!props.editable) return;
  openCourseSelector(indicatorId, year);
}

function openCourseSelector(indicatorId: number, year: string) {
  selectorIndicatorId.value = indicatorId;
  selectorYear.value = year;

  const row = scheduleData.value.find(r => r.indicatorId === indicatorId);
  const currentCourses = row?.assignments.get(year) || [];
  selectedCourses.value = new Set(currentCourses);

  courseSearchTerm.value = '';
  showCourseSelector.value = true;
}

function toggleCourseSelection(courseId: number) {
  if (selectedCourses.value.has(courseId)) {
    selectedCourses.value.delete(courseId);
  } else {
    selectedCourses.value.add(courseId);
  }
}

function applyCourseSelection() {
  if (!selectorIndicatorId.value || !selectorYear.value) return;

  const row = scheduleData.value.find(r => r.indicatorId === selectorIndicatorId.value);
  if (!row) return;

  row.assignments.set(selectorYear.value, Array.from(selectedCourses.value));

  closeCourseSelector();
  toast.success('Course assignment updated', 'Changes not yet saved');
}

function closeCourseSelector() {
  showCourseSelector.value = false;
  selectorIndicatorId.value = null;
  selectorYear.value = null;
  selectedCourses.value.clear();
}

function clearSelectedCells() {
  if (selectedRows.value.size > 0) {
    selectedRows.value.forEach(indicatorId => {
      const row = scheduleData.value.find(r => r.indicatorId === indicatorId);
      if (row) {
        academicYears.value.forEach(year => {
          row.assignments.set(year, []);
        });
      }
    });
    toast.info('Cleared selected rows', 'Changes not yet saved');
  } else if (selectedCell.value) {
    const row = scheduleData.value.find(r => r.indicatorId === selectedCell.value!.indicatorId);
    if (row) {
      row.assignments.set(selectedCell.value.year, []);
      toast.info('Cleared cell', 'Changes not yet saved');
    }
  }
}

function copySelection() {
  if (selectedRows.value.size > 0) {
    const data = Array.from(selectedRows.value)
      .map(indicatorId => {
        const row = scheduleData.value.find(r => r.indicatorId === indicatorId);
        if (!row) return '';

        return academicYears.value
          .map(year => getCourseCodesForCell(indicatorId, year))
          .join('\t');
      })
      .join('\n');

    copyToClipboard(data);
  } else if (selectedCell.value) {
    const value = getCourseCodesForCell(selectedCell.value.indicatorId, selectedCell.value.year);
    copyToClipboard(value);
  }
}

async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text);
    toast.success('Copied to clipboard');
  } catch (err) {
    console.error('Failed to copy:', err);
    toast.error('Failed to copy to clipboard');
  }
}

// Keyboard shortcuts (only for editable mode)
function handleKeyDown(event: KeyboardEvent) {
  if (!props.editable) return;

  if (event.key === 'Delete' || event.key === 'Backspace') {
    event.preventDefault();
    clearSelectedCells();
  }

  if ((event.ctrlKey || event.metaKey) && event.key === 'c') {
    event.preventDefault();
    copySelection();
  }

  if ((event.ctrlKey || event.metaKey) && event.key === 'a') {
    event.preventDefault();
    selectAllRows();
  }

  if (event.key === 'Escape') {
    event.preventDefault();
    clearSelection();
  }
}

// Save changes
async function saveChanges() {
  saving.value = true;

  try {
    const payload = scheduleData.value.map(row => ({
      indicatorId: row.indicatorId,
      assignments: Object.fromEntries(row.assignments)
    }));

    // TODO: Create backend endpoint to save assessment schedule
    // await api.post(`/program/${props.programId}/assessment-schedule`, payload);

    console.log('Would save:', payload);
    toast.success('Assessment schedule saved successfully');

  } catch (err) {
    console.error('Error saving schedule:', err);
    toast.error('Failed to save assessment schedule');
  } finally {
    saving.value = false;
  }
}

// Lifecycle
watch(() => [props.programId, props.semesterId] as const, () => {
  if (props.programId && props.semesterId) {
    loadData();
  }
}, { immediate: false });

// Track when data finishes loading successfully
watch([loading, error], ([newLoading, newError]) => {
  if (!newLoading && !newError && courses.value.length > 0 && indicators.value.length > 0) {
    const summary = dataSummary.value;
    if (summary) {
      info(
        'Assessment schedule loaded',
        `${summary.courseCount} courses, ${summary.outcomeCount} outcomes, ${summary.indicatorCount} indicators`
      );
    }
  }
});

onMounted(() => {
  if (props.programId && props.semesterId) {
    loadData();
  }

  if (props.editable) {
    window.addEventListener('keydown', handleKeyDown);
  }
});

onUnmounted(() => {
  if (props.editable) {
    window.removeEventListener('keydown', handleKeyDown);
  }
});
</script>

<template>
  <div class="assessment-schedule">
    <!-- Toolbar (only shown when editable and showToolbar is true) -->
    <div v-if="editable && showToolbar" class="toolbar">
      <div class="toolbar-section">
        <BaseButton
          variant="primary"
          size="sm"
          @click="saveChanges"
          :disabled="saving || loading"
          :loading="saving"
        >
          Save Changes
        </BaseButton>

        <BaseButton
          variant="secondary"
          size="sm"
          @click="loadData"
          :disabled="loading"
        >
          Reload
        </BaseButton>
      </div>

      <div class="toolbar-section">
        <BaseButton
          variant="ghost"
          size="sm"
          @click="selectAllRows"
          :disabled="loading"
        >
          Select All
        </BaseButton>

        <BaseButton
          variant="ghost"
          size="sm"
          @click="clearSelection"
          :disabled="selectedRows.size === 0 && !selectedCell"
        >
          Clear Selection
        </BaseButton>

        <BaseButton
          variant="ghost"
          size="sm"
          @click="clearSelectedCells"
          :disabled="selectedRows.size === 0 && !selectedCell"
        >
          Clear Content
        </BaseButton>

        <BaseButton
          variant="ghost"
          size="sm"
          @click="copySelection"
          :disabled="selectedRows.size === 0 && !selectedCell"
        >
          Copy
        </BaseButton>
      </div>

      <div class="toolbar-info">
        <span v-if="selectedRows.size > 0" class="selection-info">
          {{ selectedRows.size }} row{{ selectedRows.size !== 1 ? 's' : '' }} selected
        </span>
        <span v-else-if="selectedCell" class="selection-info">
          Cell selected
        </span>
      </div>
    </div>

    <!-- Data Summary Info Banner -->
    <div v-if="!loading && !error && dataSummary" class="data-summary">
      <div class="summary-item">
        <span class="summary-label">Courses:</span>
        <span class="summary-value">{{ dataSummary.courseCount }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Outcomes:</span>
        <span class="summary-value">{{ dataSummary.outcomeCount }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Indicators:</span>
        <span class="summary-value">{{ dataSummary.indicatorCount }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Mapped Courses:</span>
        <span class="summary-value">{{ dataSummary.mappedCourses }} / {{ dataSummary.courseCount }}</span>
      </div>
      <div class="summary-item">
        <span class="summary-label">Total Mappings:</span>
        <span class="summary-value">{{ dataSummary.totalMappings }}</span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <BaseSpinner size="lg" text="Loading assessment schedule..." />
      <p class="loading-details">
        Fetching courses, outcomes, and performance indicators...
      </p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <h3>Unable to Load Assessment Schedule</h3>
      <p class="error-message">{{ error }}</p>
      <div class="error-actions">
        <BaseButton
          variant="primary"
          @click="loadData"
        >
          Try Again
        </BaseButton>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="courses.length === 0 || indicators.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <h3>No Data Available</h3>
      <div v-if="courses.length === 0" class="empty-message">
        <p>No courses have been added to this program yet.</p>
        <p class="empty-hint">Add courses to begin creating your assessment schedule.</p>
      </div>
      <div v-else class="empty-message">
        <p>No performance indicators have been defined for this program yet.</p>
        <p class="empty-hint">Define performance indicators to begin tracking assessments.</p>
      </div>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Assessment Schedule Table -->
      <BaseCard class="schedule-section">
        <template #header>
          <h2>Assessment Schedule by Indicator</h2>
        </template>

        <div class="table-wrapper">
          <table class="schedule-table">
            <thead>
            <tr>
              <th class="indicator-header">Performance Indicator</th>
              <th
                v-for="year in academicYears"
                :key="year"
                class="year-header"
                :class="getCellClass(year)"
              >
                {{ year }}
              </th>
            </tr>
            </thead>
            <tbody>
            <tr
              v-for="row in scheduleData"
              :key="row.indicatorId"
              :class="{ 'row-selected': isRowSelected(row.indicatorId) }"
            >
              <td
                class="indicator-cell"
                :class="{ 'row-selected': isRowSelected(row.indicatorId), 'clickable': editable }"
                :title="getIndicatorTooltip(row.indicatorId)"
                @click="editable && toggleRowSelection(row.indicatorId)"
              >
                <div class="indicator-number">{{ row.outcomeNumber }}.{{ row.indicatorNumber }}</div>
              </td>

              <td
                v-for="year in academicYears"
                :key="year"
                class="data-cell"
                :class="[
                  {
                    'cell-selected': isCellSelected(row.indicatorId, year),
                    'row-selected': isRowSelected(row.indicatorId),
                    'clickable': editable
                  },
                  getCellClass(year)
                ]"
                :title="getCourseNamesForCell(row.indicatorId, year)"
                @click="handleCellClick($event, row.indicatorId, year)"
                @dblclick="handleCellDoubleClick(row.indicatorId, year)"
              >
                {{ getCourseCodesForCell(row.indicatorId, year) }}
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </BaseCard>

      <!-- Course-Indicator Mapping Table -->
      <BaseCard class="schedule-section">
        <template #header>
          <h2>Course-Indicator Mappings</h2>
        </template>

        <div class="table-wrapper">
          <table class="mapping-table">
            <thead>
            <tr>
              <th class="course-header">Course</th>
              <th
                v-for="indicator in indicators"
                :key="indicator.id"
                class="indicator-number-header"
                :title="getIndicatorTooltip(indicator.id)"
              >
                {{ indicator.outcomeNumber }}.{{ indicator.indicatorNumber }}
              </th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="mapping in courseIndicatorMappings" :key="mapping.courseId">
              <td class="course-name-cell">
                <div class="course-code-bold">{{ mapping.courseCode }}</div>
                <div class="course-name-small">{{ mapping.courseName }}</div>
              </td>
              <td
                v-for="indicator in indicators"
                :key="indicator.id"
                class="indicator-mapping-cell"
                :class="{
                  'has-mapping': mapping.indicatorMappings[`${indicator.outcomeNumber}.${indicator.indicatorNumber}`]
                }"
                :title="mapping.indicatorMappings[`${indicator.outcomeNumber}.${indicator.indicatorNumber}`]
                  ? `${indicator.outcomeNumber}.${indicator.indicatorNumber}: ${indicator.indicatorDescription}`
                  : 'Not assessed in this course'"
              >
                {{ mapping.indicatorMappings[`${indicator.outcomeNumber}.${indicator.indicatorNumber}`] ? '✓' : '' }}
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </BaseCard>
      <!-- Help Text (only for editable mode) -->
      <div v-if="editable" class="help-text">
        <p><strong>Keyboard shortcuts:</strong></p>
        <ul>
          <li><kbd>Double-click</kbd> a cell to edit course assignments</li>
          <li><kbd>Click</kbd> indicator row to select entire row</li>
          <li><kbd>Ctrl/Cmd + Click</kbd> to select multiple rows</li>
          <li><kbd>Ctrl/Cmd + A</kbd> to select all rows</li>
          <li><kbd>Ctrl/Cmd + C</kbd> to copy selection</li>
          <li><kbd>Delete</kbd> or <kbd>Backspace</kbd> to clear selection</li>
          <li><kbd>Esc</kbd> to clear selection</li>
          <li><kbd>Hover</kbd> over indicators to see full descriptions</li>
        </ul>
      </div>
    </div>

    <!-- Course Selector Modal (only for editable mode) -->
    <BaseModal
      v-if="editable"
      v-model:isOpen="showCourseSelector"
      title="Select Courses"
      size="lg"
      @close="closeCourseSelector"
    >
      <div class="course-selector">
        <div class="modal-info">
          <p v-if="selectorIndicatorId && selectorYear">
            <strong>Indicator {{
                scheduleData.find(r => r.indicatorId === selectorIndicatorId)?.outcomeNumber
              }}.{{
                scheduleData.find(r => r.indicatorId === selectorIndicatorId)?.indicatorNumber
              }}:</strong>
            {{ scheduleData.find(r => r.indicatorId === selectorIndicatorId)?.indicatorDescription }}
          </p>
          <p><strong>Academic Year:</strong> {{ selectorYear }}</p>
        </div>

        <BaseInput
          v-model="courseSearchTerm"
          placeholder="Search courses..."
          class="search-input"
        />

        <div class="course-list">
          <div
            v-for="course in filteredCourses"
            :key="course.id"
            class="course-item"
            :class="{ 'course-selected': selectedCourses.has(course.id) }"
            @click="toggleCourseSelection(course.id)"
          >
            <input
              type="checkbox"
              :checked="selectedCourses.has(course.id)"
              @click.stop="toggleCourseSelection(course.id)"
            />
            <div class="course-info">
              <div class="course-code">{{ course.courseCode }}</div>
              <div class="course-name">{{ course.courseName }}</div>
            </div>
          </div>
        </div>

        <div class="selected-summary">
          <strong>Selected:</strong>
          {{ selectedCourses.size }} course{{ selectedCourses.size !== 1 ? 's' : '' }}
        </div>
      </div>

      <template #footer>
        <BaseButton variant="secondary" @click="closeCourseSelector">
          Cancel
        </BaseButton>
        <BaseButton variant="primary" @click="applyCourseSelection">
          Apply
        </BaseButton>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.assessment-schedule {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* Toolbar */
.toolbar {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: 0.5rem;
  flex-wrap: wrap;
}

.toolbar-section {
  display: flex;
  gap: 0.5rem;
}

.toolbar-info {
  margin-left: auto;
}

.selection-info {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* Data Summary Banner */
.data-summary {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 0.875rem 1.25rem;
  background: linear-gradient(to right, rgba(59, 130, 246, 0.05), rgba(16, 185, 129, 0.05));
  border: 1px solid var(--color-border-light);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.summary-label {
  color: var(--color-text-secondary);
  font-weight: 500;
}

.summary-value {
  color: var(--color-primary);
  font-weight: 700;
  font-size: 1rem;
}

/* Loading/Error States */
.loading-state,
.error-state,
.empty-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 2rem;
  text-align: center;
}

.loading-state {
  gap: 1rem;
}

.loading-details {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.error-state {
  gap: 1rem;
  color: var(--color-error);
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

.error-state h3 {
  color: var(--color-text-primary);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.error-message {
  color: var(--color-error);
  font-size: 1rem;
  max-width: 500px;
  line-height: 1.5;
}

.error-actions {
  margin-top: 1rem;
}

.empty-state {
  gap: 1rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.empty-state h3 {
  color: var(--color-text-primary);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.empty-message {
  color: var(--color-text-secondary);
  font-size: 1rem;
  max-width: 500px;
  line-height: 1.6;
}

.empty-message p {
  margin: 0.5rem 0;
}

.empty-hint {
  font-size: 0.875rem;
  font-style: italic;
  color: var(--color-text-tertiary);
}

/* Table */
.table-wrapper {
  overflow-x: auto;
  border: 1px solid var(--color-border-light);
  border-radius: 0.5rem;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
  user-select: none;
}

.schedule-table th {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  font-weight: 600;
  padding: 0.75rem;
  text-align: left;
  border: 1px solid var(--color-border-dark);
  position: sticky;
  top: 0;
  z-index: 10;
}

.indicator-header {
  min-width: 300px;
  position: sticky;
  left: 0;
  z-index: 15;
  background: var(--color-bg-tertiary);
}

.year-header {
  min-width: 150px;
  text-align: center;
}

.schedule-table td {
  padding: 0.75rem;
  border: 1px solid var(--color-border-light);
  transition: all 0.15s ease;
}

.clickable {
  cursor: pointer;
}

.indicator-cell {
  position: sticky;
  left: 0;
  background: var(--color-bg-secondary);
  z-index: 5;
  border-right: 2px solid var(--color-border-dark);
}

.indicator-cell.clickable:hover {
  background: var(--color-bg-tertiary);
}

.indicator-number {
  font-weight: 700;
  color: var(--color-primary);
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.indicator-description {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  line-height: 1.3;
}

.data-cell {
  text-align: center;
  background: var(--color-bg-primary);
  white-space: nowrap;
}

.data-cell.clickable:hover {
  background: var(--color-bg-tertiary);
}

/* Highlight cell */
.highlight-cell {
  background-color: rgba(255, 255, 0, 0.2);
  font-weight: 600;
}

/* Selection States */
.row-selected {
  background: rgba(59, 130, 246, 0.15) !important;
}

.cell-selected {
  background: rgba(59, 130, 246, 0.25) !important;
  border: 2px solid var(--color-primary) !important;
  box-shadow: inset 0 0 0 1px var(--color-primary);
}

/* Help Text */
.help-text {
  padding: 1rem;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  text-align: left;
}

.help-text ul {
  margin: 0.5rem 0 0 0;
  padding-left: 1.5rem;
}

.help-text li {
  margin: 0.25rem 0;
}

.help-text kbd {
  padding: 0.125rem 0.375rem;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border-dark);
  border-radius: 0.25rem;
  font-family: monospace;
  font-size: 0.8rem;
}

/* Schedule Section Spacing */
.schedule-section {
  margin-bottom: 2rem;
}

/* Mapping Table */
.mapping-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.mapping-table th,
.mapping-table td {
  padding: 0.75rem;
  border: 1px solid var(--color-border-light);
  text-align: center;
}

.mapping-table thead th {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 10;
}

.course-header {
  text-align: left;
  position: sticky;
  left: 0;
  z-index: 15;
  background: var(--color-bg-tertiary);
  min-width: 250px;
}

.indicator-number-header {
  min-width: 60px;
  font-size: 0.9rem;
  cursor: help;
}

.course-name-cell {
  text-align: left;
  position: sticky;
  left: 0;
  background: var(--color-bg-secondary);
  z-index: 5;
  border-right: 2px solid var(--color-border-dark);
}

.course-code-bold {
  font-weight: 700;
  color: var(--color-primary);
  font-size: 0.9rem;
  margin-bottom: 0.125rem;
}

.course-name-small {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  line-height: 1.2;
}

.indicator-mapping-cell {
  background: var(--color-bg-primary);
  text-align: center;
  font-size: 1.2rem;
  color: var(--color-success);
  cursor: help;
}

.has-mapping {
  background: rgba(16, 185, 129, 0.1);
  font-weight: 700;
}

.mapping-table tbody tr:hover .course-name-cell {
  background: var(--color-bg-tertiary);
}

.mapping-table tbody tr:hover .indicator-mapping-cell {
  background: var(--color-bg-tertiary);
}

.mapping-table tbody tr:hover .has-mapping {
  background: rgba(16, 185, 129, 0.2);
}

/* Legend */
.legend-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0.5rem 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.legend-label {
  font-weight: 600;
  min-width: 180px;
  color: var(--color-text-primary);
}

.legend-description {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
}

.legend-box {
  width: 40px;
  height: 24px;
  border: 1px solid var(--color-border-dark);
  border-radius: 0.25rem;
  flex-shrink: 0;
}

/* Indicators Reference */
.indicators-reference {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.indicator-item {
  padding: 0.75rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.375rem;
  font-size: 0.875rem;
  line-height: 1.6;
  border-left: 3px solid var(--color-primary);
}

.indicator-item strong {
  color: var(--color-primary);
  margin-right: 0.5rem;
  font-size: 1rem;
}

/* Course Selector Modal */
.course-selector {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 60vh;
}

.modal-info {
  padding: 0.75rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.375rem;
}

.modal-info p {
  margin: 0.25rem 0;
  font-size: 0.875rem;
}

.search-input {
  margin: 0;
}

.course-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid var(--color-border-light);
  border-radius: 0.375rem;
  max-height: 400px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: background 0.15s ease;
}

.course-item:hover {
  background: var(--color-bg-tertiary);
}

.course-item:last-child {
  border-bottom: none;
}

.course-selected {
  background: rgba(59, 130, 246, 0.15);
}

.course-item input[type="checkbox"] {
  cursor: pointer;
}

.course-info {
  flex: 1;
  text-align: left;
}

.course-code {
  font-weight: 600;
  color: var(--color-primary);
  font-size: 0.9rem;
}

.course-name {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}

.selected-summary {
  padding: 0.75rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.375rem;
  font-size: 0.875rem;
}

/* Responsive */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-section {
    width: 100%;
  }

  .toolbar-info {
    margin-left: 0;
  }

  .indicator-header {
    min-width: 200px;
  }

  .year-header {
    min-width: 120px;
  }

  .course-header {
    min-width: 180px;
  }

  .indicator-number-header {
    min-width: 50px;
  }

  .legend-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .legend-label {
    min-width: auto;
  }
}
</style>
