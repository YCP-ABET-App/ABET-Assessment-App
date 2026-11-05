<script lang="ts" setup>
import { ref } from 'vue'
import { BaseButton, BaseCard, BaseInput } from "@/components/ui"
import { useRouter } from 'vue-router'
import { useToast } from "@/composables/use-toast.ts"
import { useUserStore } from '@/stores/user-store.ts'
import api from "@/api"
import { markSetupComplete } from '@/router'

const router = useRouter()
const userStore = useUserStore()
const email_input = ref('')
const password_input = ref('')
const confirm_password_input = ref('')

const { success, error: showError } = useToast()

async function signUp() {
  if (password_input.value !== confirm_password_input.value) {
    showError("Passwords do not match", "Setup Failed")
    return
  }

  if (password_input.value.length < 4) {
    showError("Password must be at least 4 characters", "Setup Failed")
    return
  }

  userStore.isLoading = true
  try {
    const { data } = await api.post("/setup", {
      email: email_input.value,
      passwordHash: password_input.value,   // plain password — backend hashes it
      firstName: "Super",                    // UsersDTO requires these
      lastName: "User"
    })

    // Store the same way login does
    localStorage.setItem("authToken", data.authToken)
    localStorage.setItem("currentUser", JSON.stringify(data.user))
    if (data.user.currentProgramId) {
      localStorage.setItem("currentProgramId", String(data.user.currentProgramId))
    }

    // Load into the store
    markSetupComplete()
    userStore.loadFromStorage()

    success("Account created", "Welcome")
    await router.push("/dashboard")
  } catch (err: any) {
    const msg = err?.response?.data?.error || err?.response?.data?.message || "Setup failed"
    showError(msg, "Setup Failed")
  } finally {
    userStore.isLoading = false
  }
}



</script>

<template>
  <div class="signup">
    <BaseCard class="signup-card" title="First Time Sign Up">
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
          id="pass"
          class="signup-input"
          v-model="password_input"
          placeholder="Password"
          type="password"
          :disabled="userStore.isLoading"
        />

        <BaseInput
          id="confirm_pass"
          class="signup-input"
          v-model="confirm_password_input"
          placeholder="Confirm Password"
          type="password"
          :disabled="userStore.isLoading"
        />


        <BaseButton
          id="signup"
          class="signup-button"
          @click="signUp"
          :loading="userStore.isLoading"
          :disabled="!email_input || !password_input || !confirm_password_input || userStore.isLoading"
        >

          Sign Up
        </BaseButton>

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
