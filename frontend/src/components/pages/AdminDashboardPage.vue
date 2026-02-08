<script setup lang="ts">
import { computed } from "vue";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/stores/user-store";
import { useRouter } from "vue-router";


import CourseListing from "@/components/CourseListing.vue";
import ProgramInstructorsPage from "@/components/pages/ProgramInstructorsPage.vue";
import BaseButton from "@/components/ui/BaseButton.vue";

const router = useRouter();
const userStore = useUserStore();


const { currentProgramId, currentSemesterId, isLoggedIn, user } = storeToRefs(userStore);


const displayName = computed(() => {
  if (user.value?.firstName && user.value?.lastName) {
    return `${user.value.firstName} ${user.value.lastName}`;
  }
  return user.value?.email || "User";
});

function handleLogout() {
  userStore.logout();
  router.push("/login");
}

function navigateToSummary() {
  if (currentProgramId.value && currentSemesterId.value) {
    router.push(`/${currentProgramId.value}/${currentSemesterId.value}/summary`);
  }
}
</script>

<template>
  <div class="page-container">
    <NavBar 
      :logged-in="isLoggedIn" 
      :username="displayName" 
      @logout="handleLogout" 
    />

    <main class="content-area">
      <section v-if="currentProgramId && currentSemesterId" class="admin-dashboard">
        <header>
          <h1>Administrator Dashboard</h1>
        </header>

        <CourseListing
          :program-id="currentProgramId"
          :semester-id="currentSemesterId"
        />

        <hr class="divider" />

        <section class="summary-section">
          <ProgramInstructorsPage :program-id="currentProgramId" />
        </section>

        <hr class="divider" />

        <div class="actions">
          <BaseButton @click="navigateToSummary">
            View 1-Year Summary Report
          </BaseButton>
        </div>
      </section>

      <section v-else class="loading-screen">
        <div class="loader-content">
          <p>Loading program and semester information...</p>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.content-area {
  flex: 1;
  background-color: var(--color-background-soft, #f9f9f9);
}

.admin-dashboard {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 2rem;
}

header h1 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
  color: var(--color-text-primary);
}

.divider {
  margin: 3rem 0;
  border: none;
  border-top: 1px solid var(--color-border-dark, #ddd);
}

.summary-section {
  margin-top: 2rem;
}

.actions {
  margin: 2rem 0 5rem;
}

.loading-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
  color: var(--color-text-secondary);
}
</style>