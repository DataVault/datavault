package org.datavaultplatform.common.util;

import org.springframework.util.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.function.BiPredicate;

public class  DateTimeUtils {

    private DateTimeUtils() {
    }
    public static final String ISO_DATE_BASIC_FORMAT = "yyyyMMdd";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static final String VERBOSE_DATE_TIME_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    // From John Pinto : 17-Jan-2024 14:17:48
    public static final String GLOBAL_DATETIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final Object GLOBAL_TIME_FORMAT = "HH:mm:ss";
    public static final Object DATE_FORMAT_DD_MMM_YYYY = "dd MMM yyyy";
    public static final Object DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy";

    private static final DateTimeFormatter LENIENT_ISO_LOCAL_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseLenient()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .toFormatter();
    
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        } else {
            DateFormat formatter = new SimpleDateFormat(ISO_DATE_FORMAT);
            return formatter.format(date);
        }
    }
    
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        // LocalDate has its own format method that takes a DateTimeFormatter
        return date.format(DateTimeFormatter.ISO_DATE);
    }
    
    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        // LocalDate has its own format method that takes a DateTimeFormatter
        return date.format(DateTimeFormatter.ISO_DATE);
    }
    
    public static Date parseDate(String value) throws ParseException {
        return new SimpleDateFormat(ISO_DATE_FORMAT).parse(value);
    }

    public static LocalDate parseLocalDate(String value) throws DateTimeParseException {
        return LocalDate.parse(value, LENIENT_ISO_LOCAL_DATE_FORMATTER);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        }
        if (date1 == null || date2 == null) {
            return false;
        }
        LocalDate ld1 = getLocalDate(date1);
        LocalDate ld2 = getLocalDate(date2);
        return ld1.equals(ld2);
    }

    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        if (date1 == null && date2 == null) {
            return true;
        }
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }

    public static boolean isBefore(Date date1, Date date2) {
        return compareDates(date1, date2, LocalDate::isBefore);
    }

    public static boolean isAfter(Date date1, Date date2) {
        return compareDates(date1, date2, LocalDate::isAfter);
    }

    private static boolean compareDates(Date date1, Date date2, BiPredicate<LocalDate, LocalDate> comparison) {
        if (date1 == null && date2 == null) {
            return false;
        }
        if (date1 == null) {
            return true;
        }
        if (date2 == null) {
            return false;
        }

        // 2. Convert to Modern API
        LocalDate ld1 = toLocalDate(date1);
        LocalDate ld2 = toLocalDate(date2);

        // 3. Apply the specific comparison logic
        return comparison.test(ld1, ld2);
    }

    public static boolean isBefore(LocalDate date1, LocalDate date2) {
        return compareLocalDates(date1, date2, LocalDate::isBefore);
    }

    public static boolean isAfter(LocalDate date1, LocalDate date2) {
        return compareLocalDates(date1, date2, LocalDate::isAfter);
    }

    private static boolean compareLocalDates(LocalDate date1, LocalDate date2, BiPredicate<LocalDate, LocalDate> comparison) {
        if (date1 == null && date2 == null) {
            return false;
        }
        if (date1 == null) {
            return true;
        }
        if (date2 == null) {
            return false;
        }
        return comparison.test(date1, date2);
    }

    private static LocalDate getLocalDate(Date date) {
        Assert.notNull(date, "The date cannot be null");
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static String formatDateBasicISO(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat formatter = new SimpleDateFormat(ISO_DATE_BASIC_FORMAT);
        return formatter.format(date);
    }
    
    public static String formatLocalDateBasicISO(LocalDate date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ISO_DATE_BASIC_FORMAT);
        return formatter.format(date);
    }
    
    public static String formatLocalDateTimeBasicISO(LocalDateTime date) {
        if (date == null) {
            return "";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ISO_DATE_BASIC_FORMAT);
            return formatter.format(date);
        }
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }

        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        
        return date.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate toLocalDate(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate();
    }

    public static LocalDateTime toLocalDateTimeAtMidnight(Date date) {
        return getLocalDateTime(date, LocalTime.MIDNIGHT);
    }

    @SuppressWarnings("SameParameterValue")
    private static LocalDateTime getLocalDateTime(Date date, LocalTime localTime) {
        Assert.notNull(localTime, "The localTime cannot be null");
        if (date == null) {
            return null;
        }

        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atTime(localTime);
        }

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate().atTime(localTime);
    }

    public static LocalDate getLocalDateAdjustedByMonths(LocalDate initialDate, int months) {
        if (initialDate == null) {
            return null;
        }
        return initialDate.plusMonths(months);
    }

    public static LocalDate getLocalDateAdjustedByYears(LocalDate initialDate, int years) {
        if (initialDate == null) {
            return null;
        }
        return initialDate.plusYears(years);
    }

    public static LocalDateTime toLocalDateTimeAtMidnight(LocalDate localDate) {
        return toLocalDateTime(localDate, LocalTime.MIDNIGHT);
    }

    private static LocalDateTime toLocalDateTime(LocalDate localDate, LocalTime localTime) {
        Assert.notNull(localTime, "The localTime cannot be null");
        if (localDate == null) {
            return null;
        }
        return localDate.atTime(localTime);
    }
}
