<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';
import IndicatorListing from '@/components/IndicatorListing.vue';
import { BaseCard, BaseModal } from "@/components/ui";
import { useUserStore } from '@/stores/user-store';

const route = useRoute()
const userStore = useUserStore();

interface Instructor {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

interface ProgramColumn {
  programId: number;
  programName: string;
  entries: { indicatorId: number; scheduleEntryId: number }[];
}

const section_id = ref(NaN);
//Contains data from both the backend section and course objects
const section_obj = ref({
  course_id: NaN,
  semester_id: NaN,
  course_code: '',
  course_name: '',
  course_description: '',
  section_number: ''
})
const semester_name = ref('');
const instructors = ref<Instructor[]>([]);
const programColumns = ref<ProgramColumn[]>([]);
const addablePrograms = ref<{ id: number; name: string }[]>([]);
const selectedAddProgramId = ref<number | null>(null);
const addingProgram = ref(false);

const canAddProgram = computed(() =>
  instructors.value.some((instructor) => instructor.id === userStore.userId)
);

// Modal state
const selectedInstructor = ref<Instructor | null>(null);

async function fetch_course_section_data() {
  try {
    const { data } = await api.get(`/section`, {
      params: {
        "ids": [section_id.value]
      }
    });

    section_obj.value = {
      course_code: data.data.courses[0].courseCode,
      course_name: data.data.courses[0].courseName,
      course_description: data.data.courses[0].courseDescription,
      semester_id: data.data.sections[0].semesterId,
      course_id: data.data.sections[0].courseId,
      section_number: "Section " + data.data.sections[0].sectionNumber
    }
  } catch (error) {
    console.error('Error fetching or parsing course data:', error);
    return
  }
}

async function fetch_semester_data() {
  try {
    const { data } = await api.get(`/semesters`, {params:{"id": section_obj.value.semester_id}});
    semester_name.value = `${data.data[0].name}`
  } catch (error) {
    console.error('Error fetching or parsing course data:', error);
  }
}

async function fetch_instructor_ids() {
  try {
    const { data } = await api.get(`/section-user`, {params:{"sectionId": section_id.value}});
    let instructorIds: number[] = [];
    for(const entry of data.data){
      instructorIds.push(entry.userId)
    }

    // Fetch full instructor details for each ID
    const instructorPromises = instructorIds.map(async (id: number) => {
      try {
        const response = await api.get(`/users/${id}`);
        return {
          id: response.data.data.id,
          firstName: response.data.data.firstName,
          lastName: response.data.data.lastName,
          email: response.data.data.email
        };
      } catch (error) {
        console.error(`Error fetching instructor ${id}:`, error);
        return null;
      }
    });

    const results = await Promise.all(instructorPromises);
    instructors.value = results.filter(i => i !== null) as Instructor[];
  } catch (error) {
    console.error('Error fetching or parsing instructor data:', error);
  }
}

async function fetch_indicator_ids() {
  programColumns.value = [];
  try {
    const { data } = await api.get(`/section-program`, { params: { sectionId: section_id.value } });
    const sectionPrograms = data.data ?? [];

    for (const sp of sectionPrograms) {
      const seRes = await api.get(`/schedule-entry`, {
        params: {
          semesterId: section_obj.value.semester_id,
          courseId: section_obj.value.course_id,
          programId: sp.programId
        }
      });
      const entries = seRes.data?.data ?? [];
      if (entries.length === 0) continue;

      let programName = `Program ${sp.programId}`;
      try {
        const programRes = await api.get(`/program/${sp.programId}`);
        programName = programRes.data?.data?.name ?? programName;
      } catch (error) {
        console.error(`Error fetching program ${sp.programId}:`, error);
      }

      programColumns.value.push({
        programId: sp.programId,
        programName,
        entries: entries.map((entry: { id: number; indicatorId: number }) => ({
          indicatorId: entry.indicatorId,
          scheduleEntryId: entry.id
        }))
      });
    }
  } catch (error) {
    console.error('Error fetching or parsing course data:', error);
  }
}

async function loadAddablePrograms() {
  addablePrograms.value = [];
  try {
    const seRes = await api.get(`/schedule-entry`, {
      params: {
        semesterId: section_obj.value.semester_id,
        courseId: section_obj.value.course_id
      }
    });
    const scheduledIds = [...new Set((seRes.data?.data ?? []).map((e: { programId: number }) => e.programId))];
    const existing = new Set(programColumns.value.map((column) => column.programId));
    const programRes = await api.get(`/program`, { params: { page: 0, size: 100 } });
    const programs = programRes.data?.content ?? [];
    addablePrograms.value = programs.filter(
      (program: { id: number; name: string }) => scheduledIds.includes(program.id) && !existing.has(program.id)
    );
  } catch (error) {
    console.error('Error loading programs that can be added:', error);
  }
}

async function addProgramToSection() {
  if (!selectedAddProgramId.value) return;
  addingProgram.value = true;
  try {
    await api.post(`/section-program`, {
      sectionId: section_id.value,
      programId: selectedAddProgramId.value
    });
    selectedAddProgramId.value = null;
    await fetch_indicator_ids();
    await loadAddablePrograms();
  } catch (error) {
    console.error('Error adding program to section:', error);
  } finally {
    addingProgram.value = false;
  }
}

async function showInstructorDetails(instructorId: number) {
  try {
    // Fetch full instructor details
    const { data } = await api.get(`/users/${instructorId}`);

    selectedInstructor.value = {
      id: data.data.id,
      firstName: data.data.firstName,
      lastName: data.data.lastName,
      email: data.data.email
    };
  } catch (error) {
    console.error('Error fetching instructor details:', error);
  }
}

function closeInspectModal() {
  selectedInstructor.value = null;
}

async function initialize() {
  section_id.value = parseInt(route.params.section_id as string, 10)

  //Fetch Section data
  await fetch_course_section_data();

  //Fetch Semester data
  await fetch_semester_data();

  //Fetch Instructor IDs
  await fetch_instructor_ids();

  //Fetch Indicator IDs
  await fetch_indicator_ids();
  await loadAddablePrograms();
}

initialize();

</script>

<template>
  <section class="course-page">

    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="course-title">
          <div id="codes">
            <span id="course-code">{{ section_obj.course_code }}</span>
            <span id="section-code">{{ section_obj.section_number }}</span>
          </div>
          <div id="course-name">{{ section_obj.course_name }}</div>
        </div>

