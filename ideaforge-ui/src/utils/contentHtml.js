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
export function isHtmlContent(text) {
  if (!text?.trim()) return false
  return HTML_TAG_PATTERN.test(text.trim())
}

/**
 * 转为可展示的 HTML：已是 HTML 则直出，否则按 Markdown 解析。
 */
export function toDisplayHtml(text) {
  if (!text?.trim()) return ''
  if (isHtmlContent(text)) return text.trim()
  return marked.parse(text.trim())
}

/**
 * 消毒 HTML，仅保留正文常用标签。
 */
export function sanitizeHtml(html) {
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
export function toEditorHtml(text) {
  return toDisplayHtml(text)
}

/**
 * 判断富文本 HTML 是否无实质内容（含 WangEditor 空态 <p><br></p>）。
 */
export function isEmptyHtml(html) {
  if (!html?.trim()) return true
  const text = html
    .replace(/<br\s*\/?>/gi, '')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .trim()
  return !text
}
