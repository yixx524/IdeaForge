# IdeaForge Backend

Spring Boot 后端服务，提供 REST API、DeepSeek AI 整理、PostgreSQL 持久化、文档解析与 Word 导出。

项目总览见 [根目录 README](../README.md)。

---

## 技术信息

| 项 | 值 |
|----|-----|
| 框架 | Spring Boot 3.4.3 |
| 语言 | Java 21 |
| 包名 | `com.exam.ideaforge` |
| 默认端口 | 8080 |
| AI 模型 | `deepseek-v4-pro`（Spring AI 1.1.0） |
| 构建工具 | Maven（`./mvnw`） |

---

## 包结构

```
src/main/java/com/exam/ideaforge/
├── IdeaForgeApplication.java       # 启动类
├── entity/                         # JPA 实体
│   ├── KnowledgeItem.java          #   知识条目
│   ├── IdeaCategoryEntity.java     #   类别字典
│   └── ItemStatus.java             #   状态枚举
├── repository/                     # 数据访问层
├── dto/                            # 请求 / 响应 DTO
├── service/                        # 业务逻辑
│   ├── IdeaProcessService.java     #   AI 整理（同步 + SSE 流式）
│   ├── IdeaService.java            #   保存、更新、软删除
│   ├── IdeaSearchService.java      #   关键词搜索与分页
│   ├── CategoryService.java        #   类别字典（含 Spring Cache）
│   ├── DocumentParseService.java   #   Word / PDF 文本提取
│   ├── IdeaExportService.java      #   Word 导出
│   ├── HtmlLayoutFormatter.java    #   HTML 排版格式化
│   └── TextLayoutFormatter.java    #   纯文本排版格式化
├── controller/                     # REST 控制器
│   ├── IdeaController.java
│   ├── CategoryController.java
│   └── HealthController.java
├── config/                         # Spring 配置（CORS 等）
└── exception/                      # 全局异常处理
```

### 依赖规则

```
controller → service → repository → entity
                ↕
              dto（仅在 controller ↔ service 边界传递）
```

| 包 | 职责 |
|----|------|
| `entity` | 数据库表映射，仅含 JPA 注解与字段 |
| `repository` | `JpaRepository` 与 `@Query`，不含业务逻辑 |
| `dto` | API 入参 / 出参，含校验注解 |
| `service` | 业务逻辑、事务、AI 调用 |
| `controller` | 参数校验、调用 Service、返回 HTTP 响应 |
| `config` | Bean 定义、CORS 等全局配置 |
| `exception` | 自定义异常与 `@ControllerAdvice` |

---

## 配置

配置文件：[`src/main/resources/application.yml`](src/main/resources/application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/knowledge_db
    username: your_username
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-v4-pro
          temperature: 0.3
  servlet:
    multipart:
      max-file-size: 10MB
```

| 环境变量 | 说明 |
|----------|------|
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥（必填） |
| `DB_PASSWORD` | PostgreSQL 密码 |

---

## 启动

```bash
./mvnw spring-boot:run        # Linux / macOS
.\mvnw.cmd spring-boot:run    # Windows
```

验证：

```bash
curl http://localhost:8080/api/health
```

---

## 数据模型

### knowledge_items

知识条目主表，字段分为三组：

| 分组 | 字段 | 说明 |
|------|------|------|
| 原始输入 | `original_title`, `original_content` | 用户提交的原始内容 |
| AI 建议 | `suggested_title`, `suggested_summary`, `suggested_tags`, `suggested_category`, `suggested_content` | AI 整理结果 |
| 用户确认 | `final_title`, `final_summary`, `final_tags`, `final_category`, `final_content` | 检索与展示使用的最终数据 |

状态字段 `status`：`pending` → `confirmed` → `deleted`（软删除）。

### idea_categories

类别字典表：`code`（稳定标识）+ `label`（展示名）+ `enabled`（是否启用）。

AI Prompt 动态读取 enabled 类别列表；保存条目时 `final_category` 必须是有效的 enabled code。

---

## API 参考

### 知识条目

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/ideas/process` | AI 整理（同步，兼容保留） |
| `POST` | `/api/ideas/process/stream` | AI 整理（SSE 流式，推荐） |
| `POST` | `/api/ideas` | 确认保存 |
| `GET` | `/api/ideas/search` | 搜索与浏览列表 |
| `GET` | `/api/ideas/{id}` | 获取详情 |
| `PUT` | `/api/ideas/{id}` | 更新条目 |
| `DELETE` | `/api/ideas/{id}` | 软删除 |
| `POST` | `/api/ideas/parse-document` | 文档解析（`multipart/form-data`，字段名 `file`） |
| `GET` | `/api/ideas/{id}/export/docx` | 导出 Word |

#### 搜索参数

```
GET /api/ideas/search?q=关键词&category=WORK&page=0&size=20
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `q` | string | — | 关键词（匹配标题 / 摘要 / 正文 / 标签） |
| `category` | string | — | 类别 code 筛选 |
| `page` | int | `0` | 页码（0-based） |
| `size` | int | `20` | 每页条数（最大 100） |

响应体 `IdeaSearchPageResponse`：

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "hasNext": false,
  "hasPrevious": false
}
```

#### SSE 流式整理

`POST /api/ideas/process/stream` 返回 `text/event-stream`，事件类型：

| 事件 | 说明 |
|------|------|
| `partial` | 阶段性完整快照 |
| `delta` | 增量更新（主要用于 `suggestedContent` 打字机效果） |
| `complete` | 整理完成，携带完整 `IdeaProcessResponse` |
| `error` | 错误信息 |

### 类别字典

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/categories` | 列出启用类别 |
| `GET` | `/api/categories?all=true` | 含已软删除类别 |
| `POST` | `/api/categories` | 新增 `{ code, label, sortOrder? }` |
| `PUT` | `/api/categories/{id}` | 更新 label / sortOrder / enabled |
| `DELETE` | `/api/categories/{id}` | 软删除（`enabled=false`） |

### 两阶段流程

```
1. POST /api/ideas/process/stream   → AI 推理，返回建议字段（不落库）
2. 用户在前端审阅编辑
3. POST /api/ideas                  → 确认后持久化
```

---

## Maven 依赖

| 依赖 | 用途 |
|------|------|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-data-jpa` | 持久化 |
| `spring-boot-starter-validation` | 参数校验 |
| `spring-boot-starter-cache` + `caffeine` | 类别字典缓存 |
| `spring-ai-starter-model-deepseek` | DeepSeek 集成 |
| `postgresql` | 数据库驱动 |
| `poi-ooxml` | Word 读写 |
| `pdfbox` | PDF 文本提取 |
| `jsoup` | HTML 解析（Word 导出） |

---

## 测试

```bash
./mvnw test
```

测试覆盖包括：格式化器、文档解析、搜索服务、导出服务等核心模块。

---

## 相关文档

- [项目根 README](../README.md) — 总览、快速开始、架构
- [前端 README](../ideaforge-ui/README.md) — 前端对接约定
- [db/public.sql](../db/public.sql) — 数据库参考脚本
