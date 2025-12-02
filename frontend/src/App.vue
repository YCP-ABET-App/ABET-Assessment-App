<script lang="ts" setup>
import { onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import GlobalToast from '@/components/ui/GlobalToast.vue'
import { useUserStore } from '@/stores/user-store'


const userStore = useUserStore()

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
    <header>
      <NavBar
        :loggedIn="userStore.isLoggedIn"
        :username="userStore.userFullName"
        @logout="handleLogout"
      />
    </header>

    <main>
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
}
</style>
