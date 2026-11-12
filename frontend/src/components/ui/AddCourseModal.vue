<script setup lang="ts">
import { useScheduleModals } from '@/composables/useScheduleModals';
import { useScheduleStore } from '@/stores/scheduleStore';
import { SEMESTER_OPTIONS } from '@/types/schedule';

const store = useScheduleStore();
const {
  activeTargetCell, addStep, searchQuery, isCreatingCourse, newCoursePrefix, newCourseNumber,
  composedNewCourse, composedCourseExists, courseFormError, filteredCourses, exactCourseMatch,
  canCreateFromQuery, isTbdTarget, outcomePis, chosenCourse, chosenSemester, chosenPis,
  chosenExistingEntries, chosenPiUsage, courseNoteForTarget,
  closeCourseSearch, chooseExistingCourse, beginCreateCourse, createAndAddNewCourse,
  addTypedAsNewCourse, backToCourseStep, togglePi, confirmAddMapping,
} = useScheduleModals();
</script>

<template>
  <div v-if="activeTargetCell" class="modal-overlay" @click.self="closeCourseSearch">
    <div class="modal-content">
      <h3>
        Add Course to Outcome {{ activeTargetCell.outcome }}
        <template v-if="isTbdTarget">(TBD)</template>
        <template v-else>({{ activeTargetCell.year }})</template>
      </h3>

      <!-- STEP 1: choose course -->
      <template v-if="addStep === 'course'">
        <div class="form-group">
          <input
            type="text"
            v-model="searchQuery"
            placeholder="Search courses (e.g. CS 400)..."
            autofocus
            @keydown.enter.prevent="filteredCourses.length === 1
              ? chooseExistingCourse(filteredCourses[0])
              : (canCreateFromQuery && addTypedAsNewCourse())"
          />
        </div>

        <p v-if="!searchQuery.trim()" class="hint">Start typing a prefix and number, such as CS 400.</p>

        <ul v-if="filteredCourses.length > 0" class="autocomplete-list">
          <li v-for="course in filteredCourses" :key="course" @click="chooseExistingCourse(course)">
            <strong>{{ course }}</strong>
            <span v-if="courseNoteForTarget(course)" class="note">
              already in Outcome {{ activeTargetCell.outcome }}: {{ courseNoteForTarget(course) }}
            </span>
          </li>
        </ul>

        <div v-if="searchQuery.trim() && filteredCourses.length === 0" class="create-course-prompt">
          <p>No matching courses.</p>
          <button v-if="!isCreatingCourse" class="btn-save btn-small" @click="addTypedAsNewCourse">Add new course</button>
        </div>

        <div v-if="canCreateFromQuery && filteredCourses.length > 0 && !isCreatingCourse" class="create-course-prompt secondary">
          <p>Don’t see the right course?</p>
          <button class="btn-save btn-small" @click="beginCreateCourse">Create new course</button>
        </div>

        <div v-if="isCreatingCourse" class="create-course-prompt">
          <p>Prefix and number are saved together as one course.</p>
          <div class="create-inline">
            <input v-model="newCoursePrefix" class="prefix-input" placeholder="CS" maxlength="8" aria-label="Course prefix"
                   @keydown.enter.prevent="createAndAddNewCourse" />
            <input v-model="newCourseNumber" class="number-input" placeholder="400" maxlength="5" aria-label="Course number"
                   @keydown.enter.prevent="createAndAddNewCourse" />
            <button class="btn-save btn-small" :disabled="!!composedCourseExists" @click="createAndAddNewCourse">Continue</button>
            <button class="btn-cancel btn-small" @click="isCreatingCourse = false">Cancel</button>
          </div>
          <p v-if="composedCourseExists" class="field-error">
            {{ composedCourseExists }} already exists. Choose it from the list instead.
          </p>
          <p v-else-if="composedNewCourse" class="preview">Will create <strong>{{ composedNewCourse }}</strong></p>
        </div>
      </template>

      <!-- STEP 2: semester + PIs -->
      <template v-else-if="chosenCourse">
        <p class="course-line">
          <strong>{{ chosenCourse.code }}</strong>
          <span v-if="chosenCourse.isNew" class="badge-new">new course</span>
        </p>

        <div v-if="chosenExistingEntries.length" class="existing-box">
          <div class="existing-title">Already associated with Outcome {{ activeTargetCell.outcome }}</div>
          <ul>
            <li v-for="e in chosenExistingEntries" :key="e.id">{{ store.describeMapping(e) }}</li>
          </ul>
        </div>

        <template v-if="isTbdTarget">
          <p class="tbd-note">
            This goes in the <strong>TBD</strong> column. No semester or PI is required: the outcome itself is recorded
            as the placeholder until the PIs and semester are known.
          </p>
        </template>

        <template v-else>
          <div class="form-group">
            <div class="group-label">Semester in {{ activeTargetCell.year }}</div>
            <div class="radio-row">
              <label v-for="s in SEMESTER_OPTIONS" :key="s.value" class="radio-pill" :class="{ on: chosenSemester === s.value }">
                <input type="radio" :value="s.value" v-model="chosenSemester" />
                {{ s.label }}
              </label>
            </div>
          </div>

          <div class="form-group">
            <div class="group-label">Performance indicators (Outcome {{ activeTargetCell.outcome }})</div>
            <label v-for="pi in outcomePis" :key="pi.number" class="pi-option" :class="{ on: chosenPis.includes(pi.number) }">
              <input type="checkbox" :checked="chosenPis.includes(pi.number)" @change="togglePi(pi.number)" />
              <span class="pi-text">
                <strong>{{ pi.name }}</strong>
                <span class="pi-def">{{ pi.definition || 'No definition yet' }}</span>
                <span v-if="chosenPiUsage[pi.number]" class="pi-used">
                  Already assessed: {{ chosenPiUsage[pi.number].join('; ') }}
                </span>
              </span>
            </label>
          </div>
        </template>

        <p v-if="courseFormError" class="field-error">{{ courseFormError }}</p>

        <div class="modal-actions">
          <button class="btn-cancel" @click="backToCourseStep">Back</button>
          <button class="btn-save" @click="confirmAddMapping">
            {{ isTbdTarget ? 'Add to TBD' : 'Add to Schedule' }}
          </button>
        </div>
      </template>

      <p v-if="addStep === 'course' && courseFormError" class="field-error">{{ courseFormError }}</p>

      <div v-if="addStep === 'course'" class="modal-actions">
        <button class="btn-cancel" @click="closeCourseSearch">Close</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.75); display: flex; align-items: center; justify-content: center; z-index: 1000; backdrop-filter: blur(2px); }
