<script setup lang="ts">
import { ref, reactive } from "vue";
import { computed } from 'vue';

// Removed unused Pinia imports to resolve TS6133

// --- TYPES ---
interface OutcomeMapping {
  id: number;
  description: string;
  pis: string[];
}

interface SelectedPI {
  outcomeId: number;
  outcomeDesc: string;
  piName: string;
}

// --- STATE: SO-PI Mapping & Modal ---
const isSoPiDropdownOpen = ref(false);
const selectedPI = ref<SelectedPI | null>(null);

const soPiMap = reactive<OutcomeMapping[]>([
  { id: 1, description: "Students show professional working behavior", pis: ["PI 1.1", "PI 1.2"] },
  { id: 2, description: "Students can apply mathematical foundations", pis: ["PI 2.1", "PI 2.2"] },
  { id: 3, description: "Students can design and implement solutions", pis: ["PI 3.1", "PI 3.2"] },
]);

const rubricForm = reactive({
  met: "",
  below: "",
  exceeds: ""
});

// Fix TS18047 / TS2339 by properly typing the DOM event
function handleDropdownToggle(event: Event) {
  const target = event.target as HTMLDetailsElement;
  isSoPiDropdownOpen.value = target.open;
}

// Fix TS7006 by assigning strict types to parameters
function openRubricModal(outcome: OutcomeMapping, pi: string) {
  selectedPI.value = { outcomeId: outcome.id, outcomeDesc: outcome.description, piName: pi };
  rubricForm.met = "";
  rubricForm.below = "";
  rubricForm.exceeds = "";
}

function closeRubricModal() {
  selectedPI.value = null;
}

function saveRubric() {
  console.log("Saving rubric for", selectedPI.value, rubricForm);
  closeRubricModal();
}

// --- STATE: 6-Year Outcome Schedule ---
const academicYears = ["2024-2025", "2025-2026", "2026-2027", "2027-2028", "2028-2029", "2029-2030"];
const outcomes = [1, 2, 3, 4, 5, 6];
// --- STATE: Table Highlight Tool ---
const highlightQuery = ref("");

// Updated to store arrays of strings instead of comma-separated strings
const sixYearSchedule = reactive<Record<number, Record<string, string[]>>>({
  1: { "2024-2025": ["CS 350", "CS 400"], "2025-2026": ["CS 101", "CS 201", "CS 360"], "2026-2027": ["CS 320", "CS 350", "CS 400"], "2027-2028": ["CS 201", "CS 360", "CS 402"], "2028-2029": ["CS 101", "CS 320", "CS 350", "CS 400"], "2029-2030": ["CS 201", "CS 320", "CS 360", "CS 402"] },
  2: { "2024-2025": ["CS 320", "CS 330"], "2025-2026": ["CS 335", "CS 360", "CS 400"], "2026-2027": ["CS 201", "CS 330"], "2027-2028": ["CS 320", "CS 335", "CS 360", "CS 402"], "2028-2029": ["CS 330"], "2029-2030": ["CS 201", "CS 330", "CS 360", "CS 402"] },
  3: { "2024-2025": ["CS 400"], "2025-2026": ["CS 320"], "2026-2027": ["CS 320", "CS 400"], "2027-2028": ["CS 402"], "2028-2029": ["CS 400"], "2029-2030": ["CS 320", "CS 402"] },
  4: { "2024-2025": ["CS 330"], "2025-2026": ["CS 335", "CS 456"], "2026-2027": ["CS 330"], "2027-2028": ["CS 335", "CS 456"], "2028-2029": ["CS 330"], "2029-2030": ["CS 335", "CS 456"] },
  5: { "2024-2025": ["CS 400"], "2025-2026": ["CS 320"], "2026-2027": ["CS 320", "CS 400"], "2027-2028": ["CS 402"], "2028-2029": ["CS 400"], "2029-2030": ["CS 320", "CS 402"] },
  6: { "2024-2025": ["CS 201", "CS 420"], "2025-2026": ["CS 101", "CS 320", "CS 335", "CS 340", "CS 400"], "2026-2027": ["CS 201", "CS 420"], "2027-2028": ["CS 101", "CS 335", "CS 340", "CS 402"], "2028-2029": ["CS 101", "CS 320", "CS 420"], "2029-2030": ["CS 201", "CS 335", "CS 340", "CS 402"] }
});

