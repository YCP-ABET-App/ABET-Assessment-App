<script lang="ts" setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import GlobalSelectors from '@/components/GlobalSelectors.vue'
import GlobalToast from '@/components/ui/GlobalToast.vue'
import { useUserStore } from '@/stores/user-store.js'

const userStore = useUserStore()
const route = useRoute()

const isHomePage = computed(() => route.path === '/dashboard' || route.name === 'Home')

onMounted(() => {
  userStore.loadFromStorage()
  userStore.applyThemeToDocument(userStore.theme)
})

function handleLogout() {
  userStore.logout()
}
</script>

<template>
  <div id="app">

    <!-- FIXED TOP TOOLBAR -->
    <div id="fixed-top">
      <NavBar
        :loggedIn="userStore.isLoggedIn"
        :username="userStore.userFullName"
        :is-admin="userStore.isAdmin"
        @logout="handleLogout"
      />

      <!-- Only show selectors when logged in AND on home page -->
      <div class="selectors-div" v-if="userStore.isLoggedIn && isHomePage">
        <GlobalSelectors/>
      </div>
    </div>

    <!-- PAGE CONTENT -->
    <main id="page-container">
      <router-view />
    </main>

    <footer class="footer">
      <p>© 2025 ABET Assessment App</p>
      <p>Definitions adapted from ABET documentation.</p>
    </footer>

    <GlobalToast />
  </div>
</template>

<style>
#app {
  margin: 0 auto;
  text-align: center;
  font-family: Noto Sans, system-ui, -apple-system, sans-serif;
}

#fixed-top {
  position: sticky;
  top: 0;
  z-index: 9999;
  background: var(--color-bg-primary);
  border-bottom: 1px solid var(--color-border-dark);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* Push the whole page down so nothing is hidden behind the fixed header */
#page-container {
  margin-top: 0;
}

header {
  margin-bottom: 1rem;
}

.footer {
  text-align: center;
  color: #555;
  font-size: 0.9rem;
  margin-top: 3rem;
}

.footer hr {
  border: none;
  border-top: 1px solid #ccc;
  margin-bottom: 1rem;
  width: 100%;
}
</style>
