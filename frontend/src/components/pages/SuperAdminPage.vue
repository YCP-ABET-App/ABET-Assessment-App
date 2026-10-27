
<script setup lang="ts">
import { onMounted, ref } from 'vue';
import api from '@/api';

interface Program {
  id: number;
  name: string;
  institution: string;
  active: boolean;
}

interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

const programs = ref<Program[]>([]);
const loading = ref(false);
const creating = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const users = ref<User[]>([]);
const loadingUsers = ref(false);

// Create-program form
const programName = ref('');
const institutionName = ref('');
const initialAdminId = ref<number | null>(null);

// Global institution setting UI.
// This cannot be saved until a backend settings endpoint exists.
const globalInstitutionName = ref('');

function getPayload(response: any): any {
  const body = response?.data ?? response;
  return body?.data ?? body;
}

async function loadPrograms() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const response = await api.get('/program?page=0&size=100');
    const payload = getPayload(response);

    programs.value = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.content)
        ? payload.content
        : [];
  } catch (error: any) {
    errorMessage.value =
      error?.response?.data?.message ??
      'Could not load programs. Please try again.';
  } finally {
    loading.value = false;
  }
}

async function loadUsers() {
  loadingUsers.value = true;

  try {
    const response = await api.get('/users?page=0&size=100');
    const payload = getPayload(response);

    users.value = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.content)
        ? payload.content
        : [];
  } catch (error: any) {
    errorMessage.value =
      error?.response?.data?.message ??
      'Could not load users. Please try again.';
  } finally {
    loadingUsers.value = false;
  }
}

async function createProgram() {
  errorMessage.value = '';
  successMessage.value = '';

  if (!programName.value.trim()) {
    errorMessage.value = 'Enter a program name.';
    return;
  }

  if (!institutionName.value.trim()) {
    errorMessage.value = 'Enter the institution for this program.';
    return;
  }

  if (
    initialAdminId.value === null ||
    !Number.isInteger(initialAdminId.value) ||
    initialAdminId.value <= 0
  ) {
    errorMessage.value = 'Enter a valid initial administrator user ID.';
    return;
  }

  creating.value = true;

  let createdProgram: any = null;

  try {
    // Step 1: Create the program.
    const response = await api.post('/program', {
      name: programName.value.trim(),
      institution: institutionName.value.trim(),
      active: true,
    });

    createdProgram = getPayload(response);

    const programId = createdProgram?.id;

    if (!programId) {
      throw new Error(
        'The program was created, but its ID was not returned by the server.'
      );
    }

    // Assign the initial administrator.
    // ProgramUser.isAdmin represents a program-level ADMIN,
    // not a global SUPER_ADMIN.
    await api.post(`/program/${programId}/users`, {
      userId: initialAdminId.value,
      isAdmin: true,
    });

    successMessage.value =
      `Program "${programName.value.trim()}" was created and its initial administrator was assigned.`;

    programName.value = '';
    institutionName.value = '';
    initialAdminId.value = null;

    await loadPrograms();
  } catch (error: any) {
    const message =
      error?.response?.data?.message ??
      error?.message ??
      'An unexpected error occurred.';

    if (createdProgram?.id) {
      errorMessage.value =
        `The program may have been created, but assigning its initial administrator failed. ` +
        `Program ID: ${createdProgram.id}. Details: ${message}`;
      await loadPrograms();
    } else {
      errorMessage.value = message;
    }
  } finally {
    creating.value = false;
  }
}

onMounted(() => {
  loadPrograms();
  loadUsers();
});

</script>

