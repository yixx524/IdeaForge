<!-- 左侧类别导航：全部 + enabled 类别列表，与 route.query.category 联动 -->
<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h3>类别</h3>
    </div>
    <nav class="category-nav">
      <button
        type="button"
        class="category-item"
        :class="{ active: !selectedCategory }"
        @click="selectCategory('')"
      >
        <span class="category-icon">▤</span>
        全部
      </button>
      <button
        v-for="item in categories"
        :key="item.code"
        type="button"
        class="category-item"
        :class="{ active: selectedCategory === item.code }"
        @click="selectCategory(item.code)"
      >
        <span class="category-dot" :style="dotStyle(item.code)" />
        {{ item.label }}
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listCategories } from '@/api/category'

const route = useRoute()
const router = useRouter()

const categories = ref([])

const selectedCategory = computed(() => route.query.category ?? '')

const PALETTE = ['#1d4ed8', '#15803d', '#a21caf', '#c2410c', '#0369a1', '#a16207']

onMounted(async () => {
  try {
    categories.value = await listCategories()
  } catch {
    categories.value = []
  }
})

function dotStyle(code) {
  let hash = 0
  for (let i = 0; i < code.length; i++) {
    hash = code.charCodeAt(i) + ((hash << 5) - hash)
  }
  return { background: PALETTE[Math.abs(hash) % PALETTE.length] }
}

function selectCategory(code) {
  const query = { ...route.query }
  if (code) {
    query.category = code
  } else {
    delete query.category
  }
  delete query.page

  if (route.name === 'idea-detail') {
    router.push({ path: '/browse', query })
    return
  }

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
  gap: 0.125rem;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition:
    background var(--transition),
    color var(--transition);
}

.category-item:hover {
  background: rgba(26, 74, 110, 0.05);
  color: var(--color-primary);
  transform: none;
}

.category-item.active {
  background: linear-gradient(135deg, rgba(26, 74, 110, 0.08), rgba(217, 160, 91, 0.1));
  color: var(--color-primary);
  font-weight: 600;
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
    gap: 0.375rem;
  }

  .category-item {
    width: auto;
    white-space: nowrap;
    flex-shrink: 0;
  }
}
</style>
