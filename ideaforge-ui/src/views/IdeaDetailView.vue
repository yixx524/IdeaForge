<!-- 知识条目详情页：查看 / 编辑模式 -->
<template>
  <div class="page detail-page">
    <div v-if="loading" class="empty-state">
      <span class="spinner detail-spinner" />
      <p>加载中…</p>
    </div>

    <template v-else-if="idea">
      <div class="detail-toolbar">
        <RouterLink :to="backTo" class="btn-secondary back-link">← 返回浏览</RouterLink>
        <div class="toolbar-actions">
          <template v-if="!editing">
            <button type="button" class="btn-secondary" @click="startEdit">编辑</button>
            <button
              type="button"
              class="btn-primary"
              :disabled="exporting"
              @click="handleExport"
            >
              <span v-if="exporting" class="spinner" />
              {{ exporting ? '导出中…' : '导出 Word' }}
            </button>
          </template>
          <template v-else>
            <button type="button" class="btn-secondary" :disabled="saving" @click="cancelEdit">
              取消
            </button>
            <button type="button" class="btn-primary" :disabled="saving" @click="handleSave">
              <span v-if="saving" class="spinner" />
              {{ saving ? '保存中…' : '保存' }}
            </button>
          </template>
        </div>
      </div>

      <Transition name="fade">
        <p v-if="message" class="toast toast-success">{{ message }}</p>
      </Transition>
      <Transition name="fade">
        <p v-if="actionError" class="toast toast-error">{{ actionError }}</p>
      </Transition>

      <!-- 查看模式 -->
      <article v-if="!editing" class="card detail-card">
        <header class="detail-header">
          <h2>{{ idea.finalTitle }}</h2>
          <CategoryBadge :code="idea.finalCategory" :label="categoryLabelText" />
        </header>

        <div class="detail-meta">
          <span class="meta-item">
            <span class="meta-icon">◷</span>
            {{ formatDate(idea.createdAt) }}
          </span>
        </div>

        <section v-if="idea.finalSummary" class="detail-section">
          <h3>摘要</h3>
          <p class="summary-text">{{ idea.finalSummary }}</p>
        </section>

        <section v-if="idea.finalTags?.length" class="detail-section">
          <h3>标签</h3>
          <div class="tags">
            <span v-for="tag in idea.finalTags" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </section>

        <section class="detail-section">
          <h3>正文</h3>
          <RichTextContent :content="displayContent" />
        </section>

        <details class="detail-section original-block">
          <summary>原始正文（只读）</summary>
          <div class="original-content">{{ idea.originalContent }}</div>
        </details>
      </article>

      <!-- 编辑模式 -->
      <article v-else class="card detail-card edit-card">
        <div class="card-header">
          <h2>编辑知识条目</h2>
          <p class="card-hint">修改最终标题、摘要、标签、类别与排版正文</p>
        </div>

        <label class="field">
          <span class="field-label">最终标题</span>
          <input v-model="editForm.title" type="text" />
        </label>

        <label class="field">
          <span class="field-label">最终摘要</span>
          <textarea v-model="editForm.summary" rows="3" />
        </label>

        <label class="field">
          <span class="field-label">标签</span>
          <input v-model="editForm.tagsText" type="text" placeholder="标签1，标签2" />
          <span class="field-hint">多个标签用逗号分隔</span>
        </label>

        <label class="field">
          <span class="field-label">类别</span>
          <select v-model="editForm.category">
            <option v-for="item in enabledCategories" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>

        <label class="field field-rich">
          <span class="field-label">排版正文</span>
          <RichTextEditor v-model="editForm.content" min-height="360px" />
          <span class="field-hint">支持标题、加粗、列表等富文本格式</span>
        </label>
      </article>
    </template>

    <div v-else-if="error" class="empty-state">
      <div class="empty-icon">!</div>
      <p class="empty-title">{{ error }}</p>
      <RouterLink :to="backTo" class="btn-secondary">返回浏览</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getIdeaById, exportIdeaDocx, updateIdea } from '@/api/idea'
