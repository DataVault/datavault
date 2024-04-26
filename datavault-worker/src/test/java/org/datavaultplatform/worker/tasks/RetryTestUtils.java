package org.datavaultplatform.worker.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class RetryTestUtils {

    private static final String[] PACKAGES = {
            "org.datavaultplatform.worker.retry",
            "org.springframework.retry"
    };
    
    private static final Map<String, Level> DEFAULT_LEVELS;

    private static LoggerContext getLoggerContext(org.slf4j.Logger slf4jLogger) {
        LoggerContext ctx = ((Logger) slf4jLogger).getLoggerContext();
        return ctx;
    }

    static {
        DEFAULT_LEVELS = new HashMap<>();
        for (String pkg : PACKAGES) {
            DEFAULT_LEVELS.put(pkg, Level.ERROR);
        }
    }

    public static void setLoggingLevelTrace(org.slf4j.Logger slf4jLogger) {
        setLoggingLevel(slf4jLogger, Level.TRACE);
    }
    
    public static void setLoggingLevelInfo(org.slf4j.Logger slf4jLogger) {
        setLoggingLevel(slf4jLogger, Level.INFO);
    }
    
    public static void resetLoggingLevel(org.slf4j.Logger slf4jLogger) {
        LoggerContext ctx = getLoggerContext(slf4jLogger);
        for (Map.Entry<String, Level> pair : DEFAULT_LEVELS.entrySet()) {
            ctx.getLogger(pair.getKey()).setLevel(pair.getValue());
        }
    }

    private static void setLoggingLevel(org.slf4j.Logger slf4jLogger, Level level) {
        LoggerContext ctx = getLoggerContext(slf4jLogger);
        for (String pkg : PACKAGES) {
            ctx.getLogger(pkg).setLevel(level);
        }
    }

}
