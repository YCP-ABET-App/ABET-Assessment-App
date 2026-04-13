<script lang="ts" setup>
import { ref } from 'vue'
import { BaseButton, BaseCard, BaseInput } from "@/components/ui"
import { useRouter } from 'vue-router'
import { useToast } from "@/composables/use-toast.ts"

const router = useRouter()
const institutionId = ref('')
const isLoading = ref(false)
const { success, error: showError } = useToast()

async function handleInstitutionSubmit() {
  if (!institutionId.value.trim()) {
    showError('Institution ID is required', 'Validation Error')
    return
  }

  isLoading.value = true

  try {
    // Store the institution ID in localStorage for use during login/signup
    //localStorage.setItem('selectedInstitutionId', institutionId.value.trim())

    if(institutionId.value !== 'ycpAbetCapstoneSpring2026!') {
      throw new Error('Invalid institution ID');
    }

    success('Institution selected successfully', 'Welcome')

    // Redirect to login page
    await router.push('/login')
  } catch (err) {
    showError('Failed to select institution', 'Error')
  } finally {
    isLoading.value = false
  }
}

// Allow Enter key to submit
function handleKeyUp(event: KeyboardEvent) {
  if (event.key === 'Enter' && institutionId.value.trim()) {
    handleInstitutionSubmit()
  }
}
</script>

<template>
  <div class="institution-login">
    <BaseCard class="institution-card" title="Welcome to ABET Assessment">
      <p class="intro-text">
        Please enter your institution ID to continue.
      </p>

      <div>
        <BaseInput
          id="institution_id"
          class="institution-input"
          v-model="institutionId"
          placeholder="Institution ID"
          :disabled="isLoading"
          @keyup="handleKeyUp"
          autofocus
        />

        <BaseButton
          id="submit"
          class="submit-button"
          @click="handleInstitutionSubmit"
          :loading="isLoading"
          :disabled="!institutionId || isLoading"
        >
          Continue
        </BaseButton>
      </div>

      <p class="tooltip">
        Don't have an institution ID? Contact your administrator.
      </p>
    </BaseCard>
  </div>
</template>

<style scoped>
.institution-login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.institution-card {
  width: 50vw;
  max-width: 500px;
}

.intro-text {
  margin-bottom: var(--spacing-md);
  color: var(--text-secondary);
  text-align: center;
}

.institution-input {
  margin-bottom: var(--spacing-md);
}

.submit-button {
  width: 100%;
  margin-bottom: var(--spacing-md);
}

.tooltip {
  text-align: center;
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0;
}

.tooltip a {
  color: var(--primary-color);
  text-decoration: none;
}

.tooltip a:hover {
  text-decoration: underline;
}
</style>
