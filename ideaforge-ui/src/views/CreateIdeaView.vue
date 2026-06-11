<!-- 录入想法页：两阶段流程 input（原始输入）→ review（编辑 AI 建议并保存） -->
<template>
  <div class="page">
    <!-- 步骤指示器 -->
    <div class="steps">
      <div class="step" :class="{ active: step === 'input', done: step === 'review' }">
        <span class="step-num">1</span>
        <span class="step-label">录入想法</span>
      </div>
      <div class="step-line" :class="{ done: step === 'review' }" />
      <div class="step" :class="{ active: step === 'review' }">
        <span class="step-num">2</span>
        <span class="step-label">确认保存</span>
      </div>
    </div>

    <!-- 阶段一：用户输入原始标题与正文 -->
    <section v-if="step === 'input'" class="card">
      <div class="card-header">
        <h2>录入想法</h2>
        <p class="hint">输入原始标题与正文，或上传 Word / PDF 文档，由 AI 整理为结构化信息</p>
      </div>

      <FileUploadZone
        :loading="parsing"
        :file-name="uploadedFileName"
        :char-count="uploadedCharCount"
        @select="handleFileUpload"
        @clear="clearUpload"
      />

      <div class="input-divider">
        <span>或手动输入</span>
      </div>

      <label class="field">
        <span class="label">标题 <em class="optional">可选</em></span>
        <input v-model="original.title" type="text" placeholder="给想法起个简短标题…" />
      </label>

      <label class="field">
        <span class="label">正文</span>
        <textarea
          v-model="original.content"
          rows="8"
          placeholder="写下你的想法、笔记或灵感…"
        />
      </label>

      <div class="card-footer">
        <button type="button" class="btn-primary" :disabled="loading" @click="handleProcess">
          <span v-if="loading" class="spinner" />
          {{ loading ? 'AI 整理中…' : '开始 AI 整理' }}
        </button>
      </div>
    </section>

    <!-- 阶段二：展示 AI 建议，用户可编辑后确认入库 -->
    <section v-else class="card">
      <div class="card-header">
        <h2>确认并保存</h2>
        <p class="hint">可编辑 AI 建议后确认入库</p>
      </div>

      <div class="ai-badge">
        <span class="ai-dot" />
        AI 已整理完成，请核对以下内容
      </div>

      <label class="field">
        <span class="label">最终标题</span>
        <input v-model="finalForm.title" type="text" />
      </label>

      <label class="field">
        <span class="label">最终摘要</span>
        <textarea v-model="finalForm.summary" rows="3" placeholder="一句话概括…" />
      </label>

      <label class="field">
        <span class="label">标签</span>
        <input v-model="finalForm.tagsText" type="text" placeholder="标签1，标签2，标签3" />
        <span class="field-hint">多个标签用逗号分隔</span>
      </label>

      <label class="field">
        <span class="label">类别</span>
        <select v-model="finalForm.category">
          <option v-for="item in categories" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>

      <label class="field">
        <span class="label">排版正文</span>
        <textarea
          v-model="finalForm.content"
          rows="12"
          placeholder="AI 排版后的正文，支持 ## 标题与 - 列表"
        />
        <span class="field-hint">空行分段；## 小节标题；- 列表项</span>
      </label>

      <div class="card-footer actions">
        <button type="button" class="btn-secondary" :disabled="loading" @click="handleBack">
          返回修改
        </button>
        <button type="button" class="btn-primary" :disabled="loading" @click="handleSave">
          <span v-if="loading" class="spinner" />
          {{ loading ? '保存中…' : '确认保存' }}
        </button>
      </div>
    </section>

    <Transition name="fade">
      <p v-if="error" class="toast toast-error">{{ error }}</p>
    </Transition>
    <Transition name="fade">
      <p v-if="success" class="toast toast-success">{{ success }}</p>
    </Transition>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { parseDocument, processIdea, saveIdea } from '@/api/idea'
import { listCategories } from '@/api/category'
import { toCategoryOptions } from '@/constants/categories'
import FileUploadZone from '@/components/FileUploadZone.vue'

/** 当前步骤：input 录入 | review 确认 */
const step = ref('input')
const loading = ref(false)
const parsing = ref(false)
const error = ref(null)
const success = ref(null)
const categories = ref([])
const uploadedFileName = ref('')
const uploadedCharCount = ref(0)

/** 用户原始输入 */
const original = reactive({
  title: '',
  content: '',
})

/** AI 返回的建议字段（保存时一并提交，便于追溯） */
const suggestion = reactive({
  suggestedTitle: '',
  suggestedSummary: '',
  suggestedTags: [],
  suggestedCategory: '',
  suggestedContent: '',
})

/** 用户编辑后的最终字段（tagsText 为逗号分隔字符串，提交前转为数组） */
const finalForm = reactive({
  title: '',
  summary: '',
  tagsText: '',
  category: '',
  content: '',
})

onMounted(async () => {
  try {
    categories.value = toCategoryOptions(await listCategories())
    if (categories.value.length && !finalForm.category) {
      const defaultCat = categories.value.find((c) => c.value === 'INSPIRATION') ?? categories.value[0]
      finalForm.category = defaultCat.value
    }
  } catch (e) {
    error.value = e.message
  }
})

