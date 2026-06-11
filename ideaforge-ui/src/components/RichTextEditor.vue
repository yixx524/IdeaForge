<!-- WangEditor 富文本编辑器，v-model 绑定 HTML -->
<template>
  <div class="rich-text-editor" :class="{ 'is-disabled': disabled }">
    <Toolbar
      class="rte-toolbar"
      :editor="editorRef"
      :default-config="toolbarConfig"
      mode="default"
    />
    <Editor
      class="rte-body"
      :default-config="editorConfig"
      mode="default"
      @on-created="handleCreated"
      @on-change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css'

import { onBeforeUnmount, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { toEditorHtml } from '@/utils/contentHtml'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
    disabled?: boolean
    minHeight?: string
  }>(),
  {
    modelValue: '',
    placeholder: '请输入正文…',
    disabled: false,
    minHeight: '280px',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editorRef = shallowRef<IDomEditor>()
let applyingExternalValue = false

const toolbarConfig: Partial<IToolbarConfig> = {
  toolbarKeys: [
    'headerSelect',
    '|',
    'bold',
    'italic',
    '|',
    'bulletedList',
    'numberedList',
    '|',
    'blockquote',
    '|',
    'undo',
    'redo',
  ],
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  readOnly: props.disabled,
}

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
  const html = toEditorHtml(props.modelValue)
  if (html) {
    applyingExternalValue = true
    editor.setHtml(html)
    applyingExternalValue = false
  }
}

function handleChange(editor: IDomEditor) {
  if (applyingExternalValue) return
  emit('update:modelValue', editor.getHtml())
}

watch(
  () => props.modelValue,
  (value) => {
    const editor = editorRef.value
    if (!editor) return
    const nextHtml = toEditorHtml(value)
    if (editor.getHtml() === nextHtml) return
    applyingExternalValue = true
    editor.setHtml(nextHtml || '<p><br></p>')
    applyingExternalValue = false
  },
)

watch(
  () => props.disabled,
  (disabled) => {
    const editor = editorRef.value
    if (!editor) return
    if (disabled) {
      editor.disable()
    } else {
      editor.enable()
    }
  },
)

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--color-surface);
}

.rte-toolbar {
  border-bottom: 1px solid var(--color-border);
}

.rte-body {
  min-height: v-bind(minHeight);
  overflow-y: hidden;
}

.rich-text-editor :deep(.w-e-text-container) {
  background: var(--color-surface);
  color: var(--color-text);
}

.rich-text-editor :deep(.w-e-text-placeholder) {
  color: var(--color-text-muted);
}

.is-disabled {
  opacity: 0.7;
}
</style>
