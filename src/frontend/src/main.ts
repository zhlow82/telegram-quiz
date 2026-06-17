import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import '@fontsource/inter/400.css'
import '@fontsource/inter/500.css'
import '@fontsource/inter/600.css'
import '@fontsource/inter/700.css'
import '@fontsource/inter/900.css'
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
