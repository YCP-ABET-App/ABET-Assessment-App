<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { useReportCollapse } from "@/composables/use-report-collapse";

import ReportOutcome from "@/components/report/ReportOutcome.vue";
import { BaseButton, BaseCard } from "@/components/ui";

const props = defineProps<{ report: any }>();

const emit = defineEmits(["update:report", "import", "save", "regenerate", "reload"]);

const localReport = reactive(JSON.parse(JSON.stringify(props.report)));

watch(
  () => props.report,
  (newVal) => Object.assign(localReport, JSON.parse(JSON.stringify(newVal))),
  { deep: true }
);

function emitLocalReport() {
  emit("update:report", JSON.parse(JSON.stringify(localReport)));
}

const editMode = ref(false);

function startEdit() { editMode.value = true; }

function cancelEdit() {
  Object.assign(localReport, JSON.parse(JSON.stringify(props.report)));
  editMode.value = false;
}

async function saveEdit() {
  editMode.value = false;
  emit("save", JSON.parse(JSON.stringify(localReport)));
}

function handleImport() { emit("import"); }

const { expandAll, collapseAll } = useReportCollapse();

function collapseIdsForAllIndicators() {
  return localReport.outcomes.flatMap((o: any) =>
    o.indicators.map((_: any, idx: number) => o.outcomeNumber * 1000 + idx)
  );
}

function expandAllOutcomes() {
  const outcomeIds = localReport.outcomes.map((o: any) => o.outcomeNumber);
  expandAll([...outcomeIds, ...collapseIdsForAllIndicators()]);
}

function collapseAllOutcomes() {
  const outcomeIds = localReport.outcomes.map((o: any) => o.outcomeNumber);
  collapseAll([...outcomeIds, ...collapseIdsForAllIndicators()]);
}

function updateOutcome(outcomeNumber: number, updated: any) {
  localReport.outcomes = localReport.outcomes.map((o: any) =>
    o.outcomeNumber === outcomeNumber ? updated : o
  );
}

function handleRegenerate() {
  emit("regenerate");
}
</script>

<template>
  <div class="unified-summary-report">

    <!-- Toolbar -->
    <div class="toolbar">
      <div class="toolbar-left">
        <BaseButton v-if="!editMode" variant="primary" @click="startEdit">Edit</BaseButton>
        <div v-if="!editMode" class="toolbar-divider"></div>
        <BaseButton v-if="editMode" variant="success" @click="saveEdit">Save</BaseButton>
        <div v-if="editMode" class="toolbar-divider"></div>
        <BaseButton v-if="editMode" variant="secondary" @click="cancelEdit">Cancel</BaseButton>
        <div v-if="editMode" class="toolbar-divider"></div>
        <BaseButton variant="danger" @click="handleRegenerate">Regenerate Report</BaseButton>
      </div>

      <div class="toolbar-right">
        <BaseButton variant="primary" @click="handleImport">Import Summary</BaseButton>
      </div>
    </div>

    <!-- Collapse Controls -->
    <div class="collapse-controls">
      <BaseButton variant="primary" size="sm" @click="expandAllOutcomes">Expand All</BaseButton>
      <BaseButton variant="primary" size="sm" @click="collapseAllOutcomes">Collapse All</BaseButton>
    </div>

    <!-- Header -->
    <BaseCard>
      <h2>Summary Results {{ localReport.academicYear }}</h2>
      <p class="header-meta">
        {{ localReport.generatedDate }} â€”
        {{ localReport.generatedBy.join(", ") }}
      </p>
    </BaseCard>

    <!-- Outcomes -->
    <div v-if="localReport.outcomes && localReport.outcomes.length > 0" class="outcomes-container">
      <ReportOutcome
        v-for="o in localReport.outcomes"
        :key="o.outcomeNumber"
        :outcome="o"
        :editable="editMode"
        @update:outcome="updateOutcome(o.outcomeNumber, $event)"
      />
    </div>

    <!-- No outcomes message -->
    <BaseCard v-else>
      <p>No outcome data available in this report.</p>
    </BaseCard>

  </div>
</template>

<style scoped>
.unified-summary-report {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-bottom: 2rem;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  background: var(--color-bg-tertiary);
  padding: .75rem 1rem;
  border: 1px solid var(--color-border-light);
  border-radius: .5rem;
}

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 0.5rem;
}

.toolbar-divider {
  width: 1px;
  height: 1.5rem;
}

.collapse-controls {
  display: flex;
  justify-content: flex-end;
  gap: .75rem;
}

.outcomes-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
