package com.exam.ideaforge.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** AI 整理结果，返回前端供用户编辑确认 */
@Getter
@Builder
public class IdeaProcessResponse {

    private final String suggestedTitle;
    private final String suggestedSummary;
    private final List<String> suggestedTags;
    private final String suggestedCategory;
    private final String suggestedContent;
}
