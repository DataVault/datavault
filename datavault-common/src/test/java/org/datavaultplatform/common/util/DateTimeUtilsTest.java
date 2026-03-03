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
    java.sql.Date firstOfMonthSQL;
    java.sql.Date lastOfMonthSQL;
    LocalDate firstOfMonthLocalDate;
    LocalDate lastOfMonthLocalDate;
    LocalDateTime firstOfMonthLocalDateTime;
    LocalDateTime lastOfMonthLocalDateTime;

    @BeforeEach
    void setup() {
        ZonedDateTime first = ZonedDateTime.of(
                LocalDateTime.of(2024, 4, 1, 12, 0, 0),
                ZoneOffset.UTC
        );
        firstOfMonth = new Date(first.toInstant().toEpochMilli());
        firstOfMonthSQL = new java.sql.Date(firstOfMonth.getTime());
        ZonedDateTime last = ZonedDateTime.of(
                LocalDateTime.of(2024, 4, 30, 12, 0, 0),
                ZoneOffset.UTC
        );
        lastOfMonth = new Date(last.toInstant().toEpochMilli());
        lastOfMonthSQL = new java.sql.Date(lastOfMonth.getTime());
        
        firstOfMonthLocalDate = LocalDate.of(2024,4,1);
        lastOfMonthLocalDate = LocalDate.of(2024,4,30);
        firstOfMonthLocalDateTime = LocalDateTime.of(firstOfMonthLocalDate, LocalTime.NOON);
        lastOfMonthLocalDateTime = LocalDateTime.of(lastOfMonthLocalDate, LocalTime.NOON);
    }

    @Test
    void testIsBeforeWithDates() {
        assertTrue(DateTimeUtils.isBefore(firstOfMonth, lastOfMonth));
        assertFalse(DateTimeUtils.isBefore(lastOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isBefore(firstOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isBefore(lastOfMonth, lastOfMonth));

        assertTrue(DateTimeUtils.isBefore(null, lastOfMonth));
        assertFalse(DateTimeUtils.isBefore((Date) null, (Date) null));
        assertFalse(DateTimeUtils.isBefore(lastOfMonth, null));
    }

    @Test
    void testIsBeforeWithLocalDates() {
        assertTrue(DateTimeUtils.isBefore(firstOfMonthLocalDate, lastOfMonthLocalDate));
        assertFalse(DateTimeUtils.isBefore(lastOfMonthLocalDate, firstOfMonthLocalDate));
        assertFalse(DateTimeUtils.isBefore(firstOfMonthLocalDate, firstOfMonthLocalDate));
        assertFalse(DateTimeUtils.isBefore(lastOfMonthLocalDate, lastOfMonthLocalDate));

        assertTrue(DateTimeUtils.isBefore(null, lastOfMonthLocalDate));
        assertFalse(DateTimeUtils.isBefore(null, (LocalDate) null));
        assertFalse(DateTimeUtils.isBefore(lastOfMonthLocalDate, null));
    }

    @Test
    void testIsAfterWithDates() {
        assertTrue(DateTimeUtils.isAfter(lastOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isAfter(firstOfMonth, lastOfMonth));
        assertFalse(DateTimeUtils.isAfter(firstOfMonth, firstOfMonth));
        assertFalse(DateTimeUtils.isAfter(lastOfMonth, lastOfMonth));

        assertFalse(DateTimeUtils.isAfter(firstOfMonth, null));
        assertFalse(DateTimeUtils.isAfter(null, (Date) null));
        assertTrue(DateTimeUtils.isAfter( null, lastOfMonth));
    }

    @Test
    void testIsAfterWithLocalDates() {
        assertTrue(DateTimeUtils.isAfter(lastOfMonthLocalDate, firstOfMonthLocalDate));
        assertFalse(DateTimeUtils.isAfter(firstOfMonthLocalDate, lastOfMonthLocalDate));
        assertFalse(DateTimeUtils.isAfter(firstOfMonthLocalDate, firstOfMonthLocalDate));
        assertFalse(DateTimeUtils.isAfter(lastOfMonthLocalDate, lastOfMonthLocalDate));

        assertFalse(DateTimeUtils.isAfter(firstOfMonthLocalDate, null));
        assertFalse(DateTimeUtils.isAfter(null, (LocalDate) null));
        assertTrue(DateTimeUtils.isAfter( null, lastOfMonthLocalDate));
    }

    @Test
    void testIsSameDay() {
        assertFalse(DateTimeUtils.isSameDay(lastOfMonth, firstOfMonth));
        assertTrue(DateTimeUtils.isSameDay(lastOfMonth, lastOfMonth));
        assertTrue(DateTimeUtils.isSameDay(firstOfMonth, firstOfMonth));
        assertTrue(DateTimeUtils.isSameDay(lastOfMonth, lastOfMonth));

        assertFalse(DateTimeUtils.isSameDay(firstOfMonth, null));
        assertTrue(DateTimeUtils.isSameDay((Date) null, null));
        assertFalse(DateTimeUtils.isSameDay(null, lastOfMonth));
    }

    @Test
    void testIsSameDayWithLocalDate() {
        assertFalse(DateTimeUtils.isSameDay(lastOfMonthLocalDate, firstOfMonthLocalDate));
        assertTrue(DateTimeUtils.isSameDay(lastOfMonthLocalDate, lastOfMonthLocalDate));
        assertTrue(DateTimeUtils.isSameDay(firstOfMonthLocalDate, firstOfMonthLocalDate));
        assertTrue(DateTimeUtils.isSameDay(lastOfMonthLocalDate, lastOfMonthLocalDate));

        assertFalse(DateTimeUtils.isSameDay(firstOfMonthLocalDate, null));
        assertTrue(DateTimeUtils.isSameDay((LocalDate) null, null));
        assertFalse(DateTimeUtils.isSameDay(null, lastOfMonthLocalDate));
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
    void testParseLocalDate() {
        LocalDate date = DateTimeUtils.parseLocalDate("2000-04-2");
        assertThat(date).isEqualTo(LocalDate.of(2000, 4, 2));
    }

    @Test
    @SneakyThrows
    void testFormatDate() {
        assertThat(DateTimeUtils.formatDate(firstOfMonth)).isEqualTo("2024-04-01");
        assertThat(DateTimeUtils.formatDate(lastOfMonth)).isEqualTo("2024-04-30");
        assertThat(DateTimeUtils.formatDate((Date) null)).isEmpty();
    }

    @Test
    @SneakyThrows
    void testFormatDateWithLocalDate() {
        assertThat(DateTimeUtils.formatDate(firstOfMonthLocalDate)).isEqualTo("2024-04-01");
        assertThat(DateTimeUtils.formatDate(lastOfMonthLocalDate)).isEqualTo("2024-04-30");
        assertThat(DateTimeUtils.formatDate((LocalDate) null)).isEmpty();
    }

    @Test
    @SneakyThrows
    void testFormatDateWithLocalDateTime() {
        assertThat(DateTimeUtils.formatDate(firstOfMonthLocalDateTime)).isEqualTo("2024-04-01");
        assertThat(DateTimeUtils.formatDate(lastOfMonthLocalDateTime)).isEqualTo("2024-04-30");
        assertThat(DateTimeUtils.formatDate((LocalDateTime) null)).isEmpty();
    }

    @Test
    @SneakyThrows
    void testFormatDateBasicISO() {
        assertThat(DateTimeUtils.formatDateBasicISO(null)).isEmpty();
        assertThat(DateTimeUtils.formatDateBasicISO(firstOfMonth)).isEqualTo("20240401");
        assertThat(DateTimeUtils.formatDateBasicISO(lastOfMonth)).isEqualTo("20240430");
    }
    
    @Test
    @SneakyThrows
    void testFormatLocalDateBasicISO() {
        assertThat(DateTimeUtils.formatLocalDateBasicISO(null)).isEmpty();
        assertThat(DateTimeUtils.formatLocalDateBasicISO(firstOfMonthLocalDate)).isEqualTo("20240401");
        assertThat(DateTimeUtils.formatLocalDateBasicISO(lastOfMonthLocalDate)).isEqualTo("20240430");
    }

    @Test
    @SneakyThrows
    void testFormatLocalDateTimeBasicISO() {
        assertThat(DateTimeUtils.formatLocalDateTimeBasicISO(null)).isEmpty();
        assertThat(DateTimeUtils.formatLocalDateTimeBasicISO(firstOfMonthLocalDateTime)).isEqualTo("20240401");
        assertThat(DateTimeUtils.formatLocalDateTimeBasicISO(lastOfMonthLocalDateTime)).isEqualTo("20240430");
    }
    
    @Test
    void testDateToLocalDate() {
        assertThat(DateTimeUtils.toLocalDate((Date) null)).isNull();
        assertThat(DateTimeUtils.toLocalDate(firstOfMonth)).isEqualTo(firstOfMonthLocalDate);
        assertThat(DateTimeUtils.toLocalDate(lastOfMonth)).isEqualTo(lastOfMonthLocalDate);

        assertThat(DateTimeUtils.toLocalDate((Date) null)).isNull();
        assertThat(DateTimeUtils.toLocalDate(firstOfMonthSQL)).isEqualTo(firstOfMonthLocalDate);
        assertThat(DateTimeUtils.toLocalDate(lastOfMonthSQL)).isEqualTo(lastOfMonthLocalDate);

    }
    @Test
    void testLocalDateTimeToLocalDate() {
        assertThat(DateTimeUtils.toLocalDate((LocalDateTime) null)).isNull();
        assertThat(DateTimeUtils.toLocalDate(firstOfMonthLocalDateTime)).isEqualTo(firstOfMonthLocalDate);
        assertThat(DateTimeUtils.toLocalDate(lastOfMonthLocalDateTime)).isEqualTo(lastOfMonthLocalDate);
    }

    @Test
    void testDateToLocalDateTime() {
        assertThat(DateTimeUtils.toLocalDateTime(null)).isNull();
        assertThat(DateTimeUtils.toLocalDateTime(firstOfMonth)).isEqualTo(firstOfMonthLocalDateTime);
        assertThat(DateTimeUtils.toLocalDateTime(lastOfMonth)).isEqualTo(lastOfMonthLocalDateTime);
        assertThat(DateTimeUtils.toLocalDateTime(firstOfMonthSQL)).isEqualTo(firstOfMonthLocalDateTime);
        assertThat(DateTimeUtils.toLocalDateTime(lastOfMonthSQL)).isEqualTo(lastOfMonthLocalDateTime);
    }

    @Test
    void testGetLocalDateAdjustedByMonths() {
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(null, -1)).isNull();
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(null, 0)).isNull();
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(null, 1)).isNull();

        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(firstOfMonthLocalDate, -1)).isEqualTo(firstOfMonthLocalDate.minusMonths(1));
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(firstOfMonthLocalDate, 0)).isEqualTo(firstOfMonthLocalDate);
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(firstOfMonthLocalDate, 1)).isEqualTo(firstOfMonthLocalDate.plusMonths(1));

        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(lastOfMonthLocalDate, -1)).isEqualTo(lastOfMonthLocalDate.minusMonths(1));
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(lastOfMonthLocalDate, 0)).isEqualTo(lastOfMonthLocalDate);
        assertThat(DateTimeUtils.getLocalDateAdjustedByMonths(lastOfMonthLocalDate, 1)).isEqualTo(lastOfMonthLocalDate.plusMonths(1));
    }

    @Test
    void testGetLocalDateAdjustedByYears() {
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(null, -1)).isNull();
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(null, 0)).isNull();
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(null, 1)).isNull();

        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(firstOfMonthLocalDate, -1)).isEqualTo(firstOfMonthLocalDate.minusYears(1));
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(firstOfMonthLocalDate, 0)).isEqualTo(firstOfMonthLocalDate);
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(firstOfMonthLocalDate, 1)).isEqualTo(firstOfMonthLocalDate.plusYears(1));

        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(lastOfMonthLocalDate, -1)).isEqualTo(lastOfMonthLocalDate.minusYears(1));
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(lastOfMonthLocalDate, 0)).isEqualTo(lastOfMonthLocalDate);
        assertThat(DateTimeUtils.getLocalDateAdjustedByYears(lastOfMonthLocalDate, 1)).isEqualTo(lastOfMonthLocalDate.plusYears(1));
    }
    
    @Test
    void testToLocalDateTimeAtNoon(){
        assertThat(DateTimeUtils.toLocalDateTimeAtNoon(null)).isNull();
        assertThat(DateTimeUtils.toLocalDateTimeAtNoon(firstOfMonthLocalDate)).isEqualTo(firstOfMonthLocalDateTime);
        assertThat(DateTimeUtils.toLocalDateTimeAtNoon(lastOfMonthLocalDate)).isEqualTo(lastOfMonthLocalDateTime);
    }

}