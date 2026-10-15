<script lang="ts" setup>
import { ref } from 'vue'
import { BaseButton, BaseCard, BaseInput } from "@/components/ui"
import { useRouter } from 'vue-router'
import { useToast } from "@/composables/use-toast.ts"
import { useUserStore } from '@/stores/user-store.ts'

const router = useRouter()
const userStore = useUserStore()
const email_input = ref('')
const recovery_code_input = ref('')
const new_password_input = ref('')
const confirm_password_input = ref('')

async function sendRecoveryCode(){
    //TODO
}

async function resetPassword(){
    //TODO
}

</script>

<template>
  <div class="recovery">
    <BaseCard class="recovery-card" title="Account Recovery">
      <div>
        <BaseInput
          id="email"
          class="recovery-input"
          v-model="email_input"
          placeholder="Email"
          :disabled="userStore.isLoading"
        />

        <BaseButton
          id="get_code"
          class="recovery-button"
          @click="sendRecoveryCode"
          :loading="userStore.isLoading"
          :disabled="!email_input || userStore.isLoading"
        >
          Get Recovery Code
        </BaseButton>

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
          placeholder="New Password"
          :disabled="userStore.isLoading"
        />

        <BaseInput
          id="confirm_pass"
          class="recovery-input"
          v-model="confirm_password_input"
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
