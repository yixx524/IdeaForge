<!-- 轻量 Markdown 正文渲染：## 标题、- 列表、段落 -->
<template>
  <div class="formatted-content">
    <template v-for="(block, index) in blocks" :key="index">
      <h3 v-if="block.type === 'heading2'" class="fmt-heading">{{ block.text }}</h3>
      <ul v-else-if="block.type === 'list'" class="fmt-list">
        <li>{{ block.text }}</li>
      </ul>
      <p v-else class="fmt-paragraph">{{ block.text }}</p>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  content: { type: String, default: '' },
})

const blocks = computed(() => parseContent(props.content))

function parseContent(raw) {
  if (!raw?.trim()) return []

  const normalized = raw.replace(/\r\n/g, '\n').replace(/\r/g, '\n').trim()
  const lines = normalized.split('\n')
  const result = []
  let paragraph = ''

  const flushParagraph = () => {
    if (paragraph.trim()) {
      result.push({ type: 'paragraph', text: paragraph.trim() })
      paragraph = ''
    }
  }

  for (const line of lines) {
    const trimmed = line.trimEnd()

    if (!trimmed) {
      flushParagraph()
      continue
    }

    if (trimmed.startsWith('## ')) {
      flushParagraph()
      result.push({ type: 'heading2', text: trimmed.slice(3).trim() })
      continue
    }

    if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
      flushParagraph()
      result.push({ type: 'list', text: trimmed.slice(2).trim() })
      continue
    }

    paragraph = paragraph ? `${paragraph} ${trimmed}` : trimmed
  }

  flushParagraph()
  return result
}
</script>

<style scoped>
.formatted-content {
  padding: 1rem 1.125rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.fmt-heading {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-primary);
  margin: 1rem 0 0.5rem;
}

.fmt-heading:first-child {
  margin-top: 0;
}

.fmt-paragraph {
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--color-text);
  margin: 0 0 0.75rem;
}

.fmt-list {
  margin: 0 0 0.75rem 1.25rem;
  padding: 0;
}

.fmt-list li {
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--color-text);
  margin-bottom: 0.25rem;
}
</style>
