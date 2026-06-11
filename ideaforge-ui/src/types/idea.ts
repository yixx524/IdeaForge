export interface IdeaResponse {
  id: string
  originalTitle: string | null
  originalContent: string
  finalTitle: string
  finalSummary: string | null
  finalTags: string[] | null
  finalCategory: string
  finalContent: string | null
  createdAt: string
}

export interface IdeaProcessResponse {
  suggestedTitle?: string
  suggestedSummary?: string | null
  suggestedTags?: string[]
  suggestedCategory?: string
  suggestedContent?: string | null
}

export type AiSuggestionResult = IdeaProcessResponse

export interface IdeaProcessRequest {
  originalTitle?: string
  originalContent: string
}

export interface IdeaSaveRequest {
  originalTitle?: string
  originalContent: string
  suggestedTitle?: string
  suggestedSummary?: string | null
  suggestedTags?: string[]
  suggestedCategory?: string
  suggestedContent?: string | null
  finalTitle: string
  finalCategory: string
  finalSummary?: string | null
  finalTags?: string[]
  finalContent?: string | null
}

export interface IdeaUpdateRequest {
  finalTitle: string
  finalCategory: string
  finalSummary?: string | null
  finalTags?: string[]
  finalContent?: string | null
  suggestedTitle?: string
  suggestedSummary?: string | null
  suggestedTags?: string[]
  suggestedCategory?: string
  suggestedContent?: string | null
}

export interface IdeaSearchPageResponse {
  content: IdeaResponse[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface DocumentParseResponse {
  fileName: string
  extractedTitle: string | null
  extractedContent: string
  charCount: number
}

export interface SseDeltaEvent {
  field: string
  text: string
  value: string
}

export interface SseErrorEvent {
  message: string
}

export interface SseHandlers {
  onPartial?: (data: IdeaProcessResponse) => void
  onDelta?: (data: SseDeltaEvent) => void
  onComplete?: (data: IdeaProcessResponse) => void
}

export interface SearchIdeasParams {
  q?: string
  category?: string
  page?: number
  size?: number
}

export interface ProcessStreamOptions {
  signal?: AbortSignal
}
