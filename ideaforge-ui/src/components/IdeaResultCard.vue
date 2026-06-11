<!-- 搜索结果卡片：点击跳转详情，保留当前筛选 query -->
<template>
  <RouterLink :to="detailTo" class="result-card">
    <header class="result-header">
      <h3>{{ item.finalTitle }}</h3>
      <CategoryBadge :code="item.finalCategory" :label="categoryLabel" />
    </header>
    <p v-if="item.finalSummary" class="summary">{{ item.finalSummary }}</p>
    <div v-if="item.finalTags?.length" class="tags">
      <span v-for="tag in item.finalTags" :key="tag" class="tag">{{ tag }}</span>
    </div>
    <p class="meta">
      <span class="meta-icon">◷</span>
      {{ formattedDate }}
      <span class="view-hint">查看详情 →</span>
    </p>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import CategoryBadge from '@/components/CategoryBadge.vue'

const props = defineProps({
  item: { type: Object, required: true },
  categoryLabel: { type: String, default: '' },
})

const route = useRoute()

const detailTo = computed(() => {
  const query = {}
  if (route.query.category) query.category = route.query.category
  if (route.query.q) query.q = route.query.q
  return { name: 'idea-detail', params: { id: props.item.id }, query }
})

const formattedDate = computed(() => {
  if (!props.item.createdAt) return ''
  return new Date(props.item.createdAt).toLocaleString('zh-CN')
})
</script>

<style scoped>
.result-card {
  display: block;
  padding: 1.25rem 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  color: inherit;
  transition:
    box-shadow var(--transition),
    transform var(--transition),
    border-color var(--transition);
}

.result-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: rgba(26, 74, 110, 0.2);
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

.view-hint {
  margin-left: auto;
  color: var(--color-primary-light);
  font-weight: 600;
  opacity: 0;
  transition: opacity var(--transition);
}

.result-card:hover .view-hint {
  opacity: 1;
}

.meta-icon {
  font-size: 0.875rem;
}
</style>