// --- STATE: Cell Course Search & Creation ---
const activeTargetCell = ref<{ outcome: number; year: string } | null>(null);
const searchQuery = ref("");
const isCreatingCourse = ref(false);
const newCourseName = ref("");

// Mock global course list
const allAvailableCourses = ref([
  "CS 101", "CS 201", "CS 320", "CS 330", "CS 335",
  "CS 340", "CS 350", "CS 360", "CS 400", "CS 402",
  "CS 420", "CS 456", "CS 499", "MATH 210"
]);

const filteredCourses = computed(() => {
  if (!searchQuery.value) return [];
  const query = searchQuery.value.toLowerCase();

  // Filter by query and exclude courses already in the active cell
  return allAvailableCourses.value.filter(course => {
    const isMatch = course.toLowerCase().includes(query);
    const isAlreadyInCell = activeTargetCell.value
      && sixYearSchedule[activeTargetCell.value.outcome][activeTargetCell.value.year].includes(course);
    return isMatch && !isAlreadyInCell;
  });
});

function removeCourseFromCell(outcome: number, year: string, courseToRemove: string) {
  sixYearSchedule[outcome][year] = sixYearSchedule[outcome][year].filter(c => c !== courseToRemove);
}

function openCourseSearch(outcome: number, year: string) {
  activeTargetCell.value = { outcome, year };
  searchQuery.value = "";
  isCreatingCourse.value = false;
}

function closeCourseSearch() {
  activeTargetCell.value = null;
}

function addCourseToActiveCell(courseName: string) {
  if (activeTargetCell.value) {
    const { outcome, year } = activeTargetCell.value;
    if (!sixYearSchedule[outcome][year].includes(courseName)) {
      sixYearSchedule[outcome][year].push(courseName);
    }
  }
  closeCourseSearch();
}

function createAndAddNewCourse() {
  if (!newCourseName.value) return;
  const courseName = newCourseName.value.toUpperCase();

  // TODO: API Call to save new course globally

  if (!allAvailableCourses.value.includes(courseName)) {
    allAvailableCourses.value.push(courseName);
  }
  addCourseToActiveCell(courseName);
  newCourseName.value = "";
}

// --- STATE: Course/Outcome Mapping ---
const courses = ["CS 101", "CS 201", "CS 320", "CS 330", "CS 335", "CS 340", "CS 350", "CS 360", "CS 400", "CS 402", "CS 420", "CS 456"];

// Fix TS7053 by explicitly typing the nested course object keys
const courseOutcomeMap: Record<string, Record<number, string>> = reactive({
  "CS 101": { 1: "1, 5", 2: "", 3: "", 4: "", 5: "", 6: "1" },
  "CS 201": { 1: "1, 3, 5", 2: "5", 3: "", 4: "", 5: "", 6: "1, 2, 3" },
  "CS 320": { 1: "2, 4", 2: "1, 2, 3, 4", 3: "1, 2, 3, 4", 4: "", 5: "1, 2, 3", 6: "2, 4, 6" },
  "CS 330": { 1: "", 2: "6, 7", 3: "", 4: "1", 5: "", 6: "" },
  "CS 335": { 1: "", 2: "6, 7", 3: "", 4: "1", 5: "", 6: "5" },
  "CS 340": { 1: "", 2: "", 3: "", 4: "", 5: "", 6: "1" },
  "CS 350": { 1: "1, 3, 5", 2: "", 3: "", 4: "", 5: "", 6: "" },
  "CS 360": { 1: "", 2: "", 3: "", 4: "", 5: "", 6: "" },
  "CS 400": { 1: "1, 3, 5, 6", 2: "5", 3: "1, 2, 3, 4", 4: "1, 2, 3, 4", 5: "1, 2, 3", 6: "4, 6" },
  "CS 402": { 1: "2, 4", 2: "1, 2, 3, 4", 3: "1, 2, 3, 4", 4: "", 5: "1, 2, 3", 6: "4, 6" },
  "CS 420": { 1: "", 2: "", 3: "", 4: "", 5: "", 6: "1, 3, 5" },
  "CS 456": { 1: "", 2: "", 3: "", 4: "1, 2", 5: "", 6: "" }
});
</script>

