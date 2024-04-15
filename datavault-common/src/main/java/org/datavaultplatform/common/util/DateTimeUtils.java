package org.datavaultplatform.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {

    public static final String ISO_DATE_BASIC_FORMAT = "yyyyMMdd";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String VERBOSE_DATE_TIME_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    // From John Pinto : 17-Jan-2024 14:17:48
    public static final String GLOBAL_DATETIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final Object GLOBAL_TIME_FORMAT = "HH:mm:ss";
    public static final Object DATE_FORMAT_DD_MMM_YYYY = "dd MMM yyyy";
    public static final Object DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy";

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        } else {
            DateFormat formatter = new SimpleDateFormat(ISO_DATE_FORMAT);
            return formatter.format(date);
        }
    }

    public static Date parseDate(String value) throws ParseException {
        return new SimpleDateFormat(ISO_DATE_FORMAT).parse(value);
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
    public static boolean isBeforeToday(Date date1) {
        return isBefore(date1, new Date());
    }

    public static boolean isBefore(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return false;
        }
        if (date1 == null) {
            return true;
        }
        if (date2 == null) {
            return false;
        }
        LocalDate ld1 = getLocalDate(date1);
        LocalDate ld2 = getLocalDate(date2);
        return ld1.isBefore(ld2);
    }
    
    public static boolean isAfter(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return false;
        }
        if (date1 == null) {
            return true;
        }
        if (date2 == null) {
            return false;
        }
        LocalDate ld1 = getLocalDate(date1);
        LocalDate ld2 = getLocalDate(date2);
        return ld1.isAfter(ld2);
    }

    private static LocalDate getLocalDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);//January is 0
        int year = cal.get(Calendar.YEAR);
        return LocalDate.of(year, month + 1, day);
    }

    public static String formatDateBasicISO(Date date) {
        DateFormat formatter = new SimpleDateFormat(ISO_DATE_BASIC_FORMAT);
        return formatter.format(date);
    }
}
