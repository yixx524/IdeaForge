import http from '@/api/http'

/** 获取类别列表；all=true 时含已软删除项（仅搜索页 label 映射用） */
export async function listCategories({ all = false } = {}) {
  const params = all ? { all: true } : {}
  const { data } = await http.get('/categories', { params })
  return data
}

export async function createCategory(payload) {
  const { data } = await http.post('/categories', payload)
  return data
}

export async function updateCategory(id, payload) {
  const { data } = await http.put(`/categories/${id}`, payload)
  return data
}

/** 软删除：enabled=false，管理页不再展示 */
export async function deleteCategory(id) {
  await http.delete(`/categories/${id}`)
}
