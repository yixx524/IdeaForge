<!-- 根布局壳：顶部导航 + 路由出口，不含具体页面业务 -->
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
        <RouterLink to="/" class="nav-link">
          <span class="nav-icon">✎</span>
          录入想法
        </RouterLink>
        <RouterLink to="/search" class="nav-link">
          <span class="nav-icon">⌕</span>
          搜索知识
        </RouterLink>
      </nav>
    </header>

    <main class="main">
      <RouterView />
    </main>

    <footer class="footer">
      <p>由 DeepSeek 智能整理 · 安全存储于 PostgreSQL</p>
    </footer>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
import logoUrl from '@/assets/logo.png'
</script>

<style scoped>
.app {
  max-width: 760px;
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
  margin-bottom: 2rem;
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
  gap: 0.5rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  padding: 0.5rem 1rem;
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

.main {
  flex: 1;
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

@media (max-width: 520px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }

  .nav {
    justify-content: center;
  }
}
</style>
