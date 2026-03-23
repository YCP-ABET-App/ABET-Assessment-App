<script lang="ts" setup>
import { ref } from 'vue';
import { useUserStore } from '@/stores/user-store';
import { storeToRefs } from 'pinia';
import { useToast } from '@/composables/use-toast';
import api from '@/api';
import MeasureListing from '@/components/MeasureListing.vue'
import BaseButton from '@/components/ui/BaseButton.vue';
import BaseModal from './ui/BaseModal.vue';
import BaseInput from './ui/BaseInput.vue';

// User store for role checking
const userStore = useUserStore();
const { isAdmin } = storeToRefs(userStore);

// Toast notifications
const toast = useToast();

const props = defineProps({piid: Number, section_id: Number, instructor_id:Number, course_indicator_id:Number})

interface Measure {
  id: number
  courseIndicatorId: number
  description: string
  observation: string | null
  recommendedAction: string | null
  rejectionNote: string | null
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
const measures = ref<any[]>([])

function close_forms(){
  editing_indicator.value = false
  adding_measure.value = false
}

// Edit indicator modal (admin only)
const editing_indicator = ref(false)
const edit_form_data = ref({
  ind_description: '',
  threshold_percentage: 0
})

// Add measure modal
const adding_measure = ref(false)
const new_measure_form_data = ref({
  description: ''
})

function open_edit_form() {
  edit_form_data.value = {
    ind_description: indicator_obj.value.ind_description,
    threshold_percentage: indicator_obj.value.threshold_percentage
  }
  editing_indicator.value = true
}

async function edit_form_submit() {
  try {
    const updatePayload = {
      description: edit_form_data.value.ind_description,
      thresholdPercentage: edit_form_data.value.threshold_percentage
    }

    await api.put(`/performance-indicators/${props.piid}`, updatePayload);

    // Update local state
    indicator_obj.value.ind_description = edit_form_data.value.ind_description
    indicator_obj.value.threshold_percentage = edit_form_data.value.threshold_percentage

    toast.success('Performance indicator updated successfully');
    close_forms()
  } catch (error) {
    console.error('Error updating performance indicator:', error);
    toast.error('Failed to update performance indicator');
  }
}

async function fetch_measures(){
  try {
    measures.value = [];
    const { data: m_data } = await api.get(`/measure`, {params:{"courseIndicatorId": props.course_indicator_id}});
    for (const m_entry of m_data.data){
      const measure_id = m_entry.id;
      const {data: mr_data} = await api.get(`measure-result`, {params: {"measureId": measure_id, "sectionId": props.section_id}})
      for (const mr_entry of mr_data.data){
        measures.value.push({
          id: mr_entry.id,
          course_indicator_id: m_entry.courseIndicatorId,
          measure_description: m_entry.description,
          observation: mr_entry.observation,
          recommended_action: m_entry.recommendedAction,
          met: mr_entry.studentsMet,
          exceeded: mr_entry.studentsExceeded,
          below: mr_entry.studentsBelow,
          created_at: m_entry.createdAt,
          active: mr_entry.active,
          deleted: mr_entry.deleted,
          deleted_at: mr_entry.deleted_at,
          new: mr_entry.new,
          status: mr_entry.status,
          rejection_note: mr_entry.rejectionNote
        })
      }
    }
  } catch (error) {
    console.error('Error fetching or parsing indicator data:', error);
  }
}

async function add_measure(){
  new_measure_form_data.value.description = ''
  adding_measure.value = true
}

async function add_measure_submit(){
  if (!new_measure_form_data.value.description.trim()) {
    toast.error('Description is required');
    return;
  }

  //Define new measure object with description from form
  const new_measure = ref({
    id: null,
    courseIndicatorId: props.course_indicator_id,
    description: new_measure_form_data.value.description,
    recommendedAction: null,
    active: true
  }) 

  //POST request to server
  try {
    await api.post(`/measure`, new_measure.value);
    toast.success('Measure created successfully');

    //Close modal and refresh measures
    close_forms()
    fetch_measures()
  } catch (error) {
    console.error('Error creating measure:', error);
    toast.error('Failed to create measure');
  }
}

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
    <div id="title">
      <span id="pi-label">Performance Indicator: </span>{{ indicator_obj.ind_description }}
    </div>
    <div id="threshold-percent-title">Threshold: <span id="threshold-percent">{{ indicator_obj.threshold_percentage }}%</span></div>
    <MeasureListing
      v-for="measure_obj in measures"
      :key="measure_obj.id"
      :measure_prop="measure_obj"
      :instructor_id="instructor_id"
      :is-admin="isAdmin"
      @refresh="fetch_measures"
    />
    <BaseButton variant="primary" @click="add_measure">Add Measure</BaseButton>
  </div>

  <!-- Edit Indicator Modal (Admin Only) -->
  <BaseModal v-model:isOpen="editing_indicator" title="Edit Performance Indicator" size="md" class="form" id="edit-indicator-form">
    <div class="input-grid">
      <BaseInput
        v-model="edit_form_data.ind_description"
        label="Description"
        placeholder="Enter indicator description"
        required
      />
      <BaseInput
        v-model.number="edit_form_data.threshold_percentage"
        label="Threshold Percentage"
        type="number"
        placeholder="Enter threshold percentage"
        required
        min="0"
        max="100"
      />
    </div>
    <div class="modal-actions">
      <BaseButton variant="secondary" @click="close_forms">Cancel</BaseButton>
      <BaseButton variant="primary" @click="edit_form_submit">Save Changes</BaseButton>
    </div>
  </BaseModal>

  <!-- Add Measure Modal -->
  <BaseModal v-model:isOpen="adding_measure" title="Add New Measure" size="md" class="form" id="add-measure-form">
    <div class="input-grid">
      <BaseInput
        v-model="new_measure_form_data.description"
        label="Measure Description"
        placeholder="Enter measure description"
        required
      />
    </div>
    <div class="modal-actions">
      <BaseButton variant="secondary" @click="close_forms">Cancel</BaseButton>
      <BaseButton variant="primary" @click="add_measure_submit">Create Measure</BaseButton>
    </div>
  </BaseModal>
</template>

<style scoped>
#title {
  font-size: 24px;
}

#pi-label {
  color: green;
}

#threshold-percent-title {
  color: lightgray;
  margin-bottom: 1rem;
}

#indicator-box {
  text-align: left;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.input-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
