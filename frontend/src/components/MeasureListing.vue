<script lang="ts" setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user-store.ts'
import { BaseButton } from '@/components/ui'
import { BaseInput } from '@/components/ui'
import { BaseModal } from '@/components/ui'
import api from '@/api';

const userStore = useUserStore()
const props = defineProps({measure_prop: {type: Object, required:true}, instructor_id: Number})
const emits = defineEmits(["refresh"])

const isAdmin = userStore.isAdmin
const userId = userStore.userId
const isInstructor = ref(false)

const completing = ref(false)
const editing = ref(false)
const deleting = ref(false)
const rec_action = ref(false)
const viewing = ref(false)

const has_chart_data = ref(false)
const below_percent = ref(NaN)
const met_percent = ref(NaN)

function open_complete_form(){
  completing.value = true
  editing.value = false
  deleting.value = false
  rec_action.value = false
  viewing.value = false
}

interface Measure {
  id: number
  courseIndicatorId: number
  description: string
  observation: string | null
  recommendedAction: string | null
  fcar: string | null
  studentsMet: number | null
  studentsExceeded: number | null
  studentsBelow: number | null
  createdAt: string
  active: boolean
  deleted: boolean | null
  deletedAt: string | null
  new: boolean | null
  status: "InProgress" | "Complete" | null
  updatedAt: string | null
  version: number | null
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
    return
  }

  //Define new measure object
  const new_measure = ref({
    id: measure_obj.value.id,
    courseIndicatorId: measure_obj.value.course_indicator_id,
    description: measure_obj.value.measure_description,
    observation: complete_form_data.value.observation,
    recommendedAction: measure_obj.value.recommended_action,
    studentsMet: newMetVal as number,
    studentsExceeded: newExceededVal as number,
    studentsBelow: newBelowVal as number,
    createdAt: measure_obj.value.created_at,
    active: measure_obj.value.is_active,
    deleted: measure_obj.value.deleted,
    deletedAt: measure_obj.value.deleted_at,
    new: measure_obj.value.new,
    status: "Complete",
    updatedAt: measure_obj.value.updated_at,
    version: measure_obj.value.version
  })

  //PUT request to server
  try {
    const { data } = await api.put(`/measure/${measure_obj.value.id}`, new_measure.value);

    //Update measure object
    measure_obj.value.observation = complete_form_data.value.observation
    measure_obj.value.met = newMetVal as number
    measure_obj.value.exceeded = newExceededVal as number
    measure_obj.value.below = newBelowVal as number
    measure_obj.value.status = "Complete"
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

  //Refresh measures
  set_status()
  calculate_chart_data()
  emits('refresh')
}

function open_edit_form(){
  completing.value = false
  editing.value = true
  deleting.value = false
  rec_action.value = false
  viewing.value = false
}

const edit_form_data = ref({
  description: ''
})

async function edit_form_submit(){
  //Check that met, exceeded, below are all ints
  let newDescVal = edit_form_data.value.description

  //Define new measure object
  const new_measure = ref({
    id: measure_obj.value.id,
    courseIndicatorId: measure_obj.value.course_indicator_id,
    description: newDescVal,
    observation: measure_obj.value.observation,
    recommendedAction: measure_obj.value.recommended_action,
    studentsMet: measure_obj.value.met,
    studentsExceeded: measure_obj.value.exceeded,
    studentsBelow: measure_obj.value.below,
    createdAt: measure_obj.value.created_at,
    active: measure_obj.value.is_active,
    deleted: measure_obj.value.deleted,
    deletedAt: measure_obj.value.deleted_at,
    new: measure_obj.value.new,
    status: measure_obj.value.status,
    updatedAt: measure_obj.value.updated_at,
    version: measure_obj.value.version
  })

  //PUT request to server
  try {
    const { data } = await api.put(`/measure/${measure_obj.value.id}`, new_measure.value);

    //Update measure object
    measure_obj.value.measure_description = edit_form_data.value.description
  } catch (error) {
    console.error('Error editing measure:', error);
  }

  //Reset form data
  edit_form_data.value = {
    description: ''
  }

  //Close forms
  close_forms()

  //Refresh measures
  set_status()
  emits('refresh')
}

function open_delete_form(){
  completing.value = false
  editing.value = false
  deleting.value = true
  rec_action.value = false
  viewing.value = false
}

async function delete_measure(){
  //DELETE request to server
  try {
    const { data } = await api.delete(`/measure/${measure_obj.value.id}`);

    //Update measure object
    measure_obj.value.recommended_action = ra_form_data.value.recommended_action
  } catch (error) {
    console.error('Error deleting measure:', error);
  }

  close_forms()

  //Refresh measures
  emits('refresh')
}

