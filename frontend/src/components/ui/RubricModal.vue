<script setup lang="ts">
import { computed } from 'vue';
import { useScheduleModals } from '@/composables/useScheduleModals';

const {
  selectedPI,
  rubricForm,
  piDefinitionForm,
  outcomeDescriptionForm,
  rubricErrors,
  showRubricErrors,
  saveStep,
  pendingCreateAsNew,
  TEXT_LIMITS,
  closeRubricModal,
  beginSaveReview,
  chooseSaveMethod,
  backFromSaveFlow,
  saveRubric,
} = useScheduleModals();

function shownError(key: keyof typeof rubricErrors.value) {
  return showRubricErrors.value ? rubricErrors.value[key] : '';
}

const warningCopy = computed(() =>
  pendingCreateAsNew.value
    ? {
        title: 'Notice: Creating a new performance indicator',
        body: 'The original student outcome and this PI will stay unchanged. A new performance indicator will be added with the definition and rubric you entered. The student outcome text is only saved when you update the existing record.',
        ask: 'Are you sure you want to proceed?',
        confirm: 'Confirm & Create',
      }
    : {
        title: 'Warning: Updating existing record',
        body: 'This will overwrite the current student outcome description and this PI’s definition and rubric. Course mappings that already use this PI keep the same number, but the meaning of that PI will change.',
        ask: 'Are you sure you want to do this?',
        confirm: 'Confirm & Save',
      },
);

function handleOverlayClick() {
  if (saveStep.value === 'edit') closeRubricModal();
}

function handleCancel() {
  if (saveStep.value === 'edit') {
    closeRubricModal();
    return;
  }
  backFromSaveFlow();
}
</script>

<template>
  <div v-if="selectedPI" class="modal-overlay" @click.self="handleOverlayClick">
    <div class="modal-content">
      <h3 v-if="saveStep === 'edit'">Outcome {{ selectedPI.outcomeId }} — {{ selectedPI.piName }}</h3>
      <h3 v-else-if="saveStep === 'choose'">Choose save method</h3>
      <h3 v-else>Final confirmation</h3>

      <template v-if="saveStep === 'edit'">
        <div class="form-group" :class="{ invalid: shownError('outcome') }">
          <label>Student Outcome</label>
          <textarea
            v-model="outcomeDescriptionForm"
            rows="3"
            :maxlength="TEXT_LIMITS.outcome.max"
            placeholder="Describe this student outcome..."
          ></textarea>
          <span class="char-count">{{ outcomeDescriptionForm.trim().length }}/{{ TEXT_LIMITS.outcome.max }}</span>
          <p v-if="shownError('outcome')" class="field-error">{{ shownError('outcome') }}</p>
        </div>

        <div class="form-group" :class="{ invalid: shownError('definition') }">
          <label>PI Definition</label>
          <textarea
            v-model="piDefinitionForm"
            rows="3"
            :maxlength="TEXT_LIMITS.piDefinition.max"
            placeholder="Describe what this performance indicator measures..."
          ></textarea>
          <span class="char-count">{{ piDefinitionForm.trim().length }}/{{ TEXT_LIMITS.piDefinition.max }}</span>
          <p v-if="shownError('definition')" class="field-error">{{ shownError('definition') }}</p>
        </div>

        <div class="rubric-heading">Rubric Criteria</div>

        <div class="form-group" :class="{ invalid: shownError('below') }">
          <label>Below Expectations</label>
          <textarea
            v-model="rubricForm.below"
            rows="2"
            :maxlength="TEXT_LIMITS.rubric.max"
            placeholder="Criteria for below expectations..."
          ></textarea>
          <p v-if="shownError('below')" class="field-error">{{ shownError('below') }}</p>
        </div>

        <div class="form-group" :class="{ invalid: shownError('met') }">
          <label>Meets Expectations</label>
          <textarea
            v-model="rubricForm.met"
            rows="2"
            :maxlength="TEXT_LIMITS.rubric.max"
            placeholder="Criteria for meeting expectations..."
          ></textarea>
          <p v-if="shownError('met')" class="field-error">{{ shownError('met') }}</p>
        </div>

        <div class="form-group" :class="{ invalid: shownError('exceeds') }">
          <label>Exceeds Expectations</label>
          <textarea
            v-model="rubricForm.exceeds"
            rows="2"
            :maxlength="TEXT_LIMITS.rubric.max"
            placeholder="Criteria for exceeding expectations..."
          ></textarea>
          <p v-if="shownError('exceeds')" class="field-error">{{ shownError('exceeds') }}</p>
        </div>
      </template>

      <div v-else-if="saveStep === 'choose'" class="choice-container">
        <p class="prompt-text">How should these changes be saved?</p>
        <div class="choice-grid">
          <button class="choice-card" type="button" @click="chooseSaveMethod(false)">
            <h4>Update Existing</h4>
            <p>Apply the new student outcome text and PI details to the current records.</p>
          </button>
          <button class="choice-card" type="button" @click="chooseSaveMethod(true)">
            <h4>Create as New</h4>
            <p>Keep the original PI and add a new performance indicator with these details.</p>
          </button>
        </div>
      </div>

      <div v-else class="warning-wrapper">
        <div class="warning-icon">⚠️</div>
        <div class="warning-text">
          <h4>{{ warningCopy.title }}</h4>
          <p>{{ warningCopy.body }}</p>
          <p class="confirm-ask">{{ warningCopy.ask }}</p>
        </div>
      </div>

      <div class="modal-actions">
        <button class="btn-cancel" type="button" @click="handleCancel">
          {{ saveStep === 'edit' ? 'Cancel' : 'Back' }}
        </button>
        <button v-if="saveStep === 'edit'" class="btn-save" type="button" @click="beginSaveReview">
          Review Changes
        </button>
        <button v-else-if="saveStep === 'warn'" class="btn-save" type="button" @click="saveRubric">
          {{ warningCopy.confirm }}
        </button>
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
  width: 560px;
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

.form-group.invalid textarea {
  border-color: #ef4444;
}

.char-count {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #9ca3af;
  text-align: right;
}

.field-error {
  margin: 0.35rem 0 0;
  color: #f87171;
  font-size: 0.8rem;
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

.choice-container {
  text-align: center;
}

.prompt-text {
  color: #d1d5db;
  margin: 0 0 1rem;
}

.choice-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.85rem;
}

.choice-card {
  background: #1e1e1e;
  border: 1px solid #454545;
  border-radius: 8px;
  padding: 1rem;
  color: #ffffff;
  text-align: left;
  cursor: pointer;
}

.choice-card:hover {
  border-color: #22c55e;
  background: #232323;
}

.choice-card h4 {
  margin: 0 0 0.4rem;
  font-size: 1rem;
}

.choice-card p {
  margin: 0;
  color: #9ca3af;
  font-size: 0.85rem;
  line-height: 1.4;
}

.warning-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 0.75rem;
}

.warning-icon {
  font-size: 3rem;
}

.warning-text h4 {
  margin: 0 0 0.65rem;
}

.warning-text p {
  color: #d1d5db;
  line-height: 1.5;
  margin: 0 0 0.75rem;
}

.confirm-ask {
  font-weight: 700;
  color: #ffffff !important;
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

@media (max-width: 640px) {
  .choice-grid {
    grid-template-columns: 1fr;
  }
}
</style>
