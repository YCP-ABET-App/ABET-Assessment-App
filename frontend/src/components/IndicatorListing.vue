<script lang="ts" setup>
    import { ref } from 'vue';
    import api from '@/api';
    import MeasureListing from '@/components/MeasureListing.vue'
    import BaseButton from '@/components/ui/BaseButton.vue';
    import BaseModal from './ui/BaseModal.vue';
    import BaseInput from './ui/BaseInput.vue';

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
    const measures = ref([])

    function close_forms(){
        adding_measure.value = false
    }

    const adding_measure = ref(false)

    function open_add_form(){
        adding_measure.value = true
    }

    const add_form_data = ref({
        description: ''
    })

    async function fetch_measures(){
        try {
            const { data } = await api.get(`/measure/byIndicator/${props.piid}`);
            measures.value = data.data;
        } catch (error) {
            console.error('Error fetching or parsing indicator data:', error);
        }
    }

    async function add_form_submit(){
        //Check that met, exceeded, below are all ints
        let newDescVal = add_form_data.value.description
        
        //Define new measure object
        const new_measure = ref({
            id: null,
            courseIndicatorId: 1, //TEMPORARY! FIND A WAY TO RETRIEVE THIS
            description: newDescVal,
            observation: null,
            recommendedAction: null,
            fcar: null,
            studentsMet: null,
            studentsExceeded: null,
            studentsBelow: null,
            createdAt: null,
            active: true,
            deleted: null,
            deletedAt: null,
            new: null,
            status: "InProgress",
            updatedAt: null,
            version: null
        })
        console.log("New Measure: ")
        console.log(new_measure)

        //POST request to server
        try {
            const { data } = await api.post(`/measure`, new_measure.value);
            console.log("Response data: ")
            console.log(data)
        } catch (error) {
            console.error('Error editing measure:', error);
        }

        //Reset form data
        add_form_data.value = {
            description: ''
        }

        //Close forms
        close_forms()

        //Refresh measures
        fetch_measures()
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
        fetch_measures();
    }

    initialize()
</script>

<template>
    <div id="indicator-box">
        <div id="title"><span id="pi-label">Performance Indicator: </span>{{ indicator_obj.ind_description }}</div>
        <div id="threshold-percent-title">Threshold: <span id="threshold-percent">{{ indicator_obj.threshold_percentage }}%</span></div>
        <MeasureListing v-for="measure_obj in measures" :measure_prop="measure_obj" @refresh="fetch_measures"></MeasureListing>
        <BaseButton variant="primary" @click="open_add_form">Add Measure</BaseButton>
    </div>

    <BaseModal v-model:isOpen="adding_measure" title="Add Measure" size="md" class="form" id="complete-form">
        <div class="input-grid">
            <BaseInput
                v-model="add_form_data.description"
                label="Description: "
                placeholder=""
                required
            />
        </div>
        <BaseButton variant="primary" class="submit-button" @click="add_form_submit">Submit</BaseButton>
    </BaseModal>
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

    #indicator-box{
        text-align: left;
    }
</style>