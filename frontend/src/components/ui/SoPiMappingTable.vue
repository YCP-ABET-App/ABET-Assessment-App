<script setup lang="ts">
import { ref } from 'vue';
import { useScheduleStore } from '@/stores/scheduleStore';
import { useScheduleModals } from '@/composables/useScheduleModals';

const store = useScheduleStore();
const { openRubricModal } = useScheduleModals();
const isSoPiDropdownOpen = ref(false);

function handleDropdownToggle(event: Event) {
  const target = event.target as HTMLDetailsElement;
  isSoPiDropdownOpen.value = target.open;
}
</script>

<template>
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
          <tr v-for="so in store.soPiMap" :key="so.id">
            <td class="so-num-cell">{{ so.id }}</td>
            <td class="desc-cell">{{ so.description }}</td>
            <td
              v-for="pi in so.pis"
              :key="pi.name"
              class="pi-cell"
              @click="openRubricModal(so, pi)"
              :title="pi.definition"
            >
              {{ pi.name }}
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </details>
  </section>
</template>

<style scoped>
.mapping-section {
  background: #282828;
  padding: 1.5rem;
  border-radius: 6px;
  border: 1px solid #383838;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  margin-bottom: 2rem;
}

details.so-pi-dropdown summary {
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  padding: 0.85rem 1rem;
  background: #2d2d2d;
  border: 1px solid #4a4a4a;
  border-radius: 4px;
  color: #ffffff;
  text-align: center;
}

details.so-pi-dropdown summary:hover {
  background: #353535;
}

.dropdown-content {
  margin-top: 1.25rem;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #181818;
}

.data-table th,
.data-table td {
  border: 1px solid #333333;
  padding: 0.85rem 1rem;
  color: #ffffff;
  font-size: 0.9rem;
}

.col-so-num { width: 70px; }
.so-num-cell { font-weight: 700; text-align: center; }
.desc-cell { text-align: left !important; }

.pi-cell {
  cursor: pointer;
  color: #e2e8f0;
  text-align: center;
  transition: background 0.15s ease;
}

.pi-cell:hover {
  background: #2a2a2a;
  color: #4ade80;
  text-decoration: underline;
}
</style>
