package com.exam.ideaforge.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ItemStatus 与数据库字符串的转换器。
 * 数据库 CHECK 约束要求小写（pending/confirmed），枚举在 Java 侧使用大写。
 */
@Converter
public class ItemStatusConverter implements AttributeConverter<ItemStatus, String> {

    @Override
    public String convertToDatabaseColumn(ItemStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public ItemStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ItemStatus.valueOf(dbData.toUpperCase());
    }
}
