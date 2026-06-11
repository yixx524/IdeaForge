<!-- 浏览页：关键词搜索 + 类别侧栏联动 + 可点击结果列表 + 分页 -->
<template>
  <div class="page browse-page">
    <section class="card search-card">
      <div class="card-header">
        <h2>浏览知识</h2>
        <p class="card-hint">左侧选择类别，或输入关键词搜索标题、摘要、正文、标签</p>
      </div>

      <div class="search-bar">
        <ElInput
          v-model="keyword"
          type="search"
          placeholder="输入关键词…"
          clearable
          class="search-input"
          @keyup.enter="applySearch"
        >
          <template #prefix>
            <span class="search-icon">⌕</span>
          </template>
        </ElInput>
        <ElButton type="primary" :loading="loading" @click="applySearch">
          {{ loading ? '加载中…' : '搜索' }}
        </ElButton>
      </div>
    </section>

    <div v-if="totalElements > 0" class="results-meta">
      共 <strong>{{ totalElements }}</strong> 条
      <span v-if="totalPages > 1"> · 第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
      <span v-if="activeCategoryLabel"> · {{ activeCategoryLabel }}</span>
      <span v-if="keyword.trim()"> · 关键词「{{ keyword.trim() }}」</span>
    </div>

    <section v-if="results.length" class="results">
      <IdeaResultCard
        v-for="item in results"
        :key="item.id"
        :item="item"
        :category-label="resolveLabel(item.finalCategory)"
        @delete="requestDelete"
      />
    </section>

    <div v-if="totalPages > 1" class="pagination-wrap">
      <ElPagination
        v-model:current-page="pageDisplay"
        :page-size="PAGE_SIZE"
        :total="totalElements"
        :disabled="loading"
        layout="prev, pager, next"
        background
        @current-change="onPageChange"
      />
    </div>

    <ElEmpty
      v-if="!loading && loaded && !error && totalElements === 0"
      description="未找到匹配条目"
    >
      <template #default>
        <p class="empty-hint">试试其他关键词或类别，或先去录入新想法</p>
        <RouterLink to="/create">
          <ElButton type="primary">去录入</ElButton>
        </RouterLink>
      </template>
    </ElEmpty>

    <div v-else-if="!loading && !loaded && !error" class="welcome-state">
      <p>正在加载知识库…</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { searchIdeas, deleteIdea } from '@/api/idea'
import { useCategoryStore } from '@/stores/category'
import IdeaResultCard from '@/components/IdeaResultCard.vue'
import { confirmDelete, showError, showSuccess } from '@/utils/message'
import type { IdeaResponse } from '@/types'

const PAGE_SIZE = 20

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()

const keyword = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const results = ref<IdeaResponse[]>([])
const loaded = ref(false)
const currentPage = ref(0)
const totalElements = ref(0)
const totalPages = ref(0)

const pageDisplay = computed({
  get: () => currentPage.value + 1,
  set: () => {},
})

const activeCategoryLabel = computed(() => {
  const code = route.query.category
  if (!code || typeof code !== 'string') return ''
  return categoryStore.labelOf(code)
})

watch(
  () => route.query,
  async () => {
    keyword.value = typeof route.query.q === 'string' ? route.query.q : ''
    await fetchResults()
  },
  { immediate: true },
)

watch(error, (msg) => {
  if (msg) showError(msg)
})

function resolveLabel(code: string) {
  return categoryStore.labelOf(code)
}

function parsePage(query: Record<string, unknown>): number {
  const raw = query.page
  if (raw == null || raw === '') return 0
  const n = Number.parseInt(String(raw), 10)
  return Number.isFinite(n) && n >= 0 ? n : 0
}

async function fetchResults() {
  loading.value = true
  error.value = null

  const q = (typeof route.query.q === 'string' ? route.query.q : keyword.value).trim()
  const category =
    typeof route.query.category === 'string' ? route.query.category : undefined
  const page = parsePage(route.query as Record<string, unknown>)

  try {
    const data = await searchIdeas({
      q: q || undefined,
      category: category || undefined,
      page,
      size: PAGE_SIZE,
    })
    results.value = data.content ?? []
    currentPage.value = data.page ?? 0
    totalElements.value = data.totalElements ?? 0
    totalPages.value = data.totalPages ?? 0
    loaded.value = true
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
    results.value = []
    totalElements.value = 0
    totalPages.value = 0
    loaded.value = true
  } finally {
    loading.value = false
  }
}

function applySearch() {
  const query: Record<string, string> = { ...route.query } as Record<string, string>
  const q = keyword.value.trim()
  if (q) {
    query.q = q
  } else {
    delete query.q
  }
  delete query.page
  router.push({ path: '/browse', query })
}

function goToPage(page: number) {
  if (page < 0 || (totalPages.value > 0 && page >= totalPages.value)) return

  const query: Record<string, string> = { ...route.query } as Record<string, string>
  if (page === 0) {
    delete query.page
  } else {
    query.page = String(page)
  }
  router.push({ path: '/browse', query })
}

function onPageChange(pageOneBased: number) {
  goToPage(pageOneBased - 1)
}

async function requestDelete(item: IdeaResponse) {
  const confirmed = await confirmDelete({
    message: `确定删除「<strong>${item.finalTitle}</strong>」？`,
    hint: '删除后浏览页不再显示，数据库记录保留。',
  })
  if (!confirmed) return

  error.value = null

  try {
    await deleteIdea(item.id)
    showSuccess(`已删除：${item.finalTitle}`)

    if (results.value.length === 1 && currentPage.value > 0) {
      goToPage(currentPage.value - 1)
    } else {
      await fetchResults()
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
  }
}
</script>

<style scoped>
.browse-page {
  gap: 1rem;
}

.search-card .search-bar {
  display: flex;
  gap: 0.75rem;
  padding: 1rem 1.5rem 1.5rem;
  align-items: center;
}

.search-input {
  flex: 1;
}

.search-icon {
  color: var(--color-text-muted);
  font-size: 1.125rem;
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

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 0.5rem 0 1rem;
}

.empty-hint {
  margin-bottom: 1rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.welcome-state p {
  font-size: 0.9375rem;
  color: var(--color-text-muted);
  opacity: 0.6;
  text-align: center;
  padding: 2rem;
}

@media (max-width: 480px) {
  .search-card .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
