package com.exam.ideaforge.repository;

import com.exam.ideaforge.entity.KnowledgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, UUID> {

    /**
     * V1 关键词搜索：匹配最终标题、摘要、原始正文及标签数组。
     * 使用原生 SQL 以支持 PostgreSQL text[] 的 unnest 查询。
     */
    @Query(value = """
            SELECT * FROM knowledge_items k
            WHERE k.status = 'confirmed' AND (
                LOWER(k.final_title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(k.final_summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(k.original_content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                EXISTS (
                    SELECT 1 FROM unnest(k.final_tags) AS tag
                    WHERE LOWER(tag) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            )
            ORDER BY k.created_at DESC
            """, nativeQuery = true)
    List<KnowledgeItem> searchByKeyword(@Param("keyword") String keyword);
}
