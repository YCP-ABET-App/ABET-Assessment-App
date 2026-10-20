<script lang="ts" setup>
import { ref, computed } from 'vue'
import { BaseButton, BaseCard, BaseInput } from "@/components/ui"
import { useRouter } from 'vue-router'
import { useToast } from "@/composables/use-toast.ts"
import { useUserStore } from '@/stores/user-store.ts'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const { success, error: showError } = useToast()

const email_input = ref('')
const recovery_code_input = ref('')
const new_password_input = ref('')
const confirm_password_input = ref('')

const codeSent = ref(false)

const isValidEmail = computed(() => {
  const value = email_input.value.trim()
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
})

async function sendRecoveryCode(){

  //error if no input or not formatted like an email
  if (!email_input.value || !isValidEmail.value) {
    showError('Invalid email. Please try again.','Error')
    return
  }

  //set loading 
  userStore.isLoading = true

  try {
    await api.post('/auth/recovery/send', {
      email: email_input.value.trim()
    })
    codeSent.value = true
    success('Code sent. Check email for recovery code')
  }catch (err: any){
    showError(err?.response?.data?.message || 'Failed to send recovery code. Please try again.','Error')
  }finally{
    userStore.isLoading = false
  }

}//end send recovery code

async function resetPassword() {
  if (new_password_input.value !== confirm_password_input.value) {
    showError('Passwords do not match', 'Error')
    return
  }

  if (!recovery_code_input.value.trim()) {
    showError('Please enter the recovery code', 'Error')
    return
  }

  userStore.isLoading = true

  try {
    await api.post('/auth/recovery/reset', {
      email: email_input.value.trim(),
      code: recovery_code_input.value.trim(),
      newPassword: new_password_input.value
    })

    success('Password reset successfully. You can now log in.', 'Success')
    await router.push('/login')
  } catch (err: any) {
    showError(
      err?.response?.data?.message || 'Failed to reset password. Please try again.',
      'Error'
    )
  } finally {
    userStore.isLoading = false
  }
}

</script>

<template>
  <div class="recovery">
    <BaseCard class="recovery-card" title="Account Recovery">
      <div>
        <template v-if="!codeSent">
          <BaseInput
            id="email"
            class="recovery-input"
            v-model="email_input"
            placeholder="Email"
            :disabled="userStore.isLoading || codeSent"
          />

          <BaseButton
            id="get_code"
            class="recovery-button"
            @click="sendRecoveryCode"
            :loading="userStore.isLoading"
            :disabled="!email_input || userStore.isLoading || codeSent"
          >
            Get Recovery Code
          </BaseButton>
        </template>

        <template v-if="codeSent">
          <BaseInput
            id="code"
            class="recovery-input"
            v-model="recovery_code_input"
            placeholder="Recovery Code"
            :disabled="userStore.isLoading"
          />
    
          <BaseInput
            id="new_pass"
            class="recovery-input"
            v-model="new_password_input"
            type="password"
            placeholder="New Password"
            :disabled="userStore.isLoading"
          />

          <BaseInput
            id="confirm_pass"
            class="recovery-input"
            v-model="confirm_password_input"
            type="password"
            placeholder="Confirm Password"
            :disabled="userStore.isLoading"
          />

          <BaseButton
            id="reset_pass"
            class="recovery-button"
            @click="resetPassword"
            :loading="userStore.isLoading"
            :disabled="!email_input || !recovery_code_input || !new_password_input || !confirm_password_input || userStore.isLoading"
          >
            Reset Password
          </BaseButton>

        
      </template>
      </div>
      <p class="tooltip">
        Signing in?
        <router-link to="/login">Return to login</router-link>.
      </p>
    </BaseCard>
  </div>
</template>

<style scoped>
.recovery {
  display: flex;
  justify-content: center;
  align-items: center;
}

.recovery-card {
  width: 50vw;
}

.recovery-input {
  margin-bottom: var(--spacing-md);
}

.recovery-button {
  margin-bottom: var(--spacing-md);
}
</style>
