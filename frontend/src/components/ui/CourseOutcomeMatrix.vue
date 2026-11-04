<script setup lang="ts">
import { useScheduleStore } from '@/stores/scheduleStore';

const store = useScheduleStore();
</script>

<template>
  <section class="mapping-section">
    <h2>Course / Outcome PI Mapping</h2>
    <div class="table-container">
      <table class="data-table">
        <thead>
        <tr>
          <th class="col-header">Course</th>
          <th class="col-header" v-for="outcome in store.outcomes" :key="outcome">{{ outcome }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="course in store.courses" :key="course">
          <th class="row-header">{{ course }}</th>
          <td v-for="outcome in store.outcomes" :key="outcome">
            <input type="text" v-model="store.courseOutcomeMap[course][outcome]" class="cell-input" />
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.mapping-section {
  background: #282828;
  padding: 1.5rem;
  border-radius: 6px;
  border: 1px solid #383838;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  margin-bottom: 2rem;
}

h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 1.25rem 0;
  text-align: center;
}

.table-container {
  overflow-x: auto;
  border: 1px solid #333333;
  border-radius: 4px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #181818;
}

.data-table th,
.data-table td {
  border: 1px solid #333333;
  padding: 0.75rem 0.4rem;
  text-align: center;
  color: #ffffff;
}

.col-header,
.row-header {
  background: #242424;
  color: #e2e8f0;
  font-size: 0.875rem;
  font-weight: 600;
}

.row-header { width: 70px; font-weight: 700; text-align: center; }

.cell-input {
  width: 100%;
  border: none;
  background: transparent;
  text-align: center;
  color: #ffffff;
  font-size: 0.95rem;
  padding: 0.25rem;
}

.cell-input:focus {
  outline: 1px solid #22c55e;
  background: #121212;
}
</style>
