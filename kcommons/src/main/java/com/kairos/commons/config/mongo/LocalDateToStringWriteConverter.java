package com.kairos.commons.config.mongo;

import com.kairos.commons.utils.DateUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.util.Date;

@WritingConverter
public class LocalDateToStringWriteConverter implements Converter<LocalDate,Date> {
    @Override
    public Date convert(LocalDate source) {
        return DateUtils.getDateFromLocalDate(source);
    }
}