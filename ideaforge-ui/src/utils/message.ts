import { ElMessage, ElMessageBox } from 'element-plus'

export function showError(message: string): void {
  ElMessage.error(message)
}

export function showSuccess(message: string): void {
  ElMessage.success(message)
}

export interface ConfirmOptions {
  title?: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  type?: 'warning' | 'info'
  dangerouslyUseHTMLString?: boolean
}

export async function confirmAction(options: ConfirmOptions): Promise<boolean> {
  try {
    await ElMessageBox.confirm(options.message, options.title ?? '确认操作', {
      confirmButtonText: options.confirmLabel ?? '确认',
      cancelButtonText: options.cancelLabel ?? '取消',
      type: options.type ?? 'warning',
      dangerouslyUseHTMLString: options.dangerouslyUseHTMLString ?? false,
    })
    return true
  } catch {
    return false
  }
}

export async function confirmDelete(options: {
  title?: string
  message: string
  hint?: string
  confirmLabel?: string
}): Promise<boolean> {
  const message = options.hint
    ? `${options.message}<p style="margin:0.5rem 0 0;font-size:0.8125rem;color:var(--el-text-color-secondary)">${options.hint}</p>`
    : options.message

  return confirmAction({
    title: options.title ?? '确认删除',
    message,
    confirmLabel: options.confirmLabel ?? '确认删除',
    type: 'warning',
    dangerouslyUseHTMLString: true,
  })
}
