<!-- 类别管理页：动态维护想法类别字典 -->
<template>
  <div class="page">
    <section class="card">
      <div class="card-header">
        <h2>类别管理</h2>
        <p class="card-hint">新增、编辑或删除想法类别；删除为软删除，历史数据保留，新录入不可选</p>
      </div>

      <ElForm class="add-form" @submit.prevent="handleCreate">
        <ElFormItem label="Code" class="form-item">
          <ElInput v-model="newCategory.code" placeholder="如 SIDE_HUSTLE" />
        </ElFormItem>
        <ElFormItem label="名称" class="form-item">
          <ElInput v-model="newCategory.label" placeholder="如 副业" />
        </ElFormItem>
        <ElFormItem label="排序" class="form-item narrow">
          <ElInputNumber v-model="newCategory.sortOrder" :min="0" controls-position="right" />
        </ElFormItem>
        <ElFormItem class="form-item submit-item">
          <ElButton type="primary" native-type="submit" :loading="saving">
            {{ saving ? '添加中…' : '添加类别' }}
          </ElButton>
        </ElFormItem>
      </ElForm>
    </section>

    <section v-if="categories.length" class="list">
      <article v-for="item in categories" :key="item.id" class="list-card">
        <div class="list-main">
          <div class="list-title">
            <strong>{{ item.label }}</strong>
            <ElTag size="small" type="info">{{ item.code }}</ElTag>
          </div>
          <p class="meta">排序 {{ item.sortOrder }}</p>
        </div>

        <div v-if="editingId === item.id" class="edit-form">
          <ElInput v-model="editForm.label" placeholder="名称" />
          <ElInputNumber v-model="editForm.sortOrder" :min="0" controls-position="right" class="narrow" />
          <ElButton type="primary" :loading="saving" @click="handleUpdate(item.id)">保存</ElButton>
          <ElButton @click="cancelEdit">取消</ElButton>
        </div>

        <div v-else class="actions">
          <ElButton @click="startEdit(item)">编辑</ElButton>
          <ElButton type="danger" plain :disabled="saving" @click="handleDelete(item)">
            删除
          </ElButton>
        </div>
      </article>
    </section>

    <ElEmpty
      v-else-if="!loading"
      description="暂无类别，请先添加或确认后端已执行 idea_categories 建表脚本"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { createCategory, deleteCategory, updateCategory } from '@/api/category'
import { useCategoryStore } from '@/stores/category'
import { confirmDelete, showError, showSuccess } from '@/utils/message'
import type { CategoryResponse } from '@/types'

const categoryStore = useCategoryStore()
const { enabled: categories, loading: storeLoading } = storeToRefs(categoryStore)
const loading = computed(() => storeLoading.value)
const saving = ref(false)
const error = ref<string | null>(null)
const editingId = ref<string | null>(null)

const newCategory = reactive({
  code: '',
  label: '',
  sortOrder: 0,
})

const editForm = reactive({
  label: '',
  sortOrder: 0,
})

watch(error, (msg) => {
  if (msg) showError(msg)
})

onMounted(async () => {
  error.value = null
  try {
    await categoryStore.ensureLoaded()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  }
})

async function handleCreate() {
  if (!newCategory.code.trim() || !newCategory.label.trim()) {
    error.value = '请填写 Code 和名称'
    return
  }

  saving.value = true
  error.value = null

  try {
    await createCategory({
      code: newCategory.code.trim().toUpperCase(),
      label: newCategory.label.trim(),
      sortOrder: newCategory.sortOrder || 0,
    })
    showSuccess('类别已添加')
    newCategory.code = ''
    newCategory.label = ''
    newCategory.sortOrder = 0
    await categoryStore.refresh()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '添加失败'
  } finally {
    saving.value = false
  }
}

function startEdit(item: CategoryResponse) {
  editingId.value = item.id
  editForm.label = item.label
  editForm.sortOrder = item.sortOrder
}

function cancelEdit() {
  editingId.value = null
}

async function handleUpdate(id: string) {
  saving.value = true
  error.value = null

  try {
    await updateCategory(id, {
      label: editForm.label.trim(),
      sortOrder: editForm.sortOrder,
    })
    showSuccess('类别已更新')
    editingId.value = null
    await categoryStore.refresh()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '更新失败'
  } finally {
    saving.value = false
  }
}

async function handleDelete(item: CategoryResponse) {
  const confirmed = await confirmDelete({
    message: `确定删除「<strong>${item.label}</strong> <code>${item.code}</code>」？`,
    hint: '删除后本页不再显示，历史知识条目仍会保留该类别。',
  })
  if (!confirmed) return

  saving.value = true
  error.value = null

  try {
    await deleteCategory(item.id)
    showSuccess(`已删除：${item.label}`)
    await categoryStore.refresh()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除失败'
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

.add-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: flex-end;
  padding: 1.25rem 1.5rem 1.5rem;
}

.form-item {
  margin-bottom: 0;
  flex: 1;
  min-width: 120px;
}

.form-item.narrow {
  flex: 0 0 140px;
  min-width: 140px;
}

.submit-item {
  flex: 0 0 auto;
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

.edit-form .narrow {
  width: 120px;
}

@media (max-width: 640px) {
  .add-form {
    flex-direction: column;
    align-items: stretch;
  }

  .form-item.narrow {
    flex: 1;
  }
}
</style>
