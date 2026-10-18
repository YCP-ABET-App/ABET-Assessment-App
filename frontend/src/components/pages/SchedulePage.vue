<script setup lang="ts">
import { ref, reactive } from "vue";
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

// Fix TS7053 by explicitly typing the object structure with Record
const sixYearSchedule: Record<number, Record<string, string>> = reactive({
  1: { "2024-2025": "350, 400", "2025-2026": "101, 201, 360", "2026-2027": "320, 350, 400", "2027-2028": "201, 360, 402", "2028-2029": "101, 320, 350, 400", "2029-2030": "201, 320, 360, 402" },
  2: { "2024-2025": "320, 330", "2025-2026": "335, 360, 400", "2026-2027": "201, 330", "2027-2028": "320, 335, 360, 402", "2028-2029": "330", "2029-2030": "201, 330, 360, 402" },
  3: { "2024-2025": "400", "2025-2026": "320", "2026-2027": "320, 400", "2027-2028": "402", "2028-2029": "400", "2029-2030": "320, 402" },
  4: { "2024-2025": "330", "2025-2026": "335, 456", "2026-2027": "330", "2027-2028": "335, 456", "2028-2029": "330", "2029-2030": "335, 456" },
  5: { "2024-2025": "400", "2025-2026": "320", "2026-2027": "320, 400", "2027-2028": "402", "2028-2029": "400", "2029-2030": "320, 402" },
  6: { "2024-2025": "201, 420", "2025-2026": "101, 320, 335, 340, 400", "2026-2027": "201, 420", "2027-2028": "101, 335, 340, 402", "2028-2029": "101, 320, 420", "2029-2030": "201, 335, 340, 402" }
});

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
              <th>Outcome</th>
              <th colspan="2">Performance Indicators</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="so in soPiMap" :key="so.id">
              <td class="desc-cell">
                <strong>{{ so.id }}:</strong> {{ so.description }}
              </td>
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
      <h2>6-Year Outcome to Course Schedule Mapping</h2>
      <div class="table-container">
        <table class="data-table">
          <thead>
          <tr>
            <th>Outcome</th>
            <th v-for="year in academicYears" :key="year">{{ year }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="outcome in outcomes" :key="outcome">
            <th>{{ outcome }}</th>
            <td v-for="year in academicYears" :key="year">
              <input type="text" v-model="sixYearSchedule[outcome][year]" class="cell-input" placeholder="e.g. 350, 400" />
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
            <th>Course</th>
            <th v-for="outcome in outcomes" :key="outcome">{{ outcome }}</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="course in courses" :key="course">
            <th>{{ course }}</th>
            <td v-for="outcome in outcomes" :key="outcome">
              <input type="text" v-model="courseOutcomeMap[course][outcome]" class="cell-input" />
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- MODAL: Rubric Editor -->
    <div v-if="selectedPI" class="modal-overlay" @click.self="closeRubricModal">
      <div class="modal-content">
        <h3>{{ selectedPI.outcomeDesc }}</h3>
        <p class="pi-highlight">[ {{ selectedPI.piName }} ]</p>

        <h4>Rubric</h4>
        <div class="form-group">
          <label>Met:</label>
          <input type="text" v-model="rubricForm.met" />
        </div>
        <div class="form-group">
          <label>Below:</label>
          <input type="text" v-model="rubricForm.below" />
        </div>
        <div class="form-group">
          <label>Exceeds:</label>
          <input type="text" v-model="rubricForm.exceeds" />
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeRubricModal">Cancel</button>
          <button class="btn-save" @click="saveRubric">Save Rubric</button>
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
  color: var(--color-text-primary);
}

header h1 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
}

.mapping-section {
  margin-bottom: 3rem;
  background: var(--color-background-soft);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

h2 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
}

/* SO-PI Dropdown */
details.so-pi-dropdown summary {
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  padding: 0.5rem;
  background: var(--color-border);
  border-radius: 4px;
}

.dropdown-content {
  margin-top: 1rem;
}

.pi-cell {
  cursor: pointer;
  color: #0056b3;
  text-align: center;
  transition: background 0.2s;
}

.pi-cell:hover {
  background: #e9ecef;
  text-decoration: underline;
}

/* Tables */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.data-table th, .data-table td {
  border: 1px solid var(--color-border-dark);
  padding: 0.5rem;
  text-align: center;
}

.data-table th {
  background: #f8f9fa;
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
}

.cell-input:focus {
  outline: 2px solid #0056b3;
  background: #fdfdfd;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
}

.pi-highlight {
  font-weight: bold;
  color: #0056b3;
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
  border: 1px solid #ccc;
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
}

.btn-cancel {
  background: #e9ecef;
}

.btn-save {
  background: #0056b3;
  color: white;
}
</style>
