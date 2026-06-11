# IdeaForge 前端

Vue 3 + Vite 前端模块，提供首页、想法录入、浏览搜索、详情查看、类别管理等页面。

全局开发规范见 [项目根 README](../README.md)。

## 技术信息

| 项 | 值 |
|----|-----|
| 框架 | Vue 3 |
| 构建 | Vite 8 |
| 语言 | JavaScript（非 TypeScript） |
| 路由 | vue-router |
| HTTP | axios |
| 富文本 | @wangeditor/editor、marked、dompurify |
| 路径别名 | `@` → `src/` |
| 开发端口 | 5173（Vite 默认） |

**规划依赖（待安装，新增须评审）：** Element Plus

## 目录结构

```
ideaforge-ui/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── main.js              # 应用入口（注册 router）
    ├── App.vue              # 根布局壳（导航 + 侧栏 + router-view）
    ├── assets/              # 静态资源（css、图片）
    ├── api/                 # 后端 HTTP 封装
    │   ├── http.js          # axios 实例
    │   ├── health.js        # 健康检查
    │   ├── idea.js          # 想法相关接口
    │   └── category.js      # 类别字典接口
    ├── router/              # 路由定义
    ├── views/               # 页面级组件
    ├── constants/           # 共享常量
    ├── utils/               # 可复用工具（contentHtml 等）
    └── components/          # 可复用 UI 组件
        ├── AppCard.vue
        ├── CategoryBadge.vue
        ├── CategorySidebar.vue
        ├── FileUploadZone.vue
        ├── IdeaResultCard.vue
        ├── RichTextEditor.vue
        └── RichTextContent.vue
```

### 页面

| 路由 | 文件 | 功能 |
|------|------|------|
| `/` | `views/HomeView.vue` | 首页：快速入口 + 最近知识 |
| `/create` | `views/CreateIdeaView.vue` | 文本/文档录入 → AI 整理 → 保存 |
| `/browse` | `views/BrowseView.vue` | 类别侧栏 + 搜索 + 分页结果列表 |
| `/ideas/:id` | `views/IdeaDetailView.vue` | 详情查看 + Word 导出 |
| `/settings/categories` | `views/CategoryManageView.vue` | 类别字典增改删（软删除） |

## 各目录职责

| 目录 / 文件 | 职责 | 允许 | 禁止 |
|-------------|------|------|------|
| `views/` | 页面级组件 | 布局、组合 components、调用 api | 直接写 axios/fetch |
| `components/` | 可复用 UI | 展示、事件 emit | 调用后端 API |
| `router/` | 路由 | path 与 component 映射 | 业务逻辑 |
| `api/` | HTTP 封装 | axios 请求、URL 常量 | DOM 操作 |
| `assets/` | 静态资源 | css、图片 | JS 逻辑 |
| `App.vue` | 根布局 | 导航、侧栏、`<router-view>` | 页面业务 |
| `main.js` | 入口 | createApp、插件注册 | 业务代码 |

详细规范见 [根 README 前端目录职责](../README.md#前端目录职责ideaforge-uidsrc)。

### 依赖方向

```
views → components + api
api   ↛ views / components（api 不依赖页面）
```

## 启动

```powershell
cd ideaforge-ui
npm install
npm run dev
```

其他命令：

```powershell
npm run build    # 生产构建
npm run preview  # 预览构建结果
```

## 与后端对接

### 代理配置

开发阶段在 `vite.config.js` 中配置 proxy，将 `/api` 转发至后端：

```js
server: {
  proxy: {
    '/api': 'http://localhost:8080',
  },
},
```

### API 约定

- 基础路径：`/api`
- 字段命名：camelCase（与后端 DTO 一致）
- 后端地址：`http://localhost:8080`

| 前端调用 | 后端接口 | 说明 |
|----------|----------|------|
| `processIdeaStream()` | `POST /api/ideas/process/stream` | SSE 流式 AI 整理（推荐） |
| `processIdea()` | `POST /api/ideas/process` | 同步 AI 整理（兼容） |
| `saveIdea()` | `POST /api/ideas` | 保存 |
| `searchIdeas({ q, category, page, size })` | `GET /api/ideas/search` | 搜索 / 浏览列表（分页，返回 `{ content, page, totalElements, ... }`） |
| `getIdeaById(id)` | `GET /api/ideas/{id}` | 详情 |
| `updateIdea(id, payload)` | `PUT /api/ideas/{id}` | 更新详情（含可选 suggested 字段） |
| `deleteIdea(id)` | `DELETE /api/ideas/{id}` | 软删除知识条目 |
| `parseDocument(file)` | `POST /api/ideas/parse-document` | 文档解析 |
| `exportIdeaDocx(id)` | `GET /api/ideas/{id}/export/docx` | Word 导出 |
| `listCategories()` | `GET /api/categories` | enabled 类别 |
| `listCategories({ all: true })` | `GET /api/categories?all=true` | 含已软删除 |
| CRUD in `category.js` | `/api/categories` | 类别管理 |

## 相关文档

- [项目根 README](../README.md) — 全局规范、架构
- [IdeaForge/README.md](../IdeaForge/README.md) — 后端 API 规划、包结构