        <p class="subtitle">
          {{ semester_name }}
        </p>
      </div>
    </div>

    <!-- Description -->
    <p class="course-description">
      {{ section_obj.course_description }}
    </p>

    <!-- Instructors -->
    <section class="detail-section">
      <h3>Instructors</h3>
      <div class="instructor-list">
        <BaseCard
          v-for="instructor in instructors"
          :key="instructor.id"
          variant="elevated"
          hoverable
          class="instructor-card"
          @click="showInstructorDetails(instructor.id)"
        >
          <div class="instructor-card-content">
            <div class="instructor-avatar">
              {{ instructor.firstName?.charAt(0) }}{{ instructor.lastName?.charAt(0) }}
            </div>

            <div class="instructor-info">
              <h3 class="instructor-name">
                {{ instructor.firstName }} {{ instructor.lastName }}
              </h3>
              <p class="instructor-email">
                {{ instructor.email }}
              </p>
            </div>
          </div>
        </BaseCard>
      </div>
    </section>

    <!-- Indicators -->
    <section class="detail-section">
      <h3>Performance Indicators</h3>

      <div v-if="canAddProgram && addablePrograms.length > 0" class="add-program-row">
        <select v-model.number="selectedAddProgramId" class="program-select">
          <option :value="null">Add program with students in this section</option>
          <option
            v-for="program in addablePrograms"
            :key="program.id"
            :value="program.id"
          >
            {{ program.name }}
          </option>
        </select>
        <button
          class="btn-primary"
          :disabled="!selectedAddProgramId || addingProgram"
          @click="addProgramToSection"
        >
          {{ addingProgram ? 'Adding...' : 'Add program' }}
        </button>
      </div>

      <div class="program-columns" v-if="programColumns.length > 0">
        <div
          v-for="column in programColumns"
          :key="column.programId"
          class="program-column"
        >
          <h4>{{ column.programName }}</h4>
          <div class="indicator-list">
            <BaseCard
              v-for="entry in column.entries"
              :key="entry.scheduleEntryId"
              variant="default"
              class="indicator-card"
            >
              <IndicatorListing
                :piid="entry.indicatorId"
                :section_id="section_id"
                :instructor_id="instructors[0]?.id"
                :program_id="column.programId"
                :schedule_entry_id="entry.scheduleEntryId"
                :semester_id="section_obj.semester_id"
              />
            </BaseCard>
          </div>
        </div>
      </div>
    </section>

