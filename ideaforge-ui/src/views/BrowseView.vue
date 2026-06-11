<!-- 浏览页：关键词搜索 + 类别侧栏联动 + 可点击结果列表 + 分页 -->
<template>
  <div class="page browse-page">
    <section class="card search-card">
      <div class="card-header">
        <h2>浏览知识</h2>
        <p class="card-hint">左侧选择类别，或输入关键词搜索标题、摘要、正文、标签</p>
      </div>

      <div class="search-bar">
        <div class="search-input-wrap">
          <span class="search-icon">⌕</span>
          <input
            v-model="keyword"
            type="search"
            placeholder="输入关键词…"
            @keyup.enter="applySearch"
          />
        </div>
        <button type="button" class="btn-primary" :disabled="loading" @click="applySearch">
          <span v-if="loading" class="spinner" />
          {{ loading ? '加载中…' : '搜索' }}
        </button>
      </div>
    </section>

    <Transition name="fade">
      <p v-if="error" class="toast toast-error">{{ error }}</p>
    </Transition>
    <Transition name="fade">
      <p v-if="success" class="toast toast-success">{{ success }}</p>
    </Transition>

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

    <nav v-if="totalPages > 1" class="pagination" aria-label="分页导航">
      <button
        type="button"
        class="pagination-btn"
        :disabled="loading || !hasPrevious"
        @click="goToPage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="pagination-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        type="button"
        class="pagination-btn"
        :disabled="loading || !hasNext"
        @click="goToPage(currentPage + 1)"
      >
        下一页
      </button>
    </nav>

    <div v-if="!loading && loaded && !error && totalElements === 0" class="empty-state">
      <div class="empty-icon">⌕</div>
      <p class="empty-title">未找到匹配条目</p>
      <p class="empty-hint">试试其他关键词或类别，或先去录入新想法</p>
      <RouterLink to="/create" class="btn-primary empty-action">去录入</RouterLink>
    </div>

    <div v-else-if="!loading && !loaded && !error" class="empty-state welcome-state">
      <p>正在加载知识库…</p>
    </div>

    <ConfirmModal
      :open="!!deleteTarget"
      title="确认删除"
      icon="delete"
      variant="danger"
      confirm-label="确认删除"
      loading-label="删除中…"
      :loading="deleting"
      hint="删除后浏览页不再显示，数据库记录保留。"
      @cancel="cancelDelete"
      @confirm="confirmDelete"
    >
      <template #message>
        确定删除「<strong>{{ deleteTarget?.finalTitle }}</strong>」？
      </template>
    </ConfirmModal>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { searchIdeas, deleteIdea } from '@/api/idea'
import { listCategories } from '@/api/category'
import { categoryLabel, toCategoryOptions } from '@/constants/categories'
import IdeaResultCard from '@/components/IdeaResultCard.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

const PAGE_SIZE = 20

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const loading = ref(false)
const deleting = ref(false)
const error = ref(null)
const success = ref(null)
const results = ref([])
const loaded = ref(false)
const allCategories = ref([])
const deleteTarget = ref(null)
const currentPage = ref(0)
const totalElements = ref(0)
const totalPages = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)

const activeCategoryLabel = computed(() => {
  const code = route.query.category
  if (!code) return ''
  return categoryLabel(code, allCategories.value)
})

watch(
  () => route.query,
  async (query) => {
    keyword.value = query.q ?? ''
    await fetchResults()
  },
  { immediate: true },
)

async function loadCategories() {
  try {
    const all = await listCategories({ all: true })
    allCategories.value = toCategoryOptions(all)
  } catch {
    allCategories.value = []
  }
}

loadCategories()

function resolveLabel(code) {
  return categoryLabel(code, allCategories.value)
}

function parsePage(query) {
  const raw = query.page
  if (raw == null || raw === '') return 0
  const n = Number.parseInt(String(raw), 10)
  return Number.isFinite(n) && n >= 0 ? n : 0
}

async function fetchResults() {
  loading.value = true
  error.value = null

  const q = (route.query.q ?? keyword.value).trim()
  const category = route.query.category ?? undefined
  const page = parsePage(route.query)

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
    hasNext.value = data.hasNext ?? false
    hasPrevious.value = data.hasPrevious ?? false
    loaded.value = true
  } catch (e) {
    error.value = e.message
    results.value = []
    totalElements.value = 0
    totalPages.value = 0
    loaded.value = true
  } finally {
    loading.value = false
  }
}

function applySearch() {
  const query = { ...route.query }
  const q = keyword.value.trim()
  if (q) {
    query.q = q
  } else {
    delete query.q
  }
  delete query.page
  router.push({ path: '/browse', query })
}

function goToPage(page) {
  if (page < 0 || (totalPages.value > 0 && page >= totalPages.value)) return

  const query = { ...route.query }
  if (page === 0) {
    delete query.page
  } else {
    query.page = String(page)
  }
  router.push({ path: '/browse', query })
}

function requestDelete(item) {
  deleteTarget.value = item
  success.value = null
}

function cancelDelete() {
  deleteTarget.value = null
}

async function confirmDelete() {
  if (!deleteTarget.value) return

  deleting.value = true
  error.value = null
  success.value = null
  const { id, finalTitle } = deleteTarget.value

  try {
    await deleteIdea(id)
    deleteTarget.value = null
    success.value = `已删除：${finalTitle}`

    if (results.value.length === 1 && currentPage.value > 0) {
      goToPage(currentPage.value - 1)
    } else {
      await fetchResults()
    }
  } catch (e) {
    error.value = e.message
  } finally {
    deleting.value = false
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

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 0.5rem 0 1rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-primary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition:
    background var(--transition),
    border-color var(--transition),
    opacity var(--transition);
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(26, 74, 110, 0.05);
  border-color: var(--color-primary);
}

.pagination-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  min-width: 4rem;
  text-align: center;
}

.welcome-state p {
  font-size: 0.9375rem;
  color: var(--color-text-muted);
  opacity: 0.6;
}

.empty-action {
  display: inline-flex;
  margin-top: 1rem;
  text-decoration: none;
}

@media (max-width: 480px) {
  .search-card .search-bar {
    flex-direction: column;
  }

  .pagination {
    gap: 0.75rem;
  }

  .pagination-btn {
    flex: 1;
  }
}
</style>
