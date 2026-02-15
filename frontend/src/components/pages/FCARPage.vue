<template>
  <section class="fcar-page">
    <h1>Edit and Export FCAR</h1>

    <div class="form-group">
      <label>Student Outcome #:</label>
      <input v-model="form.outcome" type="text" placeholder="e.g., 1.1" />

      <label>Outcome Description:</label>
      <input v-model="form.outcomeDescription" type="text" placeholder="Describe the outcome..." />

      <label>Course:</label>
      <input v-model="form.course" type="text" />

      <label>Work Used:</label>
      <input v-model="form.work" type="text" placeholder="e.g., Exam 2, Question 3" />

      <label>Description:</label>
      <textarea v-model="form.description" placeholder="Briefly describe the activity..."></textarea>
    </div>

    <div class="divider"></div>

    <div class="stats-section">
      <label>Target Goal %:</label>
      <input v-model="form.targetGoal" type="text" placeholder="70" />

      <div class="row">
        <div>
          <label>Needs Improvement:</label>
          <input v-model="form.needsImprovement" type="text" />
        </div>
        <div>
          <label>Meets Expectations:</label>
          <input v-model="form.meetsExpectations" type="text" />
        </div>
        <div>
          <label>Exceeds Expectations:</label>
          <input v-model="form.exceedsExpectations" type="text" />
        </div>
      </div>
    </div>

    <div class="divider"></div>

    <div class="evaluation-card">
      <label>Outcome Evaluation (Manual Selection):</label>
      <select v-model="form.outcomeEvaluation" class="eval-select">
        <option value="" disabled>-- Select Evaluation --</option>
        <option value="Not Met">Not Met</option>
        <option value="Barely Not Met">Barely Not Met</option>
        <option value="Met">Met</option>
        <option value="Met Comfortably">Met Comfortably</option>
      </select>
      
      <p v-if="suggestedEvaluation" class="suggestion-text">
        Calculation Suggestion: <strong>{{ suggestedEvaluation }}</strong>
      </p>
    </div>

    <label>Summary & Observations:</label>
    <textarea v-model="form.summary" placeholder="Summarize findings, trends, and recommendations..."></textarea>

    <button class="generate-btn" @click="generateReport">Generate Report</button>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import api from "@/api";

const route = useRoute();
const measureId = ref(NaN);

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
  outcomeEvaluation: "", // Manual choice
});

// Logic to suggest an evaluation based on the Target Goal vs Actual Data
const suggestedEvaluation = computed(() => {
  const ni = parseInt(form.value.needsImprovement || "0");
  const me = parseInt(form.value.meetsExpectations || "0");
  const ee = parseInt(form.value.exceedsExpectations || "0");
  const total = ni + me + ee;
  const target = parseFloat(form.value.targetGoal || "0");

  if (total === 0 || isNaN(target)) return null;
  
  const achievedPct = ((me + ee) / total) * 100;

  if (achievedPct >= target + 15) return "Met Comfortably";
  if (achievedPct >= target) return "Met";
  if (achievedPct >= target - 5) return "Barely Not Met";
  return "Not Met";
});

function generateReport() {
  if (!form.value.outcomeEvaluation) {
    alert("Please select an Outcome Evaluation from the dropdown before generating the report.");
    return;
  }

  const f = form.value;
  const lines = [];

  lines.push(`FCAR REPORT - ${f.course}`);
  lines.push(`Outcome: (${f.outcome}) ${f.outcomeDescription}`);
  lines.push(`Work Used: ${f.work}`);
  lines.push(`Description: ${f.description}`);
  lines.push("");

  const ni = parseInt(f.needsImprovement || "0");
  const me = parseInt(f.meetsExpectations || "0");
  const ee = parseInt(f.exceedsExpectations || "0");
  const total = ni + me + ee;

  const pctNI = total ? ((ni / total) * 100).toFixed(1) : "0.0";
  const pctME = total ? ((me / total) * 100).toFixed(1) : "0.0";
  const pctEE = total ? ((ee / total) * 100).toFixed(1) : "0.0";

  lines.push(`Target Goal: ${f.targetGoal}%`);
  lines.push(`Actual Performance:`);
  lines.push(`- Needs Improvement: ${ni}/${total} (${pctNI}%)`);
  lines.push(`- Meets Expectations: ${me}/${total} (${pctME}%)`);
  lines.push(`- Exceeds Expectations: ${ee}/${total} (${pctEE}%)`);
  lines.push("");

  lines.push(`FINAL EVALUATION: ${f.outcomeEvaluation}`);
  lines.push(`OBSERVATIONS: ${f.summary || "None"}`);

  const reportText = lines.join("\n");
  const blob = new Blob([reportText], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `FCAR_${f.outcome.replace(/\./g, '_')}.txt`;
  a.click();
}

async function initialize() {
  measureId.value = parseInt(route.params.measure_id as string, 10);
  
  try {
    const { data: mRes } = await api.get(`/measure/${measureId.value}`);
    const measure_data = mRes.data;

    const { data: idRes } = await api.get(`/courses/courseIndicator/getIds/${measure_data.courseIndicatorId}`);
    const [course_id, ind_id] = idRes.data;

    const { data: cRes } = await api.get(`/courses/${course_id}`);
    const { data: iRes } = await api.get(`/performance-indicators/${ind_id}`);

    form.value = {
      outcome: `${iRes.data.studentOutcomeId}.${iRes.data.id}`,
      outcomeDescription: iRes.data.outcomeDescription || iRes.data.description || "",
      indicatorDescription: iRes.data.lvlDescription || "",
      course: cRes.data.courseName,
      work: "",
      description: measure_data.description,
      targetGoal: `${iRes.data.thresholdPercentage}`,
      needsImprovement: String(measure_data.studentsBelow),
      meetsExpectations: String(measure_data.studentsMet),
      exceedsExpectations: String(measure_data.studentsExceeded),
      summary: measure_data.observation,
      outcomeEvaluation: "", // Forced manual selection
    };
  } catch (error) {
    console.error("Failed to load FCAR data", error);
  }
}

initialize();
</script>

<style scoped>
.fcar-page {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

label {
  display: block;
  font-weight: 700;
  margin-top: 1.2rem;
  color: #333;
}

input, textarea, select {
  width: 100%;
  padding: 0.6rem;
  margin-top: 0.4rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.row {
  display: flex;
  gap: 1rem;
}

.divider {
  height: 1px;
  background: #eee;
  margin: 2rem 0;
}

.evaluation-card {
  background: #f4f7f4;
  padding: 1.5rem;
  border-left: 5px solid #1b5e20;
  margin-bottom: 1.5rem;
}

.eval-select {
  border: 2px solid #1b5e20;
  font-weight: 600;
}

.suggestion-text {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.generate-btn {
  background: #1b5e20;
  color: white;
  border: none;
  padding: 1rem 2rem;
  font-size: 1.1rem;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
}

.generate-btn:hover {
  background: #14451a;
}
</style>