<template>
  <div class="schedule-page">
    <header>
      <h1>Schedule Entry</h1>
    </header>

    <!-- SECTION 1: SO-PI Dropdown -->
    <section class="mapping-section">
      <details class="so-pi-dropdown" @toggle="handleDropdownToggle">
        <summary>SO-PI Desc. Map Dropdown</summary>
        <div class="dropdown-content">
          <table class="data-table">
            <thead>
            <tr>
              <th class="col-so-num">SO #</th>
              <th class="col-desc">Description</th>
              <th colspan="2">Performance Indicators</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="so in soPiMap" :key="so.id">
              <td class="so-num-cell">{{ so.id }}</td>
              <td class="desc-cell">{{ so.description }}</td>
              <td v-for="(pi, index) in so.pis" :key="index" class="pi-cell" @click="openRubricModal(so, pi)">
                {{ pi }}
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </details>
    </section>

    <!-- SECTION 2: 6-Year Outcome Schedule -->
    <section class="mapping-section">
      <!-- Updated header for perfect centering -->
      <div class="section-header">
        <h2>6-Year Outcome to Course Schedule Mapping</h2>
        <input
          type="text"
          v-model="highlightQuery"
          placeholder="Highlight course (e.g. CS 400)..."
          class="search-input highlight-input"
        />
      </div>

      <div class="table-container">
        <table class="data-table">
          <thead>
          <tr>
            <th class="col-header">Outcome</th>
            <th class="col-header" v-for="year in academicYears" :key="year">{{ year }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="outcome in outcomes" :key="outcome">
            <th class="row-header">{{ outcome }}</th>
            <td v-for="year in academicYears" :key="year" class="cell-courses">
              <div class="chip-container">
                  <span
                    v-for="course in sixYearSchedule[outcome][year]"
                    :key="course"
                    class="course-chip"
                    :class="{
                      'is-highlighted': highlightQuery && course.toLowerCase().includes(highlightQuery.toLowerCase()),
                      'is-dimmed': highlightQuery && !course.toLowerCase().includes(highlightQuery.toLowerCase())
                    }"
                  >
                    {{ course }}
                    <button class="chip-remove" @click="removeCourseFromCell(outcome, year, course)">×</button>
                  </span>
                <!-- Removed the 'is-dimmed' class check so this button always stays active -->
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
    <!-- SECTION 3: Course/Outcome PI Mapping -->
    <section class="mapping-section">
      <h2>Course / Outcome PI Mapping</h2>
      <div class="table-container">
        <table class="data-table">
          <thead>
          <tr>
            <th class="col-header">Course</th>
            <th class="col-header" v-for="outcome in outcomes" :key="outcome">{{ outcome }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="course in courses" :key="course">
            <th class="row-header">{{ course }}</th>
            <td v-for="outcome in outcomes" :key="outcome">
              <input type="text" v-model="courseOutcomeMap[course][outcome]" class="cell-input" />
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- MODAL: Course Search & Add -->
    <div v-if="activeTargetCell" class="modal-overlay" @click.self="closeCourseSearch">
      <div class="modal-content search-modal-content">
        <h3>Add Course to Outcome {{ activeTargetCell.outcome }} ({{ activeTargetCell.year }})</h3>

        <div class="form-group">
          <input
            type="text"
            v-model="searchQuery"
            placeholder="Search courses..."
            autofocus
          />
        </div>

        <ul v-if="filteredCourses.length > 0" class="autocomplete-list">
          <li v-for="course in filteredCourses" :key="course" @click="addCourseToActiveCell(course)">
            Add: <strong>{{ course }}</strong>
          </li>
        </ul>

        <div v-if="searchQuery && filteredCourses.length === 0" class="create-course-prompt">
          <p>No matches found.</p>
          <button v-if="!isCreatingCourse" @click="isCreatingCourse = true" class="btn-save btn-small">
            Create New Course
          </button>
          <div v-if="isCreatingCourse" class="create-inline">
            <input v-model="newCourseName" placeholder="e.g. CS 500" />
            <button @click="createAndAddNewCourse" class="btn-save btn-small">Save & Add</button>
            <button @click="isCreatingCourse = false" class="btn-cancel btn-small">Cancel</button>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeCourseSearch">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.schedule-page {
  max-width: 1400px;
  margin: 2rem auto;
  padding: 0 2rem;
  color: white; /* Updated to white text */
}

header h1 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
}

.mapping-section {
  margin-bottom: 3rem;
  background: #2c2c2c; /* Grey container background */
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.3);
}

h2 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: white;
}

/* SO-PI Dropdown */
details.so-pi-dropdown summary {
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  padding: 0.75rem;
  background: #3d3d3d;
  border-radius: 4px;
  color: white;
}