function open_ra_form(){
  completing.value = false
  editing.value = false
  deleting.value = false
  rec_action.value = true
  viewing.value = false
}

const ra_form_data = ref({
  recommended_action: ''
})

async function ra_form_submit(){
  //Check that met, exceeded, below are all ints
  let newRAVal = ra_form_data.value.recommended_action

  //Define new measure object
  const new_measure = ref({
    id: measure_obj.value.id,
    courseIndicatorId: measure_obj.value.course_indicator_id,
    description: measure_obj.value.measure_description,
    observation: measure_obj.value.observation,
    recommendedAction: newRAVal,
    studentsMet: measure_obj.value.met,
    studentsExceeded: measure_obj.value.exceeded,
    studentsBelow: measure_obj.value.below,
    createdAt: measure_obj.value.created_at,
    active: measure_obj.value.is_active,
    deleted: measure_obj.value.deleted,
    deletedAt: measure_obj.value.deleted_at,
    new: measure_obj.value.new,
    status: "Complete",
    updatedAt: measure_obj.value.updated_at,
    version: measure_obj.value.version
  })

  //PUT request to server
  try {
    const { data } = await api.put(`/measure/${measure_obj.value.id}`, new_measure.value);

    //Update measure object
    measure_obj.value.recommended_action = ra_form_data.value.recommended_action
  } catch (error) {
    console.error('Error editing measure:', error);
  }

  //Reset form data
  ra_form_data.value = {
    recommended_action: ''
  }

  //Close forms
  close_forms()

  //Refresh measures
  set_status()
  emits('refresh')
}

function close_forms(){
  completing.value = false
  editing.value = false
  deleting.value = false
  rec_action.value = false
  viewing.value = false
}

function open_view(){
  completing.value = false
  editing.value = false
  deleting.value = false
  rec_action.value = false
  viewing.value = true
}

async function mark_complete(){
  const new_measure = {
    id: measure_obj.value.id,
    courseIndicatorId: measure_obj.value.course_indicator_id,
    description: measure_obj.value.measure_description,
    observation: measure_obj.value.observation,
    recommendedAction: measure_obj.value.recommended_action,
    studentsMet: measure_obj.value.met,
    studentsExceeded: measure_obj.value.exceeded,
    studentsBelow: measure_obj.value.below,
    createdAt: measure_obj.value.created_at,
    active: measure_obj.value.is_active,
    deleted: measure_obj.value.deleted,
    deletedAt: measure_obj.value.deleted_at,
    new: measure_obj.value.new,
    status: "Complete",
    updatedAt: measure_obj.value.updated_at,
    version: measure_obj.value.version
  }

  try {
    const { data } = await api.put(`/measure/${measure_obj.value.id}`, new_measure);
    measure_obj.value.status = "Complete"
    set_status()
    calculate_chart_data()
    emits('refresh')
  } catch (error) {
    console.error('Error marking measure as complete:', error);
  }
}

function set_status(){
  if(measure_obj.value.status==="InProgress"){
    status.value = 0
  }
  else if(measure_obj.value.status==="Complete"){
    status.value = 1
  }
  else{
    status.value = 2
  }
}

const measure_obj = ref<{
  id: number
  course_indicator_id: number
  measure_description: string
  observation: string | null
  recommended_action: string | null
  met: number | null
  exceeded: number | null
  below: number | null
  created_at: string
  is_active: boolean
  deleted: boolean | null
  deleted_at: string | null
  new: boolean | null
  status: "InProgress" | "Complete" | null
  updated_at: string | null
  version: number | null
  rejection_note: string | null
}>({
  id: NaN,
  course_indicator_id: NaN,
  measure_description: '',
  observation: null,
  recommended_action: null,
  met: null,
  exceeded: null,
  below: null,
  created_at: '',
  is_active: false,
  deleted: null,
  deleted_at: null,
  new: null,
  status: null,
  updated_at: null,
  version: null,
  rejection_note: ''
})

const status = ref(NaN)

