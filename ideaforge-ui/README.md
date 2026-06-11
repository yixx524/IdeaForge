# IdeaForge Frontend

Vue 3 + Vite + TypeScript 前端应用，提供知识录入、AI 整理预览、浏览检索、详情管理与类别设置等页面。

项目总览见 [根目录 README](../README.md)。

---

## 技术信息

| 项 | 值 |
|----|-----|
| 框架 | Vue 3.5 |
| 构建 | Vite 8 |
| 语言 | TypeScript |
| UI 库 | Element Plus（按需自动导入） |
| 路由 | vue-router 5 |
| HTTP | axios |
| 状态管理 | Pinia |
| 富文本 | @wangeditor/editor · marked · DOMPurify |
| 路径别名 | `@` → `src/` |
| 开发端口 | 5173 |

---

## 目录结构

```
ideaforge-ui/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
├── components.d.ts          # Element Plus 组件自动导入声明
├── auto-imports.d.ts        # API 自动导入声明
└── src/
    ├── main.ts              # 应用入口
    ├── App.vue              # 根布局（导航 + 侧栏 + 内容区）
    ├── types/               # API 类型定义（与后端 DTO 对齐）
    ├── api/                 # HTTP 封装
    │   ├── http.ts          #   axios 实例
    │   ├── idea.ts          #   知识条目接口（含 SSE 流解析）
    │   ├── category.ts      #   类别字典接口
    │   └── health.ts        #   健康检查
    ├── stores/
    │   └── category.ts      #   类别字典 Pinia 缓存
    ├── router/
    │   └── index.ts         #   路由定义
    ├── views/               # 页面级组件
    ├── components/          # 可复用 UI 组件
    ├── constants/           # 共享常量
    ├── utils/               # 工具函数
    └── assets/              # 静态资源与全局样式
```

---

## 页面与路由

| 路由 | 组件 | 功能 |
|------|------|------|
| `/` | `HomeView.vue` | 首页：快速入口与最近知识 |
| `/create` | `CreateIdeaView.vue` | 录入：文本 / 文档 → AI 流式整理 → 保存 |
| `/browse` | `BrowseView.vue` | 浏览：类别侧栏 + 搜索 + 分页 |
| `/ideas/:id` | `IdeaDetailView.vue` | 详情：查看、编辑、导出、删除 |
| `/settings/categories` | `CategoryManageView.vue` | 类别字典管理 |
| `/search` | — | 重定向至 `/browse` |

### 布局

`App.vue` 提供全局顶部导航。浏览页（`/browse`）与详情页（`/ideas/:id`）额外显示左侧 `CategorySidebar` 类别导航。

应用启动时自动调用 `categoryStore.ensureLoaded()` 预加载类别字典。

---

## 可复用组件

| 组件 | 说明 |
|------|------|
| `CategorySidebar.vue` | 左侧类别导航，支持筛选 |
| `IdeaResultCard.vue` | 搜索结果卡片 |
| `RichTextEditor.vue` | WangEditor 富文本编辑器 |
| `RichTextContent.vue` | 富文本内容安全渲染 |

---

## 启动

```bash
npm install
npm run dev
```

访问 `http://localhost:5173`。

### 其他命令

```bash
npm run typecheck   # TypeScript 类型检查
npm run build       # 生产构建（含 typecheck）
npm run preview     # 预览构建产物
```

---

## 与后端对接

### 开发代理

[`vite.config.ts`](vite.config.ts) 将 `/api` 代理至后端：

```ts
server: {
  proxy: {
    '/api': 'http://localhost:8080',
  },
}
```

### API 映射

| 前端函数 | 后端接口 | 说明 |
|----------|----------|------|
| `processIdeaStream()` | `POST /api/ideas/process/stream` | SSE 流式 AI 整理 |
| `processIdea()` | `POST /api/ideas/process` | 同步 AI 整理 |
| `saveIdea()` | `POST /api/ideas` | 确认保存 |
| `searchIdeas()` | `GET /api/ideas/search` | 搜索 / 浏览（分页） |
| `getIdeaById()` | `GET /api/ideas/{id}` | 详情 |
| `updateIdea()` | `PUT /api/ideas/{id}` | 更新 |
| `deleteIdea()` | `DELETE /api/ideas/{id}` | 软删除 |
| `parseDocument()` | `POST /api/ideas/parse-document` | 文档解析 |
| `exportIdeaDocx()` | `GET /api/ideas/{id}/export/docx` | Word 导出 |
| `listCategories()` | `GET /api/categories` | 类别列表 |

### 约定

- API 基础路径：`/api`
- 字段命名：camelCase（与后端 DTO 一致）
- 类型定义：`src/types/` 与后端 DTO 一一对应

### SSE 流式整理

`processIdeaStream()` 使用 `fetch` + `ReadableStream` 解析 SSE（POST 场景无法使用 `EventSource`），支持以下事件：

| 事件 | 回调 | 用途 |
|------|------|------|
| `partial` | `onPartial` | 阶段性快照，更新表单字段 |
| `delta` | `onDelta` | 增量更新排版正文 |
| `complete` | `onComplete` | 整理完成 |
| `error` | — | 抛出异常 |

`applyProcessResult()` 工具函数将 AI 结果统一应用到表单状态。

### 类别缓存

- `categoryStore`（Pinia）在应用启动时并行加载 enabled 与 all 两份列表
- 类别管理页 CRUD 成功后调用 `categoryStore.refresh()`
- 各页面通过 store 共享，避免重复 HTTP 请求

---

## Element Plus

使用 `unplugin-auto-import` + `unplugin-vue-components` 按需导入组件与 API（如 `ElMessage`）。

品牌色通过 `src/assets/main.css` 中的 CSS 变量覆盖 `--el-color-primary` 等，与 IdeaForge 设计系统保持一致。

---

## 目录职责

| 目录 | 职责 | 允许 | 禁止 |
|------|------|------|------|
| `views/` | 页面级组件 | 布局、组合 components、调用 api | 直接写 axios/fetch |
| `components/` | 可复用 UI | 展示、事件 emit | 调用后端 API |
| `api/` | HTTP 封装 | axios 请求、URL 常量 | DOM 操作 |
| `stores/` | 全局状态 | 字典缓存、跨页面共享 | 页面 UI |
| `types/` | 类型定义 | interface | 业务逻辑 |
| `router/` | 路由映射 | path、component | 业务逻辑 |

依赖方向：`views → components + api + stores`；`api` 不依赖 `views` / `components`。

---

## 生产部署

```bash
npm run build
```

构建产物输出至 `dist/` 目录。部署方式：

1. **同域部署**：将 `dist/` 静态文件与后端置于同一域名，由后端或 Nginx 托管
2. **反向代理**：Nginx 将 `/api` 转发至后端，其余路径指向前端静态资源

---

## 相关文档

- [项目根 README](../README.md) — 总览、快速开始
- [后端 README](../IdeaForge/README.md) — API 参考与数据模型
