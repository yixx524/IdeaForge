/*
 Navicat Premium Dump SQL

 Source Server         : ubuntu-postgres
 Source Server Type    : PostgreSQL
 Source Server Version : 160014 (160014)
 Source Host           : 192.168.226.131:5432
 Source Catalog        : knowledge_db
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160014 (160014)
 File Encoding         : 65001

 Date: 11/06/2026 11:41:12
*/


-- ----------------------------
-- Type structure for halfvec
-- ----------------------------
DROP TYPE IF EXISTS "public"."halfvec";
CREATE TYPE "public"."halfvec" (
  INPUT = "public"."halfvec_in",
  OUTPUT = "public"."halfvec_out",
  RECEIVE = "public"."halfvec_recv",
  SEND = "public"."halfvec_send",
  TYPMOD_IN = "public"."halfvec_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);

-- ----------------------------
-- Type structure for sparsevec
-- ----------------------------
DROP TYPE IF EXISTS "public"."sparsevec";
CREATE TYPE "public"."sparsevec" (
  INPUT = "public"."sparsevec_in",
  OUTPUT = "public"."sparsevec_out",
  RECEIVE = "public"."sparsevec_recv",
  SEND = "public"."sparsevec_send",
  TYPMOD_IN = "public"."sparsevec_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);

-- ----------------------------
-- Type structure for vector
-- ----------------------------
DROP TYPE IF EXISTS "public"."vector";
CREATE TYPE "public"."vector" (
  INPUT = "public"."vector_in",
  OUTPUT = "public"."vector_out",
  RECEIVE = "public"."vector_recv",
  SEND = "public"."vector_send",
  TYPMOD_IN = "public"."vector_typmod_in",
  INTERNALLENGTH = VARIABLE,
  STORAGE = external,
  CATEGORY = U,
  DELIMITER = ','
);

-- ----------------------------
-- Table structure for idea_categories
-- ----------------------------
DROP TABLE IF EXISTS "public"."idea_categories";
CREATE TABLE "public"."idea_categories" (
  "id" uuid NOT NULL DEFAULT gen_random_uuid(),
  "code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "label" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "sort_order" int4 NOT NULL DEFAULT 0,
  "enabled" bool NOT NULL DEFAULT true,
  "created_at" timestamptz(6) DEFAULT now(),
  "updated_at" timestamptz(6) DEFAULT now()
)
;
COMMENT ON COLUMN "public"."idea_categories"."id" IS '主键 UUID';
COMMENT ON COLUMN "public"."idea_categories"."code" IS '稳定机器码，写入 knowledge_items.final_category';
COMMENT ON COLUMN "public"."idea_categories"."label" IS '中文展示名，可修改';
COMMENT ON COLUMN "public"."idea_categories"."sort_order" IS '下拉排序';
COMMENT ON COLUMN "public"."idea_categories"."enabled" IS 'false=软停用，历史数据保留';
COMMENT ON COLUMN "public"."idea_categories"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."idea_categories"."updated_at" IS '最后更新时间';
COMMENT ON TABLE "public"."idea_categories" IS '想法类别字典，供前端动态维护';

-- ----------------------------
-- Records of idea_categories
-- ----------------------------
INSERT INTO "public"."idea_categories" VALUES ('cd68dd44-6463-4998-a05f-2d859f5f9ee0', 'WORK', '工作', 1, 't', '2026-06-10 08:53:27.173678+00', '2026-06-10 08:53:27.173678+00');
INSERT INTO "public"."idea_categories" VALUES ('9d894939-cbe7-420a-89bf-77a4eb911c31', 'STUDY', '学习', 2, 't', '2026-06-10 08:53:27.173678+00', '2026-06-10 08:53:27.173678+00');
INSERT INTO "public"."idea_categories" VALUES ('ada42ddd-338c-4159-9973-26ec3ee8ab83', 'LIFE', '生活', 3, 't', '2026-06-10 08:53:27.173678+00', '2026-06-10 08:53:27.173678+00');
INSERT INTO "public"."idea_categories" VALUES ('238ca83b-35c6-4d23-8e3b-68b7a668140b', 'INSPIRATION', '灵感', 4, 't', '2026-06-10 08:53:27.173678+00', '2026-06-10 08:53:27.173678+00');
INSERT INTO "public"."idea_categories" VALUES ('06e7bffc-9d5c-4735-a5f4-5dca4f68a317', 'TODO', '待办', 5, 't', '2026-06-10 08:53:27.173678+00', '2026-06-10 08:53:27.173678+00');
INSERT INTO "public"."idea_categories" VALUES ('13fb5835-c088-4ef4-b298-23acec9bae0a', 'CESHI', '测试', 0, 'f', '2026-06-10 08:56:53.129797+00', '2026-06-10 09:02:31.700122+00');

-- ----------------------------
-- Table structure for knowledge_items
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_items";
CREATE TABLE "public"."knowledge_items" (
  "id" uuid NOT NULL DEFAULT gen_random_uuid(),
  "original_title" text COLLATE "pg_catalog"."default",
  "original_content" text COLLATE "pg_catalog"."default" NOT NULL,
  "suggested_title" text COLLATE "pg_catalog"."default",
  "suggested_summary" text COLLATE "pg_catalog"."default",
  "suggested_tags" text[] COLLATE "pg_catalog"."default",
  "suggested_category" varchar(20) COLLATE "pg_catalog"."default",
  "final_title" text COLLATE "pg_catalog"."default" NOT NULL,
  "final_summary" text COLLATE "pg_catalog"."default",
  "final_tags" text[] COLLATE "pg_catalog"."default",
  "final_category" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "status" varchar(20) COLLATE "pg_catalog"."default" DEFAULT 'pending'::character varying,
  "embedding" "public"."vector",
  "created_at" timestamptz(6) DEFAULT now(),
  "updated_at" timestamptz(6) DEFAULT now(),
  "suggested_content" text COLLATE "pg_catalog"."default",
  "final_content" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."knowledge_items"."original_title" IS '用户输入的原始标题（可选）';
COMMENT ON COLUMN "public"."knowledge_items"."original_content" IS '用户输入的原始正文（必填）';
COMMENT ON COLUMN "public"."knowledge_items"."suggested_title" IS 'AI 建议的标题';
COMMENT ON COLUMN "public"."knowledge_items"."suggested_summary" IS 'AI 建议的一句话摘要';
COMMENT ON COLUMN "public"."knowledge_items"."suggested_tags" IS 'AI 建议的标签数组';
COMMENT ON COLUMN "public"."knowledge_items"."suggested_category" IS 'AI 建议的类别：工作/学习/生活/灵感/待办';
COMMENT ON COLUMN "public"."knowledge_items"."final_title" IS '用户确认后的最终标题';
COMMENT ON COLUMN "public"."knowledge_items"."final_summary" IS '用户确认后的最终摘要';
COMMENT ON COLUMN "public"."knowledge_items"."final_tags" IS '用户确认后的最终标签数组';
COMMENT ON COLUMN "public"."knowledge_items"."final_category" IS '用户确认后的最终类别';
COMMENT ON COLUMN "public"."knowledge_items"."status" IS 'pending=待确认，confirmed=已确认入库，deleted=软删除（浏览不可见）';
COMMENT ON COLUMN "public"."knowledge_items"."embedding" IS '基于 final_title+final_summary+final_tags 生成的向量，用于相似度检索';
COMMENT ON COLUMN "public"."knowledge_items"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."knowledge_items"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN "public"."knowledge_items"."suggested_content" IS 'AI 排版后的正文建议';
COMMENT ON COLUMN "public"."knowledge_items"."final_content" IS '用户确认后的排版正文（展示/导出/搜索）';
COMMENT ON TABLE "public"."knowledge_items" IS '用户知识条目：原始想法、AI建议及最终确认的版本';

-- ----------------------------
-- Records of knowledge_items
-- ----------------------------
INSERT INTO "public"."knowledge_items" VALUES ('c776ea27-485d-4957-adcd-3d27f5056de9', '测试想法', '今天学习了 Spring AI 与 DeepSeek 的集成方式', 'Spring AI 集成', '学习 Spring AI', '{"Spring AI",DeepSeek}', 'STUDY', 'Spring AI 与 DeepSeek 集成学习', '学习如何将 Spring AI 与 DeepSeek 结合', '{"Spring AI",DeepSeek,笔记整理}', 'STUDY', 'confirmed', NULL, '2026-06-10 07:35:16.752387+00', '2026-06-11 02:11:45.545881+00', NULL, '今天学习了 Spring AI 与 DeepSeek 的集成方式');
INSERT INTO "public"."knowledge_items" VALUES ('03e1d4c2-838e-49ba-82a3-f8e9ef0e2fab', '第一次测试', '2026.6.10.15.44完成第一次开发，进行第一次测试', '首次开发测试记录', '2026年6月10日15:44完成首次开发并进行初次测试', '{开发,测试,版本记录,里程碑}', 'WORK', '首次开发测试记录', '2026年6月10日15:44完成首次开发并进行初次测试', '{开发,测试,版本记录,里程碑}', 'WORK', 'confirmed', NULL, '2026-06-10 07:45:14.34944+00', '2026-06-11 02:11:45.545881+00', NULL, '2026.6.10.15.44完成第一次开发，进行第一次测试');
INSERT INTO "public"."knowledge_items" VALUES ('59b316e6-adeb-49c5-86de-43e432816372', '测试1', '测试测试测试，测试标签搜索是否正常', '测试标签搜索', '测试标签搜索功能是否正常工作。', '{测试,标签,搜索,功能验证}', 'TODO', '测试标签搜索', '测试标签搜索功能是否正常工作。', '{测试,标签,搜索,功能验证}', 'TODO', 'confirmed', NULL, '2026-06-10 08:17:19.805971+00', '2026-06-11 02:11:45.545881+00', NULL, '测试测试测试，测试标签搜索是否正常');
INSERT INTO "public"."knowledge_items" VALUES ('11b3bf28-eb83-4048-9dc4-2b89cf9da0d7', '大纲', '（区块链要自己搭建。）
论文题目：基于区块链与物联网的农产品溯源管理系统的设计与实现
摘要
研究背景：简述农产品质量安全面临的挑战与传统溯源方式的弊端。
研究内容：提出一个融合物联网、区块链和Web技术的农产品溯源管理系统解决方案。
关键技术：阐明系统采用的核心技术（Spring Boot, Vue.js, Hyperledger Fabric）。
主要成果：完成了系统的分析、设计、实现与测试，构建了一个信息可信、全程可溯、多角色协同的管理平台。
研究价值：该系统对提升食品安全监管效率、增强消费者信任、推动农业信息化具有重要意义。
关键词：农产品溯源；区块链；Hyperledger Fabric；Spring Boot；Vue.js
第一章 引言
1.1 研究背景与意义
1.1.1 农产品质量安全现状与挑战
1.1.2 传统溯源模式的局限性
1.1.3 现代信息技术在溯源中的应用潜力
1.1.4 本研究的理论与实践意义
1.2 国内外研究现状
1.2.1 国外农产品溯源系统研究综述
1.2.2 国内农产品溯源系统研究综述
1.2.3 区块链技术在溯源领域应用综述
1.2.4 现有研究的总结与不足
1.3 研究目标与主要内容
1.3.1 研究目标
1.3.2 主要研究内容
1.4 论文组织结构
阐述本文章节安排与内在逻辑。
第二章 相关理论与技术综述
2.1 农产品溯源理论
2.1.1 溯源概念与流程
2.1.2 关键溯源信息分析
2.2 区块链技术
2.2.1 区块链基本原理与特性（去中心化、不可篡改等）
2.2.2 Hyperledger Fabric 架构与核心概念（通道、链码、节点）
2.2.3 区块链在溯源中的适用性分析
2.3 物联网技术
2.3.1 物联网在数据采集中的作用
2.3.2 常用传感器与数据传输协议
2.4 系统开发关键技术
2.4.1 后端技术：Spring Boot 框架
2.4.2 前端技术：Vue.js 框架与Element UI
2.4.3 数据库技术：MySQL 与 Redis
第三章 系统需求分析
3.1 业务需求分析
3.1.1 溯源业务流程梳理（从生产到消费）
3.2 系统角色与功能性需求分析
3.2.1 参与者分析（生产者、加工商、物流商、零售商、消费者、监管方）
3.2.2 系统用例分析
3.2.3 核心功能需求（信息录入、链上存证、追溯查询、权限管理等）
3.3 非功能性需求分析
3.3.1 性能需求
3.3.2 安全性需求
3.3.3 可扩展性需求
3.3.4 易用性需求
第四章 系统设计
4.1 系统总体架构设计
4.1.1 系统架构设计原则
4.1.2 前后端分离的总体架构图
4.1.3 技术栈选型说明
4.2 功能模块设计
4.2.1 生产信息管理模块
4.2.2 加工信息管理模块
4.2.3 物流信息管理模块
4.2.4 销售信息管理模块
4.2.5 溯源查询模块
4.2.6 系统管理模块（权限、角色）
4.3 区块链模块设计
4.3.1 Fabric 网络架构设计
4.3.2 链码（智能合约）设计（数据结构、核心函数）
4.3.3 数据上链与查询流程设计
4.4 数据库设计
4.4.1 核心E-R图
4.4.2 主要数据表结构设计（用户表、产品表、环节记录表等）
4.5 接口设计
4.5.1 前后端RESTful API设计
4.5.2 区块链服务接口设计
第五章 系统实现与测试
5.1 系统开发环境
列出开发工具、服务器环境及配置。
5.2 核心功能模块实现
5.2.1 后端业务逻辑与API实现（以关键接口为例）
5.2.2 前端界面与交互实现（以管理后台和扫码页面为例）
5.2.3 区块链网络部署与链码实现
5.2.4 关键技术的实现难点与解决方案（如数据上链策略、前后端认证）
5.3 系统测试
5.3.1 测试环境部署
5.3.2 测试策略与用例设计
5.3.3 功能测试结果与分析
5.3.4 性能测试结果与分析
5.3.5 安全性测试结果与分析
5.3.6 区块链模块专项测试
5.3.7 测试总结
第六章 总结与展望
6.1 研究工作总结
系统性地回顾本研究完成的主要工作与取得的成果。
6.2 系统特色与创新点
总结系统的优势（如多技术融合、可信数据、良好的权限模型等）。
6.3 存在的问题与不足
客观分析系统当前局限性（如物联网设备集成深度、区块链性能等）。
6.4 未来工作展望
提出下一步改进方向（如引入大数据分析、扩展AI视觉识别、优化区块链共识机制等）。
参考文献
致谢
附录
附录A：系统部分核心代码
附录B：完整的数据库表结构
附录C：系统使用截图（管理界面、扫码查询结果页等）
给您的建议：
1.  文献综述是关键：第二章的“国内外研究现状”需要您单独投入大量精力进行文献检索和阅读，这是论文理论深度的体现。
2.  突出创新点：在论文中，要反复强调并深入阐述“区块链与物联网结合解决溯源可信问题”这一核心创新点。
3.  图文并茂：在第四、五章中，多使用架构图、流程图、类图、界面截图、测试结果图表等，让描述更清晰、论证更有力。
4.  数据支撑：在测试章节，尽量用具体的数据（如响应时间、并发用户数、CPU负载）来证明系统的有效性。
5.	区块链要自己搭建。', '区块链物联网农产品溯源毕业论文大纲', '基于区块链与物联网的农产品溯源管理系统毕业论文大纲，采用Hyperledger Fabric自行搭建网络，融合Spring Boot和Vue.js实现可信溯源。', '{区块链,物联网,农产品溯源,论文大纲,系统设计}', 'STUDY', '区块链物联网农产品溯源毕业论文大纲', '基于区块链与物联网的农产品溯源管理系统毕业论文大纲，采用Hyperledger Fabric自行搭建网络，融合Spring Boot和Vue.js实现可信溯源。', '{区块链,物联网,农产品溯源,论文大纲,系统设计}', 'STUDY', 'confirmed', NULL, '2026-06-11 01:48:27.827081+00', '2026-06-11 02:11:45.545881+00', NULL, '（区块链要自己搭建。）
论文题目：基于区块链与物联网的农产品溯源管理系统的设计与实现
摘要
研究背景：简述农产品质量安全面临的挑战与传统溯源方式的弊端。
研究内容：提出一个融合物联网、区块链和Web技术的农产品溯源管理系统解决方案。
关键技术：阐明系统采用的核心技术（Spring Boot, Vue.js, Hyperledger Fabric）。
主要成果：完成了系统的分析、设计、实现与测试，构建了一个信息可信、全程可溯、多角色协同的管理平台。
研究价值：该系统对提升食品安全监管效率、增强消费者信任、推动农业信息化具有重要意义。
关键词：农产品溯源；区块链；Hyperledger Fabric；Spring Boot；Vue.js
第一章 引言
1.1 研究背景与意义
1.1.1 农产品质量安全现状与挑战
1.1.2 传统溯源模式的局限性
1.1.3 现代信息技术在溯源中的应用潜力
1.1.4 本研究的理论与实践意义
1.2 国内外研究现状
1.2.1 国外农产品溯源系统研究综述
1.2.2 国内农产品溯源系统研究综述
1.2.3 区块链技术在溯源领域应用综述
1.2.4 现有研究的总结与不足
1.3 研究目标与主要内容
1.3.1 研究目标
1.3.2 主要研究内容
1.4 论文组织结构
阐述本文章节安排与内在逻辑。
第二章 相关理论与技术综述
2.1 农产品溯源理论
2.1.1 溯源概念与流程
2.1.2 关键溯源信息分析
2.2 区块链技术
2.2.1 区块链基本原理与特性（去中心化、不可篡改等）
2.2.2 Hyperledger Fabric 架构与核心概念（通道、链码、节点）
2.2.3 区块链在溯源中的适用性分析
2.3 物联网技术
2.3.1 物联网在数据采集中的作用
2.3.2 常用传感器与数据传输协议
2.4 系统开发关键技术
2.4.1 后端技术：Spring Boot 框架
2.4.2 前端技术：Vue.js 框架与Element UI
2.4.3 数据库技术：MySQL 与 Redis
第三章 系统需求分析
3.1 业务需求分析
3.1.1 溯源业务流程梳理（从生产到消费）
3.2 系统角色与功能性需求分析
3.2.1 参与者分析（生产者、加工商、物流商、零售商、消费者、监管方）
3.2.2 系统用例分析
3.2.3 核心功能需求（信息录入、链上存证、追溯查询、权限管理等）
3.3 非功能性需求分析
3.3.1 性能需求
3.3.2 安全性需求
3.3.3 可扩展性需求
3.3.4 易用性需求
第四章 系统设计
4.1 系统总体架构设计
4.1.1 系统架构设计原则
4.1.2 前后端分离的总体架构图
4.1.3 技术栈选型说明
4.2 功能模块设计
4.2.1 生产信息管理模块
4.2.2 加工信息管理模块
4.2.3 物流信息管理模块
4.2.4 销售信息管理模块
4.2.5 溯源查询模块
4.2.6 系统管理模块（权限、角色）
4.3 区块链模块设计
4.3.1 Fabric 网络架构设计
4.3.2 链码（智能合约）设计（数据结构、核心函数）
4.3.3 数据上链与查询流程设计
4.4 数据库设计
4.4.1 核心E-R图
4.4.2 主要数据表结构设计（用户表、产品表、环节记录表等）
4.5 接口设计
4.5.1 前后端RESTful API设计
4.5.2 区块链服务接口设计
第五章 系统实现与测试
5.1 系统开发环境
列出开发工具、服务器环境及配置。
5.2 核心功能模块实现
5.2.1 后端业务逻辑与API实现（以关键接口为例）
5.2.2 前端界面与交互实现（以管理后台和扫码页面为例）
5.2.3 区块链网络部署与链码实现
5.2.4 关键技术的实现难点与解决方案（如数据上链策略、前后端认证）
5.3 系统测试
5.3.1 测试环境部署
5.3.2 测试策略与用例设计
5.3.3 功能测试结果与分析
5.3.4 性能测试结果与分析
5.3.5 安全性测试结果与分析
5.3.6 区块链模块专项测试
5.3.7 测试总结
第六章 总结与展望
6.1 研究工作总结
系统性地回顾本研究完成的主要工作与取得的成果。
6.2 系统特色与创新点
总结系统的优势（如多技术融合、可信数据、良好的权限模型等）。
6.3 存在的问题与不足
客观分析系统当前局限性（如物联网设备集成深度、区块链性能等）。
6.4 未来工作展望
提出下一步改进方向（如引入大数据分析、扩展AI视觉识别、优化区块链共识机制等）。
参考文献
致谢
附录
附录A：系统部分核心代码
附录B：完整的数据库表结构
附录C：系统使用截图（管理界面、扫码查询结果页等）
给您的建议：
1.  文献综述是关键：第二章的“国内外研究现状”需要您单独投入大量精力进行文献检索和阅读，这是论文理论深度的体现。
2.  突出创新点：在论文中，要反复强调并深入阐述“区块链与物联网结合解决溯源可信问题”这一核心创新点。
3.  图文并茂：在第四、五章中，多使用架构图、流程图、类图、界面截图、测试结果图表等，让描述更清晰、论证更有力。
4.  数据支撑：在测试章节，尽量用具体的数据（如响应时间、并发用户数、CPU负载）来证明系统的有效性。
5.	区块链要自己搭建。');
INSERT INTO "public"."knowledge_items" VALUES ('0d72cd90-67b9-4400-9ba7-dcb8ffa617ed', '文帅-答辩记录', '提问
区块链的特点是什么？
答：去中心化：数据不依赖单一中心服务器或机构，而是由网络中的所有参与者共同维护。这降低了因中心节点故障或作恶而导致系统崩溃的风险。
不可篡改：一旦数据被记录到区块链上，只要网络中的多数节点遵循共识机制，已有的记录就无法被单方面修改或删除。这通过哈希值链式连接和共识算法来保证。
透明性与可追溯：链上的所有交易记录对所有参与者公开，且每笔交易都可以通过时间戳和哈希链接追溯到前一个区块，形成完整的交易历史。
匿名性与隐私保护：参与者通常以地址而非真实身份进行交互。虽然交易记录公开，但地址背后的真实身份难以直接关联，提供了较好的隐私保护。
共识机制：网络中节点需要通过某种共识算法对新区块的数据达成一致，确保各节点账本同步，且防止恶意攻击。
智能合约：许多区块链平台支持在链上运行自动执行的智能合约。这允许在无需第三方信任机构的情况下，实现复杂的逻辑和交易条件。
为什么使用微服务，不是单体架构？
答：我选择微服务，是因为系统角色多、业务链条长，生产、加工、物流、销售等模块职责差异明显，拆分后便于独立开发和扩展，也有利于接口边界清晰。论文中我也明确把生产管理、加工记录、物流追踪等功能按服务拆分，并通过RESTful方式通信。不过如果从部署复杂度和本科项目实施成本看，微服务确实会提升环境搭建和运维难度，所以这个方案更偏向为后续扩展预留空间，而不是追求最简实现。
为什选Fabric，不选以太坊？
答：我选择Fabric，核心原因是本系统不是完全公开的开放网络，而是多主体协作但权限可控的业务场景。农产品溯源涉及企业、监管机构、生产者等不同参与方，既需要共享关键数据，也要考虑隐私保护和准入控制。Fabric属于联盟链，支持节点准入、权限管理和通道隔离，更适合这类半开放、多机构协同的溯源业务场景。
“上链流程”的实际流程是什么？
答：在系统中，用户先在Web后台完成业务操作，系统会对关键业务数据计算SHA256哈希，然后通过Fabric SDK调用用户私钥对交易提案进行签名并提交给Peer节点。Peer节点模拟执行智能合约后生成读写集，SDK在收集足量响应后再提交给Orderer节点排序打包。新区块广播到各Peer节点后完成验证并写入账本，最后SDK捕获成功事件，把交易哈希、区块高度等信息写入blockchain_transaction表，实现链上存证与链下业务数据的锚定。
论文存在的问题
3.2用例图用直线
4.1.2系统功能模块图横着画，分层次
4.2流程图中的图4-5、4-7、4-8、4-10、4-11、4-12、4-14、4-15、4-17、4-18、4-25、4-28、4-31、4-34、4-36、4-84要有判断条件
4.3.2数据库表的字段长度要调整，字段长度要合理
第五章实现部分要增加，现在的太少了
结论、致谢替换掉本课题的字样', '文帅-答辩记录与论文勘误建议', '关于区块链特性、微服务选型、Fabric联盟链原因及论文格式修改的毕业答辩问答与勘误记录。', '{答辩记录,区块链,微服务,论文修改,Fabric}', 'STUDY', '文帅-答辩记录与论文勘误建议', '关于区块链特性、微服务选型、Fabric联盟链原因及论文格式修改的毕业答辩问答与勘误记录。', '{答辩记录,区块链,微服务,论文修改,Fabric}', 'STUDY', 'confirmed', NULL, '2026-06-11 02:14:42.387136+00', '2026-06-11 02:14:42.387136+00', '## 答辩提问与回答

### 区块链的特点是什么？

- 去中心化：数据不依赖单一中心服务器或机构，由网络中的所有参与者共同维护，降低中心节点故障或作恶导致系统崩溃的风险。
- 不可篡改：数据一经记录，只要多数节点遵循共识机制，已有记录无法单方面修改或删除，通过哈希值链式连接和共识算法保证。
- 透明性与可追溯：链上所有交易记录对参与者公开，每笔交易可通过时间戳和哈希链接追溯到前一区块，形成完整交易历史。
- 匿名性与隐私保护：参与者以地址而非真实身份交互，交易记录公开但真实身份难以直接关联，提供较好的隐私保护。
- 共识机制：网络节点通过共识算法对新块数据达成一致，确保各节点账本同步，防止恶意攻击。
- 智能合约：多数区块链平台支持在链上运行自动执行的智能合约，无需第三方信任机构即可实现复杂逻辑和交易条件。

### 为什么使用微服务，不是单体架构？

系统角色多、业务链条长，生产、加工、物流、销售等模块职责差异明显，拆分后便于独立开发与扩展，接口边界清晰。论文中已将生产管理、加工记录、物流追踪等功能按服务拆分，通过 RESTful 方式通信。从部署复杂度和本科项目实施成本考虑，微服务会提升环境搭建和运维难度，方案偏向为后续扩展预留空间，而非追求最简实现。

### 为什么选 Fabric，不选以太坊？

核心原因是本系统不是完全公开的开放网络，而是多主体协作但权限可控的业务场景。农产品溯源涉及企业、监管机构、生产者等不同参与方，既需要共享关键数据，也要考虑隐私保护和准入控制。Fabric 属于联盟链，支持节点准入、权限管理和通道隔离，更适合这类半开放、多机构协同的溯源业务场景。

### “上链流程”的实际流程是什么？

1. 用户在 Web 后台完成业务操作。
2. 系统对关键业务数据计算 SHA256 哈希。
3. 通过 Fabric SDK 调用用户私钥对交易提案进行签名并提交给 Peer 节点。
4. Peer 节点模拟执行智能合约后生成读写集。
5. SDK 收集足量响应后提交给 Orderer 节点排序打包。
6. 新区块广播到各 Peer 节点完成验证并写入账本。
7. SDK 捕获成功事件，将交易哈希、区块高度等信息写入 blockchain_transaction 表，实现链上存证与链下业务数据的锚定。

## 论文存在的问题

### 图表类修改
- 3.2 用例图用直线绘制。
- 4.1.2 系统功能模块图横向绘制，分层次展示。
- 4.2 流程图中以下图号需增加判断条件：4-5、4-7、4-8、4-10、4-11、4-12、4-14、4-15、4-17、4-18、4-25、4-28、4-31、4-34、4-36、4-84。

### 数据库与内容调整
- 4.3.2 数据库表的字段长度需调整，保证字段长度合理。
- 第五章实现部分内容过少，需大幅增加。
- 结论、致谢部分替换掉“本课题”字样。', '## 答辩提问与回答

### 区块链的特点是什么？

- 去中心化：数据不依赖单一中心服务器或机构，由网络中的所有参与者共同维护，降低中心节点故障或作恶导致系统崩溃的风险。
- 不可篡改：数据一经记录，只要多数节点遵循共识机制，已有记录无法单方面修改或删除，通过哈希值链式连接和共识算法保证。
- 透明性与可追溯：链上所有交易记录对参与者公开，每笔交易可通过时间戳和哈希链接追溯到前一区块，形成完整交易历史。
- 匿名性与隐私保护：参与者以地址而非真实身份交互，交易记录公开但真实身份难以直接关联，提供较好的隐私保护。
- 共识机制：网络节点通过共识算法对新块数据达成一致，确保各节点账本同步，防止恶意攻击。
- 智能合约：多数区块链平台支持在链上运行自动执行的智能合约，无需第三方信任机构即可实现复杂逻辑和交易条件。

### 为什么使用微服务，不是单体架构？

系统角色多、业务链条长，生产、加工、物流、销售等模块职责差异明显，拆分后便于独立开发与扩展，接口边界清晰。论文中已将生产管理、加工记录、物流追踪等功能按服务拆分，通过 RESTful 方式通信。从部署复杂度和本科项目实施成本考虑，微服务会提升环境搭建和运维难度，方案偏向为后续扩展预留空间，而非追求最简实现。

### 为什么选 Fabric，不选以太坊？

核心原因是本系统不是完全公开的开放网络，而是多主体协作但权限可控的业务场景。农产品溯源涉及企业、监管机构、生产者等不同参与方，既需要共享关键数据，也要考虑隐私保护和准入控制。Fabric 属于联盟链，支持节点准入、权限管理和通道隔离，更适合这类半开放、多机构协同的溯源业务场景。

### “上链流程”的实际流程是什么？

1. 用户在 Web 后台完成业务操作。
2. 系统对关键业务数据计算 SHA256 哈希。
3. 通过 Fabric SDK 调用用户私钥对交易提案进行签名并提交给 Peer 节点。
4. Peer 节点模拟执行智能合约后生成读写集。
5. SDK 收集足量响应后提交给 Orderer 节点排序打包。
6. 新区块广播到各 Peer 节点完成验证并写入账本。
7. SDK 捕获成功事件，将交易哈希、区块高度等信息写入 blockchain_transaction 表，实现链上存证与链下业务数据的锚定。

## 论文存在的问题

### 图表类修改
- 3.2 用例图用直线绘制。
- 4.1.2 系统功能模块图横向绘制，分层次展示。
- 4.2 流程图中以下图号需增加判断条件：4-5、4-7、4-8、4-10、4-11、4-12、4-14、4-15、4-17、4-18、4-25、4-28、4-31、4-34、4-36、4-84。

### 数据库与内容调整
- 4.3.2 数据库表的字段长度需调整，保证字段长度合理。
- 第五章实现部分内容过少，需大幅增加。
- 结论、致谢部分替换掉“本课题”字样。');
INSERT INTO "public"."knowledge_items" VALUES ('b25c3dd1-dffa-4bab-ab52-e529efba01bc', '中期检查', '一、存在的主要问题：
1.  内容深度有待加强，核心创新点阐述不够突出；
论文目前对“链上-链下”混合存储架构的描述偏重于功能实现，但对其相较于纯链上或纯中心化方案在性能、成本、安全性等方面的量化优势分析不足。同时，Hyperledger Fabric在本系统中的具体应用价值（如通道、私有数据集合等特性是否被有效利用）尚未深入展开，导致论文的理论深度和创新性体现不够充分。
2.  整体框架逻辑衔接存在断层，部分章节过渡生硬；
论文从“系统分析”到“系统设计”再到“系统实现”的递进关系不够流畅。例如，“系统分析”章节的需求用例与“系统设计”章节的数据库ER图、系统架构图之间缺乏明确的映射说明；“系统实现”章节的功能截图与前文的设计方案未能形成一一对应的佐证关系，使得论文整体的论证链条显得松散。
3.  学术格式规范性不足，引用与图表标准不统一；
论文中存在多处格式问题：参考文献的引用格式（如[1]、[2]）未严格遵循国标GB/T 7714规范，部分文献缺少关键信息（如页码、出版社）；正文中的图表编号混乱（如图4-1后直接出现图4-3），且图表标题位置、字体不统一；部分章节标题层级使用不规范，影响了论文的专业性和可读性。
4.  技术实现细节描述模糊，关键代码与流程缺失；
在“系统实现”章节，对于核心业务逻辑（如“如何生成数据哈希并上链”、“消费者扫码查询时链上链下数据如何协同验证”）的描述过于笼统，仅展示了前端界面截图，缺少关键的后端服务伪代码、智能合约（Chaincode）核心逻辑片段以及跨系统交互的时序图。这使得技术方案的可行性和严谨性难以被有效评估。
二、解决方法：
1.  深化内容分析，聚焦并凸显核心创新点；
在第四章（系统设计）或第五章（系统实现）中增设专门小节，通过对比分析表格，量化阐述本方案在吞吐量、存储成本、查询延迟等方面的优势。同时，结合Hyperledger Fabric的技术特性，详细说明本系统如何利用其通道（Channel）机制实现企业间数据隔离，以及如何通过私有数据集合（Private Data Collection） 处理敏感商业信息，从而强化论文的理论贡献与实践价值。
2.  优化论文框架，强化各章节间的逻辑闭环；
重新梳理论文脉络，在“系统设计”章节开头增加一段承上启下的文字，明确指出本章设计是基于第三章所分析的哪些具体需求。在展示每个核心模块（如批次管理、物流跟踪）的设计方案后，在“系统实现”章节对应部分，首先重申该模块的设计目标，再展示实现效果，并附上简短的实现说明，确保“需求-设计-实现”三者形成清晰、紧密的逻辑闭环。
3.  严格遵循学术规范，全面统一格式标准；
依据学校或学院发布的毕业论文撰写规范，使用文献管理工具（如EndNote, NoteExpress）统一修正所有参考文献的格式，确保信息完整、标点正确。对全文所有图表进行重新编号和命名，确保连续无误，并统一图表标题的样式（如置于图下方、表上方）。最后，通篇检查并修正各级标题的字体、字号和段落格式，保证全文风格一致。
4.  补充关键技术细节，增强方案的可验证性；
在“系统实现”章节的关键部分，补充核心业务流程的时序图或活动图，直观展示链上链下交互过程。同时，嵌入关键算法的伪代码或精简后的Java/Vue核心代码片段（如哈希计算与上链接口、Fabric SDK调用逻辑），并对代码功能进行简要注释。此举不仅能清晰地阐明技术实现路径，也能显著提升论文的专业性和可信度。', '毕业论文中期检查问题与改进方案', '分析论文中期检查中内容深度、逻辑衔接、格式规范、技术细节四类问题，提出聚焦创新点、强化逻辑闭环、统一标准、补充实现细节等改进方案。', '{论文写作,中期检查,区块链,学术规范,逻辑框架}', 'STUDY', '毕业论文中期检查问题与改进方案', '分析论文中期检查中内容深度、逻辑衔接、格式规范、技术细节四类问题，提出聚焦创新点、强化逻辑闭环、统一标准、补充实现细节等改进方案。', '{论文写作,中期检查,区块链,学术规范,逻辑框架}', 'STUDY', 'confirmed', NULL, '2026-06-11 03:18:36.243458+00', '2026-06-11 03:18:36.243458+00', '## 一、存在的主要问题

**1. 内容深度有待加强，核心创新点阐述不够突出**
- 论文目前对“链上-链下”混合存储架构的描述偏重于功能实现，但对其相较于纯链上或纯中心化方案在性能、成本、安全性等方面的量化优势分析不足。
- 同时，Hyperledger Fabric在本系统中的具体应用价值（如通道、私有数据集合等特性是否被有效利用）尚未深入展开，导致论文的理论深度和创新性体现不够充分。

**2. 整体框架逻辑衔接存在断层，部分章节过渡生硬**
- 论文从“系统分析”到“系统设计”再到“系统实现”的递进关系不够流畅。例如，“系统分析”章节的需求用例与“系统设计”章节的数据库ER图、系统架构图之间缺乏明确的映射说明。
- “系统实现”章节的功能截图与前文的设计方案未能形成一一对应的佐证关系，使得论文整体的论证链条显得松散。

**3. 学术格式规范性不足，引用与图表标准不统一**
- 论文中存在多处格式问题：参考文献的引用格式（如[1]、[2]）未严格遵循国标GB/T 7714规范，部分文献缺少关键信息（如页码、出版社）。
- 正文中的图表编号混乱（如图4-1后直接出现图4-3），且图表标题位置、字体不统一；部分章节标题层级使用不规范，影响了论文的专业性和可读性。

**4. 技术实现细节描述模糊，关键代码与流程缺失**
- 在“系统实现”章节，对于核心业务逻辑（如“如何生成数据哈希并上链”、“消费者扫码查询时链上链下数据如何协同验证”）的描述过于笼统，仅展示了前端界面截图，缺少关键的后端服务伪代码、智能合约（Chaincode）核心逻辑片段以及跨系统交互的时序图。这使得技术方案的可行性和严谨性难以被有效评估。

## 二、解决方法

**1. 深化内容分析，聚焦并凸显核心创新点**
- 在第四章（系统设计）或第五章（系统实现）中增设专门小节，通过对比分析表格，量化阐述本方案在吞吐量、存储成本、查询延迟等方面的优势。
- 同时，结合Hyperledger Fabric的技术特性，详细说明本系统如何利用其通道（Channel）机制实现企业间数据隔离，以及如何通过私有数据集合（Private Data Collection）处理敏感商业信息，从而强化论文的理论贡献与实践价值。

**2. 优化论文框架，强化各章节间的逻辑闭环**
- 重新梳理论文脉络，在“系统设计”章节开头增加一段承上启下的文字，明确指出本章设计是基于第三章所分析的哪些具体需求。
- 在展示每个核心模块（如批次管理、物流跟踪）的设计方案后，在“系统实现”章节对应部分，首先重申该模块的设计目标，再展示实现效果，并附上简短的实现说明，确保“需求-设计-实现”三者形成清晰、紧密的逻辑闭环。

**3. 严格遵循学术规范，全面统一格式标准**
- 依据学校或学院发布的毕业论文撰写规范，使用文献管理工具（如EndNote, NoteExpress）统一修正所有参考文献的格式，确保信息完整、标点正确。
- 对全文所有图表进行重新编号和命名，确保连续无误，并统一图表标题的样式（如置于图下方、表上方）。最后，通篇检查并修正各级标题的字体、字号和段落格式，保证全文风格一致。

**4. 补充关键技术细节，增强方案的可验证性**
- 在“系统实现”章节的关键部分，补充核心业务流程的时序图或活动图，直观展示链上链下交互过程。
- 同时，嵌入关键算法的伪代码或精简后的Java/Vue核心代码片段（如哈希计算与上链接口、Fabric SDK调用逻辑），并对代码功能进行简要注释。此举不仅能清晰地阐明技术实现路径，也能显著提升论文的专业性和可信度。', '## 一、存在的主要问题

**1. 内容深度有待加强，核心创新点阐述不够突出**
- 论文目前对“链上-链下”混合存储架构的描述偏重于功能实现，但对其相较于纯链上或纯中心化方案在性能、成本、安全性等方面的量化优势分析不足。
- 同时，Hyperledger Fabric在本系统中的具体应用价值（如通道、私有数据集合等特性是否被有效利用）尚未深入展开，导致论文的理论深度和创新性体现不够充分。

**2. 整体框架逻辑衔接存在断层，部分章节过渡生硬**
- 论文从“系统分析”到“系统设计”再到“系统实现”的递进关系不够流畅。例如，“系统分析”章节的需求用例与“系统设计”章节的数据库ER图、系统架构图之间缺乏明确的映射说明。
- “系统实现”章节的功能截图与前文的设计方案未能形成一一对应的佐证关系，使得论文整体的论证链条显得松散。

**3. 学术格式规范性不足，引用与图表标准不统一**
- 论文中存在多处格式问题：参考文献的引用格式（如[1]、[2]）未严格遵循国标GB/T 7714规范，部分文献缺少关键信息（如页码、出版社）。
- 正文中的图表编号混乱（如图4-1后直接出现图4-3），且图表标题位置、字体不统一；部分章节标题层级使用不规范，影响了论文的专业性和可读性。

**4. 技术实现细节描述模糊，关键代码与流程缺失**
- 在“系统实现”章节，对于核心业务逻辑（如“如何生成数据哈希并上链”、“消费者扫码查询时链上链下数据如何协同验证”）的描述过于笼统，仅展示了前端界面截图，缺少关键的后端服务伪代码、智能合约（Chaincode）核心逻辑片段以及跨系统交互的时序图。这使得技术方案的可行性和严谨性难以被有效评估。

## 二、解决方法

**1. 深化内容分析，聚焦并凸显核心创新点**
- 在第四章（系统设计）或第五章（系统实现）中增设专门小节，通过对比分析表格，量化阐述本方案在吞吐量、存储成本、查询延迟等方面的优势。
- 同时，结合Hyperledger Fabric的技术特性，详细说明本系统如何利用其通道（Channel）机制实现企业间数据隔离，以及如何通过私有数据集合（Private Data Collection）处理敏感商业信息，从而强化论文的理论贡献与实践价值。

**2. 优化论文框架，强化各章节间的逻辑闭环**
- 重新梳理论文脉络，在“系统设计”章节开头增加一段承上启下的文字，明确指出本章设计是基于第三章所分析的哪些具体需求。
- 在展示每个核心模块（如批次管理、物流跟踪）的设计方案后，在“系统实现”章节对应部分，首先重申该模块的设计目标，再展示实现效果，并附上简短的实现说明，确保“需求-设计-实现”三者形成清晰、紧密的逻辑闭环。

**3. 严格遵循学术规范，全面统一格式标准**
- 依据学校或学院发布的毕业论文撰写规范，使用文献管理工具（如EndNote, NoteExpress）统一修正所有参考文献的格式，确保信息完整、标点正确。
- 对全文所有图表进行重新编号和命名，确保连续无误，并统一图表标题的样式（如置于图下方、表上方）。最后，通篇检查并修正各级标题的字体、字号和段落格式，保证全文风格一致。

**4. 补充关键技术细节，增强方案的可验证性**
- 在“系统实现”章节的关键部分，补充核心业务流程的时序图或活动图，直观展示链上链下交互过程。
- 同时，嵌入关键算法的伪代码或精简后的Java/Vue核心代码片段（如哈希计算与上链接口、Fabric SDK调用逻辑），并对代码功能进行简要注释。此举不仅能清晰地阐明技术实现路径，也能显著提升论文的专业性和可信度。');

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_numeric, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_numeric, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_float4, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_float4, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_float8, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_float8, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_halfvec"(_int4, int4, bool);
CREATE FUNCTION "public"."array_to_halfvec"(_int4, int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'array_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_float4, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_float4, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_numeric, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_numeric, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_int4, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_int4, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_sparsevec"(_float8, int4, bool);
CREATE FUNCTION "public"."array_to_sparsevec"(_float8, int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'array_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_float8, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_float8, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_float4, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_float4, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_int4, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_int4, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for array_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."array_to_vector"(_numeric, int4, bool);
CREATE FUNCTION "public"."array_to_vector"(_numeric, int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'array_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for binary_quantize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."binary_quantize"("public"."halfvec");
CREATE FUNCTION "public"."binary_quantize"("public"."halfvec")
  RETURNS "pg_catalog"."bit" AS '$libdir/vector', 'halfvec_binary_quantize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for binary_quantize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."binary_quantize"("public"."vector");
CREATE FUNCTION "public"."binary_quantize"("public"."vector")
  RETURNS "pg_catalog"."bit" AS '$libdir/vector', 'binary_quantize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."cosine_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."cosine_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for cosine_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."cosine_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."cosine_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_cosine_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec"("public"."halfvec", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_accum
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_accum"(_float8, "public"."halfvec");
CREATE FUNCTION "public"."halfvec_accum"(_float8, "public"."halfvec")
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'halfvec_accum'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_add
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_add"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_add"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_add'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_avg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_avg"(_float8);
CREATE FUNCTION "public"."halfvec_avg"(_float8)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_avg'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_cmp"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_combine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_combine"(_float8, _float8);
CREATE FUNCTION "public"."halfvec_combine"(_float8, _float8)
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_combine'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_concat"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_concat"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_eq"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_eq"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_ge"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_ge"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_gt"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_gt"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_in"(cstring, oid, int4);
CREATE FUNCTION "public"."halfvec_in"(cstring, oid, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_l2_squared_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_le"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_le"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_lt"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_lt"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_mul
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_mul"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_mul"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_mul'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_ne"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_ne"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'halfvec_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_negative_inner_product"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_out"("public"."halfvec");
CREATE FUNCTION "public"."halfvec_out"("public"."halfvec")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'halfvec_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_recv"(internal, oid, int4);
CREATE FUNCTION "public"."halfvec_recv"(internal, oid, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_send"("public"."halfvec");
CREATE FUNCTION "public"."halfvec_send"("public"."halfvec")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'halfvec_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_spherical_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_spherical_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_spherical_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_sub
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_sub"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."halfvec_sub"("public"."halfvec", "public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_sub'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_to_float4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_float4"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec_to_float4"("public"."halfvec", int4, bool)
  RETURNS "pg_catalog"."_float4" AS '$libdir/vector', 'halfvec_to_float4'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec_to_sparsevec"("public"."halfvec", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'halfvec_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_to_vector"("public"."halfvec", int4, bool);
CREATE FUNCTION "public"."halfvec_to_vector"("public"."halfvec", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'halfvec_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for halfvec_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."halfvec_typmod_in"(_cstring);
CREATE FUNCTION "public"."halfvec_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for hamming_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hamming_distance"(bit, bit);
CREATE FUNCTION "public"."hamming_distance"(bit, bit)
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'hamming_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for hnsw_bit_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_bit_support"(internal);
CREATE FUNCTION "public"."hnsw_bit_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_bit_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for hnsw_halfvec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_halfvec_support"(internal);
CREATE FUNCTION "public"."hnsw_halfvec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_halfvec_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for hnsw_sparsevec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnsw_sparsevec_support"(internal);
CREATE FUNCTION "public"."hnsw_sparsevec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'hnsw_sparsevec_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for hnswhandler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hnswhandler"(internal);
CREATE FUNCTION "public"."hnswhandler"(internal)
  RETURNS "pg_catalog"."index_am_handler" AS '$libdir/vector', 'hnswhandler'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."inner_product"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."inner_product"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."inner_product"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."inner_product"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for ivfflat_bit_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflat_bit_support"(internal);
CREATE FUNCTION "public"."ivfflat_bit_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'ivfflat_bit_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for ivfflat_halfvec_support
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflat_halfvec_support"(internal);
CREATE FUNCTION "public"."ivfflat_halfvec_support"(internal)
  RETURNS "pg_catalog"."internal" AS '$libdir/vector', 'ivfflat_halfvec_support'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for ivfflathandler
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."ivfflathandler"(internal);
CREATE FUNCTION "public"."ivfflathandler"(internal)
  RETURNS "pg_catalog"."index_am_handler" AS '$libdir/vector', 'ivfflathandler'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for jaccard_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."jaccard_distance"(bit, bit);
CREATE FUNCTION "public"."jaccard_distance"(bit, bit)
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'jaccard_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."l1_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."l1_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l1_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l1_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."l1_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'l1_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."l2_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."halfvec", "public"."halfvec");
CREATE FUNCTION "public"."l2_distance"("public"."halfvec", "public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."l2_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_norm"("public"."halfvec");
CREATE FUNCTION "public"."l2_norm"("public"."halfvec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'halfvec_l2_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_norm"("public"."sparsevec");
CREATE FUNCTION "public"."l2_norm"("public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."sparsevec");
CREATE FUNCTION "public"."l2_normalize"("public"."sparsevec")
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."halfvec");
CREATE FUNCTION "public"."l2_normalize"("public"."halfvec")
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for l2_normalize
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."l2_normalize"("public"."vector");
CREATE FUNCTION "public"."l2_normalize"("public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'l2_normalize'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec"("public"."sparsevec", int4, bool);
CREATE FUNCTION "public"."sparsevec"("public"."sparsevec", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_cmp"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'sparsevec_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_eq"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_ge"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_gt"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_in"(cstring, oid, int4);
CREATE FUNCTION "public"."sparsevec_in"(cstring, oid, int4)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_l2_squared_distance"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_le"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_lt"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_ne"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'sparsevec_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_negative_inner_product"("public"."sparsevec", "public"."sparsevec")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'sparsevec_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_out"("public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_out"("public"."sparsevec")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'sparsevec_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_recv"(internal, oid, int4);
CREATE FUNCTION "public"."sparsevec_recv"(internal, oid, int4)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'sparsevec_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_send"("public"."sparsevec");
CREATE FUNCTION "public"."sparsevec_send"("public"."sparsevec")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'sparsevec_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool);
CREATE FUNCTION "public"."sparsevec_to_halfvec"("public"."sparsevec", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'sparsevec_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_to_vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool);
CREATE FUNCTION "public"."sparsevec_to_vector"("public"."sparsevec", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'sparsevec_to_vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for sparsevec_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."sparsevec_typmod_in"(_cstring);
CREATE FUNCTION "public"."sparsevec_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'sparsevec_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for subvector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."subvector"("public"."vector", int4, int4);
CREATE FUNCTION "public"."subvector"("public"."vector", int4, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'subvector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for subvector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."subvector"("public"."halfvec", int4, int4);
CREATE FUNCTION "public"."subvector"("public"."halfvec", int4, int4)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'halfvec_subvector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for update_updated_at_column
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."update_updated_at_column"();
CREATE FUNCTION "public"."update_updated_at_column"()
  RETURNS "pg_catalog"."trigger" AS $BODY$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

-- ----------------------------
-- Function structure for uuid_generate_v1
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v1"();
CREATE FUNCTION "public"."uuid_generate_v1"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v1'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_generate_v1mc
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v1mc"();
CREATE FUNCTION "public"."uuid_generate_v1mc"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v1mc'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_generate_v3
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v3"("namespace" uuid, "name" text);
CREATE FUNCTION "public"."uuid_generate_v3"("namespace" uuid, "name" text)
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v3'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_generate_v4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v4"();
CREATE FUNCTION "public"."uuid_generate_v4"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v4'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_generate_v5
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_generate_v5"("namespace" uuid, "name" text);
CREATE FUNCTION "public"."uuid_generate_v5"("namespace" uuid, "name" text)
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_generate_v5'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_nil
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_nil"();
CREATE FUNCTION "public"."uuid_nil"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_nil'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_ns_dns
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_dns"();
CREATE FUNCTION "public"."uuid_ns_dns"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_dns'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_ns_oid
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_oid"();
CREATE FUNCTION "public"."uuid_ns_oid"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_oid'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_ns_url
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_url"();
CREATE FUNCTION "public"."uuid_ns_url"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_url'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_ns_x500
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_ns_x500"();
CREATE FUNCTION "public"."uuid_ns_x500"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/uuid-ossp', 'uuid_ns_x500'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector"("public"."vector", int4, bool)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_accum
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_accum"(_float8, "public"."vector");
CREATE FUNCTION "public"."vector_accum"(_float8, "public"."vector")
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_accum'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_add
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_add"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_add"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_add'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_avg
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_avg"(_float8);
CREATE FUNCTION "public"."vector_avg"(_float8)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_avg'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_cmp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_cmp"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_cmp"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_cmp'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_combine
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_combine"(_float8, _float8);
CREATE FUNCTION "public"."vector_combine"(_float8, _float8)
  RETURNS "pg_catalog"."_float8" AS '$libdir/vector', 'vector_combine'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_concat
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_concat"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_concat"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_concat'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_dims"("public"."vector");
CREATE FUNCTION "public"."vector_dims"("public"."vector")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_dims
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_dims"("public"."halfvec");
CREATE FUNCTION "public"."vector_dims"("public"."halfvec")
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'halfvec_vector_dims'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_eq
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_eq"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_eq"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_eq'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_ge
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_ge"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_ge"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_ge'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_gt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_gt"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_gt"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_gt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_in"(cstring, oid, int4);
CREATE FUNCTION "public"."vector_in"(cstring, oid, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_l2_squared_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_l2_squared_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_l2_squared_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_l2_squared_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_le
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_le"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_le"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_le'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_lt
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_lt"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_lt"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_lt'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_mul
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_mul"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_mul"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_mul'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_ne
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_ne"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_ne"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."bool" AS '$libdir/vector', 'vector_ne'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_negative_inner_product
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_negative_inner_product"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_negative_inner_product"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_negative_inner_product'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_norm
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_norm"("public"."vector");
CREATE FUNCTION "public"."vector_norm"("public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_norm'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_out
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_out"("public"."vector");
CREATE FUNCTION "public"."vector_out"("public"."vector")
  RETURNS "pg_catalog"."cstring" AS '$libdir/vector', 'vector_out'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_recv
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_recv"(internal, oid, int4);
CREATE FUNCTION "public"."vector_recv"(internal, oid, int4)
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_recv'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_send
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_send"("public"."vector");
CREATE FUNCTION "public"."vector_send"("public"."vector")
  RETURNS "pg_catalog"."bytea" AS '$libdir/vector', 'vector_send'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_spherical_distance
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_spherical_distance"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_spherical_distance"("public"."vector", "public"."vector")
  RETURNS "pg_catalog"."float8" AS '$libdir/vector', 'vector_spherical_distance'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_sub
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_sub"("public"."vector", "public"."vector");
CREATE FUNCTION "public"."vector_sub"("public"."vector", "public"."vector")
  RETURNS "public"."vector" AS '$libdir/vector', 'vector_sub'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_to_float4
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_float4"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector_to_float4"("public"."vector", int4, bool)
  RETURNS "pg_catalog"."_float4" AS '$libdir/vector', 'vector_to_float4'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_to_halfvec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_halfvec"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector_to_halfvec"("public"."vector", int4, bool)
  RETURNS "public"."halfvec" AS '$libdir/vector', 'vector_to_halfvec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_to_sparsevec
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_to_sparsevec"("public"."vector", int4, bool);
CREATE FUNCTION "public"."vector_to_sparsevec"("public"."vector", int4, bool)
  RETURNS "public"."sparsevec" AS '$libdir/vector', 'vector_to_sparsevec'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for vector_typmod_in
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."vector_typmod_in"(_cstring);
CREATE FUNCTION "public"."vector_typmod_in"(_cstring)
  RETURNS "pg_catalog"."int4" AS '$libdir/vector', 'vector_typmod_in'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Indexes structure for table idea_categories
-- ----------------------------
CREATE UNIQUE INDEX "idx_idea_categories_code" ON "public"."idea_categories" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_idea_categories_enabled_sort" ON "public"."idea_categories" USING btree (
  "enabled" "pg_catalog"."bool_ops" ASC NULLS LAST,
  "sort_order" "pg_catalog"."int4_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table idea_categories
-- ----------------------------
CREATE TRIGGER "trigger_idea_categories_updated_at" BEFORE UPDATE ON "public"."idea_categories"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Primary Key structure for table idea_categories
-- ----------------------------
ALTER TABLE "public"."idea_categories" ADD CONSTRAINT "idea_categories_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table knowledge_items
-- ----------------------------
CREATE INDEX "idx_knowledge_items_category" ON "public"."knowledge_items" USING btree (
  "final_category" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_items_created_at" ON "public"."knowledge_items" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_items_embedding" ON "public"."knowledge_items" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_items_status" ON "public"."knowledge_items" USING btree (
  "status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_items_tags" ON "public"."knowledge_items" USING gin (
  "final_tags" COLLATE "pg_catalog"."default" "pg_catalog"."array_ops"
);

-- ----------------------------
-- Triggers structure for table knowledge_items
-- ----------------------------
CREATE TRIGGER "trigger_knowledge_items_updated_at" BEFORE UPDATE ON "public"."knowledge_items"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table knowledge_items
-- ----------------------------
ALTER TABLE "public"."knowledge_items" ADD CONSTRAINT "knowledge_items_status_check" CHECK (status::text = ANY (ARRAY['pending'::text, 'confirmed'::text, 'deleted'::text]));

-- ----------------------------
-- Primary Key structure for table knowledge_items
-- ----------------------------
ALTER TABLE "public"."knowledge_items" ADD CONSTRAINT "knowledge_items_pkey" PRIMARY KEY ("id");
