package com.exam.ideaforge.exception;

/** 类别字典条目不存在 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
