package org.datavaultplatform.webapp.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * This has been added to ensure that we convert date strings posted by forms into Dates correctly.
 * The expected format is 'yyyy-DD-mm'
 */
@Component
public class StringToDateConverter implements Converter<String, Date> {
    @Override
    public Date convert(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        } else {
            LocalDate localDate = LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
            Date result = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            return result;
        }
    }
}
