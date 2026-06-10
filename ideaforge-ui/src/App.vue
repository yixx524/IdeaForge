<script setup>
import { ref } from 'vue'
import { checkHealth } from '@/api/health'

const status = ref('idle')
const result = ref(null)
const error = ref(null)

async function testConnection() {
  status.value = 'loading'
  result.value = null
  error.value = null

  try {
    result.value = await checkHealth()
    status.value = 'success'
  } catch (e) {
    error.value = e.message ?? '连接失败'
    status.value = 'error'
  }
}
</script>

<template>
  <div class="app">
    <header class="header">
      <h1>IdeaForge</h1>
      <p class="subtitle">个人知识 / 想法整理工具</p>
    </header>

    <main class="main">
      <section class="card">
        <h2>前后端连通测试</h2>
        <p class="hint">点击按钮，通过 Vite 代理请求后端 <code>/api/health</code></p>
        <button type="button" :disabled="status === 'loading'" @click="testConnection">
          {{ status === 'loading' ? '检测中…' : '测试连接' }}
        </button>

        <p v-if="status === 'success'" class="message success">
          连接成功：{{ result?.service }} — {{ result?.status }}
        </p>
        <p v-if="status === 'error'" class="message error">
          连接失败：{{ error }}
        </p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.app {
  max-width: 640px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.header {
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 1.75rem;
  font-weight: 600;
}

.subtitle {
  margin-top: 0.5rem;
  color: #666;
}

.card {
  padding: 1.5rem;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
}

.card h2 {
  font-size: 1.125rem;
  margin-bottom: 0.75rem;
}

.hint {
  margin-bottom: 1rem;
  color: #666;
  font-size: 0.875rem;
}

code {
  padding: 0.125rem 0.375rem;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 0.875rem;
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

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  margin-top: 1rem;
  font-size: 0.9375rem;
}

.success {
  color: #2d8a5e;
}

.error {
  color: #c0392b;
}
</style>
