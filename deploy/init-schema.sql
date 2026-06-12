-- IdeaForge 空库初始化（Ubuntu 22.04 部署用）
-- 前置：已 CREATE EXTENSION vector; 已创建用户 yi_ideaforge 与库 knowledge_db
-- 用法：psql -h localhost -U yi_ideaforge -d knowledge_db -f init-schema.sql

CREATE EXTENSION IF NOT EXISTS vector;

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- idea_categories
CREATE TABLE IF NOT EXISTS idea_categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(20) NOT NULL,
    label       VARCHAR(50) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    enabled     BOOL NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ DEFAULT now(),
    updated_at  TIMESTAMPTZ DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_idea_categories_code ON idea_categories (code);
CREATE INDEX IF NOT EXISTS idx_idea_categories_enabled_sort ON idea_categories (enabled, sort_order);

DROP TRIGGER IF EXISTS trigger_idea_categories_updated_at ON idea_categories;
CREATE TRIGGER trigger_idea_categories_updated_at
    BEFORE UPDATE ON idea_categories
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

INSERT INTO idea_categories (code, label, sort_order, enabled) VALUES
    ('WORK', '工作', 1, true),
    ('STUDY', '学习', 2, true),
    ('LIFE', '生活', 3, true),
    ('INSPIRATION', '灵感', 4, true),
    ('TODO', '待办', 5, true)
ON CONFLICT (code) DO NOTHING;

-- knowledge_items
CREATE TABLE IF NOT EXISTS knowledge_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_title      TEXT,
    original_content    TEXT NOT NULL,
    suggested_title     TEXT,
    suggested_summary   TEXT,
    suggested_tags      TEXT[],
    suggested_category  VARCHAR(20),
    suggested_content   TEXT,
    final_title         TEXT NOT NULL,
    final_summary       TEXT,
    final_tags          TEXT[],
    final_category      VARCHAR(20) NOT NULL,
    final_content       TEXT,
    status              VARCHAR(20) DEFAULT 'pending',
    embedding           vector,
    created_at          TIMESTAMPTZ DEFAULT now(),
    updated_at          TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT knowledge_items_status_check CHECK (status IN ('pending', 'confirmed', 'deleted'))
);

CREATE INDEX IF NOT EXISTS idx_knowledge_items_category ON knowledge_items (final_category);
CREATE INDEX IF NOT EXISTS idx_knowledge_items_created_at ON knowledge_items (created_at);
CREATE INDEX IF NOT EXISTS idx_knowledge_items_status ON knowledge_items (status);
CREATE INDEX IF NOT EXISTS idx_knowledge_items_tags ON knowledge_items USING gin (final_tags);
CREATE INDEX IF NOT EXISTS idx_knowledge_items_embedding ON knowledge_items USING hnsw (embedding vector_cosine_ops);

DROP TRIGGER IF EXISTS trigger_knowledge_items_updated_at ON knowledge_items;
CREATE TRIGGER trigger_knowledge_items_updated_at
    BEFORE UPDATE ON knowledge_items
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
