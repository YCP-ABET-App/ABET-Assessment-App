<template>
  <BaseModal
    :is-open="!!course"
    @close="emit('close')"
    size="lg"
  >
    <template #header>
      <h2>
        {{ confirmingMode ? 'Final Confirmation' : (showConfirmChoice ? 'Choose Save Method' : 'Edit Course') }}
      </h2>
    </template>

    <div class="modal-content">
      <div v-if="error" class="error-banner">
        {{ error }}
      </div>

      <form v-if="!showConfirmChoice && !confirmingMode" class="form" @submit.prevent="handleInitialSave">
        <div class="form-group">
          <label class="label">Course Code</label>
          <div class="course-code-row">
            <BaseSelect
              v-model="selectedPrefix"
              :options="prefixOptions"
              class="prefix-select"
            />
            <input
              v-model="courseNumber"
              class="input number-input"
              placeholder="e.g. 101"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="label">Course Name</label>
          <input
            v-model="courseName"
            class="input"
            placeholder="e.g. Software Engineering"
            required
          />
        </div>

        <div class="form-group">
          <label class="label">Description</label>
          <textarea
            v-model="courseDescription"
            class="textarea"
            rows="4"
            placeholder="Optional course description..."
          />
        </div>

        <label class="checkbox-container">
          <input
            type="checkbox"
            :checked="isActive"
            @change="isActive = !isActive"
          />
          <span class="checkbox-label">Active Course</span>
        </label>
      </form>

      <div v-else-if="showConfirmChoice && !confirmingMode" class="choice-container">
        <p class="prompt-text">How should these changes be saved?</p>

        <div class="choice-grid">
          <button class="choice-card" @click="requestConfirm(false)" :disabled="saving">
            <div class="card-info">
              <h4>Update Existing</h4>
              <p>Apply changes to the current record and all connected data.</p>
            </div>
          </button>

          <button class="choice-card" @click="requestConfirm(true)" :disabled="saving">
            <div class="card-info">
              <h4>Create as New</h4>
              <p>Keep the original course and start a new branch.</p>
            </div>
          </button>
        </div>
      </div>

      <div v-else-if="confirmingMode" class="warning-container">
        <div class="warning-wrapper">
        <div class="warning-icon">⚠️</div>

        <div v-if="!pendingIsNew" class="warning-text">
          <h3>Warning: Updating Existing Record</h3>
          <p>
            By choosing <strong>Update Existing</strong>, this will affect all existing historic data
            for this course. Reports of past semesters will be affected, showing this new course
            information instead of the original details.
          </p>
          <p class="confirm-ask">Are you sure you want to do this?</p>
        </div>

        <div v-else class="warning-text">
          <h3>Notice: Creating New Branch</h3>
          <p>
            By choosing <strong>Create as New</strong>, this will branch this current course
            into a new record. This will <strong>not</strong> affect historical data or reports
            from previous semesters.
          </p>
          <p class="confirm-ask">Are you sure you want to proceed?</p>
        </div>
      </div>
      </div>
    </div>

    <template #footer>
      <div class="footer-layout">
        <BaseButton
          variant="secondary"
          @click="handleNavigationBack"
          :disabled="saving"
        >
          {{ (showConfirmChoice || confirmingMode) ? 'Back' : 'Cancel' }}
        </BaseButton>

        <BaseButton
          v-if="!showConfirmChoice || confirmingMode"
          variant="primary"
          @click="confirmingMode ? executeSave() : handleInitialSave()"
          :disabled="saving"
        >
          <span v-if="saving">Saving...</span>
          <span v-else-if="confirmingMode">Confirm & Save</span>
          <span v-else>Review Changes</span>
        </BaseButton>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import api from "@/api";
import BaseModal from "@/components/ui/BaseModal.vue";
import BaseButton from "@/components/ui/BaseButton.vue";
import BaseSelect from "@/components/ui/BaseSelect.vue";

interface Course {
  id: number;
  courseCode: string;
  courseName: string;
  courseDescription?: string;
  isActive: boolean;
}

const props = defineProps<{
  course: Course | null;
}>();

const emit = defineEmits(["close", "saved"]);

const selectedPrefix = ref("");
const courseNumber = ref("");
const courseName = ref("");
const courseDescription = ref("");
const isActive = ref(true);

const saving = ref(false);
const error = ref<string | null>(null);
const showConfirmChoice = ref(false);
const confirmingMode = ref(false);
const pendingIsNew = ref(false);

