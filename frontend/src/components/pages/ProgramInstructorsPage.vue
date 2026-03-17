<template>
  <section class="instructors-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <h2>Instructors</h2>
        <p
          class="subtitle"
          v-if="instructors.length > 0"
        >
          {{ instructors.length }} instructor{{ instructors.length !== 1 ? 's' : '' }}
          in selected program
        </p>
      </div>
    </div>

    <!-- Loading / error / grid -->
    <div v-if="loading" class="loading-state">
      <p>Loading instructors...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
    </div>

    <div v-else-if="instructors.length > 0" class="instructors-grid">
      <BaseCard
        v-for="instructor in instructors"
        :key="instructor.programUserId"
        variant="elevated"
        :hoverable="true"
        class="instructor-card"
        @click="showInstructorDetails(instructor)"
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
            <p class="instructor-meta">
              {{ instructor.sectionCount }} section{{ instructor.sectionCount !== 1 ? 's' : '' }}
              <span v-if="instructor.role === 'ADMIN'" class="role-badge">Admin</span>
            </p>
          </div>
        </div>
      </BaseCard>
    </div>

    <div v-else class="empty-state">
      <p>No instructors found in this program.</p>
    </div>

    <!-- Instructor Details Modal -->
    <BaseModal
      v-model:isOpen="showModal"
      :title="selectedInstructor
        ? `${selectedInstructor.firstName} ${selectedInstructor.lastName}`
        : 'Instructor Details'"
      size="lg"
      @close="closeModal"
    >
      <div v-if="selectedInstructor" class="instructor-details">
        <!-- Personal Info -->
        <section class="detail-section personal-info-section">
          <div class="section-header">
            <h3>Personal Information</h3>
            <button
              v-if="!isEditingInfo"
              class="btn-secondary btn-small"
              @click="startEditingInfo"
            >
              Edit Info
            </button>
          </div>

          <div v-if="!isEditingInfo" class="personal-info-display">
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
                  <span class="field-label">Role</span>
                  <span class="field-value">
                    <span :class="['role-badge-display', selectedInstructor.role.toLowerCase()]">
                      {{ selectedInstructor.role }}
                    </span>
                  </span>
                </div>
                <div class="info-field">
                  <span class="field-label">User ID</span>
                  <span class="field-value field-value-muted">{{ selectedInstructor.userId }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Edit Form -->
          <div v-else class="edit-form">
            <div class="form-row">
              <div class="form-group">
                <label>First Name</label>
                <BaseInput
                  v-model="editForm.firstName"
                  placeholder="First Name"
                  :error="editErrors.firstName"
                />
              </div>
              <div class="form-group">
                <label>Last Name</label>
                <BaseInput
                  v-model="editForm.lastName"
                  placeholder="Last Name"
                  :error="editErrors.lastName"
                />
              </div>
            </div>

            <div class="form-group">
              <label>Email</label>
              <BaseInput
                v-model="editForm.email"
                type="email"
                placeholder="Email"
                :error="editErrors.email"
              />
            </div>

            <div class="form-group">
              <label>Role</label>
              <BaseSelect
                v-model="editForm.role"
                :options="roleOptions"
              />
            </div>

            <div class="form-actions">
              <button
                class="btn-secondary"
                @click="cancelEditingInfo"
                :disabled="saving"
              >
                Cancel
              </button>
              <button
                class="btn-primary"
                @click="saveInstructorInfo"
                :disabled="saving"
              >
                {{ saving ? 'Saving...' : 'Save Changes' }}
              </button>
            </div>
          </div>
        </section>

        <!-- Sections -->
        <section class="detail-section">
          <h3>Sections ({{ currentSemesterSections.length }})</h3>

          <div v-if="loadingMeasures" class="loading-measures">
            <p>Loading measures data...</p>
          </div>

          <div v-else-if="currentSemesterSections.length > 0">
            <table class="courses-table">
              <thead>
              <tr>
                <th>Course Code</th>
                <th>Course Name</th>
                <th>Measures Progress</th>
              </tr>
              </thead>
              <tbody>
              <tr
                v-for="section in currentSemesterSections"
                :key="section.id"
                class="course-row clickable">
<!--                @click="showSectionDetails(section)"-->
<!--                <td>{{ course.courseCode || course.course_code }}</td>-->
<!--                <td>{{ course.courseName || course.course_name || '—' }}</td>-->
<!--                <td>-->
<!--                  <span v-if="course.measuresCompleted !== undefined && course.measuresTotal !== undefined">-->
<!--                    <span class="measures-count">-->
<!--                      {{ course.measuresCompleted }}/{{ course.measuresTotal }}-->
<!--                    </span>-->
<!--                    <span class="progress-percent">-->
<!--                      ({{ course.measuresTotal && course.measuresTotal > 0-->
<!--                      ? Math.round(-->
<!--                        ((course.measuresCompleted || 0) / course.measuresTotal) * 100-->
<!--                      )-->
<!--                      : 0 }}%)-->
<!--                    </span>-->
<!--                  </span>-->
<!--                  <span v-else class="no-data">—</span>-->
<!--                </td>-->
              </tr>
              </tbody>
            </table>
          </div>

          <p v-else class="no-courses">
            No courses assigned to this instructor for the current semester.
          </p>
        </section>
      </div>

      <template #footer>
        <button class="btn-primary" @click="closeModal">
          Close
        </button>
      </template>
    </BaseModal>

    <!-- Course Details Modal -->
    <BaseModal
      v-model:isOpen="showCourseModal"
      v-if="selectedCourse"
      @close="closeCourseModal"
      size="lg"
    >
      <template #header>
        <h2>{{ selectedCourse.courseCode || selectedCourse.course_code }}</h2>
      </template>

      <div class="course-modal-content">
        <!-- Section Information -->
        <section class="detail-section">
          <h3>Course Information</h3>
          <div class="info-card">
            <div class="info-row">
              <div class="info-field">
                <span class="field-label">COURSE CODE</span>
                <span class="field-value">
                  {{ selectedCourse.courseCode || selectedCourse.course_code }}
                </span>
              </div>
              <div class="info-field">
                <span class="field-label">COURSE NAME</span>
                <span class="field-value">
                  {{ selectedCourse.courseName || selectedCourse.course_name || '—' }}
                </span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-field info-field-wide">
                <span class="field-label">DESCRIPTION</span>
                <span class="field-value">
                  {{ selectedCourse.courseDescription || selectedCourse.course_description || 'No description available' }}
                </span>
              </div>
            </div>
          </div>
        </section>

        <!-- Measures Progress -->
        <section class="detail-section">
          <h3>Measures Progress</h3>
          <div class="measures-summary">
            <div class="progress-card">
              <div class="progress-label">Total Measures</div>
              <div class="progress-value">{{ selectedCourse.measuresTotal || 0 }}</div>
            </div>
            <div class="progress-card">
              <div class="progress-label">Completed</div>
              <div class="progress-value completed">{{ selectedCourse.measuresCompleted || 0 }}</div>
            </div>
            <div class="progress-card">
              <div class="progress-label">Completion Rate</div>
              <div class="progress-value">
                {{ selectedCourse.measuresTotal && selectedCourse.measuresTotal > 0
                ? Math.round(((selectedCourse.measuresCompleted || 0) / selectedCourse.measuresTotal) * 100)
                : 0 }}%
              </div>
            </div>
          </div>

          <!-- Progress Bar -->
          <div class="progress-bar-container">
            <div
              class="progress-bar-fill"
              :style="{
                width: selectedCourse.measuresTotal && selectedCourse.measuresTotal > 0
                  ? `${Math.round(((selectedCourse.measuresCompleted || 0) / selectedCourse.measuresTotal) * 100)}%`
                  : '0%'
              }"
            ></div>
          </div>
        </section>
      </div>

      <template #footer>
        <button class="btn-primary" @click="closeCourseModal">
          Close
        </button>
      </template>
    </BaseModal>
  </section>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, reactive, computed } from "vue";
