<script setup lang="ts">
import { computed, onMounted } from "vue";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user-store";
import { useRouter, useRoute } from "vue-router";

// Components
import CourseListing from "@/components/CourseListing.vue";
import ProgramInstructorsPage from "@/components/pages/ProgramInstructorsPage.vue";
import BaseButton from "@/components/ui/BaseButton.vue";
import GlobalSelectors from "@/components/GlobalSelectors.vue"; 

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// Pulling global state from the store
const { currentProgramId, currentSemesterId } = storeToRefs(userStore);

/**
 * Casting to Number satisfies the TS2322 error found in your build log.
 * We check the route params first, then fallback to store values.
 */
const activeProgramId = computed<number | null>(() => {
  const id = route.params.program_id || currentProgramId.value;
  return id ? Number(id) : null;
});

const activeSemesterId = computed<number | null>(() => {
  const id = route.params.semester_id || currentSemesterId.value;
  return id ? Number(id) : null;
});

function navigateToSummary() {
  if (activeProgramId.value && activeSemesterId.value) {
    router.push(`/${activeProgramId.value}/${activeSemesterId.value}/summary`);
  }
}

function goBackToHome() {
  router.push('/dashboard');
}

// Ensure store is initialized on mount to populate dropdowns
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    await userStore.loadFromStorage();
  }
});
</script>

<template>
  <div class="admin-page-layout">
    <div class="selector-container">
      <GlobalSelectors />
    </div>

    <main class="content-wrapper">
      <section v-if="activeProgramId && activeSemesterId" class="admin-dashboard">
        <header class="dashboard-header">
          <h1>Administrator Dashboard</h1>
          <p class="subtitle">
            Viewing Program: {{ activeProgramId }} | Semester: {{ activeSemesterId }}
          </p>
        </header>

        <div class="dashboard-body">
          <CourseListing
            :program-id="activeProgramId"
            :semester-id="activeSemesterId"
          />

          <hr class="divider" />

          <section class="instructors-section">
            <ProgramInstructorsPage :program-id="activeProgramId" />
          </section>

          <hr class="divider" />

          <footer class="dashboard-footer">
            <BaseButton variant="secondary" @click="goBackToHome">
              Back to Instructor View
            </BaseButton>
            
            <BaseButton @click="navigateToSummary">
              View 1-Year Summary Report
            </BaseButton>
          </footer>
        </div>
      </section>

      <section v-else class="selection-prompt">
        <div class="prompt-content">
          <div class="icon-placeholder">ℹ️</div>
          <h3>Selection Required</h3>
          <p>Please select a program and semester from the bar above to load administrator data.</p>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.admin-page-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* Matches the dark background bar from your image.
  Adjust z-index if the Green Nav covers it.
*/
.selector-container {
  background-color: #1a1a1a; 
  width: 100%;
  position: sticky;
  top: 0;
  z-index: 50;
}

.content-wrapper {
  flex: 1;
  background-color: var(--color-background-base);
}

.admin-dashboard {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 2rem;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 700;
}

.subtitle {
  color: var(--color-text-secondary);
  margin-top: 0.5rem;
}

.divider {
  margin: 3rem 0;
  border: none;
  border-top: 1px solid var(--color-border-dark);
}

.dashboard-footer {
  display: flex;
  gap: 1.5rem;
  padding: 2rem 0 5rem;
}

.selection-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}

.prompt-content {
  text-align: center;
  padding: 3rem;
  border: 2px dashed var(--color-border);
  border-radius: 16px;
  color: var(--color-text-secondary);
  max-width: 400px;
}

.icon-placeholder {
  font-size: 3rem;
  margin-bottom: 1rem;
}
</style>