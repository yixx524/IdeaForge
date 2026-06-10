package com.exam.ideaforge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 类别字典 API 响应 */
@Getter
@Builder
public class CategoryResponse {

    private final UUID id;
    private final String code;
    private final String label;
    private final int sortOrder;
    private final boolean enabled;
    private final OffsetDateTime createdAt;
}
