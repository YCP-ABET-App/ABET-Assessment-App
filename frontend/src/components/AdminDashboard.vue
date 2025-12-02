<script setup lang="ts">
import { defineProps } from "vue";
import CourseListing from "@/components/CourseListing.vue";
import ProgramInstructorsPage from "@/components/pages/ProgramInstructorsPage.vue";
import SummaryReport from "@/components/SummaryReport.vue";

/**
 * AdminDashboard receives BOTH programId and semesterId from HomePage
 * which get them from the GlobalSelectors via the user store
 */
const props = defineProps<{
  programId: number;
  semesterId: number;
}>();
</script>

<template>
  <section class="admin-dashboard">
    <!-- COURSE LISTING -->
    <CourseListing
      :program-id="programId"
      :semester-id="semesterId"
    />

    <hr class="divider" />

    <!-- PROGRAM INSTRUCTORS -->
    <section class="summary-section">
      <h2>Program Instructors</h2>
      <ProgramInstructorsPage :program-id="programId" />
    </section>

    <!-- ASSESSMENT SUMMARY REPORT -->
    <section class="summary-section">
      <h2>Assessment Summary Report</h2>
      <SummaryReport
        :program-id="programId"
        :semester-id="semesterId"
        :show-semester-selector="false"
        :show-export-button="true"
      />
    </section>
  </section>
</template>

<style scoped>
.admin-dashboard {
  margin: 0;
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
}
</style>