    <!-- Instructor Details Modal -->
    <BaseModal
      v-if="selectedInstructor"
      :isOpen="!!selectedInstructor"
      :title="`${selectedInstructor.firstName} ${selectedInstructor.lastName}`"
      size="lg"
      @close="closeInspectModal"
    >
      <div class="instructor-details">
        <!-- Personal Information Section -->
        <section class="detail-section personal-info-section">
          <h3>Personal Information</h3>

          <div class="info-card">
            <div class="info-row">
              <div class="info-field">
                <span class="field-label">First Name</span>
                <span class="field-value">{{ selectedInstructor.firstName }}</span>
              </div>
              <div class="info-field">
                <span class="field-label">Last Name</span>
                <span class="field-value">{{ selectedInstructor.lastName }}</span>
              </div>
            </div>

            <div class="info-row">
              <div class="info-field info-field-wide">
                <span class="field-label">Email Address</span>
                <span class="field-value">
                  <a :href="`mailto:${selectedInstructor.email}`" class="email-link">
                    {{ selectedInstructor.email }}
                  </a>
                </span>
              </div>
            </div>

            <div class="info-row">
              <div class="info-field">
                <span class="field-label">User ID</span>
                <span class="field-value field-value-muted">{{ selectedInstructor.id }}</span>
              </div>
            </div>
          </div>
        </section>
      </div>

      <template #footer>
        <button class="btn-primary" @click="closeInspectModal">
          Close
        </button>
      </template>
    </BaseModal>

  </section>
</template>

<style scoped>
.course-page {
  padding: 2rem;
  max-width: 1100px;
  margin: 0 auto;
}

/* Header */
.page-header {
  margin-bottom: 2rem;
}

.header-content {
  margin-bottom: 0.25rem;
}

.course-title {
  color: var(--color-text-primary);
  font-size: 2rem;
  font-weight: 700;
}

#course-name {
  margin-bottom: 1rem;
  color: var(--color-text-primary);
  font-size: 2rem;
  font-weight: 700;
}

#course-code {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 1rem;
  font-weight: 700;
}

#section-code {
  color: var(--color-text-secondary);
  font-size: 1rem;
  font-weight: 700;
  margin-left: 1rem;
  margin-bottom: 0rem;
}

.subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 1rem;
}

/* Description */
.course-description {
  margin-top: 0.75rem;
  margin-bottom: 2rem;
  color: var(--color-text-secondary);
  font-style: italic;
  font-size: 1rem;
}

/* Sections */
.detail-section {
  margin-top: 2.5rem;
}

.detail-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  color: var(--color-text-primary);
  border-bottom: 2px solid var(--color-border-light);
  padding-bottom: 0.5rem;
}

/* Instructor list (cards) */
.instructor-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.instructor-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.instructor-card:hover {
  transform: translateY(-2px);
}

.instructor-card-content {
  display: flex;
  align-items: center;
  text-align: left;
  gap: 1.25rem;
}

.instructor-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 600;
  flex-shrink: 0;
}

.instructor-info {
  flex: 1;
  min-width: 0;
}

.instructor-name {
  margin: 0 0 0.25rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.instructor-email {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-program-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 1rem;
}

.program-select {
  padding: 0.5rem;
  min-width: 16rem;
}

.program-columns {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
  overflow-x: auto;
}

.program-column {
  flex: 1;
  min-width: 18rem;
}

.program-column h4 {
  margin: 0 0 0.75rem 0;
  color: var(--color-text-primary);
}

/* Indicator cards */
.indicator-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.indicator-card {
  padding: 1.25rem;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: 0.5rem;
}

/* Modal Styles */
.instructor-details {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Personal Info Section */
.personal-info-section {
  background: rgba(255, 255, 255, 0.02);
  padding: 1.5rem;
  border-radius: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.personal-info-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.125rem;
  color: var(--color-text-primary);
  border-bottom: 2px solid var(--color-border-light);
  padding-bottom: 0.5rem;
}

.info-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-field-wide {
  grid-column: 1 / -1;
}

.field-label {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.5);
}

.field-value {
  font-size: 1rem;
  color: var(--color-text-primary);
  font-weight: 500;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border-light);
}

.field-value-muted {
  color: rgba(255, 255, 255, 0.5);
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
}

.email-link {
  color: var(--color-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.email-link:hover {
  text-decoration: underline;
  color: var(--color-primary-dark);
}

/* Button */
.btn-primary {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 0.625rem 1.5rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

@media (max-width: 768px) {
  .course-page {
    padding: 1rem;
  }

  .instructor-list {
    grid-template-columns: 1fr;
  }

  .info-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .personal-info-section {
    padding: 1rem;
  }

  .info-card {
    padding: 1rem;
  }
}
</style>
