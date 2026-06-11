import http from '@/api/http'
import type {
  CategoryRequest,
  CategoryResponse,
  CategoryUpdateRequest,
} from '@/types'

export interface ListCategoriesOptions {
  all?: boolean
}

/** 获取类别列表；all=true 时含已软删除项（仅搜索页 label 映射用） */
export async function listCategories(
  { all = false }: ListCategoriesOptions = {},
): Promise<CategoryResponse[]> {
  const params = all ? { all: true } : {}
  const { data } = await http.get<CategoryResponse[]>('/categories', { params })
  return data
}

export async function createCategory(
  payload: CategoryRequest,
): Promise<CategoryResponse> {
  const { data } = await http.post<CategoryResponse>('/categories', payload)
  return data
}

export async function updateCategory(
  id: string,
  payload: CategoryUpdateRequest,
): Promise<CategoryResponse> {
  const { data } = await http.put<CategoryResponse>(`/categories/${id}`, payload)
  return data
}

/** 软删除：enabled=false，管理页不再展示 */
export async function deleteCategory(id: string): Promise<void> {
  await http.delete(`/categories/${id}`)
}
