<script lang="ts" setup>
    import { ref } from 'vue'
    import { useUserStore } from '@/stores/user-store.ts'
    import { BaseButton } from '@/components/ui'
    import { BaseInput } from '@/components/ui'
    import api from '@/api';

    const userStore = useUserStore()
    const props = defineProps({mid: Number})

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

    function complete_form_submit(){
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
        //Fetch measure object
        try {
            const { data } = await api.get(`/measure/${props.mid}`);
            measure_obj.value = {
                id: data.data.id,
                course_indicator_id: data.data.courseIndicatorId,
                measure_description: data.data.description,
                observation: data.data.observation,
                recommended_action: data.data.recommendedAction,
                fcar: data.data.fcar,
                met: data.data.met,
                exceeded: data.data.exceeded,
                below: data.data.below,
                created_at: data.data.createdAt,
                is_active: data.data.isActive
            }
            console.log(`Measure ${props.mid} data: ${data}`)
        } catch (error) {
            console.error('Error fetching or parsing indicator data:', error);
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