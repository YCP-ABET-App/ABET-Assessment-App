<script lang="ts" setup>
import { computed } from "vue";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user-store";
import AdminDashboard from "@/components/AdminDashboard.vue";
import InstructorDashboard from "@/components/InstructorDashboard.vue";
import AssessmentSchedule from "@/components/AssessmentSchedule.vue";

// Get user store values
const userStore = useUserStore();
const {
  isLoggedIn,
  isAdmin,
  isInstructor,
  currentProgramId,
  currentSemesterId
} = storeToRefs(userStore);

// These are now reactive refs from the store that GlobalSelectors updates
const hasRequiredSelections = computed(() => {
  return currentProgramId.value !== null && currentSemesterId.value !== null;
});
</script>

<template>
  <div class="homepage">
    <!-- Not logged in -->
    <div v-if="!isLoggedIn" id="log-in-popup">
      Log in to view course information
    </div>

    <!-- Logged in -->
    <div v-else id="dashboards">
      <!-- Instructor Dashboard -->
      <template v-if="isInstructor && hasRequiredSelections">
        <InstructorDashboard
          :program-id="currentProgramId"
          :semester-id="currentSemesterId"
        />
      </template>

      <hr v-if="isAdmin && isInstructor && hasRequiredSelections" class="section-divider" />

      <!-- Admin Dashboard -->
      <template v-if="isAdmin && hasRequiredSelections">
        <AdminDashboard
          :program-id="currentProgramId as number"
          :semester-id="currentSemesterId as number"
        />
      </template>

      <!-- No selections yet -->
      <template v-if="!hasRequiredSelections">
        <div class="loading-screen">
          <p>Please select a program and semester from the dropdowns above.</p>
        </div>
      </template>
    </div>

    <!-- No dashboard roles -->
    <template v-if="isLoggedIn && !isAdmin && !isInstructor">
      <h2>You are logged in, but your account has no dashboard privileges.</h2>
    </template>
  </div>
</template>

<style scoped>
.homepage {
  padding: 2rem;
}

#log-in-popup {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: var(--color-text-secondary);
}

.loading-screen {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-secondary);
}

.section-divider {
  margin: 2rem 0;
  border: none;
  border-top: 1px solid var(--color-border-dark);
}

#dashboards h1, #dashboards h2 {
  margin-bottom: 1.5rem;
}
</style>
