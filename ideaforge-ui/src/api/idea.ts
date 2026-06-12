import http from '@/api/http'
import type { CategoryOption } from '@/constants/categories'
import type {
  DocumentParseResponse,
  IdeaProcessRequest,
  IdeaProcessResponse,
  IdeaResponse,
  IdeaSaveRequest,
  IdeaSearchPageResponse,
  IdeaUpdateRequest,
  ProcessStreamOptions,
  SearchIdeasParams,
  SseHandlers,
} from '@/types'

/**
 * 读取 fetch 返回的 SSE 流（POST 场景无法用 EventSource）。
 * 支持 event: partial | delta | complete | error
 */
async function readSseStream(
  response: Response,
  handlers: SseHandlers = {},
): Promise<void> {
  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('浏览器不支持流式响应')
  }

  const decoder = new TextDecoder()
  let buffer = ''
  let completed = false

  const dispatch = (eventName: string, dataText: string) => {
    if (!dataText) return
    let parsed: unknown
    try {
      parsed = JSON.parse(dataText)
    } catch {
      parsed = dataText
    }

    if (eventName === 'delta' && handlers.onDelta) {
      handlers.onDelta(parsed as Parameters<NonNullable<SseHandlers['onDelta']>>[0])
      return
    }
    if (eventName === 'partial' && handlers.onPartial) {
      handlers.onPartial(parsed as IdeaProcessResponse)
      return
    }
    if (eventName === 'complete') {
      completed = true
      if (handlers.onComplete) handlers.onComplete(parsed as IdeaProcessResponse)
      return
    }
    if (eventName === 'error') {
      const message =
        typeof parsed === 'object' &&
        parsed !== null &&
        'message' in parsed &&
        typeof (parsed as { message: unknown }).message === 'string'
          ? (parsed as { message: string }).message
          : String(parsed)
      throw new Error(message)
    }
  }

  const parseBlock = (block: string) => {
    let eventName = 'message'
    const dataLines: string[] = []

    for (const line of block.split('\n')) {
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).trimStart())
      }
    }

    if (dataLines.length) {
      dispatch(eventName, dataLines.join('\n'))
    }
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    let boundary = buffer.indexOf('\n\n')
    while (boundary !== -1) {
      const block = buffer.slice(0, boundary)
      buffer = buffer.slice(boundary + 2)
      if (block.trim()) parseBlock(block)
      boundary = buffer.indexOf('\n\n')
    }
  }

  if (buffer.trim()) parseBlock(buffer)

  if (!completed) {
    throw new Error('AI 整理未完成，连接已中断')
  }
}

/** 阶段一：SSE 流式 AI 整理，不落库 */
export async function processIdeaStream(
  payload: IdeaProcessRequest,
  handlers: SseHandlers = {},
  options: ProcessStreamOptions = {},
): Promise<void> {
  const response = await fetch('/api/ideas/process/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    body: JSON.stringify(payload),
    credentials: 'same-origin',
    signal: options.signal,
  })

  if (!response.ok) {
    let message = 'AI 整理失败'
    try {
      const body = (await response.json()) as { message?: string }
      message = body.message ?? message
    } catch {
      // ignore non-json error body
    }
    throw new Error(message)
  }

  await readSseStream(response, handlers)
}

/** 阶段一：同步 AI 整理（保留兼容） */
export async function processIdea(
  payload: IdeaProcessRequest,
): Promise<IdeaProcessResponse> {
  const { data } = await http.post<IdeaProcessResponse>('/ideas/process', payload)
  return data
}

/** Cloudflare Quick Tunnel 对 SSE POST 常返回 403，公网直连同步接口更稳 */
function prefersSyncProcess(): boolean {
  return (
    typeof window !== 'undefined' &&
    window.location.hostname.endsWith('.trycloudflare.com')
  )
}

/**
 * 优先 SSE 流式整理；公网隧道等场景流失败时自动降级为同步接口，保证功能可用。
 */
