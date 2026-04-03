<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import api from "@/api";
import BaseCard from "@/components/ui/BaseCard.vue";
import CourseInspectModal from "./CourseInspectModal.vue";
import NewCourseModal from "./NewCourseModal.vue";

interface Course {
  id: number
  courseCode: string
  courseName: string
  courseDescription?: string
  isActive: boolean
}

const props = defineProps<{
  programId: number | null,
  semesterId: number | null
}>();

const loading = ref(false);
const error = ref<string | null>(null);
const courses = ref<Course[]>([]);

const selectedCourse = ref<Course | null>(null);

const isNewCourseModalOpen = ref(false);

async function saveNewCourse(data: any) {
  try {
    await api.post("/courses", {
      courseCode: data.courseCode,
      courseName: data.courseName,
      courseDescription: data.courseDescription,
      studentCount: data.studentCount,
      isActive: true
    });
    loadCourses(); // Refresh the list
  } catch (err) {
    console.error("Failed to save course:", err);
  }
}

async function loadCourses() {
  if (!props.programId || !props.semesterId) {
    courses.value = [];
    return;
  }
  loading.value = true;
  error.value = null;
  try {
    const res = await api.get(`/courses/searchCourse`, {
      params: { isActive: true }
    });
    courses.value = res.data?.data ?? [];
  } catch (err) {
    error.value = "Failed to load courses";
  } finally {
    loading.value = false;
  }
}

watch(() => [props.programId, props.semesterId], loadCourses);
onMounted(loadCourses);

// 3. UPDATE this function to set the ref instead of navigating
function selectCourse(course: Course) {
  selectedCourse.value = course;
}
</script>

<template>
  <h2 class="h2">Courses</h2>
  <section class="course-listing">

    <div v-if="loading" class="loading-state">
      <p>Loading courses...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="courses.length === 0" class="empty-state">
      <p>No active courses found.</p>
    </div>

    <div v-else class="course-grid">

      <BaseCard
        v-for="course in courses"
        :key="course.id"
        variant="elevated"
        hoverable
        class="course-card"
        @click="selectCourse(course)"
      >
        <div class="course-card-content">
          <div class="course-code">{{ course.courseCode }}</div>
          <div class="course-info">
            <h3 class="course-name">{{ course.courseName }}</h3>
          </div>
        </div>
      </BaseCard>

      <BaseCard
        variant="bordered"
        hoverable
        class="course-card add-new-card"
        @click="isNewCourseModalOpen = true"
      >
        <div class="add-new-content">
          <span class="plus-icon">+</span>
          <span class="add-text">New Course</span>
        </div>
      </BaseCard>

    </div>

    <!-- @vue-ignore -->
    <CourseInspectModal
      :course="selectedCourse"
      @close="selectedCourse = null"
    />

    <NewCourseModal
      :is-open="isNewCourseModalOpen"
      @close="isNewCourseModalOpen = false"
      @submitted="saveNewCourse"
    />

  </section>
</template>

<style scoped>
.h2 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}
.course-listing {
  margin-top: 1.5rem;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 1.25rem;
}

/* Make BaseCard fill the available height */
.course-card :deep(.base-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-secondary);
}

/* Make card-body expand to fill available space */
.course-card :deep(.card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-card {
  cursor: pointer;
  transition: all 0.2s ease;
  height: 100%;
}

.course-card-content {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
  height: 100%;
}

.course-code {
  background: var(--color-primary);
  color: white;
  padding: 0.4rem 0.75rem;
  margin-right: 1rem;
  border-radius: 0.4rem;
  font-weight: 600;
  font-size: 0.9rem;
  width: fit-content;
  height: fit-content;
}

.course-info {
  align-items: start;
  text-align: left;
}

.course-name {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.course-description {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.error-state {
  color: var(--color-error);
}
</style>
