/** 类别枚举，value 与后端 IdeaCategory 一致，label 供前端展示 */
export const CATEGORIES = [
  { value: 'WORK', label: '工作' },
  { value: 'STUDY', label: '学习' },
  { value: 'LIFE', label: '生活' },
  { value: 'INSPIRATION', label: '灵感' },
  { value: 'TODO', label: '待办' },
]

/** 将后端返回的类别 value 转为中文标签 */
export function categoryLabel(value) {
  return CATEGORIES.find((item) => item.value === value)?.label ?? value
}
