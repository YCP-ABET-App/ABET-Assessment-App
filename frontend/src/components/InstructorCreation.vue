<template>
  <div class="instructor-creation-form">
    <p v-if="apiError" class="error-message">{{ apiError }}</p>

    <form class="form" @submit.prevent="submitForm">
      <div class="form-row">
        <div class="form-group">
          <label class="label">First Name</label>
          <input
            v-model="form.firstName"
            class="input"
            placeholder="e.g. John"
            required
          />
          <span v-if="errors.firstName" class="field-error">{{ errors.firstName }}</span>
        </div>
        <div class="form-group">
          <label class="label">Last Name</label>
          <input
            v-model="form.lastName"
            class="input"
            placeholder="e.g. Smith"
            required
          />
          <span v-if="errors.lastName" class="field-error">{{ errors.lastName }}</span>
        </div>
      </div>

      <div class="form-group">
        <label class="label">Email Address</label>
        <input
          v-model="form.email"
          type="email"
          class="input"
          placeholder="e.g. jsmith@ycp.edu"
          required
        />
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
      </div>

      <div class="form-group">
        <label class="label">Password</label>
        <input
          v-model="form.password"
          type="password"
          class="input"
          placeholder="e.g. abc123"
          required
        />
        <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
      </div>

      <div class="form-group">
        <label class="label">System Role</label>
        <select v-model="form.role" class="input select-input">
          <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <div class="form-actions-footer">
        <button
          type="button"
          class="btn-secondary"
          @click="$emit('cancel')"
          :disabled="loading"
        >
          Cancel
        </button>
        <button
          type="submit"
          class="btn-primary"
          :disabled="loading"
        >
          <span v-if="loading">Creating...</span>
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
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  role: 'INSTRUCTOR' as 'ADMIN' | 'INSTRUCTOR'
});

const errors = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: ''
});

const roleOptions = [
  { value: 'INSTRUCTOR', label: 'Instructor' },
  { value: 'ADMIN', label: 'Admin' }
];

function validate() {
  let isValid = true;
  errors.firstName = !form.firstName ? 'First name is required' : '';
  errors.lastName = !form.lastName ? 'Last name is required' : '';
  errors.email = !form.email.includes('@') ? 'Valid email is required' : '';
  errors.password = !form.password ? 'Password is required' : '';

  if (errors.firstName || errors.lastName || errors.email || errors.password) isValid = false;
  return isValid;
}

async function submitForm() {
  if (!props.programId || !validate()) return;

  loading.value = true;
  apiError.value = null;

  try {
    // 1. Create the User globally
    const userRes = await api.post('/users', {
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      passwordHash: form.password
    });

    const newUserId = userRes.data?.id || userRes.data?.data?.id;

    if (!newUserId) {
      throw new Error("User created but no ID returned from server");
    }

    await api.post(`/program/${props.programId}/users`, {
      userId: newUserId,
      adminStatus: form.role === 'ADMIN'
    });

    toast.success("Instructor created and added to program");
    emit('success');
  } catch (err: any) {
    console.error("Creation error:", err);
    apiError.value = err.response?.data?.message || "Error creating instructor";
    toast.error(apiError.value);
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

.label {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--color-text-primary);
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

.select-input {
  cursor: pointer;
  appearance: none;
}

.form-actions-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border-light);
}

.btn-primary, .btn-secondary {
  padding: 0.625rem 1.5rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  border: none;
}

.btn-secondary {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-dark);
}

.error-message {
  color: var(--color-error);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.field-error {
  color: var(--color-error);
  font-size: 0.75rem;
  margin-top: 0.25rem;
}
</style>
