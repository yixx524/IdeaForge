package com.exam.ideaforge.exception;

/** 知识条目不存在时抛出，由 GlobalExceptionHandler 返回 404 */
public class IdeaNotFoundException extends RuntimeException {

    public IdeaNotFoundException(String message) {
        super(message);
    }
}