import { storeToRefs } from "pinia";
import api from "@/api";
import { useUserStore } from "@/stores/user-store";
import BaseCard from "@/components/ui/BaseCard.vue";
import BaseModal from "@/components/ui/BaseModal.vue";
import BaseInput from "@/components/ui/BaseInput.vue";
import BaseSelect from "@/components/ui/BaseSelect.vue";
import { useToast } from "@/composables/use-toast";

const toast = useToast() as {
  success: (msg: string) => void;
  error: (msg: string) => void;
  info?: (msg: string) => void;
};

// User store for semester filtering
const userStore = useUserStore();
const { currentSemesterId } = storeToRefs(userStore);

interface Section {
  id: number;
  sectionNumber?: string;
  courseName?: string;
  formattedName?: string;
  courseDescription?: string;

  // Semester info
  semesterId?: number;
  semester?: {
    id: number;
    name?: string;
  };
}

interface Instructor {
  programUserId: number;
  userId: number;

  firstName: string;
  lastName: string;
  email: string;

  role: "ADMIN" | "INSTRUCTOR";

  sectionCount: number;
  sections: Section[];
}

interface ProgramUser {
  id: number;
  programId: number;
  userId: number;
  adminStatus: boolean;
}

interface EditForm {
  firstName: string;
  lastName: string;
  email: string;
  role: "ADMIN" | "INSTRUCTOR";
}

