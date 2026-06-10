package com.exam.ideaforge.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/** 统一错误响应体，由 GlobalExceptionHandler 返回 */
@Getter
@Builder
public class ErrorResponse {

    private final String message;
    private final OffsetDateTime timestamp;
}
