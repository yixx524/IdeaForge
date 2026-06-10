# IdeaForge

个人知识 / 想法整理工具。上传原始想法文本，由 DeepSeek 大模型智能整理为结构化信息，经用户确认后存入 PostgreSQL，并支持关键词检索。

## 开发前必读（本文件）

**每次开发前**，必须先完整阅读本 README.md（仅本文件，子模块 README 自行安排）。阅读完毕后，在对话或工作记录中输出：

```
易----大开工！
```

**每次开发完成后**，必须更新本 README.md 中的进度、已知问题等与本次改动相关的内容（子模块 README 自行维护）。更新完毕后，输出：

```
易----屎山完成！
```

## 项目简介

**V1 核心流程：**

1. 用户上传原始想法（标题 + 正文）
2. 系统调用 DeepSeek（`deepseek-v4-pro`）生成建议标题、一句话摘要、标签、类别
3. 前端展示结果，用户可编辑后确认
4. 确认后持久化至 PostgreSQL
5. 通过关键词搜索已有知识条目

**当前进度：**

| 模块 | 状态 |
|------|------|
| 后端脚手架 | 已完成（`pom.xml`、`application.yml`、启动类） |
| 后端连通 | 已完成（`/api/health`、`WebConfig` CORS、环境变量配置） |
| 前端脚手架 | 已完成（`ideaforge-ui`，Vue 3 + Vite 8） |
| 前后端联调 | 已完成（Vite proxy、全链路 API 测试通过） |
| 后端业务 | 已完成（Entity / Repository / DTO / Service / Controller / 异常处理） |
| 前端业务 | 已完成（vue-router、axios、`CreateIdeaView`、`SearchView`） |
| V1 核心流程 | 已完成（AI 整理 → 确认保存 → 关键词搜索） |
| 向量语义搜索 | V2 规划（V1 使用关键词搜索） |

## 项目结构

```
IdeaForge-v/
├── README.md                 # 本文件（项目总览 + 全局规范）
├── db/
│   └── public.sql            # VM 数据库参考脚本
├── IdeaForge/                # 后端（Spring Boot）
│   ├── README.md             # 后端专项说明
│   └── src/main/java/com/exam/ideaforge/
└── ideaforge-ui/             # 前端（Vue 3 + Vite）
    ├── README.md             # 前端专项说明
    └── src/
```

## 技术栈

| 层级 | 技术 | 版本 / 说明 |
|------|------|-------------|
| 后端框架 | Spring Boot | 3.4.3 |
| 语言 | Java | 21 |
| 构建 | Maven | wrapper（`mvnw`） |
| AI 集成 | Spring AI + DeepSeek | 1.1.0 / `deepseek-v4-pro` |
| 持久化 | Spring Data JPA + PostgreSQL | VM `192.168.226.131:5432/knowledge_db` |
| 前端 | Vue 3 + Vite | ^3.5 / ^8.0（JavaScript） |
| 向量检索（V2） | pgvector | V1 暂不启用，使用关键词搜索 |

**前端已安装依赖：** vue-router、axios

**前端规划依赖（待安装，新增须评审）：** Element Plus

## 系统架构（V1）

```mermaid
flowchart TB
    subgraph frontend [ideaforge_ui]
        Views[views]
        ApiLayer[api]
        Router[router]
    end
    subgraph backend [IdeaForge]
        Controller[controller]
        Service[service]
        Repository[repository]
        Entity[entity]
    end
    subgraph external [外部]
        DS[DeepSeek_API]
        PG[(PostgreSQL_VM)]
    end
    Views --> Router
    Views --> ApiLayer
    ApiLayer -->|REST| Controller
    Controller --> Service
    Service --> Repository --> Entity --> PG
    Service --> DS
```

## 开发规范：按包 / 目录职责管理

**总则：** 每个包（后端）或目录（前端）有且仅有一种职责。新增代码必须先确定归属，禁止把业务逻辑、API 调用、页面 UI 混写在同一文件里；禁止跨层调用（如 Controller 直接操作 Repository、View 直接写 fetch）。

### 后端包职责（`com.exam.ideaforge`）

| 包 | 职责 | 允许 | 禁止 |
|----|------|------|------|
| `entity` | 数据库表映射 | JPA 注解、字段、关联 | 业务逻辑、HTTP、调用 AI |
| `repository` | 数据访问 | JpaRepository、@Query | 业务规则、DTO 转换 |
| `dto` | API 入参 / 出参 | 字段、校验注解 | 业务逻辑、数据库操作 |
| `service` | 业务逻辑 | 调用 Repository、DeepSeek、事务 | 处理 HTTP 细节 |
| `controller` | REST 接口 | 参数校验、调用 Service、返回响应 | 业务逻辑、直接访问 DB |
| `config` | Spring 配置 | CORS、Bean 定义 | 业务逻辑 |
| `exception` | 异常处理 | 自定义异常、@ControllerAdvice | 业务逻辑 |

