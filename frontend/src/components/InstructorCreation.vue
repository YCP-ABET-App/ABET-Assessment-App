<template>
  <div class="instructor-creation-form">
    <div v-if="apiError" class="error-banner">
      {{ apiError }}
    </div>

    <form class="form" @submit.prevent="submitForm">
      <div class="form-row-triple">
        <div class="form-group title-col">
          <label class="label">Title</label>
          <input
            v-model="form.name_title"
            class="input"
            placeholder="Dr."
          />
        </div>
        <div class="form-group name-col">
          <label class="label">First Name</label>
          <input
            v-model="form.name_first"
            class="input"
            :class="{ 'input-error': errors.name_first }"
            placeholder="John"
          />
          <span v-if="errors.name_first" class="field-error">
            {{ errors.name_first }}
          </span>
        </div>
        <div class="form-group name-col">
          <label class="label">Last Name</label>
          <input
            v-model="form.name_last"
            class="input"
            :class="{ 'input-error': errors.name_last }"
            placeholder="Smith"
          />
          <span v-if="errors.name_last" class="field-error">
            {{ errors.name_last }}
          </span>
        </div>
      </div>

      <div class="form-group">
        <label class="label">Email Address</label>
        <input
          v-model="form.email"
          type="email"
          class="input"
          :class="{ 'input-error': errors.email }"
          placeholder="jsmith@ycp.edu"
        />
        <span v-if="errors.email" class="field-error">
          {{ errors.email }}
        </span>
      </div>

      <div class="form-group">
        <label class="label">Password</label>
        <input
          v-model="form.password"
          type="password"
          class="input"
          :class="{ 'input-error': errors.password }"
          placeholder="abc123"
        />
        <span v-if="errors.password" class="field-error">
          {{ errors.password }}
        </span>
      </div>

      <div class="form-group">
        <label class="label">System Role</label>
        <select v-model="form.role" class="input select-input">
          <option value="INSTRUCTOR">Instructor</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>

      <div class="form-actions-footer">
        <button
          type="button"
          class="btn-secondary"
          @click="$emit('cancel')"
        >
          Cancel
        </button>
        <button
          type="submit"
          class="btn-primary"
          :disabled="loading"
        >
          <span v-if="loading">Saving...</span>
          <span v-else>Save Instructor</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import api from '@/api';
import { useToast } from "@/composables/use-toast";

const props = defineProps<{
  programId: number | null
}>();

const emit = defineEmits(['success', 'cancel']);
const toast = useToast() as any;

const loading = ref(false);
const apiError = ref<string | null>(null);

const form = reactive({
  name_title: '',
  name_first: '',
  name_last: '',
  email: '',
  password: '',
  role: 'INSTRUCTOR' as 'ADMIN' | 'INSTRUCTOR'
});

const errors = reactive({
  name_first: '',
  name_last: '',
  email: '',
  password: ''
});

function validate() {
  let isValid = true;

  // Clear previous errors
  errors.name_first = '';
  errors.name_last = '';
  errors.email = '';
  errors.password = '';

  if (!form.name_first.trim()) {
    errors.name_first = 'First name is required';
    isValid = false;
  }

  if (!form.name_last.trim()) {
    errors.name_last = 'Last name is required';
    isValid = false;
  }

  if (!form.email.includes('@')) {
    errors.email = 'Valid email is required';
    isValid = false;
  }

  if (form.password.length < 4) {
    errors.password = 'Password must be at least 4 characters';
    isValid = false;
  }

  return isValid;
}

async function submitForm() {
  if (!props.programId || !validate()) return;

  loading.value = true;
  apiError.value = null;

  try {
    // Step 1: Create the User
    const userPayload = {
      firstName: form.name_first.trim(),
      lastName: form.name_last.trim(),
      email: form.email.trim().toLowerCase(),
      passwordHash: form.password,
      name_title: form.name_title.trim()
    };

    const userRes = await api.post('/users', userPayload);

    // Extract ID from common response patterns
    const newUserId = userRes.data?.id || userRes.data?.data?.id;

    if (!newUserId) {
      throw new Error("User created but ID was not found in response.");
    }

    // Step 2: Link User to Program
    await api.post(`/program/${props.programId}/users`, {
      userId: newUserId,
      adminStatus: form.role === 'ADMIN'
    });

    toast.success("Instructor added successfully!");
    emit('success');
  } catch (err: any) {
    console.error("Submission error:", err);
    apiError.value = err.response?.data?.message || "An unexpected error occurred. Use a unique email.";
    toast.error("Failed to save instructor.");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
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

.form-row-triple {
  display: grid;
  grid-template-columns: 0.6fr 1fr 1fr;
  gap: 1rem;
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

.input-error {
  border-color: var(--color-error);
}

.field-error {
  color: var(--color-error);
  font-size: 0.75rem;
  margin-top: 0.2rem;
}

.select-input {
  cursor: pointer;
}

.form-actions-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border-light);
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

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: transparent;
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-dark);
  padding: 0.65rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
}

.error-banner {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid var(--color-error);
  color: var(--color-error);
  padding: 0.75rem;
  border-radius: 4px;
  font-size: 0.85rem;
  margin-bottom: 1.25rem;
}
</style>
