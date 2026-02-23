<script lang="ts" setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';
import IndicatorListing from '@/components/IndicatorListing.vue';
import { BaseCard, BaseModal } from "@/components/ui";

const route = useRoute()

interface Instructor {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

const section_id = ref(NaN);
const course_obj = ref({
  id: NaN,
  course_code: '',
  course_name: '',
  course_description: '',
  semester_id: '',
  created_at: '',
  is_active: false
});
const section_obj = ref({

})
const semester_name = ref('');
const instructors = ref<Instructor[]>([]);
const indicator_ids = ref([]);

// Modal state
const selectedInstructor = ref<Instructor | null>(null);

//--------TEST DATA--------
/*
const course_id = ref(1);
const course_obj = ref({
    id: 1,
    course_code: 'CS101',
    course_name: 'Fundamentals of Computer Science I',
    course_description: 'Basic programming concepts and problem solving',
    semester_id: '1',
    created_at: '2025-11-10T10:16:56.456221',
    is_active: true
});
const semester_name = ref('Fall 2025');
const instructor_ids = ref([1, 2]);
const indicator_ids = ref([1,2])
*/
//--------------------------

async function fetch_course_section_data() {
  try {
    const { data } = await api.get(`/sections/${section_id.value}`);
    console.log(data)
    course_obj.value = {
      id: data.data.id,
      course_code: data.data.courseCode,
      course_name: data.data.courseName,
      course_description: data.data.courseDescription,
      semester_id: data.data.semesterId,
      created_at: data.data.createdAt,
      is_active: data.data.isActive
    }
  } catch (error) {
    console.error('Error fetching or parsing course data:', error);
    return
  }
}

async function fetch_semester_data() {
  try {
    const { data } = await api.get(`/semesters/${course_obj.value.semester_id}`);
    semester_name.value = `${data.data.name}`
  } catch (error) {
    console.error('Error fetching or parsing course data:', error);
  }
}

async function fetch_instructor_ids() {
  try {
    const { data } = await api.get(`/courses/${section_id.value}/instructors`);
    const instructorIds = data;

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
  try {
    const { data } = await api.get(`/courses/${section_id.value}/indicators`);
    indicator_ids.value = data
  } catch (error) {
    console.error('Error fetching or parsing course data:', error);
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

  //Fetch Course data
  await fetch_course_section_data();

  //Fetch Semester data
  await fetch_semester_data();

  //Fetch Instructor IDs
  fetch_instructor_ids();

  //Fetch Indicator IDs
  fetch_indicator_ids();
}

initialize();

</script>

<template>
  <section class="course-page">

    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="course-title">
          {{ course_obj.course_code }} — {{ course_obj.course_name }}
        </h2>

        <p class="subtitle">
          {{ semester_name }}
        </p>
      </div>
    </div>

    <!-- Description -->
    <p class="course-description">
      {{ course_obj.course_description }}
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

      <div class="indicator-list">
        <BaseCard v-for="piid in indicator_ids" :key="piid" variant="default" class="indicator-card">
          <IndicatorListing :piid="piid" :course_id="section_id" :instructor_id="instructors[0].id"/>
        </BaseCard>
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
  margin: 0;
  color: var(--color-text-primary);
  font-size: 2rem;
  font-weight: 700;
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