.modal-content { background: #2c2c2c; color: #fff; padding: 2rem; border-radius: 8px; width: 520px; max-width: 92vw; max-height: 90vh; overflow-y: auto; box-shadow: 0 10px 25px rgba(0,0,0,.6); border: 1px solid #383838; text-align: left; }
.modal-content h3 { font-size: 1.2rem; text-align: center; margin: 0 0 1.25rem; font-weight: 700; line-height: 1.4; }
.form-group { margin-bottom: .9rem; }
.form-group input[type="text"] { width: 100%; padding: .6rem .75rem; border: 1px solid #454545; background: #1c1c1c; color: #fff; border-radius: 4px; font-size: .95rem; box-sizing: border-box; }
.form-group input[type="text"]:focus { outline: 1px solid #22c55e; border-color: #22c55e; }
.group-label { font-size: .8rem; font-weight: 700; text-transform: uppercase; letter-spacing: .04em; color: #9ca3af; margin-bottom: .4rem; }
.hint { margin: 0 0 .5rem; font-size: .85rem; color: #9ca3af; text-align: center; }
.field-error { margin: .75rem 0 0; color: #f87171; font-size: .85rem; text-align: center; }
.preview { margin: .75rem 0 0; color: #86efac; font-size: .85rem; }
.autocomplete-list { background: #333; border: 1px solid #444; border-radius: 4px; list-style: none; padding: 0; margin: .5rem 0 0; max-height: 220px; overflow-y: auto; }
.autocomplete-list li { padding: .65rem 1rem; cursor: pointer; border-bottom: 1px solid #3d3d3d; font-size: .925rem; display: flex; flex-direction: column; gap: .15rem; }
.autocomplete-list li:last-child { border-bottom: none; }
.autocomplete-list li:hover { background: #3d3d3d; color: #4ade80; }
.note { font-size: .75rem; color: #fbbf24; }
.create-course-prompt { background: #222; padding: 1rem; border-radius: 4px; margin-top: 1rem; text-align: center; border: 1px solid #383838; }
.create-course-prompt.secondary { background: transparent; border-style: dashed; }
.create-course-prompt p { margin: 0 0 .75rem; font-size: .875rem; color: #d1d5db; }
.create-inline { display: flex; gap: .5rem; justify-content: center; flex-wrap: wrap; align-items: center; }
.create-inline input { padding: .45rem .65rem; border: 1px solid #454545; background: #1c1c1c; color: #fff; border-radius: 4px; }
.prefix-input { width: 4.5rem; text-transform: uppercase; }
.number-input { width: 4.5rem; }
.course-line { font-size: 1.1rem; margin: 0 0 .75rem; text-align: center; }
.badge-new { margin-left: .5rem; font-size: .7rem; background: #22c55e; padding: .1rem .45rem; border-radius: 999px; }
.existing-box { background: #222; border: 1px solid #383838; border-left: 3px solid #fbbf24; border-radius: 4px; padding: .7rem .9rem; margin-bottom: 1rem; font-size: .85rem; }
.existing-title { font-weight: 700; color: #fbbf24; margin-bottom: .3rem; }
.existing-box ul { margin: 0; padding-left: 1.1rem; color: #d1d5db; }
.tbd-note { background: rgba(245,158,11,.12); border: 1px dashed #d97706; color: #fbbf24; border-radius: 6px; padding: .8rem 1rem; font-size: .88rem; line-height: 1.45; }
.radio-row { display: flex; gap: .5rem; }
.radio-pill { flex: 1; text-align: center; padding: .5rem; border: 1px solid #454545; border-radius: 6px; cursor: pointer; background: #1c1c1c; font-size: .9rem; }
.radio-pill input { display: none; }
.radio-pill.on { border-color: #22c55e; background: rgba(34,197,94,.15); color: #4ade80; font-weight: 700; }
.pi-option { display: flex; gap: .6rem; align-items: flex-start; padding: .6rem .7rem; border: 1px solid #3a3a3a; border-radius: 6px; margin-bottom: .4rem; cursor: pointer; background: #1c1c1c; }
.pi-option.on { border-color: #22c55e; background: rgba(34,197,94,.08); }
.pi-text { display: flex; flex-direction: column; gap: .15rem; font-size: .88rem; }
.pi-def { color: #9ca3af; font-size: .8rem; }
.pi-used { color: #fbbf24; font-size: .75rem; }
.modal-actions { display: flex; justify-content: flex-end; gap: .75rem; margin-top: 1.5rem; }
.btn-cancel { background: #4b5563; color: #fff; padding: .5rem 1.25rem; border: none; border-radius: 4px; cursor: pointer; font-weight: 600; }
.btn-cancel:hover { background: #374151; }
.btn-save { background: #22c55e; color: #fff; padding: .5rem 1.25rem; border: none; border-radius: 4px; cursor: pointer; font-weight: 600; }
.btn-save:hover { background: #16a34a; }
.btn-save:disabled { opacity: .45; cursor: not-allowed; }
.btn-small { padding: .4rem .8rem; font-size: .85rem; }
</style>
