package com.exam.ideaforge.exception;

/** 文档解析失败时抛出，由 GlobalExceptionHandler 返回 422 */
public class DocumentParseException extends RuntimeException {

    public DocumentParseException(String message) {
        super(message);
    }

    public DocumentParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
