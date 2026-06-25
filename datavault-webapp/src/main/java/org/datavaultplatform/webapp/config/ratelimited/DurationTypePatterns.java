package org.datavaultplatform.webapp.config.ratelimited;

import java.util.regex.Pattern;

public class DurationTypePatterns {
    
    // the number part is the first group, group(1) - group(0) is the enter matching string
    public static final Pattern REGEX_ISO8601_SECONDS = Pattern.compile("^PT([1-9]\\d*)S$");
    public static final Pattern REGEX_ISO8601_HOURS   = Pattern.compile("^PT([1-9]\\d*)H$");
    public static final Pattern REGEX_ISO8601_MINUTES = Pattern.compile("^PT([1-9]\\d*)M$");
    public static final Pattern REGEX_ISO8601_DAYS    = Pattern.compile("^P([1-9]\\d*)D$");

    // the number part is the first group, group(1) - group(0) is the enter matching string
    public static final Pattern REGEX_SECONDS = Pattern.compile("^([1-9]\\d*)(\\s*)s$");
    public static final Pattern REGEX_HOURS =   Pattern.compile("^([1-9]\\d*)(\\s*)h$");
    public static final Pattern REGEX_MINUTES = Pattern.compile("^([1-9]\\d*)(\\s*)m$");
    public static final Pattern REGEX_DAYS =    Pattern.compile("^([1-9]\\d*)(\\s*)d$");

    private DurationTypePatterns() {
    }
}