interface EditErrors {
  firstName?: string;
  lastName?: string;
  email?: string;
}

const props = defineProps<{
  programId: number | null
}>();

const instructors = ref<Instructor[]>([]);
const selectedInstructor = ref<Instructor | null>(null);
const showModal = ref(false);
const loading = ref(false);
const error = ref<string | null>(null);
const loadingMeasures = ref(false);
const showCourseModal = ref(false);
const selectedSection = ref<Section | null>(null);

// Editing state
const isEditingInfo = ref(false);
const saving = ref(false);
const editForm = reactive<EditForm>({
  firstName: "",
  lastName: "",
  email: "",
  role: "INSTRUCTOR"
});
const editErrors = reactive<EditErrors>({});

const roleOptions = [
  { value: "INSTRUCTOR", label: "Instructor" },
  { value: "ADMIN", label: "Admin" }
];

/* -----------------------------
 * Computed: Filter sections by current semester
 * ----------------------------- */
const currentSemesterSections = computed(() => {
  if (!selectedInstructor.value) {
    return [];
  }

  if (!currentSemesterId.value) {
    return selectedInstructor.value.sections || [];
  }

  const filtered = (selectedInstructor.value.sections || []).filter(section => {
    const courseSemesterId = section.semesterId || section.semester?.id;
    return courseSemesterId === currentSemesterId.value;
  });

  // Debug: Check for duplicates in filtered results
  const sectionIds = filtered.map(s => s.id);
  const uniqueSectionIds = new Set(sectionIds);
  if (sectionIds.length !== uniqueSectionIds.size) {
    console.warn(
      'Duplicate courses detected after filtering for current semester:',
      filtered.filter((s, index) => sectionIds.indexOf(s.id) !== index)
    );
  }

  return filtered;
});

/* -----------------------------
 * Load instructors for program
 * ----------------------------- */
async function loadProgramInstructors() {
  if (!props.programId) {
    console.warn("No programId provided to component.");
    return;
  }

  loading.value = true;
  error.value = null;

  try {
    const res = await api.get(`/program/${props.programId}/users`);

    const programUsers = res.data?.data || [];

    console.log("Found raw program users:", programUsers);

    if (!Array.isArray(programUsers) || programUsers.length === 0) {
      console.warn("API returned success, but the data array was empty.");
      instructors.value = [];
      return;
    }

    const loaded = await Promise.all(
      programUsers.map(async (pu: any) => {
        try {
          const userRes = await api.get(`/users/${pu.userId}`);
          const user = userRes.data.data;
          // Fetch all sections for this instructor (API doesn't support semester filter)
          const sectionRes = await api.get(`/section`, {
            params: { userId: pu.userId }
          });
          const allSections = sectionRes.data.data.sections ?? [];

          // Debug: Log if there are duplicates
          const sectionIds = allSections.map((c: Section) => c.id);
          const uniqueSectionIds = new Set(sectionIds);
          if (sectionIds.length !== uniqueSectionIds.size) {
            console.warn(
              `Instructor ${user.firstName} ${user.lastName} has duplicate sections from API:`,
              allSections.filter((s: Section, index: number) =>
                sectionIds.indexOf(s.id) !== index
              )
            );
          }

          console.log(sectionIds)
          console.log(allSections)
          console.log(currentSemesterId.value)

          // Count only sections from current semester
          const currentSemesterCount = currentSemesterId.value
            ? allSections.filter((s: Section) => {
              const sectionSemesterId = s.semesterId || s.semester?.id;
              return sectionSemesterId === currentSemesterId.value;
            }).length
            : allSections.length;

          return {
            programUserId: pu.id,
            userId: pu.userId,
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            role: pu.adminStatus ? "ADMIN" : "INSTRUCTOR",
            sectionCount: currentSemesterCount,
            sections: allSections
          };
        } catch {
          return null;
        }
      })
    );

    instructors.value = loaded.filter((x): x is Instructor => x !== null);

    console.log("Final instructors list for UI:", instructors.value);

  } catch (err: any) {
    console.error("Fetch Error:", err);
    error.value = "Failed to load instructors.";
  } finally {
    loading.value = false;
  }
}
/* -----------------------------
 * Editing functions
 * ----------------------------- */
