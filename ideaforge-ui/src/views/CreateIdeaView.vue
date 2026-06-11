<!-- 录入想法页：两阶段流程 input（原始输入）→ review（编辑 AI 建议并保存） -->
<template>
  <div class="page">
    <ElSteps :active="stepIndex" finish-status="success" align-center class="steps">
      <ElStep title="录入想法" />
      <ElStep title="确认保存" />
    </ElSteps>

    <!-- 阶段一：用户输入原始标题与正文 -->
    <section v-if="step === 'input'" class="card">
      <div class="card-header">
        <h2>录入想法</h2>
        <p class="card-hint">输入原始标题与正文，或上传 Word / PDF 文档，由 AI 整理为结构化信息</p>
      </div>

      <div class="upload-section">
        <ElUpload
          drag
          :auto-upload="false"
          :show-file-list="false"
          accept=".docx,.pdf,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          :disabled="parsing"
          @change="onUploadChange"
        >
          <div class="upload-inner">
            <p class="upload-title">拖拽文件到此处，或点击选择</p>
            <p class="upload-hint">支持 .docx / .pdf，单文件最大 10MB</p>
          </div>
        </ElUpload>
        <div v-if="uploadedFileName" class="upload-meta">
          <span>已选：{{ uploadedFileName }}</span>
          <span v-if="uploadedCharCount">（约 {{ uploadedCharCount }} 字）</span>
          <ElButton link type="primary" @click="clearUpload">清除</ElButton>
        </div>
      </div>

      <div class="input-divider">
        <span>或手动输入</span>
      </div>

      <ElForm label-position="top" class="form-body">
        <ElFormItem label="标题（可选）">
          <ElInput v-model="original.title" placeholder="给想法起个简短标题…" />
        </ElFormItem>
        <ElFormItem label="正文" required>
          <ElInput
            v-model="original.content"
            type="textarea"
            :rows="8"
            placeholder="写下你的想法、笔记或灵感…"
          />
        </ElFormItem>
      </ElForm>

      <div class="card-footer">
        <ElButton type="primary" :loading="loading" @click="handleProcess">
          {{ loading ? 'AI 整理中…' : '开始 AI 整理' }}
        </ElButton>
      </div>
    </section>

    <!-- 阶段二：展示 AI 建议，用户可编辑后确认入库 -->
    <section v-else class="card">
      <div class="card-header">
        <h2>确认并保存</h2>
        <p class="card-hint">可编辑 AI 建议后确认入库</p>
      </div>

      <div class="ai-badge">
        <span class="ai-dot" />
        {{ streaming ? 'AI 正在整理，内容实时生成中…' : 'AI 已整理完成，请核对以下内容' }}
      </div>

      <ElForm label-position="top" class="form-body">
        <ElFormItem label="最终标题" required>
          <ElInput v-model="finalForm.title" :disabled="streaming" />
        </ElFormItem>
        <ElFormItem label="最终摘要">
          <ElInput v-model="finalForm.summary" type="textarea" :rows="3" placeholder="一句话概括…" :disabled="streaming" />
        </ElFormItem>
        <ElFormItem label="标签">
          <ElSelect
            v-model="finalFormTags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入后回车添加标签"
            :disabled="streaming"
            style="width: 100%"
          />
          <p class="field-hint">可输入多个标签，回车确认</p>
        </ElFormItem>
        <ElFormItem label="类别" required>
          <ElSelect v-model="finalForm.category" :disabled="streaming" style="width: 100%">
            <ElOption
              v-for="item in categories"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="排版正文">
          <div v-if="streaming" class="stream-preview">
            <RichTextContent :content="streamContent || '…'" />
            <span class="stream-cursor" aria-hidden="true" />
          </div>
          <RichTextEditor v-else v-model="finalForm.content" min-height="320px" />
          <p class="field-hint">支持标题、加粗、列表等富文本格式</p>
        </ElFormItem>
      </ElForm>

      <div class="card-footer actions">
        <ElButton :disabled="loading && !streaming" @click="handleBack">
          {{ streaming ? '取消整理' : '返回修改' }}
        </ElButton>
        <ElButton type="primary" :loading="loading && !streaming" :disabled="streaming" @click="handleSave">
          {{ loading && !streaming ? '保存中…' : '确认保存' }}
        </ElButton>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { UploadFile } from 'element-plus'
import { parseDocument, processIdeaStream, saveIdea, applyProcessResult } from '@/api/idea'
import { useCategoryStore } from '@/stores/category'
import RichTextEditor from '@/components/RichTextEditor.vue'
import RichTextContent from '@/components/RichTextContent.vue'
import { isEmptyHtml, toEditorHtml } from '@/utils/contentHtml'
import { showError, showSuccess } from '@/utils/message'

type Step = 'input' | 'review'

const router = useRouter()
const categoryStore = useCategoryStore()
const { enabledOptions: categories } = storeToRefs(categoryStore)

const step = ref<Step>('input')
const stepIndex = computed(() => (step.value === 'input' ? 0 : 1))
const loading = ref(false)
const streaming = ref(false)
const streamContent = ref('')
const parsing = ref(false)
const uploadedFileName = ref('')
const uploadedCharCount = ref(0)
const original = reactive({
  title: '',
  content: '',
})

const suggestion = reactive({
  suggestedTitle: '',
  suggestedSummary: '',
  suggestedTags: [] as string[],
  suggestedCategory: '',
  suggestedContent: '',
})

const finalForm = reactive({
  title: '',
  summary: '',
  tagsText: '',
  category: '',
  content: '',
})

