# IdeaForge 前端

Vue 3 + Vite 前端模块，提供想法录入、AI 整理结果编辑确认、关键词搜索等页面。

全局开发规范见 [项目根 README](../README.md)。

## 技术信息

| 项 | 值 |
|----|-----|
| 框架 | Vue 3 |
| 构建 | Vite 8 |
| 语言 | JavaScript（非 TypeScript） |
| 路径别名 | `@` → `src/` |
| 开发端口 | 5173（Vite 默认） |

**规划依赖（待安装，新增须评审）：**

- `vue-router` — 路由
- `axios` — HTTP 请求
- `element-plus` — UI 组件库

## 目录结构

```
ideaforge-ui/
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── main.js              # 应用入口
    ├── App.vue              # 根布局壳
    ├── assets/              # 静态资源（css、图片）
    ├── components/          # 可复用 UI 组件（当前为空）
    ├── views/               # 待建 — 页面级组件
    ├── router/              # 待建 — 路由定义
    └── api/                 # 待建 — 后端 HTTP 封装
```

### 规划页面

| 路由 | 文件 | 功能 |
|------|------|------|
| `/` | `views/CreateIdeaView.vue` | 输入原始文本 → AI 整理 → 编辑 → 保存 |
| `/search` | `views/SearchView.vue` | 关键词搜索 + 结果列表 |

## 各目录职责

| 目录 / 文件 | 职责 | 允许 | 禁止 |
|-------------|------|------|------|
| `views/` | 页面级组件 | 布局、组合 components、调用 api | 直接写 axios/fetch |
| `components/` | 可复用 UI | 展示、事件 emit | 调用后端 API |
| `router/` | 路由 | path 与 component 映射 | 业务逻辑 |
| `api/` | HTTP 封装 | axios 请求、URL 常量 | DOM 操作 |
| `assets/` | 静态资源 | css、图片 | JS 逻辑 |
| `App.vue` | 根布局 | 导航、`<router-view>` | 页面业务 |
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

### 代理配置（规划）

开发阶段在 `vite.config.js` 中配置 proxy，将 `/api` 转发至后端：

```js
server: {
  proxy: {
    '/api': 'http://localhost:8080',
  },
},
```

> 当前 `vite.config.js` 尚未配置 proxy，联调前需添加（变更须评审）。

### API 约定

- 基础路径：`/api`
- 字段命名：camelCase（与后端 DTO 一致）
- 后端地址：`http://localhost:8080`

| 前端调用 | 后端接口 | 说明 |
|----------|----------|------|
| `api/idea.js → processIdea()` | `POST /api/ideas/process` | AI 整理 |
| `api/idea.js → saveIdea()` | `POST /api/ideas` | 保存 |
| `api/idea.js → searchIdeas(q)` | `GET /api/ideas/search?q=` | 搜索 |

### api/ 模块规划

```
src/api/
└── idea.js      # 封装 ideas 相关 HTTP 请求
```

所有后端请求集中在此目录，views 只调用 `api/` 导出的函数，不直接写 fetch / axios。

## 开发顺序建议

1. 修复 `App.vue`（移除对缺失组件的引用，改为 router 布局）
2. 安装并配置 vue-router、axios（须评审）
3. 创建 `api/idea.js`，对接后端接口
4. 创建 `router/index.js` 与 `views/` 页面
5. 抽取可复用部分至 `components/`
6. 配置 `vite.config.js` proxy，联调

## 已知问题

- `src/App.vue` 引用了不存在的 `./components/HelloWorld.vue` 和 `./components/TheWelcome.vue`，当前无法正常启动，开发时需先修复
- `router/`、`api/`、`views/` 目录尚未创建

## 相关文档

- [项目根 README](../README.md) — 全局规范、架构
- [IdeaForge/README.md](../IdeaForge/README.md) — 后端 API 规划、包结构
