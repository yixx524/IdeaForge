export interface CategoryResponse {
  id: string
  code: string
  label: string
  sortOrder: number
  enabled: boolean
  createdAt: string
}

export interface CategoryRequest {
  code: string
  label: string
  sortOrder?: number
}

export interface CategoryUpdateRequest {
  label?: string
  sortOrder?: number
  enabled?: boolean
}
