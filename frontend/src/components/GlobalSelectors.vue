<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue";
import { storeToRefs } from "pinia";
import api from "@/api";
import { useUserStore } from "@/stores/user-store";
import BaseSelect from "@/components/ui/BaseSelect.vue";

interface Program {
  id: number;
  name: string;
  institution: string;
  active: boolean;
}

interface Semester {
  id: number;
  name: string;
  code: string;
  academicYear: number;
  isCurrent: boolean;
}

// Store refs
const userStore = useUserStore();
const { currentProgramId, currentSemesterId } = storeToRefs(userStore);

// Local state
const programs = ref<Program[]>([]);
const semesters = ref<Semester[]>([]);
const loading = ref(false);

const localProgramId = ref<number | null>(null);
const localSemesterId = ref<number | null>(null);

// Option lists for BaseSelect
const programOptions = computed(() =>
  programs.value.map(p => ({
    label: `${p.name} - ${p.institution}`,
    value: p.id
  }))
);

const semesterOptions = computed(() =>
  semesters.value.map(s => ({
    label: s.name,
    value: s.id
  }))
);

// Load programs on mount
onMounted(async () => {
  await loadPrograms();
});

async function loadPrograms() {
  loading.value = true;

  try {
    const res = await api.get("/program", { params: { page: 0, size: 100 } });
    programs.value = res.data?.content ?? [];

    localProgramId.value =
      currentProgramId.value ||
      programs.value[0]?.id ||
      null;

  } finally {
    loading.value = false;
  }
}

// When program changes → load semesters
watch(localProgramId, async (id) => {
  if (!id) return;

  currentProgramId.value = id;
  userStore.saveToStorage();

  const requestObj = {
    id : null,
    status : null,
    academicYear : null,
    startDate : null,
    endDate : null,
    type : null,
    name : null,
    code : null
  }

  const res = await api.get("/semesters", {
    params: requestObj
  });

  console.log(res.data.data)

  semesters.value = res.data.data ?? [];

  localSemesterId.value =
    currentSemesterId.value ||
    semesters.value.find(s => s.isCurrent)?.id ||
    semesters.value[0]?.id ||
    null;
});

// When semester changes → update store
watch(localSemesterId, (id) => {
  currentSemesterId.value = id;
  userStore.saveToStorage();
});
</script>

<template>
  <div class="selectors">
    <div class="selector-row" v-if="!loading">

      <BaseSelect
        label="Program"
        :options="programOptions"
        v-model="localProgramId"
        placeholder="Select a program"
      />

      <BaseSelect
        v-if="semesters.length > 0"
        label="Semester"
        :options="semesterOptions"
        v-model="localSemesterId"
        placeholder="Select a semester"
      />

    </div>
  </div>
</template>

<style scoped>
.selectors {
  padding: 0.75rem 1rem;
  background: var(--color-bg-secondary);
}

.selector-row {
  display: flex;
  gap: 1rem;
}
</style>