import { listCategories } from '@/api/category'
import { categoryLabel, toCategoryOptions } from '@/constants/categories'
import CategoryBadge from '@/components/CategoryBadge.vue'
import RichTextContent from '@/components/RichTextContent.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { isEmptyHtml, toEditorHtml } from '@/utils/contentHtml'

const route = useRoute()

const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const exporting = ref(false)
const error = ref(null)
const message = ref(null)
const actionError = ref(null)
const idea = ref(null)
const allCategories = ref([])
const enabledCategories = ref([])

const editForm = reactive({
  title: '',
  summary: '',
  tagsText: '',
  category: '',
  content: '',
})

const backTo = computed(() => {
  const query = {}
  if (route.query.category) query.category = route.query.category
  if (route.query.q) query.q = route.query.q
  return { path: '/browse', query }
})

const categoryLabelText = computed(() =>
  categoryLabel(idea.value?.finalCategory, allCategories.value),
)

const displayContent = computed(() =>
  idea.value?.finalContent || idea.value?.originalContent || '',
)

function parseTags(text) {
  if (!text?.trim()) return []
  return text.split(/[,，]/).map((tag) => tag.trim()).filter(Boolean)
}

function fillEditForm(data) {
  editForm.title = data.finalTitle ?? ''
  editForm.summary = data.finalSummary ?? ''
  editForm.tagsText = (data.finalTags ?? []).join('，')
  editForm.category = data.finalCategory ?? ''
  editForm.content = toEditorHtml(data.finalContent || data.originalContent || '')
}

async function loadDetail() {
  loading.value = true
  error.value = null
  idea.value = null
  editing.value = false

  try {
    const [data, enabled, all] = await Promise.all([
      getIdeaById(route.params.id),
      listCategories(),
      listCategories({ all: true }),
    ])
    idea.value = data
    enabledCategories.value = toCategoryOptions(enabled)
    allCategories.value = toCategoryOptions(all)
    fillEditForm(data)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)

function startEdit() {
  fillEditForm(idea.value)
  editing.value = true
  message.value = null
  actionError.value = null
}

function cancelEdit() {
  editing.value = false
  actionError.value = null
}

async function handleSave() {
  if (!editForm.title.trim()) {
    actionError.value = '请填写最终标题'
    return
  }

  saving.value = true
  actionError.value = null
  message.value = null

  try {
    idea.value = await updateIdea(route.params.id, {
      finalTitle: editForm.title.trim(),
      finalSummary: editForm.summary.trim() || null,
      finalTags: parseTags(editForm.tagsText),
      finalCategory: editForm.category,
      finalContent: isEmptyHtml(editForm.content)
        ? null
        : editForm.content.trim(),
    })
    editing.value = false
    message.value = '保存成功'
  } catch (e) {
    actionError.value = e.message
  } finally {
    saving.value = false
  }
}

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}

async function handleExport() {
  if (!idea.value?.id) return
  exporting.value = true
  message.value = null
  actionError.value = null
  try {
    const filename = await exportIdeaDocx(idea.value.id)
    message.value = `已下载：${filename}`
  } catch (e) {
    actionError.value = e.message
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.detail-page {
  gap: 1rem;
}

.detail-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.back-link {
  text-decoration: none;
}

.detail-card {
  padding: 1.5rem;
}

.edit-card .card-header {
  padding: 0 0 0.5rem;
}

.edit-card .field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  margin-top: 1.25rem;
  font-size: 0.875rem;
}

.edit-card input,
.edit-card textarea,
.edit-card select {
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.9375rem;
  background: var(--color-surface);
  color: var(--color-text);
}

.edit-card textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.5;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.detail-header h2 {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1.35;
}

.detail-meta {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.detail-section {
  margin-bottom: 1.5rem;
}

.detail-section h3 {
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 0.5rem;
}

.summary-text {
  font-size: 1rem;
  color: var(--color-text);
  line-height: 1.6;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
}

.tag {
  padding: 0.2rem 0.625rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.original-block summary {
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 0.5rem;
}

.original-content {
  white-space: pre-wrap;
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  line-height: 1.6;
}

.detail-spinner {
  width: 24px;
  height: 24px;
  border-width: 3px;
  margin-bottom: 0.75rem;
}
</style>
