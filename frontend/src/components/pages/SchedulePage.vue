<script setup lang="ts">
import SoPiMappingTable from '@/components/ui/SoPiMappingTable.vue';
import SixYearScheduleGrid from '@/components/ui/SixYearScheduleGrid.vue';
import CourseOutcomeMatrix from '@/components/ui/CourseOutcomeMatrix.vue';
import AddCourseModal from '@/components/ui/AddCourseModal.vue';
import CourseDetailModal from '@/components/ui/CourseDetailModal.vue';
import RubricModal from '@/components/ui/RubricModal.vue';
</script>

<template>
  <div class="schedule-page">
    <header>
      <h1>Schedule Entry</h1>
    </header>

    <SoPiMappingTable />
    <SixYearScheduleGrid />
    <CourseOutcomeMatrix />

    <AddCourseModal />
    <CourseDetailModal />
    <RubricModal />
  </div>
</template>

<style scoped>
.schedule-page { max-width: 1300px; margin: 1.5rem auto; padding: 0 1.5rem; color: #fff; }
header { text-align: center; margin-bottom: 2rem; }
header h1 { font-size: 2.25rem; font-weight: 700; color: #fff; margin: 0; }
</style>
