<!-- 搜索页：按关键词检索已确认的知识条目 -->
<template>
  <div class="page">
    <section class="card search-card">
      <div class="card-header">
        <h2>搜索知识</h2>
        <p class="hint">按标题、摘要、正文或标签关键词检索</p>
      </div>

      <div class="search-bar">
        <div class="search-input-wrap">
          <span class="search-icon">⌕</span>
          <input
            v-model="keyword"
            type="search"
            placeholder="输入关键词…"
            @keyup.enter="handleSearch"
          />
        </div>
        <button type="button" class="btn-primary" :disabled="loading" @click="handleSearch">
          <span v-if="loading" class="spinner" />
          {{ loading ? '搜索中…' : '搜索' }}
        </button>
      </div>
    </section>

    <Transition name="fade">
      <p v-if="error" class="toast error">{{ error }}</p>
    </Transition>

    <!-- 搜索结果 -->
    <div v-if="results.length" class="results-meta">
      找到 <strong>{{ results.length }}</strong> 条匹配结果
    </div>

    <section v-if="results.length" class="results">
      <article v-for="item in results" :key="item.id" class="result-card">
        <header class="result-header">
          <h3>{{ item.finalTitle }}</h3>
          <span class="badge" :class="`badge-${item.finalCategory?.toLowerCase()}`">
            {{ categoryLabel(item.finalCategory) }}
          </span>
        </header>
        <p v-if="item.finalSummary" class="summary">{{ item.finalSummary }}</p>
        <div v-if="item.finalTags?.length" class="tags">
          <span v-for="tag in item.finalTags" :key="tag" class="tag">{{ tag }}</span>
        </div>
        <p class="meta">
          <span class="meta-icon">◷</span>
          {{ formatDate(item.createdAt) }}
        </p>
      </article>
    </section>

    <!-- 空状态 -->
    <div v-else-if="!loading && searched && !error" class="empty-state">
      <div class="empty-icon">⌕</div>
      <p class="empty-title">未找到匹配条目</p>
      <p class="empty-hint">试试其他关键词，或先去录入新想法</p>
    </div>

    <!-- 初始引导 -->
    <div v-else-if="!loading && !searched && !error" class="welcome-state">
      <p>输入关键词开始检索你的知识库</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { searchIdeas } from '@/api/idea'
import { categoryLabel } from '@/constants/categories'

const keyword = ref('')
const loading = ref(false)
const error = ref(null)
const results = ref([])
const searched = ref(false)

/** 调用 GET /api/ideas/search?q= 检索 */
async function handleSearch() {
  if (!keyword.value.trim()) {
    error.value = '请输入搜索关键词'
    results.value = []
    searched.value = false
    return
  }

  loading.value = true
  error.value = null

  try {
    results.value = await searchIdeas(keyword.value.trim())
    searched.value = true
  } catch (e) {
    error.value = e.message
    results.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.card-header {
  padding: 1.5rem 1.5rem 0;
}

.card-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 0.375rem;
}

.hint {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.search-card .search-bar {
  display: flex;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem 1.5rem;
}

.search-input-wrap {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 0.875rem;
  color: var(--color-text-muted);
  font-size: 1.125rem;
  pointer-events: none;
}

.search-input-wrap input {
  width: 100%;
  padding: 0.625rem 0.875rem 0.625rem 2.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.9375rem;
  background: var(--color-surface);
  transition: border-color var(--transition), box-shadow var(--transition);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.5rem;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(26, 74, 110, 0.25);
  white-space: nowrap;
}

.btn-primary:not(:disabled):hover {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  box-shadow: 0 4px 12px rgba(26, 74, 110, 0.35);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.results-meta {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  padding: 0 0.25rem;
}

.results-meta strong {
  color: var(--color-primary);
}

.results {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.result-card {
  padding: 1.25rem 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition:
    box-shadow var(--transition),
    transform var(--transition),
    border-color var(--transition);
}

.result-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: rgba(26, 74, 110, 0.15);
}

.result-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.result-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.4;
}

.badge {
  padding: 0.2rem 0.625rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge-work {
  background: #eff6ff;
  color: #1d4ed8;
}

.badge-study {
  background: #f0fdf4;
  color: #15803d;
}

.badge-life {
  background: #fdf4ff;
  color: #a21caf;
}

.badge-inspiration {
  background: linear-gradient(135deg, rgba(26, 74, 110, 0.1), rgba(217, 160, 91, 0.15));
  color: var(--color-primary);
}

.badge-todo {
  background: #fff7ed;
  color: #c2410c;
}

.summary {
  color: var(--color-text-muted);
  font-size: 0.9375rem;
  margin-bottom: 0.625rem;
  line-height: 1.5;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  margin-bottom: 0.625rem;
}

.tag {
  padding: 0.15rem 0.5rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.meta {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  opacity: 0.8;
}

.meta-icon {
  font-size: 0.875rem;
}

.toast {
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
}

.error {
  color: var(--color-error);
  background: var(--color-error-bg);
  border: 1px solid rgba(220, 38, 38, 0.2);
}

.empty-state,
.welcome-state {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-icon {
  font-size: 2.5rem;
  color: var(--color-border);
  margin-bottom: 0.75rem;
}

.empty-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 0.375rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  opacity: 0.7;
}

.welcome-state p {
  font-size: 0.9375rem;
  color: var(--color-text-muted);
  opacity: 0.6;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 480px) {
  .search-card .search-bar {
    flex-direction: column;
  }

  .btn-primary {
    justify-content: center;
  }
}
</style>
