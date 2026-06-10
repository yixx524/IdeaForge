/** 前端路由：/ 录入想法，/search 关键词搜索，/settings/categories 类别管理 */
import { createRouter, createWebHistory } from 'vue-router'
import CreateIdeaView from '@/views/CreateIdeaView.vue'
import SearchView from '@/views/SearchView.vue'
import CategoryManageView from '@/views/CategoryManageView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'create', component: CreateIdeaView },
    { path: '/search', name: 'search', component: SearchView },
    { path: '/settings/categories', name: 'categories', component: CategoryManageView },
  ],
})

export default router
