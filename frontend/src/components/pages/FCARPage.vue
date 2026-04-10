<script lang="ts" setup>
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import api from "@/api";
import { BaseCard } from "@/components/ui";

const route = useRoute();
const measure_id = ref(NaN);

const form = ref({
  course_display: "",
  outcome_code: "",
  outcome_description: "",
  performance_indicator_description: "",
  work_used: "",
  activity_description: "",
  target_goal: "",
  count_below: "0",
  count_met: "0",
  count_exceeded: "0",
  summary_observations: "",
  outcome_evaluation: "",
});

const suggestedEvaluation = computed(() => {
  const ni = parseInt(form.value.count_below || "0");
  const me = parseInt(form.value.count_met || "0");
  const ee = parseInt(form.value.count_exceeded || "0");
  const total = ni + me + ee;
  const target = parseFloat(form.value.target_goal || "0");

  if (total === 0 || isNaN(target)) return null;
  const achievedPct = ((me + ee) / total) * 100;

  if (achievedPct >= target + 15) return "Met Comfortably";
  if (achievedPct >= target) return "Met";
  if (achievedPct >= target - 5) return "Barely Not Met";
  return "Not Met";
});

async function fetch_fcar_data() {
  try {
    const { data: mRes } = await api.get(`/measure/${measure_id.value}`);
    const measure = mRes.data;

    const { data: idRes } = await api.get(`/courses/courseIndicator/getIds/${measure.courseIndicatorId}`);
    const [course_id, ind_id] = idRes.data;

    const iRes = await api.get(`/performance-indicators/${ind_id}`);
    const pi = iRes.data.data;

    const [cRes, resultsRes, outcomeRes] = await Promise.all([
      api.get(`/courses/${course_id}`),
      api.get(`/measure-result`, { params: { measureId: measure_id.value } }),
    api.get(`/outcome/${pi.studentOutcomeId}`)
    ]);

    const course = cRes.data.data;
    const raw = resultsRes.data.data;
    const outcome = outcomeRes.data.data;
    const result = Array.isArray(raw) ? raw[0] : raw;

    form.value = {
      course_display: `${course.courseCode} - ${course.courseName}`,
      outcome_code: `${pi.studentOutcomeId}.${pi.indicatorNumber}`,
      outcome_description: outcome.out_description || outcome.description || "",
      performance_indicator_description: pi.description || "",
      work_used: measure.workUsed || "",
      activity_description: measure.description || "",
      target_goal: (pi.thresholdPercentage == null) ? "" : String(pi.thresholdPercentage),
      count_below: String(result?.studentsBelow || 0),
      count_met: String(result?.studentsMet || 0),
      count_exceeded: String(result?.studentsExceeded || 0),
      summary_observations: result?.observation || "",
      outcome_evaluation: "",
    };
  } catch (error: any) {
    console.error("Critical Fetch Error:", error);
    console.error("Failed URL:", error?.config?.url);
  }
}

