
<script setup lang="ts">
import { onMounted, ref } from 'vue';
import api from '@/api';

interface Program {
  id: number;
  name: string;
  institution: string;
  active: boolean;
  assignedAdmins: User[];
}

interface ProgramUser {
  userId: number;
  isAdmin?: boolean;
  adminStatus?: boolean;
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

    const rawPrograms = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.content)
        ? payload.content
        : [];

    const normalizedPrograms = rawPrograms.map((p: any) => ({
      ...p,
      id: p.id ?? p.programId,
      name: p.name ?? p.programName ?? '',
      institution:
        typeof p.institution === 'object'
          ? p.institution?.name ?? p.institution?.institutionName ?? ''
          : p.institution ?? p.institutionName ?? '',
      active: p.active ?? p.isActive ?? false,
      assignedAdmins: [] as User[],
    }));

    console.log('Programs returned by backend:', rawPrograms);

    programs.value = await Promise.all(
      normalizedPrograms.map(async (program: Program) => {
        if (program.id == null) {
          console.warn('Program is missing its ID:', program);
          return program;
        }

        try {
          const assignmentResponse = await api.get(
            `/program/${program.id}/users`
          );

          const assignments = getPayload(assignmentResponse);
          const programUsers: ProgramUser[] = Array.isArray(assignments)
            ? assignments
            : Array.isArray(assignments?.content)
              ? assignments.content
              : [];

          const adminAssignments = programUsers.filter(
            (assignment) =>
              assignment.isAdmin === true ||
              assignment.adminStatus === true
          );

          const admins = await Promise.all(
            adminAssignments.map(async (assignment) => {
              try {
                const userResponse = await api.get(
                  `/users/${assignment.userId}`
                );

                return getPayload(userResponse) as User;
              } catch (error) {
                console.error(
                  `Could not load user ${assignment.userId} for program ${program.id}`,
                  error
                );
                return null;
              }
            })
          );

          return {
            ...program,
            assignedAdmins: admins.filter(
              (admin): admin is User => admin !== null
            ),
          };
        } catch (error) {
          console.error(
            `Could not load users for program ${program.id}`,
            error
          );

          return program;
        }
      })
    );
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
  <div class="page-container">
    <main class="content-area">
      <div class="super-admin-page">
        <header class="page-title-banner">
          <h1>Super Admin Dashboard</h1>
          <p class="subtitle">
            Manage programs, assign administrators, and configure institution-wide settings.
          </p>
        </header>

        <!-- Messages -->
        <div v-if="errorMessage" class="message error-message" role="alert">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="message success-message" role="status">
          {{ successMessage }}
        </div>

        <!-- Create Program -->
        <section class="section-card">
          <div class="section-heading">
            <div>
              <h2>Create a Program</h2>
              <p>
                Create a program and assign its initial administrator.
              </p>
            </div>
          </div>

          <form class="program-form" @submit.prevent="createProgram">
            <div class="form-field">
              <label for="program-name">Program Name</label>
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
            </div>

            <div class="form-field full-width">
              <label for="initial-admin">Initial Administrator</label>
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

            <div class="form-actions full-width">
              <button
                class="primary-button"
                type="submit"
                :disabled="creating"
              >
                {{ creating ? 'Creating Program...' : 'Create Program' }}
              </button>
            </div>
          </form>
        </section>

        <section class="section-card">
          <div class="section-heading programs-heading">
            <div>
              <h2>All Programs</h2>
              <p>View the programs currently returned by the backend.</p>
            </div>

            <div class="program-list-actions">
              <span class="count-badge">
                {{ programs.length }} loaded
              </span>

              <button
                class="secondary-button"
                type="button"
                :disabled="loading"
                @click="loadPrograms"
              >
                <span aria-hidden="true">↻</span>
                {{ loading ? 'Refreshing...' : 'Refresh Programs' }}
              </button>
            </div>
          </div>

          <div
            v-if="loading && programs.length === 0"
            class="empty-state"
          >
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
                <th>Assigned Administrator</th>
                <th>Status</th>
              </tr>
              </thead>

              <tbody>
              <tr v-for="program in programs" :key="program.id">
                <td>{{ program.id ?? 'Not returned' }}</td>

                <td class="program-name-cell">
                  {{ program.name || 'Not provided' }}
                </td>

                <td>
                  {{ program.institution || 'Not provided' }}
                </td>

                <td>
                  <div
                    v-if="program.assignedAdmins?.length"
                    class="assigned-admins"
                  >
                    <div
                      v-for="admin in program.assignedAdmins"
                      :key="admin.id"
                      class="assigned-admin"
                    >
                      <strong>{{ admin.firstName }} {{ admin.lastName }}</strong>
                      <small>{{ admin.email }}</small>
                    </div>
                  </div>

                  <span v-else class="no-admin">
        No administrator found
      </span>
                </td>

                <td>
      <span
        class="status"
        :class="
          program.active ? 'status-active' : 'status-inactive'
        "
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
              <h2>Institution-Wide Settings</h2>
              <p>
                Set the institution name that should appear on summary reports.
              </p>
            </div>
          </div>

          <form class="program-form" @submit.prevent>
            <div class="form-field full-width">
              <label for="global-institution">
                Institution Name for Reports
              </label>
              <input
                id="global-institution"
                v-model="globalInstitutionName"
                type="text"
                maxlength="255"
                placeholder="e.g. York College of Pennsylvania"
                disabled
              />
              <small>
                Saving this setting requires a backend settings endpoint.
              </small>
            </div>

            <div class="form-actions full-width">
              <button
                class="primary-button"
                type="button"
                disabled
              >
                Save Institution Settings
              </button>
            </div>
          </form>
        </section>
      </div>
    </main>
  </div>
