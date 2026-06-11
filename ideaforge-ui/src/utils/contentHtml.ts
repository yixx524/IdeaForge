import DOMPurify from 'dompurify'
import { marked } from 'marked'

const HTML_TAG_PATTERN = /<\/?[a-z][\s\S]*?>/i

marked.setOptions({
  gfm: true,
  breaks: true,
})

/**
 * 判断内容是否已是 HTML（含常见块级/行内标签）。
 */
export function isHtmlContent(text: string | null | undefined): boolean {
  if (!text?.trim()) return false
  return HTML_TAG_PATTERN.test(text.trim())
}

/**
 * 转为可展示的 HTML：已是 HTML 则直出，否则按 Markdown 解析。
 */
export function toDisplayHtml(text: string | null | undefined): string {
  if (!text?.trim()) return ''
  if (isHtmlContent(text)) return text.trim()
  return marked.parse(text.trim()) as string
}

/**
 * 消毒 HTML，仅保留正文常用标签。
 */
export function sanitizeHtml(html: string | null | undefined): string {
  if (!html?.trim()) return ''
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: [
      'p', 'br', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
      'ul', 'ol', 'li', 'strong', 'b', 'em', 'i', 'u', 'blockquote', 'span', 'div',
    ],
    ALLOWED_ATTR: [],
  })
}

/**
 * 编辑器初始值：Markdown 先转 HTML，已是 HTML 则原样返回。
 */
export function toEditorHtml(text: string | null | undefined): string {
  return toDisplayHtml(text)
}

/**
 * 判断富文本 HTML 是否无实质内容（含 WangEditor 空态 <p><br></p>）。
 */
export function isEmptyHtml(html: string | null | undefined): boolean {
  if (!html?.trim()) return true
  const text = html
    .replace(/<br\s*\/?>/gi, '')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .trim()
  return !text
}

/**
 * 富文本 HTML 转纯文本，保留段落换行（供 AI 重新整理「当前排版正文」来源）。
 */
export function htmlToPlainText(html: string | null | undefined): string {
  if (!html?.trim()) return ''
  return html
    .replace(/<\/p>\s*<p[^>]*>/gi, '\n\n')
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/h[1-6]>/gi, '\n\n')
    .replace(/<\/li>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}
