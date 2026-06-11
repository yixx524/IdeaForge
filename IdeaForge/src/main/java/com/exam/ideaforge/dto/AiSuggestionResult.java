package com.exam.ideaforge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** DeepSeek 结构化输出的反序列化目标，字段名须与 AI 返回 JSON 一致 */
public record AiSuggestionResult(
        @JsonProperty("suggestedTitle") String suggestedTitle,
        @JsonProperty("suggestedSummary") String suggestedSummary,
        @JsonProperty("suggestedTags") List<String> suggestedTags,
        @JsonProperty("suggestedCategory") String suggestedCategory,
        @JsonProperty("suggestedContent") String suggestedContent
) {
}