</template>



<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--color-background-base);
}

.content-area {
  flex: 1;
  background-color: var(--color-background-soft);
}

.super-admin-page {
  width: min(1200px, 100%);
  margin: 0 auto;
  padding: 32px 24px 56px;
  color: var(--color-text-primary);
}

/* Centered title banner */
.page-title-banner {
  background: transparent;
  text-align: center;
  padding: 2rem 0;
  border-radius: 0;
}

.page-title-banner h1 {
  color: white;
}

.page-title-banner .eyebrow,
.page-title-banner p {
  color: white;
}

.page-title-banner .subtitle {
  max-width: 680px;
  margin: 12px auto 0;
  color: #edf2f7;
  line-height: 1.6;
}

/* Section cards */
.section-card {
  margin-top: 24px;
  padding: 24px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgb(0 0 0 / 4%);
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.section-heading h2 {
  margin: 0;
  color: #263746;
  font-size: 1.3rem;
  font-weight: 700;
}

.section-heading p {
  margin: 8px 0 0;
  color: #62666d;
  line-height: 1.5;
}

/* Program list heading and refresh controls */
.programs-heading {
  flex-wrap: wrap;
}

.program-list-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12px;
}

.count-badge {
  display: inline-block;
  padding: 7px 11px;
  border-radius: 999px;
  background-color: #edf1f5;
  color: #394957;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
}

/* Forms */
.program-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.full-width {
  grid-column: 1 / -1;
}

.form-field label {
  color: #303640;
  font-size: 0.92rem;
  font-weight: 600;
}

.form-field input,
.form-field select {
  box-sizing: border-box;
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border: 1px solid #cfd2d8;
  border-radius: 7px;
  background-color: #ffffff;
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
  background-color: #f0f1f3;
  opacity: 0.7;
}

.form-field small {
  color: #62666d;
  line-height: 1.4;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

/* Buttons */
.primary-button,
.secondary-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  padding: 10px 16px;
  border: 1px solid transparent;
  border-radius: 7px;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.primary-button {
  background-color: #263746;
  color: #ffffff;
}

.primary-button:hover:not(:disabled) {
  background-color: #354c60;
}

.secondary-button {
  border-color: #cfd6de;
  background-color: #ffffff;
  color: #263746;
}

.secondary-button:hover:not(:disabled) {
  background-color: #edf1f5;
}

.primary-button:disabled,
.secondary-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* Program table */
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
  border-bottom: 1px solid #e4e5e9;
  vertical-align: middle;
}

th {
  background-color: #f6f7f9;
  color: #555d67;
  font-size: 0.82rem;
  font-weight: 700;
}

tbody tr:hover {
  background-color: #f8fafc;
}

.program-name-cell {
  color: #263746;
  font-weight: 600;
}

.status {
  display: inline-block;
  padding: 5px 9px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-active {
  background-color: #e4f5e9;
  color: #216e3a;
}

.status-inactive {
  background-color: #f0f0f0;
  color: #595959;
}

/* Messages and empty states */
.message {
  margin: 16px 0;
  padding: 13px 16px;
  border: 1px solid;
  border-radius: 8px;
  line-height: 1.5;
}

.error-message {
  border-color: #e8b5b5;
  background-color: #fff1f1;
  color: #8c2424;
}

.success-message {
  border-color: #a9d9b5;
  background-color: #effaf1;
  color: #216e3a;
}

.empty-state {
  padding: 28px 12px;
  color: #62666d;
  text-align: center;
}

.assigned-admins {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.assigned-admin {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.assigned-admin strong {
  color: #263746;
  font-size: 0.9rem;
}

.assigned-admin small,
.no-admin {
  color: #62666d;
  font-size: 0.82rem;
}

/* Responsive layout */
@media (max-width: 700px) {
  .super-admin-page {
    padding: 20px 14px 36px;
  }

  .page-title-banner {
    padding: 26px 16px;
  }

  .section-card {
    padding: 18px;
  }

  .section-heading {
    align-items: flex-start;
  }

  .programs-heading {
    flex-direction: column;
  }

  .program-list-actions {
    justify-content: flex-start;
  }

  .program-form {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: auto;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions button {
    width: 100%;
  }
}
</style>

