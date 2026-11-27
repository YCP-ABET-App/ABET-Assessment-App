<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import api from '@/api';
import { useUserStore } from '@/stores/user-store';
import BaseButton from '@/components/ui/BaseButton.vue';
import BaseSelect from '@/components/ui/BaseSelect.vue';
import CourseEditorModal from '@/components/CourseEditorModal.vue';
import NewCourseModal from '@/components/NewCourseModal.vue';

interface CatalogCourse {
  id: number;
  courseCode: string;
  courseName: string;
  courseDescription?: string;
  isActive: boolean;
}

interface ProgramOption {
  id: number;
  name: string;
}

const router = useRouter();
const userStore = useUserStore();
const { isLoggedIn, user } = storeToRefs(userStore);

const displayName = computed(() => {
  if (user.value?.firstName && user.value?.lastName) {
    return `${user.value.firstName} ${user.value.lastName}`;
  }
  return user.value?.email || 'User';
});

const loading = ref(false);
const error = ref<string | null>(null);
const courses = ref<CatalogCourse[]>([]);
const programs = ref<ProgramOption[]>([]);
const programsByCourseId = ref<Map<number, number[]>>(new Map());
const expandedIds = ref<Set<number>>(new Set());

const prefixFilter = ref('');
const levelFilter = ref('');
const programFilter = ref('');

const editingCourse = ref<CatalogCourse | null>(null);
const isNewCourseModalOpen = ref(false);

function parseCourseCode(code: string) {
  const match = String(code ?? '').trim().match(/^([A-Za-z]+)\s*-?\s*(\d+)/);
  if (!match) {
    return { prefix: '', number: 0, level: null as number | null };
  }
  const number = Number(match[2]);
  return {
    prefix: match[1].toUpperCase(),
    number,
    level: Number.isFinite(number) ? Math.floor(number / 100) * 100 : null
  };
}

const prefixOptions = computed(() => {
  const prefixes = new Set<string>();
  for (const course of courses.value) {
    const { prefix } = parseCourseCode(course.courseCode);
    if (prefix) prefixes.add(prefix);
  }
  return [
    { label: 'All prefixes', value: '' },
    ...[...prefixes].sort().map((prefix) => ({ label: prefix, value: prefix }))
  ];
});

const levelOptions = [
  { label: 'All levels', value: '' },
  { label: '100', value: '100' },
  { label: '200', value: '200' },
  { label: '300', value: '300' },
  { label: '400', value: '400' },
  { label: '500+', value: '500' }
];

const programFilterOptions = computed(() => [
  { label: 'All programs', value: '' },
  ...programs.value.map((program) => ({
    label: program.name,
    value: String(program.id)
  }))
]);

function programNamesForCourse(courseId: number): string[] {
  const ids = programsByCourseId.value.get(courseId) ?? [];
  return ids
    .map((id) => programs.value.find((program) => program.id === id)?.name)
    .filter((name): name is string => Boolean(name));
}

const filteredCourses = computed(() => {
  return courses.value.filter((course) => {
    const parsed = parseCourseCode(course.courseCode);
    if (prefixFilter.value && parsed.prefix !== prefixFilter.value) {
      return false;
    }
    if (levelFilter.value) {
      const wanted = Number(levelFilter.value);
      if (wanted === 500) {
        if (parsed.level == null || parsed.level < 500) return false;
      } else if (parsed.level !== wanted) {
        return false;
      }
    }
    if (programFilter.value) {
      const attached = programsByCourseId.value.get(course.id) ?? [];
      if (!attached.includes(Number(programFilter.value))) {
        return false;
      }
    }
    return true;
  });
});

function toggleExpanded(courseId: number) {
  const next = new Set(expandedIds.value);
  if (next.has(courseId)) {
    next.delete(courseId);
  } else {
    next.add(courseId);
  }
  expandedIds.value = next;
}

async function loadCatalog() {
  loading.value = true;
  error.value = null;
  try {
    const [courseRes, programRes, scheduleRes] = await Promise.all([
      api.get('/courses/searchCourse'),
      api.get('/program', { params: { page: 0, size: 1000 } }),
      api.get('/schedule-entry')
    ]);

    courses.value = (courseRes.data?.data ?? [])
      .map((course: Omit<CatalogCourse, 'isActive'> & { isActive?: boolean }) => ({
        ...course,
        isActive: course.isActive ?? true
      }))
      .sort((a: CatalogCourse, b: CatalogCourse) =>
        String(a.courseCode).localeCompare(String(b.courseCode))
      );

    programs.value = (programRes.data?.content ?? programRes.data?.data ?? []).map((program: any) => ({
      id: Number(program.id),
      name: program.name
    }));

    const attached = new Map<number, number[]>();
    for (const entry of scheduleRes.data?.data ?? []) {
      const courseId = Number(entry.courseId);
      const programId = Number(entry.programId);
      if (!Number.isFinite(courseId) || !Number.isFinite(programId)) continue;
      const existing = attached.get(courseId) ?? [];
      if (!existing.includes(programId)) {
        existing.push(programId);
        attached.set(courseId, existing);
      }
    }
    programsByCourseId.value = attached;
  } catch (err) {
    console.error('Failed to load course catalog:', err);
    error.value = 'Failed to load course catalog';
  } finally {
    loading.value = false;
  }
}

