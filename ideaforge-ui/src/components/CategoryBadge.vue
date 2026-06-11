<!-- 类别 badge：基于 code 生成柔和配色 -->
<template>
  <span class="badge" :style="badgeStyle">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  code: { type: String, default: '' },
  label: { type: String, required: true },
})

const PALETTE = [
  { bg: '#eff6ff', color: '#1d4ed8' },
  { bg: '#f0fdf4', color: '#15803d' },
  { bg: '#fdf4ff', color: '#a21caf' },
  { bg: '#fff7ed', color: '#c2410c' },
  { bg: '#f0f9ff', color: '#0369a1' },
  { bg: '#fefce8', color: '#a16207' },
]

const badgeStyle = computed(() => {
  const code = props.code || props.label
  let hash = 0
  for (let i = 0; i < code.length; i++) {
    hash = code.charCodeAt(i) + ((hash << 5) - hash)
  }
  const item = PALETTE[Math.abs(hash) % PALETTE.length]
  return { background: item.bg, color: item.color }
})
</script>
