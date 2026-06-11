package com.exam.ideaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** PUT /api/ideas/{id} 请求体：更新用户确认后的最终字段；可选携带 AI 重新整理后的建议字段 */
@Getter
@Setter
public class IdeaUpdateRequest {

    @NotBlank(message = "最终标题不能为空")
    private String finalTitle;

    private String finalSummary;

    private List<String> finalTags;

    @NotNull(message = "最终类别不能为空")
    private String finalCategory;

    private String finalContent;

    private String suggestedTitle;
    private String suggestedSummary;
    private List<String> suggestedTags;
    private String suggestedCategory;
    private String suggestedContent;
}
