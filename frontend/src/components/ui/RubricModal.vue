<script setup lang="ts">
import { useScheduleModals } from '@/composables/useScheduleModals';

const {
  selectedPI,
  rubricForm,
  piDefinitionForm,
  closeRubricModal,
  saveRubric,
} = useScheduleModals();
</script>

<template>
  <div v-if="selectedPI" class="modal-overlay" @click.self="closeRubricModal">
    <div class="modal-content">
      <h3>Outcome {{ selectedPI.outcomeId }} — {{ selectedPI.piName }}</h3>

      <div class="pi-description-box">
        <strong>Student Outcome</strong>
        <p>{{ selectedPI.outcomeDesc }}</p>
      </div>

      <div class="form-group">
        <label>PI Definition</label>
        <textarea
          v-model="piDefinitionForm"
          rows="3"
          placeholder="Describe what this performance indicator measures..."
        ></textarea>
      </div>

      <div class="rubric-heading">Rubric Criteria</div>

      <div class="form-group">
        <label>Below Expectations</label>
        <textarea v-model="rubricForm.below" rows="2" placeholder="Criteria for below expectations..."></textarea>
      </div>

      <div class="form-group">
        <label>Meets Expectations</label>
        <textarea v-model="rubricForm.met" rows="2" placeholder="Criteria for meeting expectations..."></textarea>
      </div>

      <div class="form-group">
        <label>Exceeds Expectations</label>
        <textarea v-model="rubricForm.exceeds" rows="2" placeholder="Criteria for exceeding expectations..."></textarea>
      </div>

      <div class="modal-actions">
        <button class="btn-cancel" @click="closeRubricModal">Cancel</button>
        <button class="btn-save" @click="saveRubric">Save</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: #2c2c2c;
  color: #ffffff;
  padding: 2rem;
  border-radius: 8px;
  width: 520px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.6);
  border: 1px solid #383838;
}

.modal-content h3 {
  font-size: 1.15rem;
  text-align: center;
  margin-top: 0;
  margin-bottom: 1.25rem;
  font-weight: 700;
  line-height: 1.4;
}

.form-group {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 0.35rem;
  color: #d1d5db;
}

.form-group textarea {
  width: 100%;
  padding: 0.6rem 0.75rem;
  border: 1px solid #454545;
  background: #1c1c1c;
  color: #ffffff;
  border-radius: 4px;
  font-size: 0.9rem;
  box-sizing: border-box;
  font-family: inherit;
  resize: vertical;
}

.form-group textarea:focus {
  outline: 1px solid #22c55e;
  border-color: #22c55e;
}

.pi-description-box {
  background: #1e1e1e;
  padding: 0.85rem;
  border-radius: 4px;
  border: 1px solid #383838;
  margin-bottom: 1.25rem;
  font-size: 0.875rem;
}

.pi-description-box strong {
  color: #9ca3af;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.pi-description-box p {
  margin: 0.35rem 0 0 0;
  color: #e5e7eb;
}

.rubric-heading {
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #9ca3af;
  margin: 0.25rem 0 0.75rem;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid #383838;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.btn-cancel {
  background: #4b5563;
  color: #ffffff;
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel:hover { background: #374151; }

.btn-save {
  background: #22c55e;
  color: #ffffff;
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-save:hover { background: #16a34a; }
</style>
