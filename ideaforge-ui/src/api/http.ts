import axios from 'axios'

/** axios 实例：开发环境由 Vite proxy 将 /api 转发到 localhost:8080 */
const http = axios.create({
  baseURL: '/api',
})

/** 统一提取后端 ErrorResponse.message，供 views 直接展示 */
http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const axiosError = error as {
      response?: { data?: { message?: string } }
      message?: string
    }
    const message =
      axiosError.response?.data?.message ?? axiosError.message ?? '请求失败'
    return Promise.reject(new Error(message))
  },
)

export default http