function calculate_chart_data(){
  if (measure_obj.value.below == null){
    has_chart_data.value = false;
  }
  else{
    has_chart_data.value = true;
    let total_students = (measure_obj.value.met || 0) + (measure_obj.value.exceeded || 0) + (measure_obj.value.below || 0)
    below_percent.value = ((measure_obj.value.below || 0) / total_students * 100)
    met_percent.value = (((measure_obj.value.met || 0) + (measure_obj.value.below || 0)) / total_students * 100)
  }
}

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
    course_indicator_id: props.measure_prop.course_indicator_id,
    measure_description: props.measure_prop.measure_description,
    observation: props.measure_prop.observation,
    recommended_action: props.measure_prop.recommended_ction,
    met: props.measure_prop.met,
    exceeded: props.measure_prop.exceeded,
    below: props.measure_prop.below,
    created_at: props.measure_prop.created_at,
    is_active: props.measure_prop.is_active,
    deleted: props.measure_prop.deleted,
    deleted_at: props.measure_prop.deleted_at,
    new: props.measure_prop.new,
    status: props.measure_prop.status,
    updated_at: props.measure_prop.updated_at,
    version: props.measure_prop.version,
    rejection_note: props.measure_prop.rejection_note
  }

  set_status()
  
  //Check if the logged in user is the instructor for the section
  if (userId == props.instructor_id){
    isInstructor.value = true
  }
  else{
    isInstructor.value = false
  }
}

initialize()
calculate_chart_data()
</script>

<template>
  <div id="m-listing-body">
    <div id="measure-box">
      <div id="description-and-status" @click="open_view">
        <div id="description">{{ measure_obj.measure_description }}</div>
        <div v-if="status==0" class="complete-status status-in-progress">In Progress</div>
        <div v-else-if="status==1" class="complete-status status-completed">Complete</div>
        <div v-else class="complete-status status-error">Error</div>

        <div v-if="status==1 && has_chart_data" id="chart"></div>
        <div v-else id="blank-chart"></div>
      </div>
      <div id="buttons">
        <BaseButton
          v-if="status==0 && isInstructor"
          variant="primary"
          size="sm"
          @click="open_complete_form">
          Complete
        </BaseButton>
        <BaseButton
          v-if="status==0 && isInstructor"
          variant="primary"
          size="sm"
          @click="open_edit_form">
          Edit
        </BaseButton>
        <BaseButton
          v-if="status==1 && isAdmin"
          variant="danger"
          size="sm"
          @click="open_ra_form">
          Reject Measure
        </BaseButton>
        <BaseButton
          v-if="isAdmin && status == 0"
          variant="success"
          size="sm"
          @click="mark_complete">
          Mark Complete
        </BaseButton>
        <BaseButton
          v-if="isAdmin || isInstructor"
          variant="danger"
          size="sm"
          @click="open_delete_form">
          Delete
        </BaseButton>
      </div>
    </div>

    <BaseModal v-model:isOpen="completing" title="Complete Measure" size="md" class="form" id="complete-form">
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
    </BaseModal>

    <BaseModal v-model:isOpen="editing" title="Edit Measure" size="md" class="form" id="complete-form">
      <div class="input-grid">
        <BaseInput
          v-model="edit_form_data.description"
          label="Description: "
          placeholder=""
          required
        />
      </div>
      <BaseButton variant="primary" class="submit-button" @click="edit_form_submit">Submit</BaseButton>
    </BaseModal>

    <BaseModal v-model:isOpen="deleting" title="Delete Measure" size="md" class="form" id="complete-form">
      <div>Are you sure you want to delete measure?</div>
      <BaseButton variant="danger" class="submit-button" @click="delete_measure">Delete</BaseButton>
    </BaseModal>

    <BaseModal v-model:isOpen="rec_action" title="Add Recommended Action" class="form" id="delete-form">
      <div class="input-grid">
        <BaseInput
          v-model="ra_form_data.recommended_action"
          label="Recommended Action: "
          placeholder=""
          required
        />
      </div>
      <BaseButton variant="primary" class="submit-button" @click="ra_form_submit">Submit</BaseButton>
    </BaseModal>

    <BaseModal v-model:isOpen="viewing" title="View Measure Details" size="lg" class="form" id="view-form">
      <div class="view-content">
        <div class="view-section">
          <h4>Measure Information</h4>
          <div class="view-field">
            <span class="field-label">ID:</span>
            <span class="field-value">{{ measure_obj.id }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Description:</span>
            <span class="field-value">{{ measure_obj.measure_description }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Status:</span>
            <span v-if="status==0" class="field-value status-badge status-in-progress">In Progress</span>
            <span v-else-if="status==1" class="field-value status-badge status-completed">Complete</span>
            <span v-else class="field-value status-badge status-error">Error</span>
          </div>
        </div>

        <div class="view-section">
          <h4>Assessment Results</h4>
          <div class="view-field">
            <span class="field-label">Students Met:</span>
            <span class="field-value">{{ measure_obj.met ?? 'N/A' }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Students Exceeded:</span>
            <span class="field-value">{{ measure_obj.exceeded ?? 'N/A' }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Students Below:</span>
            <span class="field-value">{{ measure_obj.below ?? 'N/A' }}</span>
          </div>
          <div class="view-field" v-if="measure_obj.met !== null && measure_obj.exceeded !== null && measure_obj.below !== null">
            <span class="field-label">Total Students:</span>
            <span class="field-value">{{ (measure_obj.met || 0) + (measure_obj.exceeded || 0) + (measure_obj.below || 0) }}</span>
          </div>
        </div>

        <div class="view-section" v-if="measure_obj.observation">
          <h4>Observation</h4>
          <div class="view-field full-width">
            <p class="observation-text">{{ measure_obj.observation }}</p>
          </div>
        </div>

        <div class="view-section" v-if="measure_obj.recommended_action">
          <h4>Recommended Action</h4>
          <div class="view-field full-width">
            <p class="observation-text">{{ measure_obj.recommended_action }}</p>
          </div>
        </div>

        <div class="view-section">
          <h4>Additional Details</h4>
          <div class="view-field">
            <span class="field-label">Course Indicator ID:</span>
            <span class="field-value">{{ measure_obj.course_indicator_id }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Created At:</span>
            <span class="field-value">{{ measure_obj.created_at ? new Date(measure_obj.created_at).toLocaleString() : 'N/A' }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Updated At:</span>
            <span class="field-value">{{ measure_obj.updated_at ? new Date(measure_obj.updated_at).toLocaleString() : 'N/A' }}</span>
          </div>
          <div class="view-field">
            <span class="field-label">Version:</span>
            <span class="field-value">{{ measure_obj.version ?? 'N/A' }}</span>
          </div>
        </div>
      </div>
      <BaseButton variant="secondary" class="submit-button" @click="close_forms">Close</BaseButton>
    </BaseModal>
  </div>

</template>

<style>
#measure-box{
  display: flex;
  align-items: center;
  gap: 1rem;
}

#description-and-status{
  display: flex;
  gap: 1rem;
  flex-grow: 1;
  min-width: 0;
  background-color: var(--color-bg-primary);
  padding-left: 1rem;
  padding-right: 1rem;
  padding-bottom: 0.5rem;
  padding-top: 0.5rem;
  margin: 0.2rem;
  border-radius: 1rem;
  text-decoration: none;
}

#description-and-status:hover{
  background-color: var(--color-bg-tertiary);
  cursor: pointer;
}

#description {
  flex: 1 1 auto;
  min-width: 0;
  text-align: left;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

#chart,
#blank-chart {
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  flex: 0 0 1.5rem;
}

#blank-chart{
  background: conic-gradient(
    rgb(94, 94, 94) 0% 100%
  );
}

#chart{
  background: conic-gradient(
    #ef4444 0% v-bind(below_percent + '%'),
    #3b82f6 v-bind(below_percent + '%') v-bind(met_percent + '%'),
    #10b981 v-bind(met_percent + '%') 100%
  );
}

