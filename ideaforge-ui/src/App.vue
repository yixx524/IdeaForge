<!-- 根布局壳：顶部导航 + 可选侧栏区 + 主内容，不含具体页面业务 -->
<template>
  <div class="app">
    <header class="header">
      <RouterLink to="/" class="brand">
        <img :src="logoUrl" alt="IdeaForge" class="logo" />
        <div class="brand-text">
          <h1>IdeaForge</h1>
          <p class="subtitle">个人知识 / 想法整理工具</p>
        </div>
      </RouterLink>
      <nav class="nav">
        <RouterLink to="/" end class="nav-link">
          <span class="nav-icon">⌂</span>
          首页
        </RouterLink>
        <RouterLink to="/browse" class="nav-link">
          <span class="nav-icon">⌕</span>
          浏览
        </RouterLink>
        <RouterLink to="/create" class="nav-link">
          <span class="nav-icon">✎</span>
          录入
        </RouterLink>
        <RouterLink to="/settings/categories" class="nav-link">
          <span class="nav-icon">⚙</span>
          设置
        </RouterLink>
      </nav>
    </header>

    <div class="body-row" :class="{ 'with-sidebar': showSidebar }">
      <CategorySidebar v-if="showSidebar" />
      <main class="main">
        <RouterView />
      </main>
    </div>

    <footer class="footer">
      <p>由 DeepSeek 智能整理 · 安全存储于 PostgreSQL</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import CategorySidebar from '@/components/CategorySidebar.vue'
import { useCategoryStore } from '@/stores/category'
import logoUrl from '@/assets/logo.webp'

const route = useRoute()
const categoryStore = useCategoryStore()

onMounted(() => {
  categoryStore.ensureLoaded()
})

/** 浏览页与详情页将显示左侧类别导航（Phase 2 接入 CategorySidebar） */
const showSidebar = computed(() => {
  const name = route.name
  return name === 'browse' || name === 'idea-detail'
})
</script>

<style scoped>
.app {
  max-width: var(--layout-max-width);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 2.5rem;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.75rem;
  padding: 1rem 1.25rem;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-border);
  flex-wrap: wrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  text-decoration: none;
  color: inherit;
  transition: opacity var(--transition);
}

.brand:hover {
  opacity: 0.85;
}

.logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
  flex-shrink: 0;
}

.brand-text h1 {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.subtitle {
  margin-top: 0.125rem;
  color: var(--color-text-muted);
  font-size: 0.8125rem;
}

.nav {
  display: flex;
  gap: 0.375rem;
  flex-wrap: wrap;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  padding: 0.5rem 0.875rem;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  transition:
    color var(--transition),
    background var(--transition),
    border-color var(--transition);
}

.nav-icon {
  font-size: 1rem;
  opacity: 0.7;
}

.nav-link:hover {
  color: var(--color-primary);
  background: rgba(26, 74, 110, 0.05);
}

.nav-link.router-link-active {
  color: var(--color-primary);
  background: linear-gradient(135deg, rgba(26, 74, 110, 0.08), rgba(217, 160, 91, 0.1));
  border-color: rgba(26, 74, 110, 0.15);
  font-weight: 600;
}

.body-row {
  flex: 1;
  display: flex;
  gap: 1.25rem;
  min-width: 0;
}

.body-row.with-sidebar .main {
  min-width: 0;
}

@media (max-width: 900px) {
  .body-row.with-sidebar {
    flex-direction: column;
  }
}

.main {
  flex: 1;
  min-width: 0;
}

.footer {
  margin-top: 2.5rem;
  text-align: center;
}

.footer p {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  opacity: 0.7;
}

@media (max-width: 640px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }

  .nav {
    justify-content: center;
  }
}
</style>
