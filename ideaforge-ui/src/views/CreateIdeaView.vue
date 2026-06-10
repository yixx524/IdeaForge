<!-- 录入想法页：两阶段流程 input（原始输入）→ review（编辑 AI 建议并保存） -->
<template>
  <div class="page">
    <!-- 阶段一：用户输入原始标题与正文 -->
    <section v-if="step === 'input'" class="card">
      <h2>录入想法</h2>
      <p class="hint">输入原始标题与正文，由 AI 整理为结构化信息</p>

      <label class="field">
        <span>标题（可选）</span>
        <input v-model="original.title" type="text" placeholder="简短标题" />
      </label>

      <label class="field">
        <span>正文</span>
        <textarea
          v-model="original.content"
          rows="8"
          placeholder="写下你的想法、笔记或灵感…"
        />
      </label>

      <button type="button" :disabled="loading" @click="handleProcess">
        {{ loading ? 'AI 整理中…' : 'AI 整理' }}
      </button>
    </section>

    <!-- 阶段二：展示 AI 建议，用户可编辑后确认入库 -->
    <section v-else class="card">
      <h2>确认并保存</h2>
      <p class="hint">可编辑 AI 建议后确认入库</p>

      <label class="field">
        <span>最终标题</span>
        <input v-model="finalForm.title" type="text" />
      </label>

      <label class="field">
        <span>最终摘要</span>
        <textarea v-model="finalForm.summary" rows="3" />
      </label>

      <label class="field">
        <span>标签（逗号分隔）</span>
        <input v-model="finalForm.tagsText" type="text" placeholder="标签1，标签2" />
      </label>

      <label class="field">
        <span>类别</span>
        <select v-model="finalForm.category">
          <option v-for="item in CATEGORIES" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>

      <div class="actions">
        <button type="button" class="secondary" :disabled="loading" @click="handleBack">
          返回修改
        </button>
        <button type="button" :disabled="loading" @click="handleSave">
          {{ loading ? '保存中…' : '确认保存' }}
        </button>
      </div>
    </section>

    <p v-if="error" class="message error">{{ error }}</p>
    <p v-if="success" class="message success">{{ success }}</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { processIdea, saveIdea } from '@/api/idea'
import { CATEGORIES } from '@/constants/categories'

/** 当前步骤：input 录入 | review 确认 */
const step = ref('input')
const loading = ref(false)
const error = ref(null)
const success = ref(null)

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
  suggestedCategory: 'INSPIRATION',
})

/** 用户编辑后的最终字段（tagsText 为逗号分隔字符串，提交前转为数组） */
const finalForm = reactive({
  title: '',
  summary: '',
  tagsText: '',
  category: 'INSPIRATION',
})

/** 将逗号分隔的标签文本转为数组 */
function parseTags(text) {
  if (!text?.trim()) return []
  return text.split(/[,，]/).map((tag) => tag.trim()).filter(Boolean)
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
    finalForm.title = result.suggestedTitle ?? ''
    finalForm.summary = result.suggestedSummary ?? ''
    finalForm.tagsText = (result.suggestedTags ?? []).join('，')
    finalForm.category = result.suggestedCategory ?? 'INSPIRATION'
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
      finalTitle: finalForm.title.trim(),
      finalSummary: finalForm.summary.trim() || null,
      finalTags: parseTags(finalForm.tagsText),
      finalCategory: finalForm.category,
    })

    success.value = `已保存：${saved.finalTitle}`
    step.value = 'input'
    original.title = ''
    original.content = ''
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
  gap: 1rem;
}

.card {
  padding: 1.5rem;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
}

.card h2 {
  font-size: 1.125rem;
  margin-bottom: 0.5rem;
}

.hint {
  margin-bottom: 1.25rem;
  color: #666;
  font-size: 0.875rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.field span {
  color: #444;
  font-weight: 500;
}

input,
textarea,
select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9375rem;
  font-family: inherit;
}

textarea {
  resize: vertical;
}

button {
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: 6px;
  background: #42b883;
  color: #fff;
  font-size: 0.9375rem;
  cursor: pointer;
}

button.secondary {
  background: #f0f0f0;
  color: #333;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.actions {
  display: flex;
  gap: 0.75rem;
}

.message {
  font-size: 0.9375rem;
}

.success {
  color: #2d8a5e;
}

.error {
  color: #c0392b;
}
</style>
