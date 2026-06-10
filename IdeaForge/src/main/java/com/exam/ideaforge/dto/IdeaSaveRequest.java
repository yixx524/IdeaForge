package com.exam.ideaforge.dto;

import com.exam.ideaforge.entity.IdeaCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** POST /api/ideas 请求体：包含原始、AI 建议与用户最终确认的完整数据 */
@Getter
@Setter
public class IdeaSaveRequest {

    private String originalTitle;

    @NotBlank(message = "原始正文不能为空")
    private String originalContent;

    private String suggestedTitle;
    private String suggestedSummary;
    private List<String> suggestedTags;
    private IdeaCategory suggestedCategory;

    @NotBlank(message = "最终标题不能为空")
    private String finalTitle;

    private String finalSummary;

    private List<String> finalTags;

    @NotNull(message = "最终类别不能为空")
    private IdeaCategory finalCategory;
}
