package com.exam.ideaforge.exception;

/** 类别字典校验失败（如 code 重复、保存时使用了已停用类别） */
public class CategoryValidationException extends RuntimeException {

    public CategoryValidationException(String message) {
        super(message);
    }
}
