<!-- 搜索页：按关键词检索已确认的知识条目 -->
<template>
  <div class="page">
    <section class="card">
      <h2>搜索知识</h2>
      <p class="hint">按标题、摘要、正文或标签关键词检索</p>

      <div class="search-bar">
        <input
          v-model="keyword"
          type="search"
          placeholder="输入关键词…"
          @keyup.enter="handleSearch"
        />
        <button type="button" :disabled="loading" @click="handleSearch">
          {{ loading ? '搜索中…' : '搜索' }}
        </button>
      </div>
    </section>

    <p v-if="error" class="message error">{{ error }}</p>

    <!-- 搜索结果列表 -->
    <section v-if="results.length" class="results">
      <article v-for="item in results" :key="item.id" class="result-card">
        <header class="result-header">
          <h3>{{ item.finalTitle }}</h3>
          <span class="badge">{{ categoryLabel(item.finalCategory) }}</span>
        </header>
        <p v-if="item.finalSummary" class="summary">{{ item.finalSummary }}</p>
        <div v-if="item.finalTags?.length" class="tags">
          <span v-for="tag in item.finalTags" :key="tag" class="tag">{{ tag }}</span>
        </div>
        <p class="meta">{{ formatDate(item.createdAt) }}</p>
      </article>
    </section>

    <p v-else-if="!loading && keyword.trim() && !error" class="empty">未找到匹配条目</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { searchIdeas } from '@/api/idea'
import { categoryLabel } from '@/constants/categories'

/** 搜索关键词 */
const keyword = ref('')
const loading = ref(false)
const error = ref(null)
const results = ref([])

/** 调用 GET /api/ideas/search?q= 检索 */
async function handleSearch() {
  if (!keyword.value.trim()) {
    error.value = '请输入搜索关键词'
    results.value = []
    return
  }

  loading.value = true
  error.value = null

  try {
    results.value = await searchIdeas(keyword.value.trim())
  } catch (e) {
    error.value = e.message
    results.value = []
  } finally {
    loading.value = false
  }
}

/** 格式化后端返回的 ISO 时间戳 */
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
  padding: 1.5rem;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
}

.card h2 {
  font-size: 1.125rem;
  margin-bottom: 0.5rem;
}

.hint {
  margin-bottom: 1rem;
  color: #666;
  font-size: 0.875rem;
}

.search-bar {
  display: flex;
  gap: 0.75rem;
}

.search-bar input {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9375rem;
}

button {
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 6px;
  background: #42b883;
  color: #fff;
  font-size: 0.9375rem;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.results {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.result-card {
  padding: 1.25rem;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.result-header h3 {
  font-size: 1rem;
  font-weight: 600;
}

.badge {
  padding: 0.125rem 0.5rem;
  background: #eef7f2;
  color: #2d8a5e;
  border-radius: 4px;
  font-size: 0.75rem;
  white-space: nowrap;
}

.summary {
  color: #555;
  font-size: 0.9375rem;
  margin-bottom: 0.5rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  margin-bottom: 0.5rem;
}

.tag {
  padding: 0.125rem 0.5rem;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 0.8125rem;
  color: #666;
}

.meta {
  font-size: 0.8125rem;
  color: #999;
}

.message.error {
  color: #c0392b;
  font-size: 0.9375rem;
}

.empty {
  color: #666;
  font-size: 0.9375rem;
  text-align: center;
  padding: 2rem 0;
}
</style>
