<script lang="ts" setup>
    import { ref } from 'vue';
    import { useRoute } from 'vue-router';
    import SummaryReport from '@/components/SummaryReport.vue';

    const route = useRoute();

    const program_id = ref(
      Number(route.params.program_id)
    );

    const semester_id = ref(
      Number(route.params.semester_id)
    );

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
