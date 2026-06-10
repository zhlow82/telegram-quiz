import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'
import { useBrandingStore } from './stores/branding'
/// <reference types="vite/client" />

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Load branding before mount so AppLayout & LoginView have values
const brandingStore = useBrandingStore(pinia)
brandingStore.load()

app.mount('#app')
