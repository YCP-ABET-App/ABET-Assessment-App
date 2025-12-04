<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user-store";

import CourseListing from "@/components/CourseListing.vue";
import ProgramInstructorsPage from "@/components/pages/ProgramInstructorsPage.vue";
import SummaryReport from "@/components/SummaryReport.vue";
import AssessmentSchedule from "@/components/AssessmentSchedule.vue";

const userStore = useUserStore();
const { currentProgramId, currentSemesterId } = storeToRefs(userStore);
</script>

<template>
  <section v-if="currentProgramId && currentSemesterId" class="admin-dashboard">

    <header>
      <h1>Administrator Dashboard</h1>
    </header>

    <!-- COURSE LISTING -->
    <CourseListing
      :program-id="currentProgramId"
      :semester-id="currentSemesterId"
    />

    <hr class="divider" />

    <!-- PROGRAM INSTRUCTORS -->
    <section class="summary-section">
      <ProgramInstructorsPage :program-id="currentProgramId" />
    </section>

    <hr class="divider" />

    <!-- ASSESSMENT SUMMARY REPORT -->
    <section class="summary-section">
      <h2>Assessment Summary Report</h2>
      <SummaryReport
        :program-id="currentProgramId"
        :semester-id="currentSemesterId"
        :show-semester-selector="false"
        :show-export-button="true"
      />
    </section>
  </section>

  <section v-else class="loading-screen">
    <p>Loading program and semester information...</p>
  </section>
</template>

<style scoped>
.admin-dashboard {
  margin: 2rem;
}

.divider {
  margin: 2rem 0;
  border: none;
  border-top: 1px solid var(--color-border-dark);
}

.summary-section {
  margin-top: 2rem;
}

.summary-section h2 {
  margin-bottom: 1rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text-primary);
}
</style>
