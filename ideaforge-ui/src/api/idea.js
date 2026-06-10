import http from '@/api/http'

/** 阶段一：调用 AI 整理，不落库 */
export async function processIdea(payload) {
  const { data } = await http.post('/ideas/process', payload)
  return data
}

/** 阶段二：用户确认后保存至数据库 */
export async function saveIdea(payload) {
  const { data } = await http.post('/ideas', payload)
  return data
}

/**
 * 搜索已确认的知识条目
 * @param {{ q?: string, category?: string }} params - q 关键词；category 类别枚举值（WORK 等）
 */
export async function searchIdeas({ q, category } = {}) {
  const params = {}
  if (q?.trim()) params.q = q.trim()
  if (category) params.category = category
  const { data } = await http.get('/ideas/search', { params })
  return data
}