const prefixOptions = [
  { value: "EGR ", label: "EGR" },
  { value: "ECE ", label: "ECE" },
  { value: "CS ", label: "CS" },
  { value: "ME ", label: "ME" },
  { value: "CVE ", label: "CVE" }
];

watch(
  () => props.course,
  (newCourse) => {
    if (!newCourse) {
      resetUIState();
      return;
    }

    error.value = null;
    const firstSpaceIndex = newCourse.courseCode.indexOf(" ");

    if (firstSpaceIndex !== -1) {
      selectedPrefix.value = newCourse.courseCode.substring(0, firstSpaceIndex + 1);
      courseNumber.value = newCourse.courseCode.substring(firstSpaceIndex + 1);
    } else {
      selectedPrefix.value = "";
      courseNumber.value = newCourse.courseCode;
    }

    courseName.value = newCourse.courseName;
    courseDescription.value = newCourse.courseDescription || "";
    isActive.value = newCourse.isActive;
  },
  { immediate: true }
);

function resetUIState() {
  showConfirmChoice.value = false;
  confirmingMode.value = false;
  pendingIsNew.value = false;
  error.value = null;
}

function handleInitialSave() {
  if (!selectedPrefix.value || !courseNumber.value || !courseName.value) {
    error.value = "Please fill in all required fields.";
    return;
  }
  showConfirmChoice.value = true;
}

function requestConfirm(isNew: boolean) {
  pendingIsNew.value = isNew;
  confirmingMode.value = true;
}

function handleNavigationBack() {
  if (confirmingMode.value) {
    confirmingMode.value = false;
  } else if (showConfirmChoice.value) {
    showConfirmChoice.value = false;
  } else {
    emit("close");
  }
}

async function executeSave() {
  if (!props.course) return;

  saving.value = true;
  error.value = null;

  try {
    const fullCourseCode = `${selectedPrefix.value}${courseNumber.value}`.trim();
    const payload = {
      courseCode: fullCourseCode,
      courseName: courseName.value,
      courseDescription: courseDescription.value,
      isActive: isActive.value
    };

    if (pendingIsNew.value) {
      await api.post("/courses", payload);
    } else {
      await api.put(`/courses/${props.course.id}`, payload);
    }

    emit("saved");
    emit("close");
  } catch (err: any) {
    console.error("Save error:", err);
    error.value = err?.response?.data?.message || "Failed to save changes.";
    confirmingMode.value = false;
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.modal-content {
  min-height: 250px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.course-code-row {
  display: flex;
  gap: 0.75rem;
}

.prefix-select {
  width: 130px;
}

.number-input {
  flex: 1;
}

.input, .textarea {
  width: 100%;
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--color-border-dark);
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-size: 1rem;
  transition: border-color 0.2s;
}

.input:focus, .textarea:focus {
  border-color: var(--color-primary);
  outline: none;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  width: fit-content;
}

.checkbox-label {
  font-size: 0.95rem;
  font-weight: 500;
}

.choice-container {
  text-align: center;
  padding: 1rem 0;
}

.prompt-text {
  font-size: 1.1rem;
  margin-bottom: 2rem;
  color: var(--color-text-primary);
}

.choice-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.choice-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border: 1px solid var(--color-border-dark);
  border-radius: 12px;
  background: var(--color-bg-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}

.choice-card:hover:not(:disabled) {
  border-color: var(--color-primary);
  transform: translateY(-3px);
  background: var(--color-bg-tertiary);
}


.card-icon {
  text-align: center;
  font-size: 2rem;
}

.card-info h4 {
  margin: 0 0 0.5rem 0;
  color: var(--color-primary);
}

.card-info p {
  font-size: 0.85rem;
  line-height: 1.4;
  color: var(--color-text-secondary);
  margin: 0;
}

.footer-layout {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  width: 100%;
}

.error-banner {
  background: rgba(220, 53, 69, 0.1);
  text-align: center;
  color: #dc3545;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  border-left: 4px solid #dc3545;
}
.warning-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  max-width: 500px;
  margin: 0 auto;
}

.warning-icon {
  font-size: 4rem;
  margin-bottom: 0.5rem;
}

.warning-content {
  text-align: center;
}

.warning-wrapper .prompt-text {
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

@media (max-width: 600px) {
  .choice-grid {
    grid-template-columns: 1fr;
  }
}
</style>
