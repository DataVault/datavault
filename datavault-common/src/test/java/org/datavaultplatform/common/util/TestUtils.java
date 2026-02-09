package org.datavaultplatform.common.util;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {
    
    public static <T extends Comparable<T>> List<T> sort(List<T> values) {
        List<T> result = new ArrayList<>(values);
        Collections.sort(result);
        return result;
    }
    
    public static void waitUntil(Callable<Boolean> callable) {
        waitUntil(Duration.ofMinutes(5), callable);
    }
    
    public static void waitUntil(Duration duration, Callable<Boolean> callable) {
        Awaitility.await().atMost(duration).until(callable);
    }
    public static void waitUntil(Duration duration, Duration pollInterval, Callable<Boolean> callable) {
        Awaitility.await().atMost(duration).pollInterval(pollInterval).until(callable);
    }

    public static void testExpectedCommands(InvocationOnMock invocation, String[] expectedCommands) {
        Object[] slice = Arrays.copyOfRange(invocation.getArguments(), 1, 1 + expectedCommands.length);
        assertThat(slice).hasSameSizeAs(expectedCommands);
        assertThat(slice)
                .as("verify full command array")
                .isEqualTo(expectedCommands);
    }

    @SneakyThrows
    public static void withLevel(Class<?> loggerClass, Level temporaryLevel, ThrowingRunnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerClass);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(temporaryLevel);
            action.run();
        } finally {
            // This block is guaranteed to run even if 'action' throws an Exception
            logger.setLevel(originalLevel);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static List<ILoggingEvent> captureLogging(Class<?> loggerClass, ThrowingRunnable throwingRunnable) throws Exception {
        Logger actualLogger = (Logger) LoggerFactory.getLogger(loggerClass);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        actualLogger.addAppender(listAppender);
        try {
            listAppender.start();
            actualLogger.addAppender(listAppender);
            throwingRunnable.run();
        } finally {
            listAppender.stop();
            actualLogger.detachAppender(listAppender);
        }
        return listAppender.list;
    }

}
