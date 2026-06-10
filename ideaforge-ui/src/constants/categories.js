/** 将后端返回的类别 code 转为中文 label */
export function categoryLabel(code, categories = []) {
  if (!code) return ''
  return categories.find((item) => item.code === code)?.label ?? code
}

/** 将 API 类别列表转为下拉选项格式（兼容旧 value/label 用法） */
export function toCategoryOptions(categories = []) {
  return categories.map((item) => ({
    value: item.code,
    label: item.label,
    code: item.code,
    enabled: item.enabled,
    id: item.id,
    sortOrder: item.sortOrder,
  }))
}
