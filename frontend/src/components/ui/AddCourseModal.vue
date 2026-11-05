<script setup lang="ts">
import { useScheduleModals } from '@/composables/useScheduleModals';

const {
  activeTargetCell,
  searchQuery,
  isCreatingCourse,
  newCoursePrefix,
  newCourseNumber,
  composedNewCourse,
  composedCourseExists,
  courseFormError,
  filteredCourses,
  exactCourseMatch,
  alreadyInActiveCell,
  canCreateFromQuery,
  closeCourseSearch,
  addCourseToActiveCell,
  beginCreateCourse,
  createAndAddNewCourse,
  addTypedAsNewCourse,
} = useScheduleModals();
</script>

<template>
  <div v-if="activeTargetCell" class="modal-overlay" @click.self="closeCourseSearch">
    <div class="modal-content">
      <h3>Add Course to Outcome {{ activeTargetCell.outcome }} ({{ activeTargetCell.year }})</h3>

      <div class="form-group">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Search courses (e.g. CS 400)..."
          autofocus
          @keydown.enter.prevent="filteredCourses.length === 1
            ? addCourseToActiveCell(filteredCourses[0])
            : (canCreateFromQuery && addTypedAsNewCourse())"
        />
      </div>

      <p v-if="!searchQuery.trim()" class="hint">Start typing a prefix and number, such as CS 400.</p>

      <p v-if="alreadyInActiveCell" class="field-error">
        {{ exactCourseMatch }} is already scheduled in this outcome/year.
      </p>

      <p v-else-if="exactCourseMatch && !isCreatingCourse" class="exists-note">
        {{ exactCourseMatch }} already exists. Select it from the list below instead of creating it again.
      </p>

      <ul v-if="filteredCourses.length > 0" class="autocomplete-list">
        <li
          v-for="course in filteredCourses"
          :key="course"
          @click="addCourseToActiveCell(course)"
        >
          Add: <strong>{{ course }}</strong>
        </li>
      </ul>

      <div v-if="searchQuery.trim() && filteredCourses.length === 0 && !alreadyInActiveCell" class="create-course-prompt">
        <p>No matching courses.</p>
        <button
          v-if="!isCreatingCourse"
          class="btn-save btn-small"
          @click="addTypedAsNewCourse"
        >
          Add new course
        </button>
      </div>

      <div
        v-if="canCreateFromQuery && filteredCourses.length > 0 && !isCreatingCourse"
        class="create-course-prompt secondary"
      >
        <p>Don’t see the right course?</p>
        <button class="btn-save btn-small" @click="beginCreateCourse">
          Create new course
        </button>
      </div>

      <div v-if="isCreatingCourse" class="create-course-prompt">
        <p>Prefix and number are saved together as one course.</p>
        <div class="create-inline">
          <input
            v-model="newCoursePrefix"
            class="prefix-input"
            placeholder="CS"
            maxlength="8"
            aria-label="Course prefix"
            @keydown.enter.prevent="createAndAddNewCourse"
          />
          <input
            v-model="newCourseNumber"
            class="number-input"
            placeholder="400"
            maxlength="5"
            aria-label="Course number"
            @keydown.enter.prevent="createAndAddNewCourse"
          />
          <button class="btn-save btn-small" :disabled="!!composedCourseExists" @click="createAndAddNewCourse">Save &amp; Add</button>
          <button class="btn-cancel btn-small" @click="isCreatingCourse = false">Cancel</button>
        </div>
        <p v-if="composedCourseExists" class="field-error">
          {{ composedCourseExists }} already exists. Choose it from the list instead of creating a duplicate.
        </p>
        <p v-else-if="composedNewCourse" class="preview">Will add <strong>{{ composedNewCourse }}</strong></p>
      </div>

      <p v-if="courseFormError" class="field-error">{{ courseFormError }}</p>

      <div class="modal-actions">
        <button class="btn-cancel" @click="closeCourseSearch">Close</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: #2c2c2c;
  color: #ffffff;
  padding: 2rem;
  border-radius: 8px;
  width: 440px;
  max-width: 90vw;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.6);
  border: 1px solid #383838;
}

.modal-content h3 {
  font-size: 1.2rem;
  text-align: center;
  margin-top: 0;
  margin-bottom: 1.25rem;
  font-weight: 700;
  line-height: 1.4;
}

.form-group { margin-bottom: 0.75rem; }

.form-group input[type="text"] {
  width: 100%;
  padding: 0.6rem 0.75rem;
  border: 1px solid #454545;
  background: #1c1c1c;
  color: #ffffff;
  border-radius: 4px;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.form-group input[type="text"]:focus {
  outline: 1px solid #22c55e;
  border-color: #22c55e;
}

.hint {
  margin: 0 0 0.5rem;
  font-size: 0.85rem;
  color: #9ca3af;
  text-align: center;
}

.field-error {
  margin: 0.75rem 0 0;
  color: #f87171;
  font-size: 0.85rem;
  text-align: center;
}

.exists-note {
  margin: 0.5rem 0 0;
  color: #fbbf24;
  font-size: 0.85rem;
  text-align: center;
}

.preview {
  margin: 0.75rem 0 0;
  color: #86efac;
  font-size: 0.85rem;
}

.autocomplete-list {
  background: #333333;
  border: 1px solid #444444;
  border-radius: 4px;
  list-style: none;
  padding: 0;
  margin: 0.5rem 0 0 0;
  max-height: 180px;
  overflow-y: auto;
}

.autocomplete-list li {
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-bottom: 1px solid #3d3d3d;
  text-align: center;
  font-size: 0.925rem;
}

.autocomplete-list li:last-child { border-bottom: none; }

.autocomplete-list li:hover {
  background: #3d3d3d;
  color: #4ade80;
}

.create-course-prompt {
  background: #222222;
  padding: 1rem;
  border-radius: 4px;
  margin-top: 1rem;
  text-align: center;
  border: 1px solid #383838;
}

.create-course-prompt.secondary {
  background: transparent;
  border-style: dashed;
}

.create-course-prompt p {
  margin: 0 0 0.75rem;
  font-size: 0.875rem;
  color: #d1d5db;
}

.create-inline {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
  justify-content: center;
  flex-wrap: wrap;
  align-items: center;
}

.create-inline input {
  padding: 0.45rem 0.65rem;
  border: 1px solid #454545;
  background: #1c1c1c;
  color: #ffffff;
  border-radius: 4px;
}

.prefix-input { width: 4.5rem; text-transform: uppercase; }
.number-input { width: 4.5rem; }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.btn-cancel {
  background: #4b5563;
  color: #ffffff;
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel:hover { background: #374151; }

.btn-save {
  background: #22c55e;
  color: #ffffff;
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-save:hover { background: #16a34a; }

.btn-save:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.btn-small { padding: 0.4rem 0.8rem; font-size: 0.85rem; }
</style>