function startEditingInfo() {
  if (!selectedInstructor.value) return;

  editForm.firstName = selectedInstructor.value.firstName;
  editForm.lastName = selectedInstructor.value.lastName;
  editForm.email = selectedInstructor.value.email;
  editForm.role = selectedInstructor.value.role;

  Object.keys(editErrors).forEach(key => delete editErrors[key as keyof EditErrors]);
  isEditingInfo.value = true;
}

function cancelEditingInfo() {
  isEditingInfo.value = false;
  Object.keys(editErrors).forEach(key => delete editErrors[key as keyof EditErrors]);
}

function validateEditForm(): boolean {
  Object.keys(editErrors).forEach(key => delete editErrors[key as keyof EditErrors]);

  let isValid = true;

  if (!editForm.firstName.trim()) {
    editErrors.firstName = "First name is required";
    isValid = false;
  }

  if (!editForm.lastName.trim()) {
    editErrors.lastName = "Last name is required";
    isValid = false;
  }

  if (!editForm.email.trim()) {
    editErrors.email = "Email is required";
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email)) {
    editErrors.email = "Invalid email format";
    isValid = false;
  }

  return isValid;
}

async function saveInstructorInfo() {
  if (!selectedInstructor.value || !validateEditForm()) return;

  saving.value = true;

  try {
    // Update user info
    await api.put(`/users/${selectedInstructor.value.userId}`, {
      firstName: editForm.firstName,
      lastName: editForm.lastName,
      email: editForm.email
    });

    // Update program user role (admin status)
    const adminStatus = editForm.role === "ADMIN";
    await api.put(`/program/${props.programId}/users/${selectedInstructor.value.programUserId}`, {
      adminStatus
    });

    // Update local state
    selectedInstructor.value.firstName = editForm.firstName;
    selectedInstructor.value.lastName = editForm.lastName;
    selectedInstructor.value.email = editForm.email;
    selectedInstructor.value.role = editForm.role;

    // Update in instructors list
    const instructorIndex = instructors.value.findIndex(
      i => i.programUserId === selectedInstructor.value?.programUserId
    );
    if (instructorIndex !== -1) {
      instructors.value[instructorIndex] = { ...selectedInstructor.value };
    }

    toast.success("Instructor updated successfully");
    isEditingInfo.value = false;

  } catch (err: any) {
    console.error("Error saving instructor:", err);
    toast.error(
      err.response?.data?.message || "Failed to update instructor"
    );
  } finally {
    saving.value = false;
  }
}

// Watch for program or semester changes and reload
watch(() => props.programId, () => {
  loadProgramInstructors();
});

watch(currentSemesterId, () => {
  loadProgramInstructors();
});

onMounted(() => {
  if (props.programId) loadProgramInstructors();
});

function closeModal() {
  showModal.value = false;
  selectedInstructor.value = null;
  isEditingInfo.value = false;
  loadingMeasures.value = false;
}

function showInstructorDetails(instructor: Instructor) {
  selectedInstructor.value = instructor;
  showModal.value = true;
}

function showCourseDetails(section: Section) {
  selectedSection.value = section;
  showCourseModal.value = true;
}

function closeCourseModal() {
  showCourseModal.value = false;
  selectedSection.value = null;
}
</script>

<style scoped>
.instructors-page {
  width: 100%;
  padding: 0.4rem 0.75rem;
}

/* Clickable course rows */
.course-row.clickable {
  cursor: pointer;
  transition: background-color 0.2s;
}

.course-row.clickable:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

/* Course modal content */
.course-modal-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Measures summary cards */
.measures-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.progress-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0.5rem;
  padding: 1.5rem;
  text-align: center;
}

.progress-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 0.5rem;
}

.progress-value {
  font-size: 2rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.progress-value.completed {
  color: #60a5fa;
}

/* Progress bar */
.progress-bar-container {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #60a5fa, #3b82f6);
  transition: width 0.3s ease;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 2rem;
}

.header-content {
  margin-bottom: 1.5rem;
}

.page-header h2 {
  margin: 0 0 0.5rem 0;
  color: var(--color-text-primary);
  font-size: 1.5rem;
}

.subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 1rem;
}

/* Loading, Error, Empty States */
.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-secondary);
}

.error-state {
  color: var(--color-error);
}

