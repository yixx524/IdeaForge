package com.exam.ideaforge.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** PUT /api/categories/{id} 请求体（code 创建后不可修改） */
@Getter
@Setter
public class CategoryUpdateRequest {

    @Size(max = 50, message = "类别名称最长 50 字符")
    private String label;

    private Integer sortOrder;

    private Boolean enabled;
}
