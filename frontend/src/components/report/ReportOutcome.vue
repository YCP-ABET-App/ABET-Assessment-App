<template>
  <BaseCard variant="default" padded class="outcome-card">

    <!-- OUTCOME HEADER -->
    <div
      class="outcome-header collapse-header"
      @click="toggle(outcome.outcomeNumber)"
      :class="{ expanded: !isCollapsed(outcome.outcomeNumber) }"
    >
      <h2 class="outcome-title">
        Outcome {{ outcome.outcomeNumber }} –
        <span class="outcome-status" :class="statusClass">
          {{ outcome.overallStatus }}
        </span>
      </h2>

      <span
        class="collapse-icon"
        :class="{ open: !isCollapsed(outcome.outcomeNumber) }"
      >
        ▶
      </span>
    </div>

    <!-- OUTCOME BODY -->
    <ReportCollapseTransition :show="!isCollapsed(outcome.outcomeNumber)">
      <div class="outcome-body" @click.stop>
        <p class="outcome-description">{{ outcome.outcomeDescription }}</p>

        <!-- INDICATORS -->
        <div class="indicators-container">
          <ReportIndicator
            v-for="(ind, idx) in outcome.indicators"
            :key="`${outcome.outcomeNumber}-${idx}`"
            :indicator="ind"
            :editable="editable"
            :collapse-id="outcome.outcomeNumber * 1000 + idx"
            :student-count-label="studentCountLabel"
            @update:indicator="updateIndicator(idx, $event)"
          />
        </div>

      </div>
    </ReportCollapseTransition>

  </BaseCard>
</template>


<script setup lang="ts">
import { computed } from "vue";
import ReportIndicator from "./ReportIndicator.vue";
import ReportCollapseTransition from "./ReportCollapseTransition.vue";
import { BaseCard } from "@/components/ui";
import { useReportCollapse } from "@/composables/use-report-collapse";

const props = defineProps<{
  outcome: any;
  editable: boolean;
  studentCountLabel?: string;
}>();

const emit = defineEmits(["update:outcome"]);
const { isCollapsed, toggle } = useReportCollapse();


const statusClass = computed(() => {
  const status = props.outcome.overallStatus
    ?.toLowerCase()
    .replace(/[_-]/g, " ")
    .trim();

  if (
    status === "met" ||
    status === "met comfortably" ||
    status === "exceeds"
  ) {
    return "status-met";
  }

  if (status === "partially met") {
    return "status-partial";
  }

  if (
    status === "not met" ||
    status === "below" ||
    status === "no data"
  ) {
    return "status-not-met";
  }

  return "";
});

function updateIndicator(idx: number, updated: any) {
  const indicators = [...props.outcome.indicators];
  indicators[idx] = {...indicators[idx], ...updated};

  const allMeasures = indicators.flatMap(
    (indicator) => indicator.measures ?? []
  );

  let overallStatus = 'No Data';

  if (allMeasures.length > 0) {
    const metCount = allMeasures.filter(
      (measure) =>
        measure.status === 'Met comfortably' ||
        measure.status === 'Met'
    ).length;

    const percentage = (metCount / allMeasures.length) * 100;

    if (percentage >= 80) {
      overallStatus = 'MET';
    } else if (percentage >= 50) {
      overallStatus = 'Partially Met';
    } else {
      overallStatus = 'Not Met';
    }
  }

  emit('update:outcome', {
    ...props.outcome,
    indicators,
    overallStatus
  });
}

</script>


<style scoped>
.outcome-card {

}

.outcome-header.expanded {
  padding-bottom: 1rem;
}

.outcome-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.outcome-status {
  font-weight: 700;
  margin-left: .25rem;
}

.status-met { color: var(--color-success); }
.status-partial { color: var(--color-warning); }
.status-not-met { color: var(--color-error); }

.outcome-description {
  margin: 0 0 1.5rem 0;
  color: var(--color-text-secondary);
  font-style: italic;
}

.indicators-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.collapse-header {
  display: flex;
  justify-content: space-between;
  cursor: pointer;
}

.collapse-icon {
  transition: transform .25s ease;
}

.collapse-icon.open {
  transform: rotate(90deg);
}
</style>
