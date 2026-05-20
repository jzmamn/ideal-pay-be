package com.payroll.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanToZeroOneConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) return "0";
        return attribute ? "1" : "0";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) return false;
        return "1".equals(dbData.trim());
    }
}
