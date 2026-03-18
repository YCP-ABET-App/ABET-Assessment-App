// Enable Vue devtools in production builds (must run before Vue is imported)
;(window as any).__VUE_PROD_DEVTOOLS__ = true

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './assets/styles/variables.css'
import { createInternalToastAPI } from './composables/use-toast.ts'

const app = createApp(App)

// set devtools flag (cast to any to avoid TS AppConfig type mismatch)
;(app.config as any).devtools = true

const toastAPI = createInternalToastAPI()
app.provide('toast', toastAPI)

app.use(createPinia())
app.use(router)

app.mount('#app')