<template>
  <main class="super-admin-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">System management</p>
        <h1>Super Admin</h1>
        <p class="subtitle">
          Manage programs, assign program administrators, and configure
          institution-wide settings.
        </p>
      </div>

      <button
        class="secondary-button"
        type="button"
        :disabled="loading"
        @click="loadPrograms"
      >
        {{ loading ? 'Refreshing...' : 'Refresh programs' }}
      </button>
    </header>

    <div
      v-if="errorMessage"
      class="message error-message"
      role="alert"
    >
      {{ errorMessage }}
    </div>

    <div
      v-if="successMessage"
      class="message success-message"
      role="status"
    >
      {{ successMessage }}
    </div>

    <section class="section-card">
      <div class="section-heading">
        <div>
          <h2>Create a program</h2>
          <p>
            Every new program should have an administrator responsible
            for managing it.
          </p>
        </div>
      </div>

      <form class="program-form" @submit.prevent="createProgram">
        <div class="form-field">
          <label for="program-name">Program name</label>
          <input
            id="program-name"
            v-model="programName"
            type="text"
            maxlength="255"
            placeholder="e.g. Computer Engineering"
            required
          />
        </div>

        <div class="form-field">
          <label for="program-institution">Institution</label>
          <input
            id="program-institution"
            v-model="institutionName"
            type="text"
            minlength="2"
            maxlength="255"
            placeholder="e.g. York College of Pennsylvania"
            required
          />
          <small>
            This is the institution value stored on this program.
          </small>
        </div>

        <div class="form-field">
          <label for="initial-admin">Initial administrator</label>

          <select
            id="initial-admin"
            v-model="initialAdminId"
            required
            :disabled="loadingUsers || users.length === 0"
          >
            <option :value="null" disabled>
              {{
                loadingUsers
                  ? 'Loading users...'
                  : users.length === 0
                    ? 'No users available'
                    : 'Select an administrator'
              }}
            </option>

            <option
              v-for="user in users"
              :key="user.id"
              :value="user.id"
            >
              {{ user.firstName }} {{ user.lastName }} ({{ user.email }})
            </option>
          </select>

          <small>
            Choose the user who will administer this program.
          </small>
        </div>

        <div class="form-actions">
          <button
            class="primary-button"
            type="submit"
            :disabled="creating"
          >
            {{ creating ? 'Creating program...' : 'Create program' }}
          </button>
        </div>
      </form>
    </section>

    <section class="section-card">
      <div class="section-heading">
        <div>
          <h2>All programs</h2>
          <p>Programs currently returned by the backend.</p>
        </div>

        <span class="count-badge">{{ programs.length }} loaded</span>
      </div>

      <div v-if="loading && programs.length === 0" class="empty-state">
        Loading programs...
      </div>

      <div
        v-else-if="programs.length === 0"
        class="empty-state"
      >
        No programs were found.
      </div>

      <div v-else class="table-wrapper">
        <table>
          <thead>
          <tr>
            <th>ID</th>
            <th>Program</th>
            <th>Institution</th>
            <th>Status</th>
          </tr>
          </thead>

          <tbody>
          <tr v-for="program in programs" :key="program.id">
            <td>{{ program.id }}</td>
            <td class="program-name-cell">{{ program.name }}</td>
            <td>{{ program.institution }}</td>
            <td>
                <span
                  class="status"
                  :class="program.active ? 'status-active' : 'status-inactive'"
                >
                  {{ program.active ? 'Active' : 'Inactive' }}
                </span>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="section-card">
      <div class="section-heading">
        <div>
          <h2>Institution-wide settings</h2>
          <p>
            Set the institution name that should appear on program
            summary reports.
          </p>
        </div>
      </div>

      <form class="program-form" @submit.prevent>
        <div class="form-field">
          <label for="global-institution">
            Institution name for reports
          </label>
          <input
            id="global-institution"
            v-model="globalInstitutionName"
            type="text"
            maxlength="255"
            placeholder="e.g. York College of Pennsylvania"
            disabled
          />
        </div>

        <div class="form-actions">
          <button
            class="primary-button"
            type="button"
            disabled
          >
            Save institution settings
          </button>
        </div>
      </form>
    </section>
  </main>
</template>

<style scoped>
.super-admin-page {
  width: min(1200px, 100%);
  margin: 0 auto;
  padding: 32px 24px 48px;
  color: var(--text-primary, #202124);
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
}

.eyebrow {
  margin: 0 0 6px;
  color: var(--text-secondary, #62666d);
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(1.8rem, 4vw, 2.4rem);
}

.subtitle {
  max-width: 680px;
  margin-top: 10px;
  color: var(--text-secondary, #62666d);
  line-height: 1.6;
}

.section-card {
  margin-top: 24px;
  padding: 24px;
  border: 1px solid var(--border-color, #dedfe3);
  border-radius: 12px;
  background: var(--card-background, #fff);
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.section-heading h2 {
  margin: 0;
  font-size: 1.25rem;
}

.section-heading p {
  margin: 8px 0 0;
  color: var(--text-secondary, #62666d);
  line-height: 1.5;
}

.program-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field label {
  font-size: 0.92rem;
  font-weight: 600;
}

.form-field input,
.form-field select {
  box-sizing: border-box;
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border: 1px solid var(--border-color, #cfd2d8);
  border-radius: 7px;
  background-color: #fff;
  color: #202124;
  font: inherit;
}

.form-field input:focus,
.form-field select:focus {
  outline: 2px solid #536dfe;
  outline-offset: 1px;
}

.form-field input:disabled,
.form-field select:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.form-field small {
  color: #62666d;
  line-height: 1.4;
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
}

.primary-button,
.secondary-button {
  min-height: 42px;
  padding: 10px 16px;
  border: 1px solid transparent;
  border-radius: 7px;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}



.primary-button:disabled,
.secondary-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid var(--border-color, #e4e5e9);
  vertical-align: middle;
}

th {
  color: var(--text-secondary, #62666d);
  font-size: 0.82rem;
  font-weight: 700;
}

.program-name-cell {
  font-weight: 600;
}

.status,
.count-badge {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-active {
  background: #e4f5e9;
  color: #216e3a;
}

.status-inactive {
  background: #f0f0f0;
  color: #595959;
}

.count-badge {
  background: var(--badge-background, #f0f1f5);
  color: var(--text-secondary, #62666d);
}

.message {
  margin: 16px 0;
  padding: 13px 16px;
  border: 1px solid;
  border-radius: 8px;
  line-height: 1.5;
}

.error-message {
  border-color: #e8b5b5;
  background: #fff1f1;
  color: #8c2424;
}

.success-message {
  border-color: #a9d9b5;
  background: #effaf1;
  color: #216e3a;
}

.empty-state {
  padding: 28px 12px;
  color: var(--text-secondary, #62666d);
  text-align: center;
}

.settings-note {
  grid-column: 1 / -1;
  margin: 0;
  color: var(--text-secondary, #62666d);
  font-size: 0.9rem;
  line-height: 1.5;
}

@media (max-width: 700px) {
  .super-admin-page {
    padding: 24px 16px 36px;
  }

  .page-header {
    flex-direction: column;
  }

  .section-card {
    padding: 18px;
  }

  .program-form {
    grid-template-columns: 1fr;
  }

  .form-actions,
  .settings-note {
    grid-column: auto;
  }
}
</style>
