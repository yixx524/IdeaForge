<!-- 搜索结果卡片：点击跳转详情，保留当前筛选 query；悬停显示删除 -->
<template>
  <article class="result-card">
    <RouterLink :to="detailTo" class="result-link">
      <header class="result-header">
        <h3>{{ item.finalTitle }}</h3>
        <ElTag
          size="small"
          effect="light"
          :style="categoryTagStyle(item.finalCategory)"
        >
          {{ categoryLabel }}
        </ElTag>
      </header>
      <p v-if="item.finalSummary" class="summary">{{ item.finalSummary }}</p>
      <div v-if="item.finalTags?.length" class="tags">
        <ElTag v-for="tag in item.finalTags" :key="tag" size="small" type="info">
          {{ tag }}
        </ElTag>
      </div>
      <footer class="meta">
        <span class="meta-left">
          <span class="meta-icon">◷</span>
          {{ formattedDate }}
        </span>
        <span class="view-hint">查看详情 →</span>
      </footer>
    </RouterLink>

    <ElButton
      class="card-delete"
      type="danger"
      circle
      plain
      size="small"
      title="删除此条目"
      aria-label="删除此条目"
      @click.stop="emit('delete', item)"
    >
      ×
    </ElButton>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { categoryTagStyle } from '@/utils/categoryColor'
import type { IdeaResponse } from '@/types'

const props = defineProps<{
  item: IdeaResponse
  categoryLabel?: string
}>()

const emit = defineEmits<{
  delete: [item: IdeaResponse]
}>()

const route = useRoute()

const detailTo = computed(() => {
  const query: Record<string, string> = {}
  if (route.query.category && typeof route.query.category === 'string') {
    query.category = route.query.category
  }
  if (route.query.q && typeof route.query.q === 'string') {
    query.q = route.query.q
  }
  return { name: 'idea-detail', params: { id: props.item.id }, query }
})

const formattedDate = computed(() => {
  if (!props.item.createdAt) return ''
  return new Date(props.item.createdAt).toLocaleString('zh-CN')
})
</script>

<style scoped>
.result-card {
  position: relative;
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
  border-color: rgba(26, 74, 110, 0.2);
}

.result-link {
  display: block;
  padding: 1.25rem 3rem 1.25rem 1.5rem;
  text-decoration: none;
  color: inherit;
}

.card-delete {
  position: absolute;
  top: 0.875rem;
  right: 0.875rem;
  opacity: 0;
  z-index: 1;
}

.result-card:hover .card-delete,
.card-delete:focus-visible {
  opacity: 1;
}

.result-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
  padding-right: 0.25rem;
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

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.meta-left {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  opacity: 0.85;
}

.view-hint {
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

@media (max-width: 640px) {
  .result-link {
    padding-right: 2.75rem;
  }

  .card-delete {
    opacity: 1;
  }
}
</style>
