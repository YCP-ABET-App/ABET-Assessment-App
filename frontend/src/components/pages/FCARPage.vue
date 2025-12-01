<template>
  <section class="fcar-page">
    <h1>Edit and Export FCAR</h1>

    <label>Student Outcome #:</label>
    <input v-model="form.outcome" type="text" placeholder="e.g., 1.1" />

    <label>Course:</label>
    <input v-model="form.course" type="text" placeholder="CS 101 - Fundamentals of Computer Science I" />

    <label>Work Used:</label>
    <input v-model="form.work" type="text" placeholder="Assessment 1, Question 5" />

    <label>Description:</label>
    <textarea v-model="form.description" placeholder="Briefly describe the assignment or activity used for assessment..."></textarea>

    <div class="divider"></div>

    <label>Target Goal:</label>
    <input
      v-model="form.targetGoal"
      type="text"
      placeholder="70% of students meeting or exceeding expectations"
    />

    <div class="row">
      <div>
        <label>Needs Improvement proportion:</label>
        <input v-model="form.needsImprovement" type="text" placeholder="e.g., 5/15 (33%)" />
      </div>

      <div>
        <label>Meets Expectations:</label>
        <input v-model="form.meetsExpectations" type="text" placeholder="10/15 (66%)" />
      </div>

      <div>
        <label>Exceeds Expectations:</label>
        <input v-model="form.exceedsExpectations" type="text" placeholder="5/15 (33%)" />
      </div>
    </div>

    <div class="divider"></div>

    <label>Summary & Observations:</label>
    <textarea
      v-model="form.summary"
      placeholder="Summarize findings, trends, and recommendations..."
    ></textarea>

    <button @click="generateReport">Generate Report</button>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';

const measureId = ref(NaN)
const route = useRoute()

const form = ref({
  outcome: "",
  course: "",
  work: "",
  description: "",
  targetGoal: "",
  needsImprovement: "",
  meetsExpectations: "",
  exceedsExpectations: "",
  summary: "",
  resultsMet: false,
});

function makeTallies(text: string) {
  const match = text.match(/^(\d+)/);
  if (!match) return "";
  const n = parseInt(match[1]);
  return "1".repeat(Math.min(n, 15));
}

function generateReport() {
  alert("Report generated successfully (placeholder function).");

  const formValue = form.value;

  const lines = [];
  lines.push(`(${formValue.outcome || ""})`);
  lines.push("");

  if (formValue.course) lines.push(`Course: ${formValue.course}`);
  if (formValue.work) lines.push(`Work used: ${formValue.work}`);
  lines.push("");

  lines.push("Description");
  lines.push(formValue.description || "");
  lines.push("");

  lines.push(`Target goal: ${formValue.targetGoal}`);
  lines.push("");

  lines.push(
    `Needs improvement: ${formValue.needsImprovement} : [${makeTallies(
      formValue.needsImprovement
    )}]`
  );

  lines.push(
    `Meets expectations: ${formValue.meetsExpectations} : [${makeTallies(
      formValue.meetsExpectations
    )}]`
  );

  lines.push(
    `Exceeds expectations: ${formValue.exceedsExpectations} : [${makeTallies(
      formValue.exceedsExpectations
    )}]`
  );
  lines.push("");

  const resultLine = formValue.targetGoal
    ? `Results: The target of ${formValue.targetGoal} was ${
        formValue.resultsMet ? "achieved." : "not achieved."
      }`
    : "Results: Target goal data unavailable.";
  lines.push(resultLine);
  lines.push("");

  lines.push(`Summary/Observations: ${formValue.summary}`);
  lines.push("");

  const reportText = lines.join("\n");

  const blob = new Blob([reportText], { type: "text/plain;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");

  const safeCourse = (formValue.course || "FCAR_Report").replace(/[^\w\-]+/g, "_");
  a.href = url;
  a.download = `${safeCourse}.txt`;

  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

async function initialize(){
  measureId.value = parseInt(route.params.measure_id as string, 10)
  
  //Fetch measure data
  try {
    const { data } = await api.get(`/measure/${measureId.value}`);
    form.value = {
      outcome: data.data.outcome,
      course: "",
      work: "",
      description: data.data.description,
      targetGoal: "",
      needsImprovement: data.data.studentsBelow,
      meetsExpectations: data.data.studentsMet,
      exceedsExpectations: data.data.studentsExceeded,
      summary: data.data.observation,
      resultsMet: false,
    };
  } catch (error) {
    console.error('Error fetching or parsing measure data:', error);
  }
}

initialize()
</script>

<style scoped>
.fcar-page {
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  max-width: 750px;
  margin: auto;
  padding: 1.5rem;
}

label {
  margin-top: 1rem;
  display: block;
  font-weight: 600;
}

input,
textarea {
  width: 100%;
  margin-top: 0.25rem;
  padding: 0.5rem;
  border: 1px solid #bbb;
  border-radius: 6px;
  font-size: 1rem;
}

textarea {
  min-height: 80px;
}

.row {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.divider {
  height: 2px;
  background: #ddd;
  margin: 1.5rem 0;
}

button {
  margin-top: 1.5rem;
  padding: 0.75rem 1.25rem;
  background: #1B5E20; /* DARK GREEN */
  color: white;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button:hover {
  background: #14451A; /* DARKER GREEN */
}
</style>

