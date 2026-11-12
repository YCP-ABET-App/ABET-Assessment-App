<script setup lang="ts">
import { useScheduleModals } from '@/composables/useScheduleModals';
import { useScheduleStore } from '@/stores/scheduleStore';
import { SEMESTER_OPTIONS, TBD } from '@/types/schedule';

const store = useScheduleStore();
const {
  detailTarget, detailRows, detailOutcomePis, drafts, rowErrors,
  closeCourseDetail, setDraftYear, toggleDraftPi, saveDetailRow, removeDetailRow,
} = useScheduleModals();
</script>

<template>
  <div v-if="detailTarget" class="modal-overlay" @click.self="closeCourseDetail">
    <div class="modal-content">
      <h3>{{ detailTarget.course }} — Outcome {{ detailTarget.outcome }}</h3>
      <p class="hint">Every place this course is assessed for this outcome. Edit the year, semester or PIs, or remove an entry.</p>

      <div v-for="row in detailRows" :key="row.id" class="row-card" :class="{ tbd: row.year === TBD }">
        <template v-if="drafts[row.id]">
          <div class="row-top">
            <label>
              <span class="lbl">Year</span>
              <select :value="drafts[row.id].year" @change="setDraftYear(row.id, ($event.target as HTMLSelectElement).value)">
                <option v-for="col in store.scheduleColumns" :key="col" :value="col">
                  {{ col === TBD ? 'TBD (not scheduled yet)' : col }}
                </option>
              </select>
            </label>

            <label v-if="drafts[row.id].year !== TBD">
              <span class="lbl">Semester</span>
              <select v-model="drafts[row.id].semester">
                <option v-for="s in SEMESTER_OPTIONS" :key="s.value" :value="s.value">{{ s.label }}</option>
              </select>
            </label>
          </div>

          <div v-if="drafts[row.id].year !== TBD" class="pi-list">
            <span class="lbl">PIs</span>
            <label v-for="pi in detailOutcomePis" :key="pi.number" class="pi-chip" :class="{ on: drafts[row.id].pis.includes(pi.number) }">
              <input type="checkbox" :checked="drafts[row.id].pis.includes(pi.number)" @change="toggleDraftPi(row.id, pi.number)" />
              {{ pi.name }}
            </label>
          </div>
          <p v-else class="tbd-note">Outcome-level placeholder. Choose a year to assign a semester and PIs.</p>

          <p v-if="rowErrors[row.id]" class="field-error">{{ rowErrors[row.id] }}</p>

          <div class="row-actions">
            <button class="btn-danger btn-small" @click="removeDetailRow(row.id)">Remove</button>
            <button class="btn-save btn-small" @click="saveDetailRow(row.id)">Save</button>
          </div>
        </template>
      </div>

      <div class="modal-actions">
        <button class="btn-cancel" @click="closeCourseDetail">Close</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.75); display: flex; align-items: center; justify-content: center; z-index: 1000; backdrop-filter: blur(2px); }
.modal-content { background: #2c2c2c; color: #fff; padding: 2rem; border-radius: 8px; width: 560px; max-width: 92vw; max-height: 90vh; overflow-y: auto; border: 1px solid #383838; text-align: left; }
h3 { text-align: center; margin: 0 0 .5rem; font-size: 1.2rem; }
.hint { text-align: center; color: #9ca3af; font-size: .85rem; margin: 0 0 1rem; }
.row-card { background: #1e1e1e; border: 1px solid #3a3a3a; border-radius: 8px; padding: .9rem; margin-bottom: .8rem; }
.row-card.tbd { border: 1px dashed #d97706; background: #241c0e; }
.row-top { display: flex; gap: .75rem; margin-bottom: .7rem; }
.row-top label { flex: 1; display: flex; flex-direction: column; gap: .25rem; }
.lbl { font-size: .7rem; font-weight: 700; text-transform: uppercase; letter-spacing: .05em; color: #9ca3af; }
select { padding: .45rem; background: #1c1c1c; color: #fff; border: 1px solid #454545; border-radius: 4px; }
.pi-list { display: flex; flex-wrap: wrap; align-items: center; gap: .4rem; }
.pi-chip { display: inline-flex; gap: .35rem; align-items: center; padding: .3rem .6rem; border: 1px solid #454545; border-radius: 999px; font-size: .8rem; cursor: pointer; }
.pi-chip.on { border-color: #22c55e; background: rgba(34,197,94,.15); color: #4ade80; }
.tbd-note { color: #fbbf24; font-size: .85rem; margin: 0; }
.field-error { color: #f87171; font-size: .8rem; margin: .5rem 0 0; }
.row-actions { display: flex; justify-content: flex-end; gap: .5rem; margin-top: .8rem; }
.modal-actions { display: flex; justify-content: flex-end; margin-top: 1rem; }
.btn-cancel, .btn-save, .btn-danger { border: none; border-radius: 4px; cursor: pointer; font-weight: 600; color: #fff; padding: .5rem 1.25rem; }
.btn-cancel { background: #4b5563; } .btn-save { background: #22c55e; } .btn-danger { background: #dc2626; }
.btn-small { padding: .35rem .8rem; font-size: .8rem; }
</style>
