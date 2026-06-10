package com.exam.ideaforge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** POST /api/ideas/process 请求体：用户原始输入 */
@Getter
@Setter
public class IdeaProcessRequest {

    private String originalTitle;

    @NotBlank(message = "正文不能为空")
    private String originalContent;
}
