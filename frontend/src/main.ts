import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './assets/styles/variables.css'
import { createInternalToastAPI } from './composables/use-toast.ts'

const app = createApp(App)

const toastAPI = createInternalToastAPI()
app.provide('toast', toastAPI)

app.use(createPinia())
app.use(router)

app.mount('#app')
