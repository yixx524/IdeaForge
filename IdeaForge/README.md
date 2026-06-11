# IdeaForge 后端

Spring Boot 后端模块，负责 REST API、DeepSeek AI 整理、PostgreSQL 持久化与关键词搜索。

全局开发规范见 [项目根 README](../README.md)。

## 技术信息

| 项 | 值 |
|----|-----|
| 框架 | Spring Boot 3.4.3 |
| Java | 21 |
| 包名 | `com.exam.ideaforge` |
| 端口 | 8080 |
| AI 模型 | `deepseek-v4-pro`（Spring AI 1.1.0） |

## 包结构

```
src/main/java/com/exam/ideaforge/
├── IdeaForgeApplication.java    # 启动类
├── entity/                      # KnowledgeItem、IdeaCategoryEntity、ItemStatus
├── repository/                  # KnowledgeItemRepository、IdeaCategoryRepository
├── dto/                         # 请求 / 响应 DTO
├── service/                     # IdeaProcessService、IdeaService、IdeaSearchService、CategoryService、DocumentParseService、IdeaExportService、HtmlLayoutFormatter
├── controller/                  # HealthController、IdeaController、CategoryController
├── config/                      # WebConfig（CORS）
└── exception/                   # GlobalExceptionHandler
```

## 各包职责与开发规则

| 包 | 职责 | 新增代码示例 |
|----|------|-------------|
| `entity` | JPA 实体，映射数据库表 | `Idea.java`、`Category.java` |
| `repository` | 数据访问接口 | `IdeaRepository.java` |
| `dto` | 请求 / 响应对象 | `IdeaProcessRequest`、`IdeaResponse` |
| `service` | 业务逻辑 | `IdeaProcessService`（AI 整理）、`IdeaService`（保存）、`IdeaSearchService`（搜索） |
| `controller` | REST 端点 | `IdeaController.java` |
| `config` | 全局配置 Bean | `WebConfig.java`（CORS） |
| `exception` | 统一异常响应 | `GlobalExceptionHandler.java` |

### 依赖规则

```
controller → service → repository → entity
                ↕
              dto（仅在 controller ↔ service 边界传递）
```

**禁止：**

- Controller 直接注入 Repository 或 Entity
- Service 处理 HTTP 状态码、Request 对象
- Entity 中包含 API 字段校验或 AI 调用
- Repository 中包含业务判断逻辑

**新增接口时：**

1. 在 `dto/` 定义 Request / Response
2. 在 `service/` 实现业务逻辑
3. 在 `controller/` 添加端点，只做校验与转发
4. 若需新表或字段，先改 `entity/`，再改 `repository/`

## 配置说明

配置文件：`src/main/resources/application.yml`

| 配置项 | 说明 |
|--------|------|
| `server.port` | 8080 |
| `spring.datasource.url` | PostgreSQL 连接（VM `192.168.226.131:5432/knowledge_db`） |
| `spring.datasource.password` | `${DB_PASSWORD:root}` |
| `spring.jpa.hibernate.ddl-auto` | `validate`（表由 VM 维护，应用无 ALTER 权限） |
| `spring.ai.deepseek.api-key` | `${DEEPSEEK_API_KEY}`（须通过环境变量注入） |
| `spring.ai.deepseek.chat.options.model` | `deepseek-v4-pro` |
| `spring.ai.deepseek.chat.options.temperature` | `0.3` |

> **安全提醒：** 当前配置中 API Key 存在默认值，应移除默认值，仅保留 `${DEEPSEEK_API_KEY}`。

### 环境变量

```powershell
$env:DEEPSEEK_API_KEY = "sk-..."
$env:DB_PASSWORD = "root"
```

## 启动

```powershell
cd IdeaForge
.\mvnw.cmd spring-boot:run
```

验证：应用启动无报错，端口 8080 监听。

## V1 API 规划

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| POST | `/api/ideas/process` | 接收原始文本，调用 DeepSeek 整理，返回建议字段（不落库） | 已完成（同步，保留兼容） |
| POST | `/api/ideas/process/stream` | SSE 流式 AI 整理，推送 partial / delta / complete 事件 | 已完成 |
| POST | `/api/ideas` | 接收用户确认后的完整信息，持久化 | 已完成 |
| GET | `/api/ideas/search?q=&category=&page=&size=` | 关键词搜索 + 类别筛选（分页）；无参数时返回最近条目，默认 page=0、size=20 | 已完成 |
| GET | `/api/ideas/{id}` | 查看单条详情 | 已完成 |
| PUT | `/api/ideas/{id}` | 更新最终标题/摘要/标签/类别/排版正文；可选携带 suggested 字段（AI 重新整理后） | 已完成 |
| DELETE | `/api/ideas/{id}` | 软删除（status=deleted，浏览不可见） | 已完成 |
| POST | `/api/ideas/parse-document` | 上传 `.docx`/`.pdf`，提取文本（multipart `file`） | 已完成 |
| GET | `/api/ideas/{id}/export/docx` | 导出单条为 Word 文档（排版分段） | 已完成 |

`GET /api/ideas/search` 响应体为 `IdeaSearchPageResponse`：`content`（条目数组）、`page`、`size`、`totalElements`、`totalPages`、`hasNext`、`hasPrevious`。

### 类别字典 API

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/api/categories` | 列出未删除类别；`?all=true` 含已软删除 | 已完成 |
| POST | `/api/categories` | 新增 `{ code, label, sortOrder? }` | 已完成 |
| PUT | `/api/categories/{id}` | 更新 label / sortOrder / enabled | 已完成 |
| DELETE | `/api/categories/{id}` | 软删除（enabled=false） | 已完成 |

### 两阶段流程

1. `POST /api/ideas/process/stream`（推荐）或 `POST /api/ideas/process` — AI 推理，结果返回前端，暂不入库
2. 用户在前端编辑确认
3. `POST /api/ideas` — 一次性保存至 PostgreSQL

### 类别字典

想法类别存储于 `idea_categories` 表（`code` + `label` + `enabled`），不再使用 Java 枚举。AI Prompt 动态读取 enabled 类别列表。保存想法时 `finalCategory` 必须是 enabled 的 code。

种子数据：`WORK` / `STUDY` / `LIFE` / `INSPIRATION` / `TODO`（见 [`db/migrate_idea_categories.sql`](../db/migrate_idea_categories.sql)）

## 开发顺序建议

1. `entity/` + `repository/` — 数据模型
2. `dto/` — 接口契约
3. `service/IdeaProcessService` — DeepSeek 整理
4. `service/IdeaService` + `service/IdeaSearchService` — 保存与搜索
5. `controller/IdeaController` — 暴露 REST
6. `config/` + `exception/` — 横切关注点

## Maven 依赖要点

- `spring-boot-starter-web` — REST
- `spring-boot-starter-data-jpa` — 持久化
- `spring-boot-starter-validation` — 参数校验
- `spring-ai-starter-model-deepseek` — DeepSeek 集成
- `postgresql` — 数据库驱动
- `poi-ooxml` — Word 读写（文档解析与导出）
- `pdfbox` — PDF 文本提取
- `jsoup` — HTML 正文解析（Word 导出）

> pgvector 相关依赖已在 V1 移除，语义搜索留待 V2 再引入。

## 相关文档

- [项目根 README](../README.md) — 全局规范、架构、协作注意事项
- [ideaforge-ui/README.md](../ideaforge-ui/README.md) — 前端对接说明
- [db/public.sql](../db/public.sql) — 数据库参考
