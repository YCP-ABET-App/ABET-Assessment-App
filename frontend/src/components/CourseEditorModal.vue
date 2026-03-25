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
      showConfirmChoice.value = false;
      return;
    }

    error.value = null;
    showConfirmChoice.value = false;

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

async function handleInitialSave() {
  if (!selectedPrefix.value || !courseNumber.value || !courseName.value) {
    error.value = "Please fill in all required fields.";
    return;
  }
  showConfirmChoice.value = true;
}

async function executeSave(isNew: boolean) {
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

    if (isNew) {
      await api.post("/courses", payload);
    } else {
      await api.put(`/courses/${props.course.id}`, payload);
    }

    emit("saved");
    emit("close");
  } catch (err: any) {
    console.error("Save error:", err);
    error.value = err?.response?.data?.message || "Failed to save course changes.";
    showConfirmChoice.value = false;
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <BaseModal
    :is-open="!!course"
    @close="emit('close')"
    size="lg"
  >
    <template #header>
      <h2>{{ showConfirmChoice ? 'Confirm Save Type' : 'Edit Course' }}</h2>
    </template>

    <div class="modal-content">
      <div v-if="error" class="error-banner">
        {{ error }}
      </div>

      <form v-if="!showConfirmChoice" class="form" @submit.prevent="handleInitialSave">
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
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="label">Course Name</label>
          <input
            v-model="courseName"
            class="input"
            required
          />
        </div>

        <div class="form-group">
          <label class="label">Description</label>
          <textarea
            v-model="courseDescription"
            class="textarea"
            rows="4"
          />
        </div>

        <label class="checkbox-container">
          <input type="checkbox" v-model="isActive" />
          <span class="checkbox-label">Active Course</span>
        </label>
      </form>

      <div v-else class="choice-container">
        <p class="prompt-text">How should these changes be saved?</p>

        <div class="choice-grid">
          <button class="choice-card" @click="executeSave(false)" :disabled="saving">
            <div class="card-info">
              <h4>Update Existing</h4>
              <p>Overwrite <strong>{{ props.course?.courseCode }}</strong> with new details.</p>
            </div>
          </button>

          <button class="choice-card highlight" @click="executeSave(true)" :disabled="saving">
            <div class="card-info">
              <h4>Create as New</h4>
              <p>Keep old course and create <strong>{{ selectedPrefix }}{{ courseNumber }}</strong>.</p>
            </div>
          </button>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="footer-layout">
        <BaseButton
          variant="secondary"
          @click="showConfirmChoice ? (showConfirmChoice = false) : emit('close')"
          :disabled="saving"
        >
          {{ showConfirmChoice ? 'Back' : 'Cancel' }}
        </BaseButton>

        <BaseButton
          v-if="!showConfirmChoice"
          variant="primary"
          @click="handleInitialSave"
          :disabled="saving"
        >
          Review & Save
        </BaseButton>
      </div>
    </template>
  </BaseModal>
</template>

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

/* Choice View */
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

.choice-card.highlight {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary), transparent 95%);
}

.card-icon {
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
  color: #dc3545;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  border-left: 4px solid #dc3545;
}

@media (max-width: 600px) {
  .choice-grid {
    grid-template-columns: 1fr;
  }
}
</style>