#buttons{
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 1rem;
  z-index: 2;
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.complete-status{
  padding: 0.275rem 0.675rem;
  border-radius: 0.5rem;
  font-size: 0.65rem;
  font-weight: 500;
  white-space: nowrap;
}

.form{
  background-color: var(--color-bg-primary);
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

/* View Modal Styles */
.view-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.view-section {
  background-color: var(--color-bg-secondary);
  padding: 1rem;
  border-radius: 0.5rem;
}

.view-section h4 {
  margin: 0 0 1rem 0;
  color: #60a5fa;
  font-size: 1.1rem;
}

.view-field {
  display: flex;
  align-items: flex-start;
  margin-bottom: 0.75rem;
  gap: 0.5rem;
}

.view-field.full-width {
  flex-direction: column;
}

.view-field:last-child {
  margin-bottom: 0;
}

.field-label {
  font-weight: 600;
  color: var(--color-text-primary);
  min-width: 180px;
  flex-shrink: 0;
}

.field-value {
  color: var(--color-text-secondary);
  flex: 1;
}

.observation-text {
  margin: 0;
  padding: 0.75rem;
  background-color: var(--color-bg-tertiary);
  border-radius: 0.375rem;
  line-height: 1.6;
  white-space: pre-wrap;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  display: inline-block;
}

.status-in-progress {
  background-color: #3b82f6;
  color: white;
}

.status-error {
  background-color: #f59e0b;
  color: white;
}

.status-completed {
  background-color: #10b981;
  color: white;
}
</style>
