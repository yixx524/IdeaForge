<!-- 首页：快速录入与浏览知识入口 -->
<template>
  <div class="page home-page">
    <section class="hero">
      <h2 class="hero-title">整理想法，沉淀知识</h2>
      <p class="hero-desc">
        输入原始文本或上传文档，由 DeepSeek 智能整理为结构化信息，确认后安全存入你的知识库。
      </p>
    </section>

    <div class="entry-grid">
      <RouterLink to="/create" class="entry-card entry-create">
        <span class="entry-icon">✎</span>
        <h3>快速录入</h3>
        <p>输入想法或上传 Word / PDF，AI 整理后确认保存</p>
        <span class="entry-action">开始录入 →</span>
      </RouterLink>

      <RouterLink to="/browse" class="entry-card entry-browse">
        <span class="entry-icon">⌕</span>
        <h3>浏览知识</h3>
        <p>按类别筛选、关键词搜索，查看已保存的知识条目</p>
        <span class="entry-action">进入浏览 →</span>
      </RouterLink>
    </div>

    <section v-if="recentItems.length" class="recent-section">
      <div class="recent-header">
        <h4>最近知识</h4>
        <RouterLink to="/browse" class="recent-more">查看全部 →</RouterLink>
      </div>
      <div class="recent-list">
        <RouterLink
          v-for="item in recentItems"
          :key="item.id"
          :to="{ name: 'idea-detail', params: { id: item.id } }"
          class="recent-item"
        >
          <span class="recent-title">{{ item.finalTitle }}</span>
          <span class="recent-date">{{ formatDate(item.createdAt) }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="tips-card">
      <h4>使用提示</h4>
      <ul>
        <li>录入分两步：AI 整理 → 核对确认后入库</li>
        <li>浏览页可按类别快速筛选，点击条目查看详情</li>
        <li>详情页支持导出 Word 文档</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { searchIdeas } from '@/api/idea'

const recentItems = ref([])

onMounted(async () => {
  try {
    const { content } = await searchIdeas({ page: 0, size: 5 })
    recentItems.value = content ?? []
  } catch {
    recentItems.value = []
  }
})

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.home-page {
  gap: 2rem;
}

.hero {
  text-align: center;
  padding: 1rem 0 0.5rem;
}

.hero-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.02em;
  margin-bottom: 0.75rem;
}

.hero-desc {
  max-width: 36rem;
  margin: 0 auto;
  color: var(--color-text-muted);
  font-size: 0.9375rem;
  line-height: 1.6;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

.entry-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1.75rem 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  text-decoration: none;
  color: inherit;
  transition:
    box-shadow var(--transition),
    transform var(--transition),
    border-color var(--transition);
}

.entry-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-3px);
  border-color: rgba(26, 74, 110, 0.2);
}

.entry-create:hover {
  border-color: rgba(26, 74, 110, 0.25);
}

.entry-browse:hover {
  border-color: rgba(217, 160, 91, 0.35);
}

.entry-icon {
  font-size: 1.75rem;
  line-height: 1;
  opacity: 0.85;
}

.entry-card h3 {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-primary);
}

.entry-card p {
  flex: 1;
  font-size: 0.875rem;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.entry-action {
  margin-top: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-primary-light);
}

.recent-section {
  padding: 1.25rem 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.recent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.recent-header h4 {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-primary);
}

.recent-more {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-primary-light);
  text-decoration: none;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.625rem 0.75rem;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: inherit;
  transition: background var(--transition);
}

.recent-item:hover {
  background: rgba(26, 74, 110, 0.05);
}

.recent-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-date {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.tips-card {
  padding: 1.25rem 1.5rem;
  background: linear-gradient(135deg, rgba(26, 74, 110, 0.04), rgba(217, 160, 91, 0.06));
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.tips-card h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 0.625rem;
}

.tips-card ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.tips-card li {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  padding-left: 1rem;
  position: relative;
}

.tips-card li::before {
  content: '·';
  position: absolute;
  left: 0;
  color: var(--color-accent);
  font-weight: 700;
}

@media (max-width: 640px) {
  .entry-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 1.375rem;
  }
}
</style>
