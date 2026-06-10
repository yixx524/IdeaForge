<!-- 类别管理页：动态维护想法类别字典 -->
<template>
  <div class="page">
    <section class="card">
      <div class="card-header">
        <h2>类别管理</h2>
        <p class="hint">新增、编辑或删除想法类别；删除为软删除，历史数据保留，新录入不可选</p>
      </div>

      <form class="add-form" @submit.prevent="handleCreate">
        <label class="field inline">
          <span class="label">Code</span>
          <input v-model="newCategory.code" type="text" placeholder="如 SIDE_HUSTLE" />
        </label>
        <label class="field inline">
          <span class="label">名称</span>
          <input v-model="newCategory.label" type="text" placeholder="如 副业" />
        </label>
        <label class="field inline narrow">
          <span class="label">排序</span>
          <input v-model.number="newCategory.sortOrder" type="number" min="0" />
        </label>
        <button type="submit" class="btn-primary" :disabled="saving">
          {{ saving ? '添加中…' : '添加类别' }}
        </button>
      </form>
    </section>

    <Transition name="fade">
      <p v-if="error" class="toast error">{{ error }}</p>
    </Transition>
    <Transition name="fade">
      <p v-if="success" class="toast success">{{ success }}</p>
    </Transition>

    <section v-if="categories.length" class="list">
      <article v-for="item in categories" :key="item.id" class="list-card">
        <div class="list-main">
          <div class="list-title">
            <strong>{{ item.label }}</strong>
            <code class="code">{{ item.code }}</code>
          </div>
          <p class="meta">排序 {{ item.sortOrder }}</p>
        </div>

        <div v-if="editingId === item.id" class="edit-form">
          <input v-model="editForm.label" type="text" placeholder="名称" />
          <input v-model.number="editForm.sortOrder" type="number" min="0" class="narrow" />
          <button type="button" class="btn-primary" :disabled="saving" @click="handleUpdate(item.id)">保存</button>
          <button type="button" class="btn-secondary" @click="cancelEdit">取消</button>
        </div>

        <div v-else class="actions">
          <button type="button" class="btn-secondary" @click="startEdit(item)">编辑</button>
          <button
            type="button"
            class="btn-danger"
            :disabled="saving"
            @click="handleDelete(item)"
          >
            删除
          </button>
        </div>
      </article>
    </section>

    <div v-else-if="!loading" class="empty-state">
      <p>暂无类别，请先添加或确认后端已执行 idea_categories 建表脚本</p>
    </div>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="deleteTarget" class="modal-backdrop" @click.self="cancelDelete">
          <div class="modal" role="dialog" aria-modal="true" aria-labelledby="delete-modal-title">
            <h3 id="delete-modal-title">确认删除</h3>
            <p class="modal-message">
              确定删除「<strong>{{ deleteTarget.label }}</strong>」
              <code class="code">{{ deleteTarget.code }}</code>？
            </p>
            <p class="modal-hint">删除后本页不再显示，历史知识条目仍会保留该类别。</p>
            <div class="modal-actions">
              <button type="button" class="btn-secondary" :disabled="saving" @click="cancelDelete">
                取消
              </button>
              <button type="button" class="btn-danger-solid" :disabled="saving" @click="confirmDelete">
                {{ saving ? '删除中…' : '确认删除' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createCategory, deleteCategory, listCategories, updateCategory } from '@/api/category'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref(null)
const success = ref(null)
const editingId = ref(null)
const deleteTarget = ref(null)

const newCategory = reactive({
  code: '',
  label: '',
  sortOrder: 0,
})

const editForm = reactive({
  label: '',
  sortOrder: 0,
})

async function loadCategories() {
  loading.value = true
  error.value = null
  try {
    categories.value = await listCategories()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(loadCategories)

async function handleCreate() {
  if (!newCategory.code.trim() || !newCategory.label.trim()) {
    error.value = '请填写 Code 和名称'
    return
  }

  saving.value = true
  error.value = null
  success.value = null

  try {
    await createCategory({
      code: newCategory.code.trim().toUpperCase(),
      label: newCategory.label.trim(),
      sortOrder: newCategory.sortOrder || 0,
    })
    success.value = '类别已添加'
    newCategory.code = ''
    newCategory.label = ''
    newCategory.sortOrder = 0
    await loadCategories()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

function startEdit(item) {
  editingId.value = item.id
  editForm.label = item.label
  editForm.sortOrder = item.sortOrder
}

function cancelEdit() {
  editingId.value = null
}

async function handleUpdate(id) {
  saving.value = true
  error.value = null
  success.value = null

  try {
    await updateCategory(id, {
      label: editForm.label.trim(),
      sortOrder: editForm.sortOrder,
    })
    success.value = '类别已更新'
    editingId.value = null
    await loadCategories()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

function handleDelete(item) {
  deleteTarget.value = item
}

function cancelDelete() {
  if (saving.value) return
  deleteTarget.value = null
}

async function confirmDelete() {
  if (!deleteTarget.value) return

  const { id, label } = deleteTarget.value
  saving.value = true
  error.value = null
  success.value = null

  try {
    await deleteCategory(id)
    success.value = `已删除：${label}`
    deleteTarget.value = null
    await loadCategories()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

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

.add-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: flex-end;
  padding: 1.25rem 1.5rem 1.5rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  flex: 1;
  min-width: 120px;
}

.field.inline.narrow {
  flex: 0 0 80px;
  min-width: 80px;
}

.label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text);
}

input {
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 0.9375rem;
  background: var(--color-surface);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.list-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.list-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
}

.code {
  padding: 0.1rem 0.4rem;
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.meta {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.actions,
.edit-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.edit-form input.narrow {
  width: 80px;
}

.btn-primary,
.btn-secondary,
.btn-danger {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  color: #fff;
}

.btn-secondary {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.btn-danger {
  background: var(--color-error-bg);
  color: var(--color-error);
  border: 1px solid rgba(220, 38, 38, 0.2);
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.25rem;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}

.modal {
  width: 100%;
  max-width: 400px;
  padding: 1.5rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.modal h3 {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 0.75rem;
}

.modal-message {
  font-size: 0.9375rem;
  color: var(--color-text);
  line-height: 1.6;
}

.modal-message strong {
  color: var(--color-primary);
}

.modal-message .code {
  margin-left: 0.25rem;
}

.modal-hint {
  margin-top: 0.5rem;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.625rem;
  margin-top: 1.25rem;
}

.btn-danger-solid {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  background: var(--color-error);
  color: #fff;
}

.btn-danger-solid:not(:disabled):hover {
  background: #b91c1c;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toast {
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
}

.success {
  color: var(--color-success);
  background: var(--color-success-bg);
  border: 1px solid rgba(5, 150, 105, 0.2);
}

.error {
  color: var(--color-error);
  background: var(--color-error-bg);
  border: 1px solid rgba(220, 38, 38, 0.2);
}

.empty-state {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--color-text-muted);
  font-size: 0.9375rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .add-form {
    flex-direction: column;
    align-items: stretch;
  }

  .field.inline.narrow {
    flex: 1;
  }
}
</style>
