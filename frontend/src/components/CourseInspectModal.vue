<script setup lang="ts">
import { ref, watch } from "vue";
import api from "@/api";
import BaseModal from "@/components/ui/BaseModal.vue";
import BaseCard from "@/components/ui/BaseCard.vue";
import BaseSpinner from "@/components/ui/BaseSpinner.vue";
import {BaseButton} from "@/components/ui";

interface Course {
  id: number;
  courseCode: string;
  courseName: string;
  courseDescription?: string;
  studentCount?: number;
  threshold?: number;
}

interface PerformanceIndicator {
  id: number;
  indicatorNumber: number;
  description: string;
  thresholdPercentage: number;
  studentOutcomeId: number;
}

interface Measure {
  id: number;
  description: string;
  observation?: string;
  recommendedAction?: string;
  studentsMet?: number;
  studentsExceeded?: number;
  studentsBelow?: number;
  status: string;
}

interface IndicatorWithMeasures {
  indicator: PerformanceIndicator;
  measures: Measure[];
}

const props = defineProps<{
  course: Course | null;
}>();

const emit = defineEmits(["close"]);

const loading = ref(false);
const error = ref<string | null>(null);
const indicatorsWithMeasures = ref<IndicatorWithMeasures[]>([]);

/* -----------------------------------------------
 * Load course indicators and measures
 * ----------------------------------------------- */
async function loadCourseData() {
  if (!props.course) return;

  loading.value = true;
  error.value = null;
  indicatorsWithMeasures.value = [];

  try {
    const indicatorIdsRes = await api.get(`/courses/searchCourse`, {
      params: {
        courseId: props.course.id
      }
    });

    const res = await api.get(`/courses/searchCourse`, { // Added "const res ="
      params: { courseId: props.course.id }
    });
    const rawData = res.data?.data ?? res.data ?? [];

    // 3. Ensure we are working with an array of numbers
    // This helps TypeScript understand exactly what 'indicatorId' is
    const indicatorIds: number[] = Array.isArray(rawData)
      ? rawData.map((item: any) => typeof item === 'object' ? item.id : item)
      : [];

    if (indicatorIds.length === 0) {
      loading.value = false;
      return;
    }

    // Now indicatorId won't be red because TypeScript knows indicatorIds is number[]
    const indicatorPromises = indicatorIds.map(async (indicatorId) => {
      try {
        const indicatorRes = await api.get(`/performance-indicators/${indicatorId}`);
        const indicator = indicatorRes.data.data as PerformanceIndicator;

        const measuresRes = await api.get(`/measure/byIndicator/${indicatorId}`);
        const measures = measuresRes.data.data as Measure[];

        return { indicator, measures };
      } catch (err) {
        console.error(`Error loading indicator ${indicatorId}:`, err);
        return null;
      }
    });

    const results = await Promise.all(indicatorPromises);
    indicatorsWithMeasures.value = results.filter(
      (item): item is IndicatorWithMeasures => item !== null
    );

  } catch (err: any) {
    console.error("Failed to load course data:", err);
    error.value = "Failed to load course details. Please try again.";
  } finally {
    loading.value = false;
  }
}

/* -----------------------------------------------
 * Watch for course changes
 * ----------------------------------------------- */
watch(
  () => props.course,
  (newCourse) => {
    if (newCourse) {
      loadCourseData();
    } else {
      indicatorsWithMeasures.value = [];
      error.value = null;
    }
  },
  { immediate: true }
);

/* -----------------------------------------------
 * Delete Logic
 * ----------------------------------------------- */
const deleting = ref(false);

async function deleteCourse() {
  if (!props.course) return;

  const confirmed = window.confirm(
    `Are you sure you want to delete ${props.course.courseCode}? This action cannot be undone.`
  );
  if (!confirmed) return;

  deleting.value = true;
  error.value = null;

  try {
    await api.delete(`/courses/${props.course.id}`);

    emit("close");
    window.location.reload();
  } catch (err: any) {
    console.error("Failed to delete course:", err);
    error.value = err?.response?.data?.message || "Failed to delete course. Please try again.";
  } finally {
    deleting.value = false;
  }
}

/* -----------------------------------------------
 * Helper functions
 * ----------------------------------------------- */
function getStatusClass(status: string): string {
  switch (status) {
    case 'InProgress':
      return 'status-in-progress';
    case 'InReview':
      return 'status-in-review';
    case 'Submitted':
      return 'status-submitted';
    case 'Complete':
      return 'status-complete';
    default:
      return '';
  }
}

