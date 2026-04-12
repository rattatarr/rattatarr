import './assets/main.css'
import 'primeicons/primeicons.css'
import 'vue-sonner/style.css'
import './styles/custom-scrollbar.css'
// TODO: Prime Icons is doing a weird full load of icons, might need to do a tree shaking in the future to avoid large bundle size

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'
import Aura from '@primeuix/themes/aura'

import App from './App.vue'
import router from './router'
import { queryClient } from '@/plugins/queryClient'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(VueQueryPlugin, { queryClient })
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      prefix: 'p',
      darkModeSelector: '.app-dark',
      cssLayer: false,
    },
  },
})
app.directive('tooltip', Tooltip)

app.mount('#app')