export async function processIdeaWithFallback(
  payload: IdeaProcessRequest,
  handlers: SseHandlers = {},
  options: ProcessStreamOptions = {},
): Promise<void> {
  if (prefersSyncProcess()) {
    const result = await processIdea(payload)
    handlers.onComplete?.(result)
    return
  }

  try {
    await processIdeaStream(payload, handlers, options)
  } catch (streamError) {
    if (options.signal?.aborted) {
      throw streamError
    }
    if (streamError instanceof DOMException && streamError.name === 'AbortError') {
      throw streamError
    }
    const result = await processIdea(payload)
    handlers.onComplete?.(result)
  }
}

/** 阶段二：用户确认后保存至数据库 */
export async function saveIdea(payload: IdeaSaveRequest): Promise<IdeaResponse> {
  const { data } = await http.post<IdeaResponse>('/ideas', payload)
  return data
}

/**
 * 搜索已确认的知识条目（分页）；无 q/category 时返回最近条目列表
 */
export async function searchIdeas({
  q,
  category,
  page = 0,
  size = 20,
}: SearchIdeasParams = {}): Promise<IdeaSearchPageResponse> {
  const params: Record<string, string | number> = { page, size }
  if (q?.trim()) params.q = q.trim()
  if (category) params.category = category
  const { data } = await http.get<IdeaSearchPageResponse>('/ideas/search', { params })
  return data
}

/** 获取单条知识条目详情 */
export async function getIdeaById(id: string): Promise<IdeaResponse> {
  const { data } = await http.get<IdeaResponse>(`/ideas/${id}`)
  return data
}

/** 更新已确认的知识条目 */
export async function updateIdea(
  id: string,
  payload: IdeaUpdateRequest,
): Promise<IdeaResponse> {
  const { data } = await http.put<IdeaResponse>(`/ideas/${id}`, payload)
  return data
}

/** 上传 Word/PDF，提取文本（不落库） */
export async function parseDocument(file: File): Promise<DocumentParseResponse> {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await http.post<DocumentParseResponse>(
    '/ideas/parse-document',
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
    },
  )
  return data
}

/** 软删除知识条目（status=deleted，浏览不可见） */
export async function deleteIdea(id: string): Promise<void> {
  await http.delete(`/ideas/${id}`)
}

/** 导出单条知识条目为 Word 文档并触发浏览器下载 */
export async function exportIdeaDocx(id: string): Promise<string> {
  const response = await http.get<Blob>(`/ideas/${id}/export/docx`, {
    responseType: 'blob',
  })
  const blob = response.data
  const disposition = (response.headers['content-disposition'] as string | undefined) ?? ''
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

export interface ProcessFormState {
  title: string
  summary: string
  tagsText: string
  category: string
}

export interface ProcessSuggestionState {
  suggestedTitle?: string
  suggestedSummary?: string | null
  suggestedTags?: string[]
  suggestedCategory?: string
  suggestedContent?: string | null
}

/** 将流式 partial / complete 结果应用到表单与 suggestion 缓存 */
export function applyProcessResult(
  form: ProcessFormState,
  suggestion: ProcessSuggestionState,
  result: IdeaProcessResponse,
  categories: CategoryOption[] = [],
): string | null | undefined {
  if (result.suggestedTitle) {
    form.title = result.suggestedTitle
    suggestion.suggestedTitle = result.suggestedTitle
  }
  if (result.suggestedSummary != null) {
    form.summary = result.suggestedSummary
    suggestion.suggestedSummary = result.suggestedSummary
  }
  if (result.suggestedTags?.length) {
    form.tagsText = result.suggestedTags.join('，')
    suggestion.suggestedTags = result.suggestedTags
  }
  if (result.suggestedCategory) {
    const known = categories.find((item) => item.value === result.suggestedCategory)
    if (known || !categories.length) {
      form.category = result.suggestedCategory
      suggestion.suggestedCategory = result.suggestedCategory
    }
  }
  if (result.suggestedContent != null) {
    suggestion.suggestedContent = result.suggestedContent
  }
  return result.suggestedContent
}
