<!-- 富文本正文只读展示 -->
<template>
  <div
    v-if="safeHtml"
    class="rich-text-content"
    v-html="safeHtml"
  />
  <p v-else class="rich-text-empty">（无正文）</p>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { sanitizeHtml, toDisplayHtml } from '@/utils/contentHtml'

const props = withDefaults(
  defineProps<{
    content?: string
  }>(),
  {
    content: '',
  },
)

const safeHtml = computed(() => {
  const html = toDisplayHtml(props.content)
  return html ? sanitizeHtml(html) : ''
})
</script>

<style scoped>
.rich-text-content {
  padding: 1rem 1.125rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--color-text);
  word-break: break-word;
}

.rich-text-content :deep(h1),
.rich-text-content :deep(h2),
.rich-text-content :deep(h3),
.rich-text-content :deep(h4),
.rich-text-content :deep(h5),
.rich-text-content :deep(h6) {
  font-weight: 700;
  color: var(--color-primary);
  margin: 1rem 0 0.5rem;
}

.rich-text-content :deep(h1:first-child),
.rich-text-content :deep(h2:first-child),
.rich-text-content :deep(h3:first-child),
.rich-text-content :deep(h4:first-child),
.rich-text-content :deep(h5:first-child),
.rich-text-content :deep(h6:first-child) {
  margin-top: 0;
}

.rich-text-content :deep(p) {
  margin: 0 0 0.75rem;
}

.rich-text-content :deep(ul),
.rich-text-content :deep(ol) {
  margin: 0 0 0.75rem 1.25rem;
  padding: 0;
}

.rich-text-content :deep(li) {
  margin-bottom: 0.25rem;
}

.rich-text-content :deep(blockquote) {
  margin: 0 0 0.75rem;
  padding: 0.5rem 0.75rem;
  border-left: 3px solid var(--color-border);
  color: var(--color-text-muted);
}

.rich-text-empty {
  padding: 1rem 1.125rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-muted);
  font-size: 0.9375rem;
}
</style>
