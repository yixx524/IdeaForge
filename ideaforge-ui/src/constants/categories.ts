import type { CategoryResponse } from '@/types'

export interface CategoryOption {
  value: string
  label: string
  code: string
  enabled: boolean
  id: string
  sortOrder: number
}

/** 将后端返回的类别 code 转为中文 label */
export function categoryLabel(
  code: string | null | undefined,
  categories: CategoryResponse[] = [],
): string {
  if (!code) return ''
  return categories.find((item) => item.code === code)?.label ?? code
}

/** 将 API 类别列表转为下拉选项格式（兼容旧 value/label 用法） */
export function toCategoryOptions(
  categories: CategoryResponse[] = [],
): CategoryOption[] {
  return categories.map((item) => ({
    value: item.code,
    label: item.label,
    code: item.code,
    enabled: item.enabled,
    id: item.id,
    sortOrder: item.sortOrder,
  }))
}
