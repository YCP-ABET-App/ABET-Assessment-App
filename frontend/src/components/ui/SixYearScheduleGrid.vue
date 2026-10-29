<script setup lang="ts">
import { useScheduleStore } from '@/stores/scheduleStore';
import { useScheduleModals } from '@/composables/useScheduleModals';

const store = useScheduleStore();
const { openCourseSearch } = useScheduleModals();
</script>

<template>
  <section class="mapping-section">
    <div class="section-header">
      <h2>6-Year Outcome to Course Schedule Mapping</h2>
      <input
        type="text"
        v-model="store.highlightQuery"
        placeholder="Highlight course (e.g. CS 400)..."
        class="search-input highlight-input"
      />
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
        <tr>
          <th class="col-header">Outcome</th>
          <th class="col-header" v-for="year in store.academicYears" :key="year">{{ year }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="outcome in store.outcomes" :key="outcome">
          <th class="row-header">{{ outcome }}</th>
          <td v-for="year in store.academicYears" :key="year" class="cell-courses">
            <div class="chip-container">
                <span
                  v-for="course in store.sixYearSchedule[outcome][year]"
                  :key="course"
                  class="course-chip"
                  :class="{
                    'is-highlighted': store.highlightQuery && course.toLowerCase().includes(store.highlightQuery.toLowerCase()),
                    'is-dimmed': store.highlightQuery && !course.toLowerCase().includes(store.highlightQuery.toLowerCase())
                  }"
                >
                  {{ course }}
                  <button class="chip-remove" @click="store.removeCourseFromCell(outcome, year, course)">×</button>
                </span>
              <button
                class="btn-add-chip"
                @click="openCourseSearch(outcome, year)"
                title="Add Course"
              >+</button>
            </div>
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

.section-header {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 1.25rem;
}

.section-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  text-align: center;
}

.highlight-input {
  position: absolute;
  right: 0;
  width: 220px;
  padding: 0.4rem 0.65rem;
  border: 1px solid #4a4a4a;
  background: #181818;
  color: #ffffff;
  border-radius: 4px;
  font-size: 0.85rem;
}

.highlight-input:focus {
  outline: 1px solid #22c55e;
  border-color: #22c55e;
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

.row-header { width: 70px; font-weight: 700; }

.cell-courses {
  vertical-align: top;
  min-width: 135px;
  height: 75px;
}

.chip-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  justify-content: center;
  align-items: center;
}

.course-chip {
  background: #333333;
  border: 1px solid #444444;
  border-radius: 14px;
  padding: 0.2rem 0.5rem 0.2rem 0.65rem;
  font-size: 0.8rem;
  color: #e5e7eb;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  transition: all 0.2s ease;
}

.chip-remove {
  background: transparent;
  border: none;
  color: #ef4444;
  font-size: 0.85rem;
  cursor: pointer;
  line-height: 1;
  padding: 0 0.1rem;
  font-weight: bold;
}

.chip-remove:hover { color: #f87171; }

.course-chip.is-highlighted {
  background: #22c55e;
  color: #ffffff;
  border-color: #16a34a;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
  transform: scale(1.03);
}

.course-chip.is-highlighted .chip-remove { color: #ffffff; }

.course-chip.is-dimmed {
  opacity: 0.2;
  filter: grayscale(80%);
}

.btn-add-chip {
  background: #22c55e;
  color: #ffffff;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: bold;
}

.btn-add-chip:hover { background: #16a34a; }
</style>
