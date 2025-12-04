<template>
  <section class="fcar-page">
    <h1>Edit and Export FCAR</h1>

    <label>Student Outcome #:</label>
    <input v-model="form.outcome" type="text" placeholder="e.g., 1.1" />

    <label>Outcome Description:</label>
    <input
      v-model="form.outcomeDescription"
      type="text"
      placeholder="Describe the outcome..."
    />

    <label>Course:</label>
    <input
      v-model="form.course"
      type="text"
      placeholder="CS 101 - Fundamentals of Computer Science I"
    />

    <label>Work Used:</label>
    <input
      v-model="form.work"
      type="text"
      placeholder="Assessment 1, Question 5"
    />

    <label>Description:</label>
    <textarea
      v-model="form.description"
      placeholder="Briefly describe the assignment or activity used for assessment..."
    ></textarea>

    <div class="divider"></div>

    <label>Target Goal %:</label>
    <input
      v-model="form.targetGoal"
      type="text"
      placeholder="70"
    />

    <div class="row">
      <div>
        <label>Needs Improvement:</label>
        <input
          v-model="form.needsImprovement"
          type="text"
          placeholder="e.g., 5"
        />
      </div>

      <div>
        <label>Meets Expectations:</label>
        <input
          v-model="form.meetsExpectations"
          type="text"
          placeholder="10"
        />
      </div>

      <div>
        <label>Exceeds Expectations:</label>
        <input
          v-model="form.exceedsExpectations"
          type="text"
          placeholder="5"
        />
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
import { ref } from "vue";
import { useRoute } from "vue-router";
import api from "@/api";

const measureId = ref(NaN);
const route = useRoute();

const form = ref({
  outcome: "",
  outcomeDescription: "",
  indicatorDescription: "",
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
  alert("Report generated successfully.");

  const f = form.value;
  const lines = [];

  lines.push(`(${f.outcome}) ${f.outcomeDescription}`);
  lines.push("");

  if (f.course) lines.push(`Course: ${f.course}`);
  if (f.work) lines.push(`Work used: ${f.work}`);
  lines.push("");

  lines.push("Description");
  lines.push(f.description || "");
  lines.push("");

  lines.push(`Target goal: ${f.targetGoal}%`);
  lines.push("");

  const ni = parseInt(f.needsImprovement || "0");
  const me = parseInt(f.meetsExpectations || "0");
  const ee = parseInt(f.exceedsExpectations || "0");
  const total = ni + me + ee;

  const pctNI = total ? ((ni / total) * 100).toFixed(1) : "0.0";
  const pctME = total ? ((me / total) * 100).toFixed(1) : "0.0";
  const pctEE = total ? ((ee / total) * 100).toFixed(1) : "0.0";

  lines.push(`Needs improvement: ${ni}/${total} (${pctNI}%) : [${"1".repeat(ni)}]`);
  lines.push(`Meets expectations: ${me}/${total} (${pctME}%) : [${"1".repeat(me)}]`);
  lines.push(`Exceeds expectations: ${ee}/${total} (${pctEE}%) : [${"1".repeat(ee)}]`);
  lines.push("");

  const achieved = total ? (me + ee) / total * 100 >= parseFloat(f.targetGoal) : false;
  const resultText = achieved ? "achieved." : "not achieved.";
  lines.push(`Results: The target of ${f.targetGoal}% meeting or exceeding expectations was ${resultText}`);
  lines.push("");

  lines.push(`Summary/Observations: ${f.summary || ""}`);
  lines.push("");

  const reportText = lines.join("\n");

  const blob = new Blob([reportText], { type: "text/plain;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");

  const safeCourse = (f.course || "FCAR_Report").replace(/[^\w\-]+/g, "_");
  a.href = url;
  a.download = `${safeCourse}.txt`;

  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}


async function initialize() {
  measureId.value = parseInt(route.params.measure_id as string, 10);
  let measure_data;

  try {
    const { data } = await api.get(`/measure/${measureId.value}`);
    measure_data = data.data;
  } catch (error) {}

  let course_id, ind_id;
  try {
    const { data } = await api.get(
      `/courses/courseIndicator/getIds/${measure_data.courseIndicatorId}`
    );
    course_id = data.data[0];
    ind_id = data.data[1];
  } catch (error) {}

  let course_data;
  try {
    const { data } = await api.get(`/courses/${course_id}`);
    course_data = data.data;
  } catch (error) {}

  let ind_data;
  try {
    const { data } = await api.get(`/performance-indicators/${ind_id}`);
    ind_data = data.data;
  } catch (error) {}

  form.value = {
    outcome: `${ind_data.studentOutcomeId}.${ind_data.id}`,
    outcomeDescription: ind_data.outcomeDescription || ind_data.description || "",
    indicatorDescription: ind_data.lvlDescription || "",
    course: course_data.courseName,
    work: "",
    description: measure_data.description,
    targetGoal: `${ind_data.thresholdPercentage}`,
    needsImprovement: String(measure_data.studentsBelow),
    meetsExpectations: String(measure_data.studentsMet),
    exceedsExpectations: String(measure_data.studentsExceeded),
    summary: measure_data.observation,
    resultsMet: false,
  };
}

initialize();
</script>

<style scoped>
.fcar-page {
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, Helvetica, Arial,
    sans-serif;
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
textarea,
select {
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
  background: #1b5e20;
  color: white;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button:hover {
  background: #14451a;
}
</style>