.dropdown-content {
  margin-top: 1rem;
}

.col-so-num { width: 80px; }
.so-num-cell { font-weight: bold; font-size: 1.1rem; }

.pi-cell {
  cursor: pointer;
  color: #4ade80; /* Light green for clickable items */
  text-align: center;
  transition: background 0.2s;
}

.pi-cell:hover {
  background: #3d3d3d;
  text-decoration: underline;
}

/* Tables */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #1e1e1e; /* Darker inner table background */
}

.data-table th, .data-table td {
  border: 1px solid #444; /* Dark grey borders */
  padding: 0.75rem;
  text-align: center;
  color: white;
}

.col-header, .row-header {
  background: #333; /* Distinct color for headers */
  color: #a7f3d0; /* Soft green tint for column/row titles */
  font-weight: 600;
}

.desc-cell {
  text-align: left !important;
}

.cell-input {
  width: 100%;
  border: none;
  background: transparent;
  text-align: center;
  font-family: inherit;
  font-size: 1rem;
  padding: 0.25rem;
  color: white; /* White text for inputs */
}

.cell-input:focus {
  outline: 2px solid #22c55e; /* Green focus ring */
  background: #2a2a2a;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #2c2c2c;
  color: white;
  padding: 2rem;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
}

.pi-highlight {
  font-weight: bold;
  color: #4ade80;
  margin: 1rem 0;
}

.form-group {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.form-group input {
  padding: 0.5rem;
  border: 1px solid #555;
  background: #1e1e1e;
  color: white;
  border-radius: 4px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-cancel, .btn-save {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel {
  background: #555;
  color: white;
}

.btn-save {
  background: #22c55e; /* Green button */
  color: white;
}

.btn-save:hover {
  background: #16a34a;
}

.cell-courses {
  vertical-align: top;
}

.chip-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
  align-items: center;
}

.course-chip {
  background: #3d3d3d;
  border: 1px solid #555;
  border-radius: 16px;
  padding: 0.2rem 0.5rem 0.2rem 0.75rem;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.chip-remove {
  background: transparent;
  border: none;
  color: #ef4444;
  font-size: 1rem;
  cursor: pointer;
  line-height: 1;
  padding: 0 0.2rem;
  border-radius: 50%;
}

.chip-remove:hover {
  background: rgba(239, 68, 68, 0.2);
}

.btn-add-chip {
  background: #22c55e;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
}

.btn-add-chip:hover {
  background: #16a34a;
}

.search-modal-content {
  width: 400px;
}

.autocomplete-list {
  background: #3d3d3d;
  border: 1px solid #555;
  border-radius: 4px;
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
}

.autocomplete-list li {
  padding: 0.75rem;
  cursor: pointer;
  border-bottom: 1px solid #444;
}

.autocomplete-list li:hover {
  background: #4ade80;
  color: #1e1e1e;
}

.create-course-prompt {
  background: #3d3d3d;
  padding: 1rem;
  border-radius: 4px;
  margin-top: 1rem;
  text-align: center;
}

.create-inline {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
  justify-content: center;
}

.create-inline input {
  padding: 0.5rem;
  border: 1px solid #555;
  background: #1e1e1e;
  color: white;
  border-radius: 4px;
}

.btn-small {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

/* Section Header for Highlight Tool */
.section-header {
  position: relative; /* Allows absolute positioning of the input */
  display: flex;
  justify-content: center; /* Centers the h2 */
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h2 {
  margin-bottom: 0; /* Override default bottom margin to stay vertically centered */
}

.highlight-input {
  position: absolute;
  right: 0; /* Pins the search bar to the far right */
  width: 250px;
  padding: 0.5rem;
  border: 1px solid #555;
  background: #1e1e1e;
  color: white;
  border-radius: 4px;
}

/* Highlight & Dimming Effects */
.course-chip {
  transition: all 0.3s ease; /* Smooth fade effect */
}

.course-chip.is-dimmed,
.btn-add-chip.is-dimmed {
  opacity: 0.15;
  filter: grayscale(100%);
  pointer-events: none; /* Prevents clicking while dimmed out */
}

.course-chip.is-highlighted {
  background: #22c55e;
  color: white;
  border-color: #16a34a;
  transform: scale(1.05); /* Slight pop-out effect */
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
  z-index: 2;
}

.course-chip.is-highlighted .chip-remove {
  color: white;
}

</style>