/** 将逗号分隔的标签文本转为数组 */
function parseTags(text) {
  if (!text?.trim()) return []
  return text.split(/[,，]/).map((tag) => tag.trim()).filter(Boolean)
}

async function handleFileUpload({ file, error: uploadError }) {
  if (uploadError) {
    error.value = uploadError
    return
  }
  if (!file) return

  parsing.value = true
  error.value = null
  success.value = null

  try {
    const result = await parseDocument(file)
    uploadedFileName.value = result.fileName ?? file.name
    uploadedCharCount.value = result.charCount ?? result.extractedContent?.length ?? 0
    original.content = result.extractedContent ?? ''
    if (result.extractedTitle && !original.title.trim()) {
      original.title = result.extractedTitle
    }
    success.value = `已从「${uploadedFileName.value}」提取文本，请核对后点击 AI 整理`
  } catch (e) {
    error.value = e.message
    clearUpload()
  } finally {
    parsing.value = false
  }
}

function clearUpload() {
  uploadedFileName.value = ''
  uploadedCharCount.value = 0
}

/** 调用 POST /api/ideas/process，成功后切换到 review 步骤 */
async function handleProcess() {
  if (!original.content.trim()) {
    error.value = '请输入想法正文'
    return
  }

  loading.value = true
  error.value = null
  success.value = null

  try {
    const result = await processIdea({
      originalTitle: original.title.trim() || null,
      originalContent: original.content.trim(),
    })

    Object.assign(suggestion, result)
    suggestion.suggestedContent = result.suggestedContent ?? ''
    finalForm.title = result.suggestedTitle ?? ''
    finalForm.summary = result.suggestedSummary ?? ''
    finalForm.tagsText = (result.suggestedTags ?? []).join('，')
    finalForm.category = result.suggestedCategory ?? finalForm.category ?? categories.value[0]?.value ?? ''
    finalForm.content = result.suggestedContent ?? original.content.trim()
    step.value = 'review'
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

/** 调用 POST /api/ideas 持久化，成功后重置表单 */
async function handleSave() {
  if (!finalForm.title.trim()) {
    error.value = '请填写最终标题'
    return
  }

  loading.value = true
  error.value = null
  success.value = null

  try {
    const saved = await saveIdea({
      originalTitle: original.title.trim() || null,
      originalContent: original.content.trim(),
      suggestedTitle: suggestion.suggestedTitle,
      suggestedSummary: suggestion.suggestedSummary,
      suggestedTags: suggestion.suggestedTags,
      suggestedCategory: suggestion.suggestedCategory,
      suggestedContent: suggestion.suggestedContent,
      finalTitle: finalForm.title.trim(),
      finalSummary: finalForm.summary.trim() || null,
      finalTags: parseTags(finalForm.tagsText),
      finalCategory: finalForm.category,
      finalContent: finalForm.content.trim() || original.content.trim(),
    })

    success.value = `已保存：${saved.finalTitle}`
    step.value = 'input'
    original.title = ''
    original.content = ''
    clearUpload()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function handleBack() {
  step.value = 'input'
  error.value = null
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

/* 步骤指示器 */
.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 0.5rem 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-text-muted);
  font-size: 0.8125rem;
  font-weight: 500;
}

.step-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  background: var(--color-border);
  color: var(--color-text-muted);
  font-size: 0.75rem;
  font-weight: 600;
  transition: all var(--transition);
}

.step.active .step-num {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  box-shadow: 0 2px 8px rgba(26, 74, 110, 0.3);
}

.step.done .step-num {
  background: var(--color-accent);
  color: #fff;
}

.step.active .step-label,
.step.done .step-label {
  color: var(--color-primary);
  font-weight: 600;
}

.step-line {
  width: 3rem;
  height: 2px;
  background: var(--color-border);
  margin: 0 0.75rem;
  transition: background var(--transition);
}

.step-line.done {
  background: linear-gradient(90deg, var(--color-accent), var(--color-primary-light));
}

/* 卡片 */
.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.card-header {
  padding: 1.5rem 1.5rem 0;
}

.card-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 0.375rem;
}

.hint {
  color: var(--color-text-muted);
  font-size: 0.875rem;
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

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  padding: 0 1.5rem;
  margin-top: 1.25rem;
  font-size: 0.875rem;
}

.label {
  color: var(--color-text);
  font-weight: 600;
  font-size: 0.8125rem;
}

.optional {
  font-style: normal;
  font-weight: 400;
  color: var(--color-text-muted);
}

.field-hint {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

input,
textarea,
select {
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.9375rem;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color var(--transition), box-shadow var(--transition);
}

textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.5;
}

select {
  cursor: pointer;
}

.card-footer {
  padding: 1.5rem;
  margin-top: 1.5rem;
  border-top: 1px solid var(--color-border);
  background: rgba(240, 244, 248, 0.5);
}

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.5rem;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(26, 74, 110, 0.25);
}

.btn-primary:not(:disabled):hover {
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
  box-shadow: 0 4px 12px rgba(26, 74, 110, 0.35);
}

.btn-secondary {
  padding: 0.625rem 1.25rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
}

.btn-secondary:not(:disabled):hover {
  background: var(--color-bg);
  border-color: var(--color-primary-light);
  color: var(--color-primary);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 提示消息 — 样式见 main.css .toast-* */

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
