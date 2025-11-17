<script lang="ts" setup>
    import { ref } from 'vue';
    import api from '@/api';
    import MeasureListing from '@/components/MeasureListing.vue'
    import BaseButton from '@/components/ui/BaseButton.vue';

    const props = defineProps({piid: Number})

    const indicator_obj = ref({
        id: NaN,
        ind_number: NaN,
        ind_value: NaN,
        ind_description: '',
        evaluation: '',
        student_outcome_id: NaN,
        threshold_percentage: NaN,
        created_at: '',
        is_active: false
    })
    const measures = ref([1,2])

    const adding_measure = ref(false)
    const add_measure_form_data = ref({
        description: ''
    })

    function submit_add_measure(){
        const new_measure = {
            id: NaN,
            course_indicator_id: NaN,
            measure_description: '',
            observation: '',
            recommended_action: '',
            fcar: '',
            met: NaN,
            exceeded: NaN,
            below: NaN,
            created_at: '',
            is_active: false
        }
    }

    //-----TEST DATA-----
    /*
    const indicator_obj = ref({
        id: 1,
        ind_number: 1,
        ind_value: null,
        ind_description: 'Identify and formulate computing problems',
        evaluation: null,
        student_outcome_id: 1,
        threshold_percentage: 75.00,
        created_at: '2025-11-06 12:06:00.872447	',
        is_active: true
    })
    const measures = ref([1,2])
    */
    //-------------------

    async function initialize(){
        //Fetch indicator object
        try {
            const { data } = await api.get(`/performance-indicators/${props.piid}`);
            indicator_obj.value = {
                id: data.data.id,
                ind_number: data.data.indicatorNumber,
                ind_value: data.data.indicatorValue,
                ind_description: data.data.description,
                evaluation: data.data.evaluation,
                student_outcome_id: data.data.studentOutcomeId,
                threshold_percentage: data.data.thresholdPercentage,
                created_at: data.data.createdAt,
                is_active: data.data.isActive
            }
        } catch (error) {
            console.error('Error fetching or parsing indicator data:', error);
        }
        
        //Fetch measures
        /*
        try {
            const { data } = await api.get(`/measure/byIndicator/${props.piid}`);
            measures.value = data;
        } catch (error) {
            console.error('Error fetching or parsing indicator data:', error);
        }*/
    }

    initialize()
</script>

<template>
    <div id="indicator_box">
        <div id="title"><span id="pi-label">Performance Indicator: </span>{{ indicator_obj.ind_description }}</div>
        <div id="threshold-percent-title">Threshold: <span id="threshold-percent">{{ indicator_obj.threshold_percentage }}%</span></div>
        <MeasureListing v-for="measure_id in measures" :mid="measure_id"></MeasureListing>
        <BaseButton variant="primary">Add Measure</BaseButton>
    </div>
</template>

<style>
    #title{
        font-size: 24px;
    }

    #pi-label{
        color: green;
    }

    #threshold-percent-title{
        color: lightgray;
        margin-bottom: 1rem;
    }
</style>