<!-- 知识条目详情页：查看 / 编辑模式 -->
<template>
  <div class="page detail-page">
    <div v-if="loading" class="loading-state" v-loading="true">
      <p>加载中…</p>
    </div>

    <template v-else-if="idea">
      <div class="detail-toolbar">
        <RouterLink :to="backTo">
          <ElButton>← 返回浏览</ElButton>
        </RouterLink>
        <div class="toolbar-actions">
          <template v-if="!editing">
            <ElButton @click="startEdit">编辑</ElButton>
            <ElButton type="primary" :loading="exporting" @click="handleExport">
              {{ exporting ? '导出中…' : '导出 Word' }}
            </ElButton>
          </template>
          <template v-else>
            <ElButton :disabled="saving" @click="cancelEdit">取消</ElButton>
            <ElButton type="primary" :loading="saving" @click="handleSave">
              {{ saving ? '保存中…' : '保存' }}
            </ElButton>
          </template>
        </div>
      </div>

      <!-- 查看模式 -->
      <article v-if="!editing" class="card detail-card">
        <header class="detail-header">
          <h2>{{ idea.finalTitle }}</h2>
          <ElTag size="small" effect="light" :style="categoryTagStyle(idea.finalCategory)">
            {{ categoryLabelText }}
          </ElTag>
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
            <ElTag v-for="tag in idea.finalTags" :key="tag" size="small" type="info">
              {{ tag }}
            </ElTag>
          </div>
        </section>

        <section class="detail-section">
          <h3>正文</h3>
          <RichTextContent :content="displayContent" />
        </section>

        <ElCollapse class="detail-section">
          <ElCollapseItem title="原始正文（只读）" name="original">
            <div class="original-content">{{ idea.originalContent }}</div>
          </ElCollapseItem>
        </ElCollapse>
      </article>

      <!-- 编辑模式 -->
      <article v-else class="card detail-card edit-card">
        <div class="card-header">
          <h2>编辑知识条目</h2>
          <p class="card-hint">修改最终标题、摘要、标签、类别与排版正文，或使用 AI 重新整理</p>
        </div>

        <div class="ai-panel">
          <div class="ai-panel-header">
            <span class="ai-panel-dot" />
            AI 重新整理
          </div>
          <div class="ai-panel-row">
            <div class="ai-panel-sources">
              <p class="ai-panel-label">整理来源</p>
              <ElRadioGroup v-model="reprocessSource">
                <ElRadio value="original">原始正文</ElRadio>
                <ElRadio value="current">当前排版正文</ElRadio>
              </ElRadioGroup>
            </div>
            <ElButton
              :loading="reprocessing"
              :disabled="saving"
              @click="requestReprocess"
            >
              {{ reprocessing ? 'AI 整理中…' : '开始整理' }}
            </ElButton>
          </div>
          <p v-if="reprocessing" class="ai-panel-stream-hint">内容正在实时生成，请稍候…</p>
        </div>

        <ElForm label-position="top" class="edit-form">
          <ElFormItem label="最终标题" required>
            <ElInput v-model="editForm.title" :disabled="reprocessing" />
          </ElFormItem>
          <ElFormItem label="最终摘要">
            <ElInput v-model="editForm.summary" type="textarea" :rows="3" :disabled="reprocessing" />
          </ElFormItem>
          <ElFormItem label="标签">
            <ElSelect
              v-model="editFormTags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入后回车添加标签"
              :disabled="reprocessing"
              style="width: 100%"
            />
          </ElFormItem>
          <ElFormItem label="类别" required>
            <ElSelect v-model="editForm.category" :disabled="reprocessing" style="width: 100%">
              <ElOption
                v-for="item in enabledCategories"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="排版正文">
            <div v-if="reprocessStreaming" class="stream-preview">
              <RichTextContent :content="reprocessStreamContent || '…'" />
              <span class="stream-cursor" aria-hidden="true" />
            </div>
            <RichTextEditor v-else v-model="editForm.content" min-height="360px" />
          </ElFormItem>
        </ElForm>
      </article>
    </template>

    <ElEmpty v-else-if="error" :description="error">
      <RouterLink :to="backTo">
        <ElButton>返回浏览</ElButton>
      </RouterLink>
    </ElEmpty>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import {
  getIdeaById,
  exportIdeaDocx,
  updateIdea,
  processIdeaStream,
  applyProcessResult,
} from '@/api/idea'
import { useCategoryStore } from '@/stores/category'
import RichTextContent from '@/components/RichTextContent.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { isEmptyHtml, toEditorHtml, htmlToPlainText } from '@/utils/contentHtml'
import { categoryTagStyle } from '@/utils/categoryColor'
import { confirmAction, showError, showSuccess } from '@/utils/message'
import type { IdeaProcessRequest, IdeaResponse } from '@/types'

