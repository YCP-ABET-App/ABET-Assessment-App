<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import api from "@/api";
import { useUserStore } from "@/stores/user-store";

import BaseCard from "@/components/ui/BaseCard.vue";
import BaseSelect from "@/components/ui/BaseSelect.vue";
import BaseButton from "@/components/ui/BaseButton.vue";
import BaseSpinner from "@/components/ui/BaseSpinner.vue";

// Store
const userStore = useUserStore();

// STATE
const loading = ref(true);
const saving = ref(false);
const profileError = ref<string | null>(null);

// User profile info
const profile = ref<any>(null);

// Program list
const programs = computed(() => userStore.programs ?? []);

// Theme selector
const selectedTheme = ref<"light" | "dark">(userStore.theme ?? "dark");

// Program preference selector
const selectedProgram = ref<number | undefined>(
  userStore.currentProgramId ?? undefined
);

// LOAD PROFILE
async function loadProfile() {
  loading.value = true;
  profileError.value = null;

  try {
    const uid = userStore.user?.id;
    if (!uid) {
      profileError.value = "User not loaded.";
      return;
    }

    const { data } = await api.get(`/users/${uid}`);

    profile.value = {
      fullName:
        `${data.data.firstName || ""} ${data.data.lastName || ""}`.trim(),
      email: data.data.email,
      title: data.data.title,
      active: data.data.active,
      createdAt: data.data.createdAt,
      updatedAt: data.data.updatedAt
    };
  } catch {
    profileError.value = "Failed to load profile.";
  } finally {
    loading.value = false;
  }
}

// SAVE SETTINGS
async function saveSettings() {
  saving.value = true;
  profileError.value = null;

  try {
    userStore.setTheme(selectedTheme.value);

    if (selectedProgram.value) {
      await userStore.switchProgram(selectedProgram.value);
    }

    userStore.saveToStorage();
  } catch {
    profileError.value = "Failed to save settings.";
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  loadProfile();
});
</script>

<template>
  <div class="settings-wrapper">
    <h1 class="page-title">Settings</h1>

    <!-- LOADING -->
    <div v-if="loading" class="loading-state">
      <BaseSpinner size="lg" />
      <p>Loading your profile...</p>
    </div>

    <!-- CONTENT -->
    <div v-else>
      <div v-if="profileError" class="error-msg">
        {{ profileError }}
      </div>

      <!-- PROFILE CARD -->
      <BaseCard title="Your Profile" variant="elevated">
        <div class="profile-grid">
          <div class="item">
            <span class="label">Name:</span>
            <span class="value">{{ profile.fullName }}</span>
          </div>

          <div class="item">
            <span class="label">Email:</span>
            <span class="value">{{ profile.email }}</span>
          </div>

          <div class="item" v-if="profile.title">
            <span class="label">Title:</span>
            <span class="value">{{ profile.title }}</span>
          </div>

          <div class="item">
            <span class="label">Status:</span>
            <span class="status-badge" :class="{ active: profile.active }">
              {{ profile.active ? "Active" : "Inactive" }}
            </span>
          </div>

          <div class="item">
            <span class="label">Role:</span>
            <span class="status-badge role-badge">
              {{ userStore.user?.role === "ADMIN" ? "Admin" : "Instructor" }}
            </span>
          </div>

          <div class="item">
            <span class="label">Created:</span>
            <span class="value">
              {{ new Date(profile.createdAt).toLocaleString() }}
            </span>
          </div>
        </div>
      </BaseCard>

      <!-- THEME -->
      <BaseCard title="Appearance" variant="elevated">
        <BaseSelect
          v-model="selectedTheme"
          :options="[
            { label: 'Dark Mode', value: 'dark' },
            { label: 'Light Mode', value: 'light' }
          ]"
          label="Theme"
        />
      </BaseCard>

      <!-- PROGRAM PREFERENCE -->
      <BaseCard title="Program Preferences" variant="elevated">
        <BaseSelect
          v-model="selectedProgram"
          :options="
            programs.map(p => ({
              label: 'Computer Science – York College of Pennsylvania',
              value: p.programId
            }))
          "
          label="Preferred Program"
        />
      </BaseCard>

      <!-- SAVE BUTTON -->
      <div class="save-area">
        <BaseButton
          variant="primary"
          size="lg"
          :disabled="saving"
          @click="saveSettings"
        >
          <span v-if="!saving">Save Settings</span>
          <span v-else>Saving...</span>
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
  text-align: left;
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  margin-bottom: 2rem;
  color: var(--color-text-primary);
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  font-weight: 600;
}

.value {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
}

/* status badge */
.status-badge {
  padding: 0.3rem 0.75rem;
  border-radius: var(--radius-full);
  background-color: #fee2e2;
  color: #991b1b;
  font-size: var(--font-size-sm);
  font-weight: 600;
}

/* active badge */
.status-badge.active {
  background-color: #dcfce7;
  color: #166534;
}

/* role badge (same style as active) */
.role-badge {
  background-color: #dcfce7;
  color: #166534;
}

.save-area {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-end;
}

.error-msg {
  color: var(--color-error);
  margin-bottom: 1rem;
}

.loading-state {
  text-align: center;
  padding: 2rem;
}
</style>
