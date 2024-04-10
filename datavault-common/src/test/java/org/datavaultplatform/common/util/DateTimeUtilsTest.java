package org.datavaultplatform.common.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {
    Date firstOfMonth;
    Date lastOfMonth;

    @BeforeEach
    void setup() {
        ZonedDateTime first = ZonedDateTime.of(
                LocalDateTime.of(2024, 4, 1, 12, 0, 0),
                ZoneOffset.UTC
        );
        firstOfMonth = new Date(first.toInstant().toEpochMilli());
        ZonedDateTime last = ZonedDateTime.of(
                LocalDateTime.of(2024, 4, 30, 12, 0, 0),
                ZoneOffset.UTC
        );
        lastOfMonth = new Date(last.toInstant().toEpochMilli());
    }

    @Test
    void testIsBefore() {
        assertTrue(DateTimeUtils.isBefore(firstOfMonth, lastOfMonth));
        assertFalse(DateTimeUtils.isBefore(lastOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isBefore(firstOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isBefore(lastOfMonth, lastOfMonth));
    }

    @Test
    void testIsAfter() {
        assertTrue(DateTimeUtils.isAfter(lastOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isAfter(firstOfMonth, lastOfMonth));
        assertFalse(DateTimeUtils.isAfter(firstOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isAfter(lastOfMonth, lastOfMonth));
    }

    @Test
    void testIsSameDay() {
        assertFalse(DateTimeUtils.isSameDay(lastOfMonth, firstOfMonth));
        assertTrue(DateTimeUtils.isSameDay(lastOfMonth, lastOfMonth));
        assertTrue(DateTimeUtils.isSameDay(firstOfMonth, firstOfMonth));
        assertTrue(DateTimeUtils.isSameDay(lastOfMonth, lastOfMonth));

    }
    
    @Test
    @SneakyThrows
    void testParseDate() {
       Date date = DateTimeUtils.parseDate("2000-04-2");
       LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
       assertThat(localDate).isEqualTo(LocalDate.of(2000, 4, 2));
    }

    @Test
    @SneakyThrows
    void testFormatDate() {
        assertThat(DateTimeUtils.formatDate(firstOfMonth)).isEqualTo("2024-04-01");
        assertThat(DateTimeUtils.formatDate(lastOfMonth)).isEqualTo("2024-04-30");
    }

    @Test
    @SneakyThrows
    void testFormatDateBasicISO() {
        assertThat(DateTimeUtils.formatDateBasicISO(firstOfMonth)).isEqualTo("20240401");
        assertThat(DateTimeUtils.formatDateBasicISO(lastOfMonth)).isEqualTo("20240430");
    }

}