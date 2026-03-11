<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useUserStore } from "@/stores/user-store";
import { storeToRefs } from "pinia";
import api from "@/api";
import MeasureListing from "@/components/MeasureListing.vue";
import { BaseCard, BaseButton } from "@/components/ui";

// ------------------------------
// TYPES
// ------------------------------
interface ProgramUser {
  id: number;
  userId: number;
  programId: number;
  role: string;
}

interface RawCourse {
  id: number;
  courseCode: string;
}

interface Measure {
  id: number;
  courseIndicatorId: number;
  description: string;
  observation: string | null;
  recommendedAction: string | null;
  fcar: string | null;
  studentsMet: number | null;
  studentsExceeded: number | null;
  studentsBelow: number | null;
  createdAt: string;
  active: boolean;
  deleted: boolean;
  deletedAt: string | null;
  new: boolean;
  status: string | null;
  updatedAt: string | null;
  version: number | null;
}

interface InstructorDashboardCourse {
  id: number;
  courseCode: string;
  instructorName: string;
  measuresCompleted: number;
  measuresTotal: number;
  measures: Measure[];
  expanded: boolean;
}

// ------------------------------
// STORE REFERENCES
// ------------------------------
const userStore = useUserStore();
const { currentProgramId: programId, currentSemesterId: semesterId } = storeToRefs(userStore);

// ------------------------------
// STATE
// ------------------------------
const courses = ref<InstructorDashboardCourse[]>([]);
const programUserId = ref<number | null>(null);
const isLoading = ref<boolean>(false);
const errorMessage = ref<string | null>(null);

// ------------------------------
// LOAD PROGRAM USER FOR CURRENT PROGRAM
// ------------------------------
async function loadProgramUserId() {
  if (!programId.value) return;

  const res = await api.get(`/program/${programId.value}/users`);
  const programUsers = res.data.data as ProgramUser[];

  const me = programUsers.find((pu) => pu.userId === userStore.userId);
  if (!me) throw new Error("User is not assigned to this program");

  programUserId.value = me.id;
}

// ------------------------------
// LOAD MEASURES FOR A COURSE
// ------------------------------
async function loadCourseMeasures(courseId: number): Promise<Measure[]> {
  try {
    const res = await api.get(`/measure/byCourse/${courseId}`);
    return res.data.data as Measure[];
  } catch (err) {
    console.error(`Failed to load measures for course ${courseId}:`, err);
    return [];
  }
}

// ------------------------------
// LOAD INSTRUCTOR COURSE LIST
// ------------------------------
async function loadInstructorCourses() {
  if (!programUserId.value || !semesterId.value) return;

  const cRes = await api.get("/section", {
    params: {
      semesterId: semesterId.value
    },
  });

  const rawCourses = cRes.data.data as RawCourse[];

  console.log(rawCourses)

  const results: InstructorDashboardCourse[] = [];

  for (const course of rawCourses) {
    const completenessRes = await api.get(`/courses/${course.id}/completeness`);
    const comp = completenessRes.data.data;

    results.push({
      id: course.id,
      courseCode: course.courseCode,
      instructorName: `${userStore.user?.firstName} ${userStore.user?.lastName}`,
      measuresCompleted: comp.completedMeasures,
      measuresTotal: comp.totalMeasures,
      measures: [],
      expanded: false,
    });
  }

  courses.value = results;
}

// ------------------------------
// TOGGLE COURSE EXPANSION
// ------------------------------
async function toggleCourse(course: InstructorDashboardCourse) {
  course.expanded = !course.expanded;

  // Load measures if expanding and not already loaded
  if (course.expanded && course.measures.length === 0) {
    course.measures = await loadCourseMeasures(course.id);
  }
}

