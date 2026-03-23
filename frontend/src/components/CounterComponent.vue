<template>
  <div class="counter-field">
    <label v-if="label" class="counter-label">{{ label }}</label>
    <div class="counter-controls">
      <button type="button" @click="decrement" class="btn-step" :disabled="modelValue <= 0">-</button>
      <input
        type="number"
        :value="modelValue"
        @input="updateValue($event.target.value)"
        class="counter-input"
      />
      <button type="button" @click="increment" class="btn-step">+</button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: [Number, String], default: 0 },
  label: { type: String, default: '' }
});

const emit = defineEmits(['update:modelValue']);

const updateValue = (val) => {
  emit('update:modelValue', Math.max(0, Number(val)));
};

const increment = () => {
  emit('update:modelValue', Number(props.modelValue) + 1);
};

const decrement = () => {
  if (props.modelValue > 0) {
    emit('update:modelValue', Number(props.modelValue) - 1);
  }
};
</script>

<style scoped>
.counter-controls {
  display: flex;
  align-items: center;
  border: 1px solid #ccc;
  border-radius: 4px;
  width: fit-content;
}
.counter-input {
  width: 60px;
  text-align: center;
  border: none;
  font-size: 1rem;
  appearance: textfield;
}
.btn-step {
  background: #f4f4f4;
  border: none;
  padding: 8px 12px;
  cursor: pointer;
  font-weight: bold;
}
.btn-step:hover:not(:disabled) {
  background: #e0e0e0;
}

.counter-input::-webkit-outer-spin-button,
.counter-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
