<script lang="ts" setup>
import { ref } from 'vue'
import { BaseButton, BaseCard, BaseInput } from "@/components/ui"
import { useRouter } from 'vue-router'
import { useToast } from "@/composables/use-toast.ts"
import { useUserStore } from '@/stores/user-store.ts'

const router = useRouter()
const userStore = useUserStore()
const email_input = ref('')
const password_input = ref('')
const confirm_password_input = ref('')
const institution_name_input = ref('')


async function signUp(){
    //TODO
}



</script>

<template>
  <div class="signup">
    <BaseCard class="signup-card" title="Institution Sign Up">
      <div>
        <BaseInput
          id="email"
          class="signup-input"
          v-model="email_input"
          placeholder="Email"
          :disabled="userStore.isLoading"
        />

      </div>

        <BaseInput
          id="new_pass"
          class="signup-input"
          v-model="password_input"
          placeholder="New Password"
          :disabled="userStore.isLoading"
        />

        <BaseInput
          id="confirm_pass"
          class="signup-input"
          v-model="confirm_password_input"
          placeholder="Confirm Password"
          :disabled="userStore.isLoading"
        />

        <BaseInput
          id="institution_name"
          class="signup-input"
          v-model="institution_name_input"
          placeholder="Institution Name"
          :disabled="userStore.isLoading"
        />

        <BaseButton
          id="signup"
          class="signup-button"
          @click="signUp"
          :loading="userStore.isLoading"
          :disabled="!email_input || userStore.isLoading"
        >

          Sign Up
        </BaseButton>


      <p class="tooltip">
        Signing in?
        <router-link to="/login">Return to login</router-link>.
      </p>
    </BaseCard>
  </div>
</template>

<style scoped>
.signup {
  display: flex;
  justify-content: center;
  align-items: center;
}

.signup-card {
  width: 50vw;
}

.signup-input {
  margin-bottom: var(--spacing-md);
}

.signup-button {
  margin-bottom: var(--spacing-md);
}
</style>
