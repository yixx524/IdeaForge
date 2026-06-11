package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.entity.KnowledgeItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Entity 与 DTO 之间的转换工具（非 MyBatis Mapper）。
 * 仅供 service 包内使用，负责 KnowledgeItem ↔ IdeaResponse 及标签类型转换。
 */
final class IdeaMapper {

    private IdeaMapper() {
    }

    static IdeaResponse toResponse(KnowledgeItem item) {
        return IdeaResponse.builder()
                .id(item.getId())
                .originalTitle(item.getOriginalTitle())
                .originalContent(item.getOriginalContent())
                .finalTitle(item.getFinalTitle())
                .finalSummary(item.getFinalSummary())
                .finalTags(toTagList(item.getFinalTags()))
                .finalCategory(item.getFinalCategory())
                .finalContent(resolveFinalContent(item))
                .createdAt(item.getCreatedAt())
                .build();
    }

    static String resolveFinalContent(KnowledgeItem item) {
        if (item.getFinalContent() != null && !item.getFinalContent().isBlank()) {
            return item.getFinalContent();
        }
        return item.getOriginalContent();
    }

    static String[] toTagArray(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toArray(String[]::new);
    }

    static List<String> toTagList(String[] tags) {
        if (tags == null || tags.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(tags);
    }
}
