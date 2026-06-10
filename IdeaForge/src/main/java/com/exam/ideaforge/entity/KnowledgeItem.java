package com.exam.ideaforge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 知识条目实体，映射 PostgreSQL 表 knowledge_items。
 * 保存用户原始输入、AI 建议字段与用户确认后的最终字段。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "knowledge_items")
public class KnowledgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- 用户原始输入 ---
    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "original_content", nullable = false)
    private String originalContent;

    // --- AI 建议字段（process 阶段生成，不落库前仅存在于前端） ---
    @Column(name = "suggested_title")
    private String suggestedTitle;

    @Column(name = "suggested_summary")
    private String suggestedSummary;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "suggested_tags", columnDefinition = "text[]")
    private String[] suggestedTags = new String[0];

    @Column(name = "suggested_category", length = 20)
    private String suggestedCategory;

    // --- 用户确认后的最终字段（搜索与展示使用这组数据） ---
    @Column(name = "final_title", nullable = false)
    private String finalTitle;

    @Column(name = "final_summary")
    private String finalSummary;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "final_tags", columnDefinition = "text[]")
    private String[] finalTags = new String[0];

    @Column(name = "final_category", nullable = false, length = 20)
    private String finalCategory;

    /** 数据库存小写值（pending/confirmed），由 ItemStatusConverter 转换 */
    @Convert(converter = ItemStatusConverter.class)
    @Column(name = "status", length = 20)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
