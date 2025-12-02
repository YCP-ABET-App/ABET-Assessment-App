<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user-store";

import CourseListing from "@/components/CourseListing.vue";
import ProgramInstructorsPage from "@/components/pages/ProgramInstructorsPage.vue";
import SummaryReport from "@/components/SummaryReport.vue";

const userStore = useUserStore();
const { currentProgramId: programId } = storeToRefs(userStore);
</script>

<template>
  <section v-if="programId" class="admin-dashboard">

    <header>
      <h1>Administrator Dashboard</h1>
    </header>

    <!-- COURSE LISTING -->
    <CourseListing :program-id="programId" />

    <hr />

    <!-- PROGRAM INSTRUCTORS -->
    <ProgramInstructorsPage :program-id="programId" />

    <hr />

    <!-- ASSESSMENT SUMMARY REPORT -->
    <section class="summary-section">
      <h2>Assessment Summary Report</h2>
      <SummaryReport
        :program-id="programId"
        :show-semester-selector="true"
        :show-export-button="true"
      />
    </section>
  </section>

  <section v-else class="loading-screen">
    <p>Loading program information...</p>
  </section>
</template>

<style scoped>
.admin-dashboard {
  margin: 2rem;
}

hr {
  margin: 3rem 0;
  border: none;
  border-top: 2px solid var(--color-border-light);
}

.summary-section {
  margin-top: 3rem;
}

.summary-section h2 {
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

@media (max-width: 768px) {
  .admin-dashboard {
    margin: 1rem;
  }
}
</style>
