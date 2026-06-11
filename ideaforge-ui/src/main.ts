/** Vue 3 应用入口：挂载根组件并注册路由 */
import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from '@/router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
