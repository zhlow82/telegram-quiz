import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'
import { useBrandingStore } from './stores/branding'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const brandingStore = useBrandingStore(pinia)
brandingStore.load().then(() => {
  app.mount('#app')
})
