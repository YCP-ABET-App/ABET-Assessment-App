<script setup lang="ts">
import { useScheduleStore } from '@/stores/scheduleStore';
import { useScheduleModals } from '@/composables/useScheduleModals';
import { TBD } from '@/types/schedule';

const store = useScheduleStore();
const { openCourseSearch, openCourseDetail } = useScheduleModals();

function chipTitle(entries: ReturnType<typeof store.coursesInCell>[number]['entries']) {
  return entries.map(e => store.describeMapping(e)).join('\n') + '\n(click for details)';
}

function matches(course: string) {
  return !!store.highlightQuery && course.toLowerCase().includes(store.highlightQuery.toLowerCase());
}
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
          <th
            v-for="col in store.scheduleColumns"
            :key="col"
            class="col-header"
            :class="{ 'tbd-col': col === TBD }"
          >
            {{ col }}
            <span v-if="col === TBD" class="col-sub">semester unknown</span>
          </th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="outcome in store.outcomes" :key="outcome">
          <th class="row-header">{{ outcome }}</th>
          <td
            v-for="col in store.scheduleColumns"
            :key="col"
            class="cell-courses"
            :class="{ 'tbd-cell': col === TBD }"
          >
            <div class="chip-container">
              <span
                v-for="item in store.coursesInCell(outcome, col)"
                :key="item.course"
                class="course-chip"
                :class="{
                  'is-tbd': col === TBD,
                  'is-highlighted': matches(item.course),
                  'is-dimmed': store.highlightQuery && !matches(item.course),
                }"
                :title="chipTitle(item.entries)"
                @click="openCourseDetail(item.course, outcome)"
              >
                {{ item.course }}
                <small v-if="col !== TBD" class="sem-tag">
                  {{ item.entries.map(e => e.semester.charAt(0)).join('/') }}
                </small>
              </span>
              <button class="btn-add-chip" @click="openCourseSearch(outcome, col)" title="Add Course">+</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.mapping-section { background: #282828; padding: 1.5rem; border-radius: 6px; border: 1px solid #383838; box-shadow: 0 4px 12px rgba(0,0,0,.4); margin-bottom: 2rem; }
.section-header { position: relative; display: flex; justify-content: center; align-items: center; margin-bottom: 1.25rem; }
.section-header h2 { font-size: 1.25rem; font-weight: 700; color: #fff; margin: 0; text-align: center; }
.highlight-input { position: absolute; right: 0; width: 220px; padding: .4rem .65rem; border: 1px solid #4a4a4a; background: #181818; color: #fff; border-radius: 4px; font-size: .85rem; }
.highlight-input:focus { outline: 1px solid #22c55e; border-color: #22c55e; }
.table-container { overflow-x: auto; border: 1px solid #333; border-radius: 4px; }
.data-table { width: 100%; border-collapse: collapse; background: #181818; }
.data-table th, .data-table td { border: 1px solid #333; padding: .75rem .4rem; text-align: center; color: #fff; }
.col-header, .row-header { background: #242424; color: #e2e8f0; font-size: .875rem; font-weight: 600; }
.row-header { width: 70px; font-weight: 700; }
.col-sub { display: block; font-size: .65rem; font-weight: 500; opacity: .8; }
.cell-courses { vertical-align: top; min-width: 135px; height: 75px; }
.chip-container { display: flex; flex-wrap: wrap; gap: .35rem; justify-content: center; align-items: center; }
.course-chip { background: #333; border: 1px solid #444; border-radius: 14px; padding: .2rem .65rem; font-size: .8rem; color: #e5e7eb; display: inline-flex; align-items: center; gap: .3rem; cursor: pointer; transition: all .2s ease; }
.course-chip:hover { border-color: #22c55e; }
.sem-tag { font-size: .65rem; color: #9ca3af; }
.course-chip.is-highlighted { background: #22c55e; color: #fff; border-color: #16a34a; box-shadow: 0 0 8px rgba(34,197,94,.4); transform: scale(1.03); }
.course-chip.is-dimmed { opacity: .2; filter: grayscale(80%); }

/* TBD column: visually distinct */
.data-table th.tbd-col { background: #4a3410; color: #fbbf24; border-left: 3px solid #d97706; }
.tbd-cell { background: #1f1809; border-left: 3px solid #d97706 !important; border-style: dashed; }
.course-chip.is-tbd { background: #3a2a0c; border: 1px dashed #d97706; color: #fcd34d; }
.btn-add-chip { background: #22c55e; color: #fff; border: none; border-radius: 50%; width: 20px; height: 20px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer; font-size: .85rem; font-weight: bold; }
.btn-add-chip:hover { background: #16a34a; }
.tbd-cell .btn-add-chip { background: #d97706; }
</style>
