<script lang="ts" setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import MultiYearReport from '@/components/MultiYearReport.vue';

const route = useRoute();

const program_id = ref(NaN);

function initialize() {
    program_id.value = parseInt(route.params.program_id as string, 10);
}

initialize();
</script>

<template>
    <div class="reportContainer">
        <MultiYearReport :program-id="program_id" />
    </div>
</template>

<style scoped>
.reportContainer {
    margin: 4rem;
}
</style>