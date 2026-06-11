<!-- 通用确认弹窗：删除、AI 重新整理等二次确认 -->
<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="open"
        class="modal-backdrop"
        role="presentation"
        @click.self="emit('cancel')"
      >
        <div
          class="modal"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="titleId"
        >
          <div class="modal-icon" :class="`modal-icon--${variant}`" aria-hidden="true">
            {{ iconChar }}
          </div>
          <h3 :id="titleId">{{ title }}</h3>
          <p class="modal-message">
            <slot name="message">{{ message }}</slot>
          </p>
          <p v-if="hint" class="modal-hint">{{ hint }}</p>
          <div class="modal-actions">
            <button
              type="button"
              class="btn-secondary"
              :disabled="loading"
              @click="emit('cancel')"
            >
              {{ cancelLabel }}
            </button>
            <button
              type="button"
              :class="confirmButtonClass"
              :disabled="loading"
              @click="emit('confirm')"
            >
              <span v-if="loading" class="spinner" />
              {{ loading ? loadingLabel : confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '' },
  hint: { type: String, default: '' },
  confirmLabel: { type: String, default: '确认' },
  cancelLabel: { type: String, default: '取消' },
  loadingLabel: { type: String, default: '处理中…' },
  loading: { type: Boolean, default: false },
  /** danger | primary */
  variant: { type: String, default: 'danger' },
  /** delete | ai | warn */
  icon: { type: String, default: 'warn' },
})

const emit = defineEmits(['confirm', 'cancel'])

const titleId = `confirm-modal-${Math.random().toString(36).slice(2, 9)}`

const iconChar = computed(() => {
  if (props.icon === 'delete') return '⌫'
  if (props.icon === 'ai') return '✦'
  return '!'
})

const confirmButtonClass = computed(() =>
  props.variant === 'primary' ? 'btn-primary' : 'btn-danger-solid',
)
</script>
