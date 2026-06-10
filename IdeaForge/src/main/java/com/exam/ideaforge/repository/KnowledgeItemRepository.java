package com.exam.ideaforge.repository;

import com.exam.ideaforge.entity.KnowledgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, UUID> {

    /**
     * 按类别检索已确认条目。
     * 使用原生 SQL 避免 Hibernate 6 对 @Convert(ItemStatusConverter) 字段
     * 在派生查询中参数绑定异常（可能将枚举按序数绑定导致 PostgreSQL 类型不匹配）。
     */
    @Query(value = """
            SELECT k.id, k.original_title, k.original_content,
                   k.suggested_title, k.suggested_summary, k.suggested_tags, k.suggested_category,
                   k.final_title, k.final_summary, k.final_tags, k.final_category,
                   k.status, k.created_at, k.updated_at
            FROM knowledge_items k
            WHERE k.status = 'confirmed' AND k.final_category = :category
            ORDER BY k.created_at DESC
            """, nativeQuery = true)
    List<KnowledgeItem> findConfirmedByCategory(@Param("category") String category);

    /**
     * V1 关键词搜索：匹配最终标题、摘要、原始标题/正文及标签数组。
     * 使用原生 SQL 以支持 PostgreSQL text[] 的 unnest 查询；显式列名避免 embedding 列干扰。
     */
    @Query(value = """
            SELECT k.id, k.original_title, k.original_content,
                   k.suggested_title, k.suggested_summary, k.suggested_tags, k.suggested_category,
                   k.final_title, k.final_summary, k.final_tags, k.final_category,
                   k.status, k.created_at, k.updated_at
            FROM knowledge_items k
            WHERE k.status = 'confirmed' AND (
                k.final_title ILIKE '%' || :keyword || '%' OR
                k.final_summary ILIKE '%' || :keyword || '%' OR
                k.original_title ILIKE '%' || :keyword || '%' OR
                k.original_content ILIKE '%' || :keyword || '%' OR
                EXISTS (
                    SELECT 1 FROM unnest(COALESCE(k.final_tags, ARRAY[]::text[])) AS tag
                    WHERE tag ILIKE '%' || :keyword || '%'
                )
            )
            ORDER BY k.created_at DESC
            """, nativeQuery = true)
    List<KnowledgeItem> searchByKeyword(@Param("keyword") String keyword);
}
