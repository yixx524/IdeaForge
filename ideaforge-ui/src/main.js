/** Vue 3 应用入口：挂载根组件并注册路由 */
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router'

createApp(App).use(router).mount('#app')
