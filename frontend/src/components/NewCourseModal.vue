<template>
  <BaseModal :is-open="isOpen" @close="handleClose" size="md">
    <template #header>
      <h3 class="modal-title">Add New Course</h3>
    </template>
      <div class="form-group">
        <label class="label">Course Name</label>
        <input
          v-model="formData.courseName"
          type="text"
          class="input"
          placeholder="Introduction to Computer Science"
        />
      </div>

    <div class="form-container">
      <div class="form-group">
        <label class="label">Course Code</label>
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
          />
        </div>
      </div>


      <div class="form-group">
        <label class="label">Description</label>
        <textarea
          v-model="formData.courseDescription"
          class="input"
          rows="3"
          placeholder="Enter course description..."
        ></textarea>
      </div>

      <div class="form-row-double">
        <div class="form-group">
          <label class="label">Student Count</label>
          <input
            v-model.number="formData.studentCount"
            type="number"
            class="input"
          />
        </div>

        <div class="form-group">
          <label class="label">Threshold (%)</label>
          <input
            v-model.number="formData.threshold"
            type="number"
            class="input"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="form-actions-footer">
        <button class="btn-secondary" @click="handleClose">
          Cancel
        </button>
        <button class="btn-primary" @click="submitForm">
          Save Course
        </button>
      </div>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from "vue";
import BaseModal from "@/components/ui/BaseModal.vue";

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits(["close", "submitted"]);

const prefixes = ["ECE", "ME", "CS", "CVE"];
const codePrefix = ref("ECE");
const codeSuffix = ref("");

const formData = ref({
  courseName: "",
  courseDescription: "",
  studentCount: 0,
  threshold: 0
});

function handleClose() {
  formData.value = { courseName: "", courseDescription: "", studentCount: 0, threshold: 0 };
  codePrefix.value = "ECE";
  codeSuffix.value = "";
  emit("close");
}

function submitForm() {
  const fullCourseCode = `${codePrefix.value} ${codeSuffix.value.trim()}`;
  emit("submitted", { ...formData.value, courseCode: fullCourseCode });
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

/* Horizontal alignment for numeric inputs */
.form-row-double {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

/* Dropdown + Input group */
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

.input {
  padding: 0.6rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--color-border-dark);
  background: var(--color-bg-secondary); /* This provides that black/dark background */
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
  transition: opacity 0.2s;
}

.btn-primary:hover {
  opacity: 0.9;
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
