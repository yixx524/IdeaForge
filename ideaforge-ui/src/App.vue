<!-- 根布局壳：顶部导航 + 路由出口，不含具体页面业务 -->
<template>
  <div class="app">
    <header class="header">
      <div class="brand">
        <h1>IdeaForge</h1>
        <p class="subtitle">个人知识 / 想法整理工具</p>
      </div>
      <nav class="nav">
        <RouterLink to="/">录入想法</RouterLink>
        <RouterLink to="/search">搜索知识</RouterLink>
      </nav>
    </header>

    <!-- 根据路由渲染 CreateIdeaView 或 SearchView -->
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<style scoped>
.app {
  max-width: 720px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.brand h1 {
  font-size: 1.75rem;
  font-weight: 600;
}

.subtitle {
  margin-top: 0.5rem;
  color: #666;
}

.nav {
  display: flex;
  gap: 1rem;
}

.nav a {
  color: #666;
  text-decoration: none;
  font-size: 0.9375rem;
  padding-bottom: 0.125rem;
  border-bottom: 2px solid transparent;
}

.nav a:hover {
  color: #42b883;
}

.nav a.router-link-active {
  color: #42b883;
  border-bottom-color: #42b883;
  font-weight: 500;
}
</style>
