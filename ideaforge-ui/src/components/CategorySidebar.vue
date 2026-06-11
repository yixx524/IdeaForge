<!-- 左侧类别导航：全部 + enabled 类别列表，与 route.query.category 联动 -->
<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h3>类别</h3>
    </div>
    <ElRadioGroup
      :model-value="selectedCategory"
      class="category-nav"
      @change="selectCategory"
    >
      <ElRadioButton label="" class="category-radio">
        <span class="category-icon">▤</span>
        全部
      </ElRadioButton>
      <ElRadioButton
        v-for="item in categories"
        :key="item.code"
        :label="item.code"
        class="category-radio"
      >
        <span class="category-dot" :style="{ background: categoryDotColor(item.code) }" />
        {{ item.label }}
      </ElRadioButton>
    </ElRadioGroup>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { categoryDotColor } from '@/utils/categoryColor'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const { enabled: categories } = storeToRefs(categoryStore)

const selectedCategory = computed(() =>
  typeof route.query.category === 'string' ? route.query.category : '',
)

function selectCategory(code: string | number | boolean | undefined) {
  const categoryCode = String(code ?? '')
  const query: Record<string, string> = { ...route.query } as Record<string, string>
  if (categoryCode) {
    query.category = categoryCode
  } else {
    delete query.category
  }
  delete query.page

  router.push({ path: '/browse', query })
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  align-self: flex-start;
  position: sticky;
  top: 1rem;
}

.sidebar-header {
  padding: 1rem 1.125rem 0.625rem;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-header h3 {
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.category-nav {
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
  gap: 0.25rem;
  width: 100%;
}

.category-nav :deep(.el-radio-button) {
  width: 100%;
}

.category-nav :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  width: 100%;
  border: none !important;
  border-radius: var(--radius-md) !important;
  box-shadow: none !important;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  font-weight: 500;
  text-align: left;
  padding: 0.625rem 0.75rem;
  justify-content: flex-start;
}

.category-nav :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: linear-gradient(135deg, rgba(26, 74, 110, 0.08), rgba(217, 160, 91, 0.1));
  color: var(--color-primary);
  font-weight: 600;
}

.category-nav :deep(.el-radio-button__inner:hover) {
  background: rgba(26, 74, 110, 0.05);
  color: var(--color-primary);
}

.category-icon {
  font-size: 0.875rem;
  opacity: 0.6;
  width: 0.625rem;
  text-align: center;
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .sidebar {
    width: 100%;
    position: static;
  }

  .category-nav {
    flex-direction: row;
    flex-wrap: nowrap;
    overflow-x: auto;
    padding: 0.5rem 0.75rem 0.75rem;
  }

  .category-nav :deep(.el-radio-button) {
    width: auto;
    flex-shrink: 0;
  }

  .category-nav :deep(.el-radio-button__inner) {
    white-space: nowrap;
  }
}
</style>
