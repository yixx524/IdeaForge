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
 * 搜索已确认的知识条目；无 q/category 时返回最近条目列表
 * @param {{ q?: string, category?: string }} params
 */
export async function searchIdeas({ q, category } = {}) {
  const params = {}
  if (q?.trim()) params.q = q.trim()
  if (category) params.category = category
  const { data } = await http.get('/ideas/search', { params })
  return data
}

/** 获取单条知识条目详情 */
export async function getIdeaById(id) {
  const { data } = await http.get(`/ideas/${id}`)
  return data
}

/** 更新已确认的知识条目 */
export async function updateIdea(id, payload) {
  const { data } = await http.put(`/ideas/${id}`, payload)
  return data
}

/** 上传 Word/PDF，提取文本（不落库） */
export async function parseDocument(file) {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await http.post('/ideas/parse-document', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

/** 软删除知识条目（status=deleted，浏览不可见） */
export async function deleteIdea(id) {
  await http.delete(`/ideas/${id}`)
}

/** 导出单条知识条目为 Word 文档并触发浏览器下载 */
export async function exportIdeaDocx(id) {
  const response = await http.get(`/ideas/${id}/export/docx`, { responseType: 'blob' })
  const blob = response.data
  const disposition = response.headers['content-disposition'] ?? ''
  let filename = '知识条目.docx'
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  const plainMatch = disposition.match(/filename="([^"]+)"/i)
  if (utf8Match?.[1]) {
    filename = decodeURIComponent(utf8Match[1])
  } else if (plainMatch?.[1]) {
    filename = plainMatch[1]
  }

  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
  return filename
}