**依赖方向（单向）：** `controller → service → repository → entity`；`dto` 仅在 controller 与 service 边界使用，不进入 repository。

### 前端目录职责（`ideaforge-ui/src`）

| 目录 / 文件 | 职责 | 允许 | 禁止 |
|-------------|------|------|------|
| `views/` | 页面级组件（对应路由） | 布局、组合 components、调用 api | 直接写 axios/fetch、复杂可复用 UI |
| `components/` | 可复用 UI 组件 | 展示、事件 emit | 直接调用后端 API |
| `router/` | 路由定义 | path、component 映射 | 业务逻辑、API 调用 |
| `api/` | 后端 HTTP 封装 | axios 请求、URL 常量 | DOM 操作、页面状态 |
| `assets/` | 静态资源 | css、图片、字体 | JS 逻辑 |
| `App.vue` | 根布局壳 | 导航栏、`<router-view>` | 具体页面业务 |
| `main.js` | 应用入口 | createApp、插件注册 | 业务代码 |

**依赖方向（单向）：** `views → components + api`；`api` 不依赖 `views` / `components`。

### 跨模块约定

- API 路径统一前缀 `/api`；前后端字段命名保持一致（camelCase）
- 后端改 DTO 字段时，同步修改前端 `api/` 与 `views/`；前端不假设未实现的接口
- 可复用逻辑：后端放 `service`，前端放 `components/` 或 `api/`，不随意新建平行包 / 目录

## 协作与开发注意事项

### 文件变更

- **不得擅自新增或删除文件 / 目录**；任何新包、新目录、删文件须先经评审确认
- 新增文件必须能对应上文职责表中的某一类；若无法归类，先讨论再建
- 优先在已有文件中扩展，而非重复造轮子

### 模块隔离

- 修改某一包 / 目录时，**不得破坏其他模块已有功能与逻辑**
- 后端各包之间、前后端之间通过明确接口（DTO / REST）通信

### 全局视角

- 开发前先查现有代码，**能复用则复用**（如同一 Service 处理保存与搜索的数据组装）
- 避免「每个功能新建一个 Controller / Service / View」；先评估是否扩展现有类
- 配置集中管理：后端 `application.yml`，前端 `vite.config.js`（proxy 等）
- 密钥、密码仅通过环境变量注入，**禁止提交到 Git**

### 已知问题

- JPA `ddl-auto` 已改为 `validate`（VM 表由 DBA 维护，应用用户无 ALTER 权限）
- `status` 字段数据库存储小写（`pending` / `confirmed`），通过 `ItemStatusConverter` 映射

## 环境要求

- JDK 21
- Maven（或使用 `IdeaForge/mvnw`）
- Node.js ^20.19.0 或 >=22.12.0（前端）
- VM 上 PostgreSQL 可远程访问
- DeepSeek API Key（环境变量）

## 快速开始

### 1. 配置环境变量

复制 [`.env.example`](.env.example) 为 `.env` 并填入真实值（`.env` 已 gitignore，不会提交），或手动设置：

```powershell
$env:DEEPSEEK_API_KEY = "sk-你的密钥"
$env:DB_PASSWORD = "root"   # 若数据库密码不同则修改
```

### 2. 确认数据库连通

从开发机测试 VM PostgreSQL：`192.168.226.131:5432/knowledge_db`

### 3. 启动后端

```powershell
cd IdeaForge
.\mvnw.cmd spring-boot:run
```

后端地址：`http://localhost:8080`

### 4. 启动前端

```powershell
cd ideaforge-ui
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 开发阶段建议

按模块、按层次推进，不跳层：

1. **后端：** `entity` → `repository` → `dto` → `service` → `controller`
2. **前端：** `api/` → `router/` + `views/` → `components/`
3. **联调与验收**

## V1 功能范围

| 功能 | V1 | V2（规划） |
|------|-----|-----------|
| AI 整理 | DeepSeek `deepseek-v4-pro` | 同左，可优化 Prompt |
| 数据持久化 | PostgreSQL + JPA | 同左 |
| 搜索 | 关键词匹配（LIKE） | pgvector 语义搜索 |
| Embedding 模型 | 不需要 | 需额外接入 |

## 相关文档

- [IdeaForge/README.md](IdeaForge/README.md) — 后端包细则、配置、API 规划
- [ideaforge-ui/README.md](ideaforge-ui/README.md) — 前端目录细则、对接约定
- [db/public.sql](db/public.sql) — 数据库参考脚本