function getStatusLabel(status: string): string {
  switch (status) {
    case 'InProgress':
      return 'In Progress';
    case 'InReview':
      return 'In Review';
    case 'Submitted':
      return 'Submitted';
    case 'Complete':
      return 'Complete';
    default:
      return status;
  }
}

function calculatePercentage(met: number, exceeded: number, total: number): number {
  if (total === 0) return 0;
  return Math.round(((met + exceeded) / total) * 100);
}
</script>

<template>
  <BaseModal
    :is-open="!!course"
    @close="emit('close')"
    size="xl"
  >

    <!-- HEADER CONTENT (inline, not a slot) -->
<!--    <div class="modal-header-content">-->
<!--      <h2>{{ course?.courseName }}</h2>-->
<!--      <BaseButton-->
<!--        variant="primary"-->
<!--        size="sm"-->
<!--        @click="$router.push(`/course/${course?.id}`)"-->
<!--      >-->
<!--        {{ course?.courseCode }}-->
<!--      </BaseButton>-->
<!--    </div>-->

    <div class="course-code-badge">
      <h2>{{ course?.courseName }}</h2>
      {{ course?.courseCode }}
    </div>


    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <BaseSpinner size="lg" text="Loading course details..." />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <p class="error-message">{{ error }}</p>
    </div>

    <!-- Course Details -->
    <div v-else class="course-details">
      <!-- Course Info -->
      <section class="info-section">
        <p v-if="course?.courseDescription" class="description">
          {{ course.courseDescription }}
        </p>
        <p v-if="course?.studentCount" class="student-count">
          <strong>Student Count:</strong> {{ course.studentCount }}
        </p>
        <p v-if="course?.threshold !== undefined && course?.threshold !== null" class="threshold">
          <strong>Threshold:</strong> {{ course.threshold }}
        </p>
      </section>

      <!-- Empty State -->
      <div v-if="indicatorsWithMeasures.length === 0" class="empty-state">
        <p>No performance indicators attached to this course.</p>
      </div>

      <!-- Indicators and Measures -->
      <div v-else class="indicators-container">
        <BaseCard
          v-for="item in indicatorsWithMeasures"
          :key="item.indicator.id"
          variant="bordered"
          class="indicator-card"
        >
          <!-- Indicator Header -->
          <div class="indicator-header">
            <div class="indicator-title">
              <span class="indicator-number">PI {{ item.indicator.indicatorNumber }}</span>
              <h3>{{ item.indicator.description }}</h3>
            </div>
            <div class="indicator-meta">
              <span class="threshold">
                Threshold: {{ item.indicator.thresholdPercentage }}%
              </span>
            </div>
          </div>

          <!-- Measures -->
          <div v-if="item.measures.length > 0" class="measures-section">
            <h4 class="measures-title">Measures ({{ item.measures.length }})</h4>

            <div
              v-for="measure in item.measures"
              :key="measure.id"
              class="measure-item"
            >
              <div class="measure-header">
                <p class="measure-description">{{ measure.description }}</p>
                <span :class="['status-badge', getStatusClass(measure.status)]">
                  {{ getStatusLabel(measure.status) }}
                </span>
              </div>

              <div
                v-if="measure.studentsMet !== null || measure.studentsExceeded !== null"
                class="measure-data"
              >
                <div class="data-row">
                  <span class="data-label">Exceeded:</span>
                  <span class="data-value">{{ measure.studentsExceeded || 0 }}</span>
                </div>
                <div class="data-row">
                  <span class="data-label">Met:</span>
                  <span class="data-value">{{ measure.studentsMet || 0 }}</span>
                </div>
                <div class="data-row">
                  <span class="data-label">Below:</span>
                  <span class="data-value">{{ measure.studentsBelow || 0 }}</span>
                </div>
                <div class="data-row total">
                  <span class="data-label">Success Rate:</span>
                  <span class="data-value">
                    {{ calculatePercentage(
                    measure.studentsMet || 0,
                    measure.studentsExceeded || 0,
                    (measure.studentsMet || 0) +
                    (measure.studentsExceeded || 0) +
                    (measure.studentsBelow || 0)
                  ) }}%
                  </span>
                </div>
              </div>

              <div v-if="measure.observation" class="measure-observation">
                <strong>Observation:</strong>
                <p>{{ measure.observation }}</p>
              </div>

              <div v-if="measure.recommendedAction" class="measure-action">
                <strong>Recommended Action:</strong>
                <p>{{ measure.recommendedAction }}</p>
              </div>
            </div>
          </div>

          <div v-else class="no-measures">
            <p>No measures defined for this indicator.</p>
          </div>
        </BaseCard>
      </div>
    </div>

    <template #footer>
      <div class="footer-container">
        <BaseButton
          variant="danger"
          @click="deleteCourse"
          :disabled="loading || deleting"
          class="btn-action btn-delete"
        >
          <span v-if="deleting">Deleting...</span>
          <span v-else>Delete Course</span>
        </BaseButton>

        <button
          class="btn-action btn-close"
          @click="emit('close')"
          :disabled="deleting"
        >
          Close
        </button>
      </div>
    </template>

  </BaseModal>
