<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import api from "@/api";
import BaseCard from "@/components/ui/BaseCard.vue";
import CourseInspectModal from "@/components/CourseInspectModal.vue";

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

const emit = defineEmits(["select"]);

const loading = ref(false);
const error = ref<string | null>(null);
const courses = ref<Course[]>([]);
const selectedCourse = ref<Course | null>(null);


/* -----------------------------------------------------------
 * Load courses (requires programId + semesterId)
 * ----------------------------------------------------------- */
async function loadCourses() {
  if (!props.programId || !props.semesterId) {
    courses.value = [];
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const res = await api.get(`/courses/active`, {
      params: {
        semesterId: props.semesterId,
        page: 0,
        size: 200
      }
    });

    console.log("ACTIVE COURSE RESPONSE:", res.data);

    courses.value = res.data?.content ?? [];

    console.log("COURSES SET TO:", courses.value);

  } catch (err) {
    console.error("Failed to load courses:", err);
    error.value = "Failed to load courses";
  }
  finally {
    loading.value = false;
  }
}


/* -----------------------------------------------------------
 * Watch for BOTH programId and semesterId
 * ----------------------------------------------------------- */
watch(
  () => [props.programId, props.semesterId],
  () => {
    loadCourses();
  }
);


/* -----------------------------------------------------------
 * Initial fetch
 * ----------------------------------------------------------- */
onMounted(loadCourses);


/* -----------------------------------------------------------
 * Course selection
 * ----------------------------------------------------------- */
function selectCourse(course: Course) {
  selectedCourse.value = course;
  emit("select", course);
}

function closeInspectModal() {
  selectedCourse.value = null;
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
      <p>No active courses found for this semester.</p>
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
          <div class="course-code">
            {{ course.courseCode }}
          </div>

          <div class="course-info">
            <h3 class="course-name">{{ course.courseName }}</h3>
            <p v-if="course.courseDescription" class="course-description">
              {{ course.courseDescription }}
            </p>
          </div>
        </div>
      </BaseCard>
    </div>
  </section>

  <!-- Course Inspect Modal -->
  <CourseInspectModal
    :course="selectedCourse"
    @close="closeInspectModal"
  />
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

.course-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.course-card-content {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
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
