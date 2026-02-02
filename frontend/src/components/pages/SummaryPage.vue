<script lang="ts" setup>
    import { ref } from 'vue';
    import { useRoute } from 'vue-router';
    import SummaryReport from '@/components/SummaryReport.vue';

    const route = useRoute();

    const program_id = ref(NaN);
    const semester_id = ref(NaN);

    async function initialize(){
        program_id.value = parseInt(route.params.program_id as string, 10)
        semester_id.value = parseInt(route.params.semester_id as string, 10)
    }

    initialize();
</script>

<template>
    <div class="reportContainer">
        <SummaryReport
            :program-id="program_id"
            :semester-id="semester_id"
            :show-semester-selector="false"
            :show-export-button="true"
        />
    </div>
</template>

<style scoped>
.reportContainer{
    margin: 4rem;
}
</style>