const route = useRoute()
const categoryStore = useCategoryStore()
const { enabledOptions: enabledCategories } = storeToRefs(categoryStore)

const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const reprocessing = ref(false)
const reprocessStreaming = ref(false)
const reprocessStreamContent = ref('')
const exporting = ref(false)
const reprocessSource = ref<'original' | 'current'>('original')
const error = ref<string | null>(null)
const idea = ref<IdeaResponse | null>(null)
const hasSuggestion = ref(false)

const editForm = reactive({
  title: '',
  summary: '',
  tagsText: '',
  category: '',
  content: '',
})

const editFormTags = computed({
  get: () =>
    editForm.tagsText
      ? editForm.tagsText.split(/[,，]/).map((t) => t.trim()).filter(Boolean)
      : [],
  set: (tags: string[]) => {
    editForm.tagsText = tags.join('，')
  },
})

const suggestion = reactive({
  suggestedTitle: '',
  suggestedSummary: '',
  suggestedTags: [] as string[],
  suggestedCategory: '',
  suggestedContent: '',
})

let reprocessAbortController: AbortController | null = null

onBeforeUnmount(() => {
  reprocessAbortController?.abort()
})

function applyReprocessPartial(partial: Parameters<typeof applyProcessResult>[2]) {
  applyProcessResult(editForm, suggestion, partial, enabledCategories.value)
  if (partial.suggestedContent != null) {
    reprocessStreamContent.value = partial.suggestedContent
  }
}

function finalizeReprocessResult(
  result: Parameters<typeof applyProcessResult>[2],
  fallbackContent: string,
) {
  applyReprocessPartial(result)
  editForm.content = toEditorHtml(result.suggestedContent ?? fallbackContent)
  hasSuggestion.value = true
}

const backTo = computed(() => {
  const query: Record<string, string> = {}
  if (route.query.category && typeof route.query.category === 'string') {
    query.category = route.query.category
  }
  if (route.query.q && typeof route.query.q === 'string') {
    query.q = route.query.q
  }
  return { path: '/browse', query }
})

const categoryLabelText = computed(() =>
  categoryStore.labelOf(idea.value?.finalCategory),
)

const displayContent = computed(() =>
  idea.value?.finalContent || idea.value?.originalContent || '',
)

