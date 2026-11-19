<script setup lang="ts">
import { useScheduleStore } from '@/stores/scheduleStore';
import { TBD } from '@/types/schedule';

const store = useScheduleStore();

function cell(course: string, outcome: number) {
  const entries = store.courseOutcomeEntries(course, outcome);
  return {
    pis: store.courseOutcomePis(course, outcome),
    tbd: entries.some(e => e.year === TBD),
    tooltip: entries.map(e => store.describeMapping(e)).join('\n'),
  };
}
</script>

<template>
  <section class="mapping-section">
    <h2>Course / Outcome PI Mapping</h2>
    <p class="hint">
      Read-only summary generated from the 6-year schedule above. To change a course's PIs for an outcome, edit it in the schedule.
    </p>

    <div v-if="store.mappedCourses.length === 0" class="empty">No courses have been scheduled yet.</div>

    <div v-else class="table-container">
      <table class="data-table">
        <thead>
        <tr>
          <th class="col-header">Course</th>
          <th class="col-header" v-for="outcome in store.outcomes" :key="outcome">
            {{ outcome }}
            <span class="pi-range">PIs 1-{{ store.piCountForOutcome(outcome) }}</span>
          </th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="course in store.mappedCourses" :key="course">
          <th class="row-header">{{ course }}</th>
          <td v-for="outcome in store.outcomes" :key="outcome" :title="cell(course, outcome).tooltip">
            <span v-if="cell(course, outcome).pis.length">{{ cell(course, outcome).pis.join(', ') }}</span>
            <span v-if="cell(course, outcome).tbd" class="tbd-badge">TBD</span>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.mapping-section { background: #282828; padding: 1.5rem; border-radius: 6px; border: 1px solid #383838; box-shadow: 0 4px 12px rgba(0,0,0,.4); margin-bottom: 2rem; }
h2 { font-size: 1.25rem; font-weight: 700; color: #fff; margin: 0 0 .5rem; text-align: center; }
.hint { margin: 0 0 1.25rem; text-align: center; color: #9ca3af; font-size: .85rem; }
.empty { text-align: center; color: #9ca3af; padding: 1rem; }
.table-container { overflow-x: auto; border: 1px solid #333; border-radius: 4px; }
.data-table { width: 100%; border-collapse: collapse; background: #181818; }
.data-table th, .data-table td { border: 1px solid #333; padding: .75rem .4rem; text-align: center; color: #fff; cursor: default; }
.col-header, .row-header { background: #242424; color: #e2e8f0; font-size: .875rem; font-weight: 600; }
.row-header { width: 90px; font-weight: 700; }
.pi-range { display: block; font-size: .7rem; font-weight: 500; color: #9ca3af; margin-top: .2rem; }
.tbd-badge { margin-left: .35rem; font-size: .7rem; font-weight: 700; color: #fbbf24; border: 1px dashed #d97706; border-radius: 999px; padding: .05rem .4rem; }
</style>
