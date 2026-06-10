package com.exam.ideaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** POST /api/categories 请求体 */
@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "类别 code 不能为空")
    @Size(max = 20, message = "类别 code 最长 20 字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "类别 code 仅允许大写字母、数字和下划线")
    private String code;

    @NotBlank(message = "类别名称不能为空")
    @Size(max = 50, message = "类别名称最长 50 字符")
    private String label;

    private Integer sortOrder;
}
