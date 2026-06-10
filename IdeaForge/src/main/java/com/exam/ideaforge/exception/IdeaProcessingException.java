package com.exam.ideaforge.exception;

/** AI 整理失败时抛出，由 GlobalExceptionHandler 返回 503 */
public class IdeaProcessingException extends RuntimeException {

    public IdeaProcessingException(String message) {
        super(message);
    }

    public IdeaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