function generateReport() {
  if (!form.value.outcome_evaluation) {
    alert("Please select an Outcome Evaluation.");
    return;
  }

  const f = form.value;
  const ni = parseInt(f.count_below || "0");
  const me = parseInt(f.count_met || "0");
  const ee = parseInt(f.count_exceeded || "0");
  const total = ni + me + ee;

  const pct = (val: number) => total ? ((val / total) * 100).toFixed(1) : "0.0";

  const content = [
    `FCAR REPORT - ${f.course_display}`,
    `------------------------------------------`,
    `Performance Indicator: ${f.performance_indicator_description}`,
    `Outcome: (${f.outcome_code}) ${f.outcome_description}`,
    `Work Used: ${f.work_used}`,
    `Activity Description: ${f.activity_description}`,
    `\nTarget Goal: ${f.target_goal}%`,
    `Actual Performance:`,
    `- Below: ${ni}/${total} (${pct(ni)}%)`,
    `- Met: ${me}/${total} (${pct(me)}%)`,
    `- EXCEEDED: ${ee}/${total} (${pct(ee)}%)`,
    `\nFINAL EVALUATION: ${f.outcome_evaluation}`,
    `OBSERVATIONS: ${f.summary_observations || "None"}`
  ].join("\n");

  const blob = new Blob([content], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `FCAR_${f.outcome_code.replace(/\./g, '_')}.txt`;
  a.click();
}

async function initialize() {
  measure_id.value = parseInt(route.params.measure_id as string, 10);
  await fetch_fcar_data();
}

initialize();
</script>

<template>
  <section class="fcar-page">

    <div class="page-header">
      <div class="header-content">
        <div class="course-title">
          <div id="codes">
            <span id="course-code">FCAR Generation</span>
          </div>
          <div id="course-name">{{ form.course_display }}</div>
        </div>
        <p class="subtitle">Review and Edit Assessment Data</p>
      </div>
    </div>

    <div class="fcar-content">

      <section class="detail-section">
        <h3>Course & Outcome Information</h3>
        <BaseCard variant="default" class="form-card">
          <div class="info-grid">
            <div class="info-field">
              <span class="field-label">Student Outcome</span>
              <input v-model="form.outcome_code" class="styled-input" />
            </div>

            <div class="info-field">
              <span class="field-label">Performance Indicator Description</span>
              <textarea v-model="form.performance_indicator_description" rows="2" class="styled-input"></textarea>
            </div>

            <div class="info-field">
              <span class="field-label">Outcome Description</span>
              <textarea v-model="form.outcome_description" rows="2" class="styled-input"></textarea>
            </div>
          </div>
        </BaseCard>
      </section>

      <section class="detail-section">
        <h3>Assessment</h3>
        <BaseCard variant="default" class="form-card">
          <div class="info-grid">
            <div class="info-field">
              <span class="field-label">Work Used</span>
              <input v-model="form.work_used" class="styled-input" />
            </div>
            <div class="info-field">
              <span class="field-label">Activity Description</span>
              <textarea v-model="form.activity_description" rows="3" class="styled-input"></textarea>
            </div>
          </div>
        </BaseCard>
      </section>

      <section class="detail-section">
        <h3>Quantitative Results</h3>
        <BaseCard variant="default" class="form-card">
          <div class="info-row stats-row">
            <div class="info-field">
              <span class="field-label">Target Goal %</span>
              <input
                v-model="form.target_goal"
                class="styled-input"
                placeholder="--"
              />
            </div>
            <div class="info-field">
              <span class="field-label">Below</span>
              <input v-model="form.count_below" class="styled-input" />
            </div>
            <div class="info-field">
              <span class="field-label">Met</span>
              <input v-model="form.count_met" class="styled-input" />
            </div>
            <div class="info-field">
              <span class="field-label">Exceeded</span>
              <input v-model="form.count_exceeded" class="styled-input" />
            </div>
          </div>
        </BaseCard>
      </section>

      <section class="detail-section">
        <h3>Final Evaluation & Observations</h3>
        <BaseCard variant="elevated" class="evaluation-card">
          <div class="info-grid">
            <div class="info-field">
              <span class="field-label">Outcome Evaluation</span>
              <select v-model="form.outcome_evaluation" class="styled-select">
                <option value="" disabled>Select Result</option>
                <option value="Not Met">Not Met</option>
                <option value="Barely Not Met">Barely Not Met</option>
                <option value="Met">Met</option>
                <option value="Met Comfortably">Met Comfortably</option>
              </select>
              <p v-if="suggestedEvaluation" class="suggestion">
                Suggested based on data: <strong>{{ suggestedEvaluation }}</strong>
              </p>
            </div>

            <div class="info-field">
              <span class="field-label">Summary & Observations</span>
              <textarea v-model="form.summary_observations" rows="4" class="styled-input"></textarea>
            </div>
          </div>
        </BaseCard>
      </section>

      <div class="action-footer">
        <button class="btn-primary" @click="generateReport">
          Generate FCAR Report (.txt)
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.fcar-page {
  padding: 2rem;
  max-width: 1100px;
  margin: 0 auto;
  background-color: #121212;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 2rem;
}

.course-title {
  color: #ffffff;
  font-size: 2rem;
  font-weight: 700;
}

#course-name {
  margin-bottom: 1rem;
  color: #ffffff;
  font-size: 2rem;
  font-weight: 700;
}

#course-code {
  margin: 0;
  color: #4caf50;
  font-size: 2rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #b0b0b0;
  font-size: 1rem;
}

.detail-section {
  margin-top: 2.5rem;
}

.detail-section h3 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  color: #ffffff;
  border-bottom: 2px solid #333;
  padding-bottom: 0.5rem;
}

.form-card {
  padding: 1.5rem;
  background: #1e1e1e !important;
  border: 1px solid #333 !important;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.stats-row {
  grid-template-columns: repeat(4, 1fr);
}

.info-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-label {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: white;
}

.styled-input, .styled-select {
  background: #2c2c2c;
  border: 1px solid #444;
  border-radius: 0.375rem;
  padding: 0.75rem;
  color: #ffffff;
  font-size: 1rem;
  width: 100%;
}

.styled-input:focus {
  outline: none;
  border-color: #4caf50;
}

.evaluation-card {
  background: #252525 !important;
  border-left: 4px solid #4caf50 !important;
  padding: 1.5rem;
}

.suggestion {
  font-size: 0.85rem;
  margin-top: 0.5rem;
  color: #81c784;
}

.action-footer {
  margin-top: 3rem;
  display: flex;
  justify-content: center;
  padding-bottom: 5rem;
}

.btn-primary {
  background: #4caf50;
  color: white;
  border: none;
  padding: 1rem 3rem;
  border-radius: 0.375rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #388e3c;
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