</template>

<style scoped>
/* Modal Header */
.modal-header-content {
  text-align: left;
}

.modal-header-content h2 {
  margin: 0 0 0.25rem 0;
  font-size: 1.5rem;
  color: var(--color-text-primary);
}

.course-code {
  margin: 0;
  color: var(--color-primary);
  font-weight: 600;
  font-size: 1rem;
}

/* Loading & Error */
.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 3rem;
}

.error-message {
  color: var(--color-error);
  font-size: 1rem;
}

/* Course Details */
.course-details {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-section {
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border-light);
}

.description {
  color: var(--color-text-secondary);
  font-style: italic;
  margin: 0 0 0.75rem 0;
}

.student-count {
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  margin: 0;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

/* Indicators */
.indicators-container {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.indicator-card {
  padding: 0;
}

.indicator-header {
  padding: 1.25rem;
  background: var(--color-bg-secondary);
  border-bottom: 2px solid var(--color-border-dark);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.indicator-title {
  flex: 1;
}

.indicator-number {
  display: inline-block;
  background: var(--color-primary);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.indicator-title h3 {
  margin: 0.5rem 0 0 0;
  font-size: 1.125rem;
  color: var(--color-text-primary);
  font-weight: 600;
}

.indicator-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

.threshold {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* Measures Section */
.measures-section {
  padding: 1.25rem;
}

.measures-title {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  color: var(--color-text-primary);
  font-weight: 600;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border-light);
}

.measure-item {
  padding: 1rem;
  margin-bottom: 1rem;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border-light);
  border-radius: 0.5rem;
}

.measure-item:last-child {
  margin-bottom: 0;
}

.measure-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.measure-description {
  flex: 1;
  margin: 0;
  font-weight: 500;
  color: var(--color-text-primary);
}

/* Status Badge */
.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  white-space: nowrap;
}

.status-in-progress {
  background: #3b82f6;
  color: white;
}

.status-in-review {
  background: #f59e0b;
  color: white;
}

.status-submitted {
  background: #8b5cf6;
  color: white;
}

.status-complete {
  background: #10b981;
  color: white;
}

/* Measure Data */
.measure-data {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  padding: 0.75rem;
  background: var(--color-bg-secondary);
  border-radius: 0.375rem;
}

.data-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-row.total {
  grid-column: 1 / -1;
  padding-top: 0.5rem;
  border-top: 1px solid var(--color-border-light);
  font-weight: 600;
}

.data-label {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.data-value {
  color: var(--color-text-primary);
  font-weight: 600;
  font-size: 0.875rem;
}

/* Observation & Action */
.measure-observation,
.measure-action {
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: var(--color-bg-secondary);
  border-radius: 0.375rem;
  border-left: 3px solid var(--color-primary);
}

.measure-observation strong,
.measure-action strong {
  display: block;
  margin-bottom: 0.25rem;
  color: var(--color-text-primary);
  font-size: 0.875rem;
}

.measure-observation p,
.measure-action p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
}

.no-measures {
  padding: 1.25rem;
  text-align: center;
  color: var(--color-text-secondary);
  font-style: italic;
}

/* Container to push buttons to opposite sides */
.footer-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 1rem; /* Adds a safety gap */
}

/* Shared sizing for both buttons */
.btn-action {
  padding: 0.625rem 1.5rem; /* Matches your existing .btn-close padding */
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  height: 40px; /* Forces identical height */
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 140px; /* Ensures buttons look balanced */
}

/* Specific Delete styles */
.btn-delete {
  background-color: transparent;
  color: #dc3545; /* Standard red */
  border: 1px solid #dc3545;
}

.btn-delete:hover:not(:disabled) {
  background-color: #dc3545;
  color: white;
}

.btn-close {
  background: var(--color-primary);
  color: white;
  border: none;
}

.btn-close:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}



/* Responsive */
@media (max-width: 768px) {
  .indicator-header {
    flex-direction: column;
  }

  .indicator-meta {
    align-items: flex-start;
  }

  .measure-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .measure-data {
    grid-template-columns: 1fr;
  }

  .data-row.total {
    grid-column: 1;
  }
}
</style>
