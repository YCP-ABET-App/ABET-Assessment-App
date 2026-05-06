<template>
  <BaseModal :is-open="isOpen" @close="handleClose" size="md">
    <template #header>
      <h3 class="modal-title">Add New Course</h3>
    </template>

    <div class="form-container">
      <div class="form-group">
        <label class="label">Course Name <span class="required">*</span></label>
        <input
          v-model="formData.courseName"
          type="text"
          class="input"
          placeholder="Introduction to Computer Science"
          required
        />
      </div>

      <div class="form-group">
        <label class="label">Course Code <span class="required">*</span></label>
        <div class="course-code-row">
          <select v-model="codePrefix" class="input prefix-select">
            <option v-for="code in prefixes" :key="code" :value="code">
              {{ code }}
            </option>
          </select>
          <input
            v-model="codeSuffix"
            type="text"
            placeholder="101"
            class="input"
            required
          />
        </div>
      </div>

      <div class="form-group">
        <label class="label">Description<span class="required">*</span></label>
        <textarea
          v-model="formData.courseDescription"
          class="input"
          rows="3"
          placeholder="Enter course description..."
        ></textarea>
      </div>

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </div>

    <template #footer>
      <div class="form-actions-footer">
        <button class="btn-secondary" @click="handleClose">Cancel</button>
        <button
          class="btn-primary"
          :disabled="!isFormValid"
          @click="submitForm"
        >
          Save Course
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import BaseModal from "@/components/ui/BaseModal.vue";

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits(["close", "submitted"]);


const prefixes = ["N/A", "ECE", "ME", "CS", "CVE", "EGR"];
const codePrefix = ref("N/A");
const codeSuffix = ref("");
const errorMessage = ref("");

const formData = ref({
  courseName: "",
  courseDescription: ""
});


const isFormValid = computed(() => {
  return (
    formData.value.courseName.trim().length > 0 &&
    codeSuffix.value.trim().length > 0 &&
    codePrefix.value !== "N/A"
  );
});

function handleClose() {
  formData.value = { courseName: "", courseDescription: "" };
  codePrefix.value = "N/A"; // Reset to N/A
  codeSuffix.value = "";
  errorMessage.value = "";
  emit("close");
}

function submitForm() {
  if (!isFormValid.value) {
    errorMessage.value = "Please select a valid Course Prefix and fill in all fields.";
    return;
  }

  const fullCourseCode = `${codePrefix.value} ${codeSuffix.value.trim()}`;
  emit("submitted", {
    ...formData.value,
    courseCode: fullCourseCode
  });

  handleClose();
}
</script>

<style scoped>
.form-container {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 0.5rem 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.course-code-row {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 0.75rem;
}

.label {
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--color-text-primary);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.required {
  color: #ff4d4d;
  margin-left: 2px;
}

.error-text {
  color: #ff4d4d;
  font-size: 0.85rem;
  margin-top: -0.5rem;
  font-weight: 500;
}

.input {
  padding: 0.6rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--color-border-dark);
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  width: 100%;
  font-size: 0.95rem;
  transition: border-color 0.2s;
}

.input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.prefix-select {
  cursor: pointer;
}

.form-actions-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  width: 100%;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 0.65rem 1.5rem;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, filter 0.2s;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.5);
}

.btn-secondary {
  background: transparent;
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-dark);
  padding: 0.65rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
}

.modal-title {
  margin: 0;
  font-size: 1.25rem;
  color: var(--color-text-primary);
}
</style>
