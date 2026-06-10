/** 检测后端 /api/health 是否可达 */
export async function checkHealth() {
  const response = await fetch('/api/health')
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }
  return response.json()
}
