<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user-store";
import { useRouter } from "vue-router";

import CourseListing from "@/components/CourseListing.vue";
import ProgramInstructorsPage from "@/components/pages/ProgramInstructorsPage.vue";
import SummaryReport from "@/components/SummaryReport.vue";
import AssessmentSchedule from "@/components/AssessmentSchedule.vue";
import BaseButton from "@/components/ui/BaseButton.vue";

const router = useRouter();

const userStore = useUserStore();
const { currentProgramId, currentSemesterId } = storeToRefs(userStore);

function navigateToSummary(){
  router.push(`/${currentProgramId.value}/${currentSemesterId.value}/summary`)
}
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
    <BaseButton @click="navigateToSummary()">View 1-Year Summary Report</BaseButton>
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
