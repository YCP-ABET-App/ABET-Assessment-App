<script lang="ts" setup>
    import { ref } from 'vue'
    import { useUserStore } from '@/stores/user-store.ts'
    import { BaseButton } from '@/components/ui'
    import { BaseInput } from '@/components/ui'
    import api from '@/api';

    const userStore = useUserStore()
    const props = defineProps({measure_prop: {type: Object, required:true}})

    const isAdmin = userStore.isAdmin

    const completing = ref(false)
    const editing = ref(false)
    const deleting = ref(false)
    const rec_action = ref(false)

    function open_complete_form(){
        completing.value = true
        editing.value = false
        deleting.value = false
        rec_action.value = false
    }

    const complete_form_data = ref({
        met: '',
        exceeded: '',
        below: '',
        observation: ''
    })

    async function complete_form_submit(){
        //Check that met, exceeded, below are all ints
        let newMetVal, newExceededVal, newBelowVal
        try{
            newMetVal = parseInt(complete_form_data.value.met)
            newExceededVal = parseInt(complete_form_data.value.exceeded)
            newBelowVal = parseInt(complete_form_data.value.below)
        }
        catch{
            console.error("Met, Exceeded, and Below values must all be valid integers")
        }
        
        //Define new measure object
        const new_measure = ref({
            "id": measure_obj.value.id,
            "courseIndicatorId": measure_obj.value.course_indicator_id,
            "description": measure_obj.value.measure_description,
            "observation": complete_form_data.value.observation,
            "recommendedAction": measure_obj.value.recommended_action,
            "fcar": measure_obj.value.fcar,
            "met": newMetVal as number,
            "exceeded": newExceededVal as number,
            "below": newBelowVal as number,
            "createdAt": measure_obj.value.created_at,
            "isActive": measure_obj.value.is_active
        })

        //PUT request to server
        try {
            const { data } = await api.put(`/measure/${measure_obj.value.id}`, new_measure);

            //Update measure object
            measure_obj.value.observation = complete_form_data.value.observation
            measure_obj.value.met = newMetVal as number
            measure_obj.value.exceeded = newExceededVal as number
            measure_obj.value.below = newBelowVal as number
        } catch (error) {
            console.error('Error editing measure:', error);
        }

        //Reset form data
        complete_form_data.value = {
            met: '',
            exceeded: '',
            below: '',
            observation: ''
        }

        //Close forms
        close_forms()
    }

    function open_edit_form(){
        completing.value = false
        editing.value = true
        deleting.value = false
        rec_action.value = false
    }

    const edit_form_data = ref({
        description: ''
    })

    function edit_form_submit(){
        close_forms()
    }

    function open_delete_form(){
        completing.value = false
        editing.value = false
        deleting.value = true
        rec_action.value = false
    }

    function delete_measure(){
        close_forms()
    }

    function open_ra_form(){
        completing.value = false
        editing.value = false
        deleting.value = false
        rec_action.value = true
    }

    const ra_form_data = ref({
        recommended_action: ''
    })

    function ra_form_submit(){
        close_forms();
    }

    function close_forms(){
        completing.value = false
        editing.value = false
        deleting.value = false
        rec_action.value = false
    }

    
    const measure_obj = ref({
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
    })
    

    //-----TEST DATA-----
    /*
    const measure_obj = ref({
        id: 1,
        course_indicator_id: 1,
        measure_description: 'Students will be given an exam question to find the error in a section of code',
        observation: null,
        recommended_action: null,
        fcar: null,
        met: null,
        exceeded: null,
        below: null,
        created_at: '2025-11-06 12:06:00.880463',
        is_active: true
    })
    */
    //-------------------

    async function initialize(){
        measure_obj.value = {
            id: props.measure_prop.id,
            course_indicator_id: props.measure_prop.courseIndicatorId,
            measure_description: props.measure_prop.description,
            observation: props.measure_prop.observation,
            recommended_action: props.measure_prop.recommendedAction,
            fcar: props.measure_prop.fcar,
            met: props.measure_prop.met,
            exceeded: props.measure_prop.exceeded,
            below: props.measure_prop.below,
            created_at: props.measure_prop.createdAt,
            is_active: props.measure_prop.isActive
        }
    }

    initialize()
