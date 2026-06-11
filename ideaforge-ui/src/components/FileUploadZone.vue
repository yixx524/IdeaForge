<!-- 文档上传区：支持拖拽或点击上传 .docx / .pdf -->
<template>
  <div
    class="upload-zone"
    :class="{ dragging: isDragging, loading: loading, 'has-file': !!fileName }"
    @dragover.prevent="isDragging = true"
    @dragleave.prevent="isDragging = false"
    @drop.prevent="handleDrop"
  >
    <input
      ref="fileInput"
      type="file"
      accept=".docx,.pdf,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      class="file-input"
      @change="handleFileSelect"
    />

    <div v-if="loading" class="upload-content">
      <span class="spinner upload-spinner" />
      <p>正在解析文档…</p>
    </div>

    <div v-else-if="fileName && charCount" class="upload-content success">
      <span class="upload-icon">✓</span>
      <p class="upload-title">{{ fileName }}</p>
      <p class="upload-meta">已提取 {{ charCount }} 字，内容已填入下方表单</p>
      <button type="button" class="btn-secondary upload-clear" @click="clearFile">重新选择</button>
    </div>

    <div v-else class="upload-content" @click="openFilePicker">
      <span class="upload-icon">↑</span>
      <p class="upload-title">上传 Word 或 PDF</p>
      <p class="upload-hint">拖拽文件到此处，或点击选择 · 支持 .docx / .pdf · 最大 10MB</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  loading: { type: Boolean, default: false },
  fileName: { type: String, default: '' },
  charCount: { type: Number, default: 0 },
})

const emit = defineEmits(['select', 'clear'])

const fileInput = ref(null)
const isDragging = ref(false)

const ALLOWED = ['.docx', '.pdf']

function isAllowed(file) {
  const name = file.name.toLowerCase()
  return ALLOWED.some((ext) => name.endsWith(ext))
}

function handleFile(file) {
  if (!file) return
  if (!isAllowed(file)) {
    emit('select', { error: '仅支持 .docx 和 .pdf 文件' })
    return
  }
  emit('select', { file })
}

function handleDrop(event) {
  isDragging.value = false
  const file = event.dataTransfer?.files?.[0]
  handleFile(file)
}

function handleFileSelect(event) {
  const file = event.target.files?.[0]
  handleFile(file)
  event.target.value = ''
}

function openFilePicker() {
  fileInput.value?.click()
}

function clearFile() {
  emit('clear')
}
</script>

<style scoped>
.upload-zone {
  margin: 1.25rem 1.5rem 0;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  background: rgba(240, 244, 248, 0.4);
  transition:
    border-color var(--transition),
    background var(--transition);
  cursor: pointer;
}

.upload-zone:hover,
.upload-zone.dragging {
  border-color: var(--color-primary-light);
  background: rgba(26, 74, 110, 0.04);
}

.upload-zone.loading {
  cursor: wait;
  pointer-events: none;
}

.upload-zone.has-file {
  border-style: solid;
  border-color: rgba(5, 150, 105, 0.35);
  background: var(--color-success-bg);
  cursor: default;
}

.file-input {
  display: none;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 1.5rem 1rem;
  gap: 0.375rem;
}

.upload-icon {
  font-size: 1.5rem;
  color: var(--color-primary-light);
  line-height: 1;
}

.upload-zone.has-file .upload-icon {
  color: var(--color-success);
  font-weight: 700;
}

.upload-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-primary);
}

.upload-hint,
.upload-meta {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  max-width: 22rem;
  line-height: 1.5;
}

.upload-clear {
  margin-top: 0.5rem;
}

.upload-spinner {
  width: 20px;
  height: 20px;
  border-width: 2px;
  margin-bottom: 0.25rem;
}
</style>