.loading-measures {
  text-align: center;
  padding: 1.5rem;
  color: var(--color-text-secondary);
  font-style: italic;
}

/* Instructors Grid */
.instructors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.25rem;
}

.instructor-card {
  cursor: pointer;
  transition: all 0.2s ease;
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
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.instructor-meta {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text-tertiary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.role-badge {
  display: inline-block;
  padding: 0.125rem 0.5rem;
  background: var(--color-primary);
  color: white;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

/* Instructor Details */
.instructor-details {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.detail-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.125rem;
  color: var(--color-text-primary);
  border-bottom: 2px solid var(--color-border-light);
  padding-bottom: 0.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  border-bottom: 2px solid var(--color-border-light);
  padding-bottom: 0.5rem;
}

.section-header h3 {
  margin: 0;
  border: none;
  padding: 0;
}

/* Personal Info Display */
.personal-info-section {
  background: rgba(255, 255, 255, 0.02);
  padding: 1.5rem;
  border-radius: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.personal-info-display {
  margin-top: 0;
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
  color: var(--color-text-primary, #212529);
  font-weight: 500;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border-light, #e9ecef);
}

.field-value-muted {
  color: rgba(255, 255, 255, 0.5);
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
}

.email-link {
  color: var(--color-primary, #60a5fa);
  text-decoration: none;
  transition: color 0.2s;
}

.email-link:hover {
  text-decoration: underline;
  color: var(--color-primary-dark, #0056b3);
}

.role-badge-display {
  display: inline-flex;
  align-items: center;
  padding: 0.375rem 0.875rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.role-badge-display.admin {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.role-badge-display.instructor {
  background: rgba(168, 85, 247, 0.15);
  color: #a78bfa;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

/* Outcomes List */
.outcomes-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.outcome-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0.5rem;
  padding: 1.25rem;
  transition: all 0.2s;
}

.outcome-card:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.15);
}

.outcome-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.outcome-number {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
  color: white;
  font-weight: 700;
  font-size: 0.875rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.375rem;
  min-width: 2.5rem;
  text-align: center;
  flex-shrink: 0;
}

.outcome-content {
  flex: 1;
}

.outcome-description {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}

/* Indicators */
.indicators-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.indicators-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.indicators-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: rgba(255, 255, 255, 0.5);
}

.indicator-count {
  background: rgba(168, 85, 247, 0.15);
  color: #a78bfa;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

.indicators-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.indicator-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 0.375rem;
  border-left: 3px solid #a78bfa;
}

.indicator-badge {
  background: rgba(168, 85, 247, 0.2);
  color: #a78bfa;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  min-width: 2rem;
  text-align: center;
  flex-shrink: 0;
}

.indicator-text {
  flex: 1;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
}

.indicator-threshold {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
  flex-shrink: 0;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: rgba(255, 255, 255, 0.5);
}

.empty-state-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.3;
}

.empty-state-text {
  font-size: 0.95rem;
  font-style: italic;
}

@media (max-width: 768px) {
  .outcome-header {
    flex-direction: column;
  }

  .indicator-item {
    flex-direction: column;
    gap: 0.5rem;
  }
}

/* Edit Form */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border-light);
}

/* Courses Table */
.courses-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0.5rem;
}

.courses-table th,
.courses-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--color-border-light);
}

.courses-table th {
  background: var(--color-bg-tertiary);
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.courses-table tbody tr:hover {
  background: var(--color-bg-secondary);
}

.measures-count {
  font-weight: 600;
  color: var(--color-text-primary);
}

.progress-percent {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin-left: 0.25rem;
}

.no-data {
  color: var(--color-text-tertiary);
  font-style: italic;
}

.no-courses {
  color: var(--color-text-secondary);
  font-style: italic;
  margin: 1rem 0;
}

/* Button Styles */
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

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-dark);
  padding: 0.625rem 1.5rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background: var(--color-bg-secondary);
  border-color: var(--color-border-dark);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-small {
  padding: 0.375rem 1rem;
  font-size: 0.8125rem;
}

/* Responsive */
@media (max-width: 768px) {
  .instructors-page {
    padding: 1rem;
  }

  .instructors-grid {
    grid-template-columns: 1fr;
  }

  .info-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .courses-table {
    font-size: 0.875rem;
    display: block;
    overflow-x: auto;
  }

  .courses-table th,
  .courses-table td {
    padding: 0.5rem;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions button {
    width: 100%;
  }

  .personal-info-section {
    padding: 1rem;
  }

  .info-card {
    padding: 1rem;
  }
}
</style>