function fillEditForm(data: IdeaResponse) {
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

  const id = String(route.params.id)

  try {
    const [data] = await Promise.all([
      getIdeaById(id),
      categoryStore.ensureLoaded(),
    ])
    idea.value = data
    fillEditForm(data)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)

function resetSuggestion() {
  hasSuggestion.value = false
  suggestion.suggestedTitle = ''
  suggestion.suggestedSummary = ''
  suggestion.suggestedTags = []
  suggestion.suggestedCategory = ''
  suggestion.suggestedContent = ''
}

function startEdit() {
  if (!idea.value) return
  fillEditForm(idea.value)
  reprocessSource.value = 'original'
  resetSuggestion()
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  resetSuggestion()
}

function buildProcessPayload(): IdeaProcessRequest {
  if (reprocessSource.value === 'original') {
    const content = idea.value?.originalContent?.trim()
    if (!content) {
      throw new Error('原始正文为空，无法整理')
    }
    return {
      originalTitle: idea.value?.originalTitle?.trim() || undefined,
      originalContent: content,
    }
  }

  const content = htmlToPlainText(editForm.content)
  if (!content) {
    throw new Error('当前排版正文为空，无法整理')
  }
  return {
    originalTitle: editForm.title.trim() || undefined,
    originalContent: content,
  }
}

async function requestReprocess() {
  try {
    buildProcessPayload()
  } catch (e) {
    showError(e instanceof Error ? e.message : '无法整理')
    return
  }

  const confirmed = await confirmAction({
    title: 'AI 重新整理',
    message: '重新整理将覆盖当前编辑区中的标题、摘要、标签、类别与排版正文，是否继续？',
    confirmLabel: '开始整理',
    type: 'info',
  })
  if (!confirmed) return

  await handleReprocess()
}

async function handleReprocess() {
  let payload: IdeaProcessRequest
  try {
    payload = buildProcessPayload()
  } catch (e) {
    showError(e instanceof Error ? e.message : '无法整理')
    return
  }

  reprocessAbortController?.abort()
  reprocessAbortController = new AbortController()

  reprocessing.value = true
  reprocessStreaming.value = true
  reprocessStreamContent.value = ''

  try {
    await processIdeaStream(
      payload,
      {
        onPartial: applyReprocessPartial,
        onComplete: (result) => {
          finalizeReprocessResult(result, payload.originalContent)
          showSuccess('AI 重新整理完成，请核对后保存')
        },
      },
      { signal: reprocessAbortController.signal },
    )
  } catch (e) {
    if (!(e instanceof DOMException && e.name === 'AbortError')) {
      showError(e instanceof Error ? e.message : 'AI 整理失败')
    }
  } finally {
    reprocessing.value = false
    reprocessStreaming.value = false
    reprocessAbortController = null
  }
}

async function handleSave() {
  if (!editForm.title.trim()) {
    showError('请填写最终标题')
    return
  }

  saving.value = true

  const id = String(route.params.id)

  try {
    const payload: Parameters<typeof updateIdea>[1] = {
      finalTitle: editForm.title.trim(),
      finalSummary: editForm.summary.trim() || null,
      finalTags: editFormTags.value,
      finalCategory: editForm.category,
      finalContent: isEmptyHtml(editForm.content) ? null : editForm.content.trim(),
    }

    if (hasSuggestion.value) {
      payload.suggestedTitle = suggestion.suggestedTitle
      payload.suggestedSummary = suggestion.suggestedSummary
      payload.suggestedTags = suggestion.suggestedTags
      payload.suggestedCategory = suggestion.suggestedCategory
      payload.suggestedContent = suggestion.suggestedContent
    }

    idea.value = await updateIdea(id, payload)
    editing.value = false
    resetSuggestion()
    showSuccess('保存成功')
  } catch (e) {
    showError(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

function formatDate(value: string | null | undefined) {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}

async function handleExport() {
  if (!idea.value?.id) return
  exporting.value = true
  try {
    const filename = await exportIdeaDocx(idea.value.id)
    showSuccess(`已下载：${filename}`)
  } catch (e) {
    showError(e instanceof Error ? e.message : '导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.detail-page {
  gap: 1rem;
}

.loading-state {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.detail-card {
  padding: 1.5rem;
}

.edit-form {
  margin-top: 1rem;
}

.ai-panel-sources {
  flex: 1;
  min-width: 0;
}

.ai-panel-stream-hint {
  margin: 0.75rem 0 0;
  font-size: 0.8125rem;
  color: var(--color-primary);
}

.stream-preview {
  position: relative;
  min-height: 360px;
  width: 100%;
}

.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  vertical-align: text-bottom;
  background: var(--color-accent);
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
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

.original-content {
  white-space: pre-wrap;
  font-size: 0.875rem;
  color: var(--color-text-muted);
  padding: 0.75rem 1rem;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  line-height: 1.6;
}

@media (max-width: 640px) {
  .ai-panel-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
