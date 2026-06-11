/** 前端路由 */
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import CreateIdeaView from '@/views/CreateIdeaView.vue'
import BrowseView from '@/views/BrowseView.vue'
import IdeaDetailView from '@/views/IdeaDetailView.vue'
import CategoryManageView from '@/views/CategoryManageView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/create', name: 'create', component: CreateIdeaView },
    { path: '/browse', name: 'browse', component: BrowseView },
    { path: '/ideas/:id', name: 'idea-detail', component: IdeaDetailView },
    { path: '/search', redirect: '/browse' },
    { path: '/settings/categories', name: 'categories', component: CategoryManageView },
  ],
})

export default router
