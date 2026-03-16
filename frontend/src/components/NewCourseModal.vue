<script setup lang="ts">
import { ref } from "vue";
import BaseModal from "@/components/ui/BaseModal.vue";
import { BaseButton } from "@/components/ui";

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits(["close", "submitted"]);

const formData = ref({
  courseCode: "",
  courseName: "",
  courseDescription: "",
  studentCount: 0,
});

function handleClose() {
  formData.value = { courseCode: "", courseName: "", courseDescription: "", studentCount: 0 };
  emit("close");
}

function submitForm() {
  emit("submitted", { ...formData.value });
  handleClose();
}
</script>

<template>
  <BaseModal :is-open="isOpen" @close="handleClose" size="md">
    <template #header>
      <h3>Add New Course</h3>
    </template>

    <div class="form-container">
      <div class="form-group">
        <label>Course Code</label>
        <input v-model="formData.courseCode" type="text" placeholder="CS101" />
      </div>

      <div class="form-group">
        <label>Course Name</label>
        <input v-model="formData.courseName" type="text" placeholder="Introduction to Computer Science" />
      </div>

      <div class="form-group">
        <label>Description</label>
        <textarea v-model="formData.courseDescription" rows="3"></textarea>
      </div>

      <div class="form-group">
        <label>Student Count</label>
        <input v-model.number="formData.studentCount" type="number" />
      </div>
    </div>

    <template #footer>
      <BaseButton variant="secondary" @click="handleClose">Cancel</BaseButton>
      <BaseButton variant="primary" @click="submitForm">Save Course</BaseButton>
    </template>
  </BaseModal>
</template>

<style scoped>
.form-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem 0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.form-group label {
  font-weight: 600;
  font-size: 0.9rem;
}

.form-group input,
.form-group textarea {
  padding: 0.5rem;
  border: 1px solid var(--color-border-dark);
  border-radius: 4px;
}
</style>
