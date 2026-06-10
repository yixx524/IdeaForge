package com.exam.ideaforge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** 知识条目 API 响应，用于保存回显、搜索列表与详情查询 */
@Getter
@Builder
public class IdeaResponse {

    private final UUID id;
    private final String originalTitle;
    private final String originalContent;
    private final String finalTitle;
    private final String finalSummary;
    private final List<String> finalTags;
    private final String finalCategory;
    private final OffsetDateTime createdAt;
}
