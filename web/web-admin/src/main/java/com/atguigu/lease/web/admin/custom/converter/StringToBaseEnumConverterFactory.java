package com.atguigu.lease.web.admin.custom.converter;

import com.atguigu.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return code -> {
            T[] enumConstants = targetType.getEnumConstants();
            for(T enumConstant: enumConstants) {
                if(enumConstant.getCode().equals(Integer.valueOf(code))) {
                    return enumConstant;
                }
            }
            throw new RuntimeException("code: " + code + "非法");
        };
    }
}