</script>

<template>
    <div id="m-listing-body">
        <div id="measure-box">
            <div id="description">{{ measure_obj.measure_description }}</div>
            <div v-if="measure_obj.met==null" class="complete-status">Not Completed</div>
            <div v-else class="complete-status">Completed</div>
            <div id="buttons">
                <BaseButton 
                    v-if="measure_obj.met==null && !isAdmin" 
                    variant="primary" 
                    size="sm" 
                    @click="open_complete_form">
                    Complete
                </BaseButton>
                <BaseButton 
                    v-if="measure_obj.met==null && !isAdmin" 
                    variant="primary" 
                    size="sm" 
                    @click="open_edit_form">
                    Edit
                </BaseButton>
                <BaseButton 
                    v-if="measure_obj.met!=null && isAdmin" 
                    variant="primary" 
                    size="sm" 
                    @click="open_ra_form">
                    Recommend Action
                </BaseButton>
                <BaseButton 
                    variant="primary" 
                    size="sm" 
                    @click="open_delete_form">
                    Delete
                </BaseButton>
            </div>
        </div>

        <div v-if="completing" class="form" id="complete-form">
            <h4>Complete Measure</h4>
            <div class="input-grid">
                <BaseInput
                    v-model="complete_form_data.met"
                    label="Met: "
                    placeholder="Number of students who met measure"
                    required
                />
                <BaseInput
                    v-model="complete_form_data.exceeded"
                    label="Exceeded: "
                    placeholder="Number of students who exceeded measure"
                    required
                />
                <BaseInput
                    v-model="complete_form_data.below"
                    label="Below: "
                    placeholder="Number of students who failed measure"
                    required
                />
                <BaseInput
                    v-model="complete_form_data.observation"
                    label="Observation: "
                    placeholder=""
                />
            </div>
            <BaseButton variant="primary" class="submit-button" @click="complete_form_submit">Submit</BaseButton>
        </div>

        <div v-if="editing" class="form" id="edit-form">
            <h4>Edit Measure</h4>
            <div class="input-grid">
                <BaseInput
                    v-model="edit_form_data.description"
                    label="Description: "
                    placeholder=""
                    required
                />
            </div>
            <BaseButton variant="primary" class="submit-button" @click="edit_form_submit">Submit</BaseButton>
        </div>

        <div v-if="deleting" class="form" id="delete-form">
            <h4>Delete Measure</h4>
            Are you sure you want to delete measure?
            <BaseButton variant="danger" class="submit-button" @click="delete_measure">Delete</BaseButton>
        </div>

        <div v-if="rec_action" class="form" id="delete-form">
            <div class="input-grid">
                <BaseInput
                    v-model="ra_form_data.recommended_action"
                    label="Recommended Action: "
                    placeholder=""
                    required
                />
            </div>
            <BaseButton variant="primary" class="submit-button" @click="ra_form_submit">Submit</BaseButton>
        </div>
    </div>
    
</template>

<style>
    #m-listing-body{
        background-color: rgb(43, 43, 43);
        padding-left: 1rem;
        padding-right: 1rem;
        padding-bottom: 0.5rem;
        padding-top: 0.5rem;
        margin: 0.2rem;
        border-radius: 1rem;
        text-decoration: none;
    }

    #measure-box{
        display: flex;
    }

    #buttons{
        margin-left: auto;
    }

    #buttons BaseButton{
        margin-left: 1rem;
    }

    .complete-status{
        margin-left: 2rem;
    }

    .form{
        background-color: rgb(36, 36, 36);
        padding-left: 1rem;
        padding-right: 1rem;
        padding-bottom: 0.5rem;
        padding-top: 0.5rem;
        margin: 0.2rem;
        border-radius: 1rem;
        text-decoration: none;
    }

    .submit-button{
        margin-top: 1rem;
    }

    #delete-form h4{
        margin-bottom: 0rem;
    }
    
    #delete-form BaseButton{
        margin-left: 6rem;
    }
</style>
