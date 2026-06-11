const PALETTE = [
  { bg: '#eff6ff', color: '#1d4ed8', dot: '#1d4ed8' },
  { bg: '#f0fdf4', color: '#15803d', dot: '#15803d' },
  { bg: '#fdf4ff', color: '#a21caf', dot: '#a21caf' },
  { bg: '#fff7ed', color: '#c2410c', dot: '#c2410c' },
  { bg: '#f0f9ff', color: '#0369a1', dot: '#0369a1' },
  { bg: '#fefce8', color: '#a16207', dot: '#a16207' },
]

function hashCode(text: string): number {
  let hash = 0
  for (let i = 0; i < text.length; i++) {
    hash = text.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

export function categoryTagStyle(code: string): { backgroundColor: string; color: string; borderColor: string } {
  const key = code || 'default'
  const item = PALETTE[hashCode(key) % PALETTE.length]
  return {
    backgroundColor: item.bg,
    color: item.color,
    borderColor: item.bg,
  }
}

export function categoryDotColor(code: string): string {
  const key = code || 'default'
  return PALETTE[hashCode(key) % PALETTE.length].dot
}