const finalFormTags = computed({
  get: () =>
    finalForm.tagsText
      ? finalForm.tagsText.split(/[,，]/).map((t) => t.trim()).filter(Boolean)
      : [],
  set: (tags: string[]) => {
    finalForm.tagsText = tags.join('，')
  },
})

let processAbortController: AbortController | null = null

onBeforeUnmount(() => {
  processAbortController?.abort()
})

function resetReviewForm() {
  finalForm.title = ''
  finalForm.summary = ''
  finalForm.tagsText = ''
  finalForm.content = ''
  streamContent.value = ''
  Object.assign(suggestion, {
    suggestedTitle: '',
    suggestedSummary: '',
    suggestedTags: [],
    suggestedCategory: '',
    suggestedContent: '',
  })
}

function applyStreamPartial(partial: Parameters<typeof applyProcessResult>[2]) {
  applyProcessResult(finalForm, suggestion, partial, categories.value)
  if (partial.suggestedContent != null) {
    streamContent.value = partial.suggestedContent
  }
}

function finalizeStreamResult(result: Parameters<typeof applyProcessResult>[2]) {
  applyStreamPartial(result)
  finalForm.content = toEditorHtml(result.suggestedContent ?? original.content.trim())
}

onMounted(async () => {
  try {
    await categoryStore.ensureLoaded()
    if (categories.value.length && !finalForm.category) {
      const defaultCat =
        categories.value.find((c) => c.value === 'INSPIRATION') ?? categories.value[0]
      finalForm.category = defaultCat.value
    }
  } catch (e) {
    showError(e instanceof Error ? e.message : '加载类别失败')
  }
})

async function onUploadChange(uploadFile: UploadFile) {
  const file = uploadFile.raw
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    showError('文件大小不能超过 10MB')
    return
  }

  parsing.value = true

  try {
    const result = await parseDocument(file)
    uploadedFileName.value = result.fileName ?? file.name
    uploadedCharCount.value = result.charCount ?? result.extractedContent?.length ?? 0
    original.content = result.extractedContent ?? ''
    if (result.extractedTitle && !original.title.trim()) {
      original.title = result.extractedTitle
    }
    showSuccess(`已从「${uploadedFileName.value}」提取文本，请核对后点击 AI 整理`)
  } catch (e) {
    showError(e instanceof Error ? e.message : '解析失败')
    clearUpload()
  } finally {
    parsing.value = false
  }
}

function clearUpload() {
  uploadedFileName.value = ''
  uploadedCharCount.value = 0
}

async function handleProcess() {
  if (!original.content.trim()) {
    showError('请输入想法正文')
    return
  }

  processAbortController?.abort()
  processAbortController = new AbortController()

  loading.value = true
  streaming.value = true
  resetReviewForm()
  step.value = 'review'

  const payload = {
    originalTitle: original.title.trim() || undefined,
    originalContent: original.content.trim(),
  }

  try {
    await processIdeaStream(
      payload,
      {
        onPartial: applyStreamPartial,
        onComplete: finalizeStreamResult,
      },
      { signal: processAbortController.signal },
    )
  } catch (e) {
    if (e instanceof DOMException && e.name === 'AbortError') {
      step.value = 'input'
      return
    }
    showError(e instanceof Error ? e.message : 'AI 整理失败')
    step.value = 'input'
  } finally {
    loading.value = false
    streaming.value = false
    processAbortController = null
  }
}

async function handleSave() {
  if (!finalForm.title.trim()) {
    showError('请填写最终标题')
    return
  }

  loading.value = true

  try {
    const saved = await saveIdea({
      originalTitle: original.title.trim() || undefined,
      originalContent: original.content.trim(),
      suggestedTitle: suggestion.suggestedTitle,
      suggestedSummary: suggestion.suggestedSummary || null,
      suggestedTags: suggestion.suggestedTags,
      suggestedCategory: suggestion.suggestedCategory,
      suggestedContent: suggestion.suggestedContent,
      finalTitle: finalForm.title.trim(),
      finalSummary: finalForm.summary.trim() || null,
      finalTags: finalFormTags.value,
      finalCategory: finalForm.category,
      finalContent: isEmptyHtml(finalForm.content)
        ? toEditorHtml(original.content.trim())
        : finalForm.content.trim(),
    })

    await router.push({ name: 'idea-detail', params: { id: saved.id } })
  } catch (e) {
    showError(e instanceof Error ? e.message : '保存失败')
  } finally {
    loading.value = false
  }
}

function handleBack() {
  if (streaming.value) {
    processAbortController?.abort()
    return
  }
  step.value = 'input'
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.steps {
  padding: 0.5rem 0;
}

.upload-section {
  padding: 0 1.5rem;
  margin-top: 1rem;
}

.upload-inner {
  padding: 1rem;
}

.upload-title {
  font-size: 0.9375rem;
  color: var(--color-text);
  margin-bottom: 0.25rem;
}

.upload-hint {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.upload-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.input-divider {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1.5rem 0;
  color: var(--color-text-muted);
  font-size: 0.8125rem;
}

.input-divider::before,
.input-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.form-body {
  padding: 0 1.5rem;
}

.ai-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 1.25rem 1.5rem 0;
  padding: 0.625rem 1rem;
  background: linear-gradient(135deg, rgba(26, 74, 110, 0.06), rgba(217, 160, 91, 0.1));
  border-radius: var(--radius-md);
  font-size: 0.8125rem;
  color: var(--color-primary);
  font-weight: 500;
}

.ai-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-accent);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.field-hint {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.stream-preview {
  position: relative;
  min-height: 320px;
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

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}
</style>