// ------------------------------
// REFRESH MEASURES FOR A COURSE
// ------------------------------
async function refreshCourseMeasures(courseId: number) {
  const course = courses.value.find(c => c.id === courseId);
  if (!course) return;

  course.measures = await loadCourseMeasures(courseId);

  // Refresh completeness
  const completenessRes = await api.get(`/courses/${courseId}/completeness`);
  const comp = completenessRes.data.data;
  course.measuresCompleted = comp.completedMeasures;
  course.measuresTotal = comp.totalMeasures;
}

// ------------------------------
// COMBINED LOADER
// ------------------------------
async function reload() {
  if (!programId.value) {
    errorMessage.value = "No program selected. Please select a program from the navigation menu.";
    return;
  }

  if (!semesterId.value) {
    errorMessage.value = "No semester selected. Please select a semester from the navigation menu.";
    return;
  }

  isLoading.value = true;
  errorMessage.value = null;

  try {
    await loadProgramUserId();
    await loadInstructorCourses();
  } catch (err) {
    console.error(err);
    errorMessage.value = "Failed to load instructor dashboard";
  } finally {
    isLoading.value = false;
  }
}

onMounted(reload);

// Reload when program ID or semester ID changes
watch([programId, semesterId], () => {
  // Reset courses when semester changes to avoid showing stale data
  courses.value = [];
  reload();
});
</script>

<template>
  <section v-if="!isLoading" class="instructor-dashboard">
    <h1>Instructor Dashboard</h1>

    <div v-if="errorMessage" class="error">
      {{ errorMessage }}
    </div>

    <div v-else-if="!programId || !semesterId">
      <p class="info-message">
        Please select a program and semester from the navigation menu to view your courses.
      </p>
    </div>

    <div v-else-if="courses.length === 0">
      <p>No courses assigned for the current semester.</p>
    </div>

    <div v-else class="courses-container">
      <BaseCard
        v-for="course in courses"
        :key="course.id"
        class="course-card"
      >
        <div class="course-header" @click="toggleCourse(course)">
          <div class="course-info">
            <h3>{{ course.courseCode }}</h3>
          </div>
          <div class="course-stats">
            <span class="measures-count">
              {{ course.measuresCompleted }} / {{ course.measuresTotal }} measures completed
            </span>
            <button class="expand-button" :class="{ expanded: course.expanded }">
              {{ course.expanded ? '▼' : '▶' }}
            </button>
          </div>
        </div>

        <div v-if="course.expanded" class="measures-container">
          <div v-if="course.measures.length === 0" class="no-measures">
            No measures found for this course.
          </div>
          <MeasureListing
            v-for="measure in course.measures"
            :key="measure.id"
            :measure_prop="measure"
            @refresh="refreshCourseMeasures(course.id)"
          />
        </div>
      </BaseCard>
    </div>
  </section>

  <section v-else class="loading-screen">
    Loading instructor dashboard...
  </section>
</template>

<style scoped>
.instructor-dashboard {
  margin: 2rem;
}

.error {
  color: red;
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: #fee;
  border-radius: 0.5rem;
}

.info-message {
  color: #9ca3af;
  padding: 1rem;
  background-color: rgb(36, 36, 36);
  border-radius: 0.5rem;
  text-align: center;
}

.courses-container {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.course-card {
  background-color: rgb(36, 36, 36);
  border-radius: 0.75rem;
  overflow: hidden;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.course-header:hover {
  background-color: rgb(43, 43, 43);
}

.course-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.125rem;
  color: white;
}

.instructor-name {
  margin: 0;
  color: #9ca3af;
  font-size: 0.8125rem;
}

.course-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.measures-count {
  color: #9ca3af;
  font-size: 0.8125rem;
}

.expand-button {
  background: none;
  border: none;
  color: white;
  font-size: 0.875rem;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 0.25rem;
}

.expand-button.expanded {
  transform: rotate(0deg);
}

.measures-container {
  padding: 0 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.no-measures {
  padding: 1.5rem;
  text-align: center;
  color: #9ca3af;
  font-style: italic;
}

.loading-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  font-size: 1.25rem;
  color: #9ca3af;
}
</style>
