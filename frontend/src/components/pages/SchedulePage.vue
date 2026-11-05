<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { storeToRefs } from 'pinia';
import SoPiMappingTable from '@/components/ui/SoPiMappingTable.vue';
import SixYearScheduleGrid from '@/components/ui/SixYearScheduleGrid.vue';
import CourseOutcomeMatrix from '@/components/ui/CourseOutcomeMatrix.vue';
import AddCourseModal from '@/components/ui/AddCourseModal.vue';
import RubricModal from '@/components/ui/RubricModal.vue';
import { useScheduleStore } from '@/stores/scheduleStore';
import { useUserStore } from '@/stores/user-store';

const scheduleStore = useScheduleStore();
const userStore = useUserStore();
const { currentProgramId, currentSemesterId } = storeToRefs(userStore);

onMounted(() => {
  scheduleStore.loadData();
});

watch([currentProgramId, currentSemesterId], () => {
  scheduleStore.loadData();
});
</script>

<template>
  <div class="schedule-page">
    <header>
      <h1>Schedule Entry</h1>
      <p v-if="scheduleStore.loading" class="status-text">Loading assessment plan…</p>
      <p v-else-if="scheduleStore.error" class="status-text error">{{ scheduleStore.error }}</p>
      <p v-else-if="scheduleStore.saving" class="status-text">Saving…</p>
    </header>

    <SoPiMappingTable />
    <SixYearScheduleGrid />
    <CourseOutcomeMatrix />

    <AddCourseModal />
    <RubricModal />
  </div>
</template>

<style scoped>
.schedule-page {
  max-width: 1300px;
  margin: 1.5rem auto;
  padding: 0 1.5rem;
  color: #ffffff;
}

header {
  text-align: center;
  margin-bottom: 2rem;
}

header h1 {
  font-size: 2.25rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
}

.status-text {
  margin-top: 0.75rem;
  color: #cbd5e1;
  font-size: 0.95rem;
}

.status-text.error {
  color: #f87171;
}
</style>
