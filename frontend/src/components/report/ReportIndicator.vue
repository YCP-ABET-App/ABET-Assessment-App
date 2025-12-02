<template>
  <div class="indicator-section" @click.stop>

    <!-- HEADER -->
    <div
      class="indicator-header"
      :class="{ expanded: !isCollapsed(collapseId) }"
    >
      <div class="indicator-left">
        <span class="indicator-number">{{ indicator.indicatorNumber }}</span>
        <span class="course-code">{{ indicator.courseCode }}</span>

        <!-- STUDENTS IN CLASS -->
        <div class="student-inline" v-if="!isCollapsed(collapseId)">
          <template v-if="editable">
            <label class="inline-label">
              Students in Class:
              <input
                type="number"
                min="0"
                v-model.number="local.studentCount"
                @input="emitUpdate"
                class="inline-input"
              />
            </label>
          </template>

          <template v-else>
          <span class="inline-readonly">
            Students in Class: {{ indicator.studentCount }}
          </span>
          </template>
        </div>
      </div>
      <span class="collapse-icon" :class="{ open: !isCollapsed(collapseId) }" @click.stop="toggle(collapseId)">
          ▶
        </span>
    </div>

    <!-- BODY -->
    <ReportCollapseTransition :show="!isCollapsed(collapseId)">
      <div class="indicator-body">
        <div class="measures-list">
          <ReportMeasure
            v-for="m in indicator.measures"
            :key="m.measureId"
            :measure="{ ...m, studentCount: local.studentCount }"
            :editable="editable"
            @update:measure="updateMeasure(m.measureId, { ...m, ...$event })"
          />
        </div>
      </div>
    </ReportCollapseTransition>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from "vue";
import ReportMeasure from "./ReportMeasure.vue";
import ReportCollapseTransition from "./ReportCollapseTransition.vue";
import { useReportCollapse } from "@/composables/use-report-collapse";
import type { IndicatorData, IndicatorMeasure } from "@/types/summary";

const props = defineProps<{
  indicator: IndicatorData;
  editable: boolean;
  collapseId: number;
}>();

const emit = defineEmits<{
  "update:indicator": [value: IndicatorData]
}>();

const { isCollapsed, toggle } = useReportCollapse();

// Local editable copy
const local = reactive({
  studentCount: props.indicator?.studentCount ?? 0
});

// Sync local when parent changes
watch(
  () => props.indicator.studentCount,
  (val) => local.studentCount = val ?? 0
);

function emitUpdate() {
  emit("update:indicator", {
    ...props.indicator,
    studentCount: local.studentCount,
    courseCode: props.indicator.courseCode
  });
}

function updateMeasure(id: number, updated: Partial<IndicatorMeasure>) {
  emit("update:indicator", {
    ...props.indicator,
    measures: props.indicator.measures.map((m: IndicatorMeasure) =>
      m.measureId === id ? { ...m, ...updated } : m
    )
  });
}
</script>

<style scoped>
.indicator-section {
  padding: 1rem;
  background: var(--color-bg-tertiary);
  border-radius: 0.5rem;
}

.indicator-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  transition: margin .2s ease;
  margin-bottom: 0; /* default for collapsed */
}

.indicator-header.expanded {
  margin-bottom: .75rem;
}

.indicator-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.course-code {
  background: var(--color-primary);
  color: white;
  padding: .25rem .75rem;
  border-radius: .375rem;
}

.student-inline {
  display: flex;
  align-items: center;
  gap: .75rem;
  color: var(--color-text-primary);
}

.inline-label {
  display: flex;
  color: var(--color-text-primary);
  align-items: center;
  gap: .4rem;
  font-size: .9rem;
}

.inline-input {
  width: 80px;
  padding: .3rem .4rem;
  border-radius: 4px;
  border: 1px solid var(--color-border-light);
  background: var(--color-white);
}

.inline-readonly {
  color: var(--color-text-primary);
  font-size: .9rem;
}

.collapse-icon {
  transition: transform .25s ease;
  cursor: pointer;
}

.collapse-icon.open {
  transform: rotate(90deg);
}

.measures-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