async function saveNewCourse(data: any) {
  await api.post('/courses', {
    courseCode: data.courseCode,
    courseName: data.courseName,
    courseDescription: data.courseDescription,
    studentCount: data.studentCount,
    isActive: true
  });
  await loadCatalog();
}

function handleLogout() {
  userStore.logout();
  router.push('/login');
}

onMounted(loadCatalog);
</script>

<template>
  <div class="page-container">
    <NavBar
      :logged-in="isLoggedIn"
      :username="displayName"
      @logout="handleLogout"
    />

    <main class="content-area">
      <section class="catalog">
        <header class="catalog-header">
          <div>
            <h1>Course Catalog</h1>
            <p class="subtitle">All catalog courses. Editing a course does not change schedule entries.</p>
          </div>
          <BaseButton variant="primary" @click="isNewCourseModalOpen = true">
            Add Course
          </BaseButton>
        </header>

        <div class="filters">
          <BaseSelect
            v-model="prefixFilter"
            label="Prefix"
            :options="prefixOptions"
          />
          <BaseSelect
            v-model="levelFilter"
            label="Level"
            :options="levelOptions"
          />
          <BaseSelect
            v-model="programFilter"
            label="Program"
            :options="programFilterOptions"
          />
        </div>

        <div v-if="loading" class="state">Loading catalog...</div>
        <div v-else-if="error" class="state error">{{ error }}</div>
        <div v-else-if="filteredCourses.length === 0" class="state">
          No courses match these filters.
        </div>

        <div v-else class="course-list">
          <article
            v-for="course in filteredCourses"
            :key="course.id"
            class="course-row"
          >
            <button class="row-main" type="button" @click="toggleExpanded(course.id)">
              <span class="expand-icon" :class="{ open: expandedIds.has(course.id) }">▶</span>
              <span class="course-code">{{ course.courseCode }}</span>
              <span class="course-name">{{ course.courseName }}</span>
              <span class="program-tags">
                <span
                  v-for="name in programNamesForCourse(course.id)"
                  :key="name"
                  class="program-tag"
                >
                  {{ name }}
                </span>
                <span v-if="programNamesForCourse(course.id).length === 0" class="muted">
                  No programs
                </span>
              </span>
            </button>
            <button
              class="simple-edit-btn"
              type="button"
              title="Edit course"
              @click="editingCourse = course"
            >
              <img src="@/assets/icons/edit-pencil.svg" alt="Edit" class="edit-icon" />
            </button>
            <p v-if="expandedIds.has(course.id)" class="description">
              {{ course.courseDescription || 'No description provided.' }}
            </p>
          </article>
        </div>
      </section>
    </main>

    <NewCourseModal
      :is-open="isNewCourseModalOpen"
      @close="isNewCourseModalOpen = false"
      @submitted="saveNewCourse"
    />

    <CourseEditorModal
      :course="editingCourse"
      @close="editingCourse = null"
      @saved="loadCatalog"
    />
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-background-base);
}

.content-area {
  flex: 1;
  background-color: var(--color-background-soft);
}

.catalog {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 2rem 4rem;
  color: var(--color-text-primary);
}

.catalog-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.35rem;
}

.subtitle {
  margin: 0;
  color: var(--color-text-secondary);
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.state.error {
  color: var(--color-error);
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.course-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.75rem;
  padding: 0.9rem 1rem;
  background: var(--color-bg-secondary);
  border-radius: 0.5rem;
}

.row-main {
  display: grid;
  grid-template-columns: auto auto 1fr auto;
  align-items: center;
  gap: 0.75rem;
  background: none;
  border: none;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.expand-icon {
  transition: transform 0.2s ease;
  font-size: 0.75rem;
}

.expand-icon.open {
  transform: rotate(90deg);
}

.course-code {
  background: var(--color-primary);
  color: white;
  padding: 0.3rem 0.65rem;
  border-radius: 0.35rem;
  font-weight: 600;
  font-size: 0.85rem;
}

.course-name {
  font-weight: 600;
}

.program-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  justify-content: flex-end;
}

.program-tag {
  background: var(--color-bg-tertiary);
  border-radius: 999px;
  padding: 0.15rem 0.55rem;
  font-size: 0.75rem;
}

.muted {
  color: var(--color-text-secondary);
  font-size: 0.8rem;
}

.description {
  grid-column: 1 / -1;
  margin: 0;
  padding-top: 0.25rem;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
}

.simple-edit-btn {
  background: var(--color-primary);
  border: none;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  height: fit-content;
}

.edit-icon {
  width: 18px;
  height: 18px;
  display: block;
}
</style>
