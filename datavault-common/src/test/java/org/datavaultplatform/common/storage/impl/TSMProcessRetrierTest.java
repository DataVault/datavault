package org.datavaultplatform.common.storage.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.datavaultplatform.common.util.ProcessInfo;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TSMProcessRetrierTest {

    static final String[] COMMANDS = {"cmd", "arg1", "arg2"};

    @Mock
    TSMProcessRetrier.ProcessInfoFactory mProcessInfoFactory;

    @Test
    void testContinuousFailureViaProcessInfoFailure() throws Exception {

        ProcessInfo processInfo1 = new ProcessInfo("test1", 2112L, 111, List.of("message1", "message2"), Duration.ofMillis(500));
        ProcessInfo processInfo2 = new ProcessInfo("test2", 2112L, 222, List.of("message1", "message2"), Duration.ofMillis(500));
        ProcessInfo processInfo3 = new ProcessInfo("test3", 2112L, 333, List.of("message1", "message2"), Duration.ofMillis(500));

        lenient().doReturn(processInfo1, processInfo2, processInfo3).when(mProcessInfoFactory).createProcessInfo(anyString(), any(String[].class));

        List<ILoggingEvent> events = TestUtils.captureLogging(TSMProcessRetrier.class, () -> {
            TSMProcessRetrier retrier = new TSMProcessRetrier("test-desc", 3, 1, mProcessInfoFactory, COMMANDS);
            ProcessInfo result = retrier.execute();
            assertThat(result).isEqualTo(processInfo3);
        });
        String messages = events.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
        String expectedMessages = """                
                Executing TSM [test-desc] (Attempt 1)
                Attempt [1/3] failed for [test-desc]. Reason: Retry trigger: Exit code [111]. Will retry?[true]
                Executing TSM [test-desc] (Attempt 2)
                Attempt [2/3] failed for [test-desc]. Reason: Retry trigger: Exit code [222]. Will retry?[true]
                Executing TSM [test-desc] (Attempt 3)
                Attempt [3/3] failed for [test-desc]. Reason: Retry trigger: Exit code [333]. Will retry?[false]
                Retrying Process for [test-desc] has ended after [3] attempt(s).
                All [3] attempt(s) exhausted for [test-desc]. Final error: Retry trigger: Exit code [333]""";
        assertThat(messages).isEqualTo(expectedMessages);

        verify(mProcessInfoFactory, times(3)).createProcessInfo(anyString(), any(String[].class));
        verifyNoMoreInteractions(mProcessInfoFactory);
    }

    @Nested
    class ProcessInfoFactoryThrowsSomething {

        @Test
        void testExceptionThatIsNotTimeoutException1DoesNotRetry() throws Exception {
            checkExceptionThatIsNotTimeoutExceptionDoesNotRetry(new CustomException("oops1a"));
        }

        @Test
        void testExceptionThatIsNotTimeoutException2DoesNotRetry() throws Exception {
            checkExceptionThatIsNotTimeoutExceptionDoesNotRetry(new InterruptedException("oops1b"));
        }

        @Test
        void testErrorThatIsNotTimeoutExceptionDoesNotRetry() throws Exception {
            checkExceptionThatIsNotTimeoutExceptionDoesNotRetry(new Error("oops2"));
        }

        void checkExceptionThatIsNotTimeoutExceptionDoesNotRetry(Throwable th) throws Exception {
            checkExceptionThatIsNotTimeoutExceptionDoesNotRetry(th, th.getClass(), th.getMessage());
        }

        void checkExceptionThatIsNotTimeoutExceptionDoesNotRetry(Throwable th, Class<? extends Throwable> expectedThrowableClass, String expectedMessage) throws Exception {
            doThrow(th).when(mProcessInfoFactory).createProcessInfo(anyString(), any(String[].class));

            List<ILoggingEvent> events = TestUtils.captureLogging(TSMProcessRetrier.class, () -> {
                TSMProcessRetrier retrier = new TSMProcessRetrier("test-desc", 3, 1, mProcessInfoFactory, COMMANDS);
                Throwable finalException = assertThrows(expectedThrowableClass, retrier::execute);
                assertThat(finalException).hasMessage(expectedMessage);
            });
            String messages = events.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
            String expectedMessages =
                    "Executing TSM [test-desc] (Attempt 1)\n" +
                            "Attempt [1/3] failed for [test-desc]. Reason: %s/%s. Will retry?[false]%n".formatted(th.getClass().getName(), th.getMessage()) +
                            "Retrying Process for [test-desc] has ended after [1] attempt(s).\n" +
                            "All [1] attempt(s) exhausted for [test-desc]. Final error: %s/%s".formatted(th.getClass().getName(), th.getMessage());
            assertThat(messages).isEqualTo(expectedMessages);

            verify(mProcessInfoFactory, times(1)).createProcessInfo(anyString(), any(String[].class));
            verifyNoMoreInteractions(mProcessInfoFactory);

        }
    }

    @Test
    void testContinuousFailureViaTimeoutException() throws Exception {
        Exception ex1 = new TimeoutException("problem1");
        Exception ex2 = new TimeoutException("problem2");
        Exception ex3 = new TimeoutException("problem3");

        lenient().doThrow(ex1, ex2, ex3).when(mProcessInfoFactory).createProcessInfo(anyString(), any(String[].class));

        List<ILoggingEvent> events = TestUtils.captureLogging(TSMProcessRetrier.class, () -> {
            TSMProcessRetrier retrier = new TSMProcessRetrier("test-desc", 3, 1, mProcessInfoFactory, COMMANDS);
            TimeoutException finalException = assertThrows(TimeoutException.class, () -> {
                retrier.execute();
            });
            assertThat(finalException).hasMessage("problem3");
        });
        String messages = events.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
        String expectedMessages = """                
                Executing TSM [test-desc] (Attempt 1)
                Attempt [1/3] failed for [test-desc]. Reason: java.util.concurrent.TimeoutException/problem1. Will retry?[true]
                Executing TSM [test-desc] (Attempt 2)
                Attempt [2/3] failed for [test-desc]. Reason: java.util.concurrent.TimeoutException/problem2. Will retry?[true]
                Executing TSM [test-desc] (Attempt 3)
                Attempt [3/3] failed for [test-desc]. Reason: java.util.concurrent.TimeoutException/problem3. Will retry?[false]
                Retrying Process for [test-desc] has ended after [3] attempt(s).
                All [3] attempt(s) exhausted for [test-desc]. Final error: java.util.concurrent.TimeoutException/problem3""";
        assertThat(messages).isEqualTo(expectedMessages);

        verify(mProcessInfoFactory, times(3)).createProcessInfo(anyString(), any(String[].class));
        verifyNoMoreInteractions(mProcessInfoFactory);
    }
    
    @Test
    void testProcessInfoFactoryReturnsNull() throws Exception {
        when(mProcessInfoFactory.createProcessInfo(anyString(), any(String[].class))).thenReturn(null);

        List<ILoggingEvent> events = TestUtils.captureLogging(TSMProcessRetrier.class, () -> {
            TSMProcessRetrier retrier = new TSMProcessRetrier("test-desc", 3, 1, mProcessInfoFactory, COMMANDS);
            ProcessInfo result = retrier.execute();
            assertThat(result).isNull();
        });
        String messages = events.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
        String expectedMessages = """                
                Executing TSM [test-desc] (Attempt 1)
                Attempt [1/3] was successful for [test-desc]. non-ProcessInfo result[null]""";
        assertThat(messages).isEqualTo(expectedMessages);
    }

    /**
     * @param exitCode - 0 and 4 are treated as success via TivoliStorageManager.TsmExitCode::isFailure
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void test2ndAttemptOf3Works(int exitCode) throws Exception {

        assertThat(TivoliStorageManager.TsmExitCode.of(exitCode).hasFailed()).isFalse();

        ProcessInfo processInfo1 = new ProcessInfo("test1", 2112L, 111, List.of("message1", "message2"), Duration.ofMillis(500));
        ProcessInfo processInfo2 = new ProcessInfo("test2", 2112L, exitCode, List.of("message1", "message2"), Duration.ofMillis(500));

        lenient().doReturn(processInfo1, processInfo2).when(mProcessInfoFactory).createProcessInfo(anyString(), any(String[].class));

        List<ILoggingEvent> events = TestUtils.captureLogging(TSMProcessRetrier.class, () -> {
            TSMProcessRetrier retrier = new TSMProcessRetrier("test-desc", 3, 1, mProcessInfoFactory, TivoliStorageManager.TsmExitCode::isFailure, COMMANDS);
            ProcessInfo result = retrier.execute();
            assertThat(result).isEqualTo(processInfo2);
        });
        String messages = events.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
        String expectedMessages = """                
                Executing TSM [test-desc] (Attempt 1)
                Attempt [1/3] failed for [test-desc]. Reason: Retry trigger: Exit code [111]. Will retry?[true]
                Executing TSM [test-desc] (Attempt 2)
                Attempt [2/3] was successful for [test-desc]. Exit code [""" + exitCode + "].";
        assertThat(messages).isEqualTo(expectedMessages);

        verify(mProcessInfoFactory, times(2)).createProcessInfo(anyString(), any(String[].class));
        verifyNoMoreInteractions(mProcessInfoFactory);
    }

    static class CustomException extends Exception {
        public CustomException(String msg) {
            super(msg);
        }
    }

    @Nested
    class ArgumentValidation {

        @Test
        void testArgMaxAttemptsCannotBeLessThan1() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("test-desc", 0, 1, mProcessInfoFactory, COMMANDS);
            });
            assertThat(ex.getMessage()).isEqualTo("The maxRetries cannot be less than 1");
        }

        @Test
        void testRetryTimeSecondsCannotBeLessThanZero() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("test-desc", 1, -1, mProcessInfoFactory, COMMANDS);
            });
            assertThat(ex.getMessage()).isEqualTo("The retryTimeSeconds cannot be less than 0");
        }

        @Test
        void testDescriptionCannotBeBlank() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("", 1, 0, mProcessInfoFactory, COMMANDS);
            });
            assertThat(ex.getMessage()).isEqualTo("The description cannot be blank");
        }

        @Test
        void testCommandsCannotBeNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("test-desc", 1, 0, mProcessInfoFactory, null);
            });
            assertThat(ex.getMessage()).isEqualTo("At least one command must be provided");
        }

        @Test
        void testCommandsCannotBeEmpty() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("test-desc", 1, 0, mProcessInfoFactory);
            });
            assertThat(ex.getMessage()).isEqualTo("At least one command must be provided");
        }

        @Test
        void testProcessInfoFactoryCanotBeNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("test-desc", 1, 0, null, COMMANDS);
            });
            assertThat(ex.getMessage()).isEqualTo("The processInfoFactory cannot be null");
        }

        @Test
        void testProcessInfoExitStatusSupportCannotBeNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                new TSMProcessRetrier("test-desc", 1, 0, mProcessInfoFactory, null, COMMANDS);
            });
            assertThat(ex.getMessage()).isEqualTo("The processInfoExitStatusSupport cannot be null");
        }
    }
}