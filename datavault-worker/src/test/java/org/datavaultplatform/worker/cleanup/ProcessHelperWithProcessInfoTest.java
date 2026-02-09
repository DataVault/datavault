package org.datavaultplatform.worker.cleanup;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.common.task.TaskConfig;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.ProcessExitCodes;
import org.datavaultplatform.common.util.ProcessHelper;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests ProcessHelper/ProcessInfo on its own and with TaskExecutor 
 * with non-zero exit codes, task-time-outs, and TaskExecutor timeouts
 */
class ProcessHelperWithProcessInfoTest {
    
    public static final String TIMEOUT_SIGTERM_REGEX = "OS process desc\\[(.*?)]pid\\[(\\d+)]TimedOutAfter\\[(PT\\d+S)]forcedToShutdown\\[false]";
    public static final String TIMEOUT_SIGKILL_REGEX = "OS process desc\\[(.*?)]pid\\[(\\d+)]TimedOutAfter\\[(PT\\d+S)]forcedToShutdown\\[true]";

    public static final String SCRIPT_PATH = "./target/test-classes/testScripts/processHelperTestUnixScript.sh";

    static final Duration DEFAULT_MAX_TASK_DURATION = Duration.ofSeconds(1);

    private ProcessHelper.ProcessInfo getProcessInfo(String label, long delayMillis, int exitCode, Duration processMaxDuration, boolean ignoreSigterm) throws Exception {
        String ignoreSigtermStr = ignoreSigterm ? "yes" : "no";
        var helper = new ProcessHelper("processHelperTestUnixScript.sh", processMaxDuration,
                TivoliStorageManager.cleanTsmCommand(SCRIPT_PATH, label, String.valueOf(delayMillis), String.valueOf(exitCode), ignoreSigtermStr));
        ProcessHelper.ProcessInfo result = helper.execute();
        if (!result.wasSuccess()) {
            int actualExitCode = result.getExitValue();
            String actualExitCodeStr = ProcessExitCodes.getExitCodeString(actualExitCode);
            String expectedExitCodeStr = ProcessExitCodes.getExitCodeString(exitCode);
            throw new ProcessException("label[%s]delayMs[%d]expectedExitCode[%s]actualExitCode[%s]".formatted(label, delayMillis, expectedExitCodeStr, actualExitCodeStr), actualExitCode);
        }
        return result;
    }


    @Test
    void testUserDir() {
        assertThat(System.getProperty("user.dir")).isEqualTo("/Users/davidhay/UOFE3/DV/datavault/datavault-worker");
    }

    Callable<ProcessHelper.ProcessInfo> getProcessCallable(String label, long delayMs, int exitCode, boolean ignoreSigTerm) {
        return () -> getProcessInfo(label, delayMs, exitCode, DEFAULT_MAX_TASK_DURATION, ignoreSigTerm);
    }

    Callable<ProcessHelper.ProcessInfo> getProcessCallable(String label, long delayMs, int exitCode, boolean ignoreSigTerm, Duration processMaxDuration) {
        return () -> getProcessInfo(label, delayMs, exitCode, processMaxDuration, ignoreSigTerm);
    }

    
    static class ProcessException extends RuntimeException {
        @Getter
        private final int exitCode;

        public ProcessException(String msg, int exitCode) {
            super(msg);
            this.exitCode = exitCode;
        }
    }

    @Nested
    class SingleTests {

        @SuppressWarnings("CodeBlock2Expr")
        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class NonExecutor {

            @Test
            @SneakyThrows
            @Order(1)
            void testSingleSuccess() {
                TestUtils.withLevel(ProcessHelper.class, Level.DEBUG, () -> {
                    ProcessHelper.ProcessInfo info = getProcessInfo("test1", 100, 0, DEFAULT_MAX_TASK_DURATION, false);
                    assertThat(info.wasSuccess()).isTrue();
                });
            }

            @Test
            @SneakyThrows
            @Order(2)
            void testExitCodeFailure() {
                ProcessException pe = assertThrows(ProcessException.class, () -> {
                    getProcessInfo("test2", 100, 123, DEFAULT_MAX_TASK_DURATION, false);
                });
                assertThat(pe.getMessage()).isEqualTo("label[test2]delayMs[100]expectedExitCode[123]actualExitCode[123]");
                assertThat(pe.getExitCode()).isEqualTo(123);
            }

            @Test
            @SneakyThrows
            @Order(3)
            void testTimeoutFailureWithSigTerm() {
                TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                    getProcessInfo("test3", 2000, 123, DEFAULT_MAX_TASK_DURATION, false);
                });
                assertThat(timeout.getMessage()).matches(TIMEOUT_SIGTERM_REGEX);
            }

            /**
             * This is a SLOW test - takes 30+ seconds
             */
            @Test
            @SneakyThrows
            @Order(4)
            void testTimeoutFailureWithSigKill() {
                long startNanos = System.nanoTime();
                TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                    getProcessInfo("test4", 120_000, 123, Duration.ofSeconds(10), true);
                });
                assertThat(timeout.getMessage()).matches(TIMEOUT_SIGKILL_REGEX);
                Duration diffDuration = Duration.ofNanos(System.nanoTime() - startNanos);
                assertThat(diffDuration).isGreaterThanOrEqualTo(TaskConfig.INSTANCE.getProcessSigTermTimeoutDuration());
            }
        }

        @SuppressWarnings("CodeBlock2Expr")
        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class Executor {

            @Test
            @Order(1)
            @SneakyThrows
            void testSingleSuccessWithExecutor() {
                List<ProcessHelper.ProcessInfo> results = new ArrayList<>();
                Callable<ProcessHelper.ProcessInfo> callable1 = getProcessCallable("test1", 100, 0, false);
                TaskExecutor<ProcessHelper.ProcessInfo> executor = new TaskExecutor<>(1, "test");
                executor.add(callable1);
                executor.execute(results::add);
                assertThat(results.get(0).wasSuccess()).isTrue();
            }

            @Test
            @Order(2)
            @SneakyThrows
            void testExitCodeFailureWithExecutor() {
                List<ProcessHelper.ProcessInfo> results = new ArrayList<>();
                Callable<ProcessHelper.ProcessInfo> callable1 = getProcessCallable("test2", 100, 123, false);
                TaskExecutor<ProcessHelper.ProcessInfo> executor = new TaskExecutor<>(1, "test");
                executor.add(callable1);
                ProcessException pe = assertThrows(ProcessException.class, () -> {
                    executor.execute(results::add);
                });
                assertThat(pe.getMessage()).isEqualTo("label[test2]delayMs[100]expectedExitCode[123]actualExitCode[123]");
                assertThat(pe.getExitCode()).isEqualTo(123);
                assertThat(results).isEmpty();
            }

            @Test
            @Order(3)
            @SneakyThrows
            void testProcessTimeoutWithSigTermFailureWithExecutor() {
                List<ProcessHelper.ProcessInfo> results = new ArrayList<>();
                Callable<ProcessHelper.ProcessInfo> callable1 = getProcessCallable("test3", 40_000, 123, false);
                TaskExecutor<ProcessHelper.ProcessInfo> executor = new TaskExecutor<>(1, "test");
                executor.add(callable1);
                TimeoutException pe = assertThrows(TimeoutException.class, () -> {
                    executor.execute(results::add);
                });
                assertThat(pe.getMessage()).matches(TIMEOUT_SIGTERM_REGEX);
                assertThat(results).isEmpty();
            }
            
            @Test
            @Order(4)
            @SneakyThrows
            void testProcessTimeoutWithSigKillFailureWithExecutor() {
                List<ProcessHelper.ProcessInfo> results = new ArrayList<>();
                Callable<ProcessHelper.ProcessInfo> callable1 = getProcessCallable("test3", 120_000, 123, true, Duration.ofSeconds(10));
                TaskExecutor<ProcessHelper.ProcessInfo> executor = new TaskExecutor<>(1, "test");
                executor.add(callable1);
                TimeoutException pe = assertThrows(TimeoutException.class, () -> {
                    executor.execute(results::add);
                });
                assertThat(pe.getMessage()).matches(TIMEOUT_SIGKILL_REGEX);
                assertThat(results).isEmpty();
            }

            @Test
            @SneakyThrows
            @Order(5)
            /*
             * Note - when the Executor cancels each Future - there is no way for the Executor to get output from that Future.
             * However, the ProcessHelper class does log the output of the terminated Process.
             * We have captured the logging of the ProcessHelper to check that the Process that was running in the executor stopped as expected.
             */
            void testExecutorTimeoutFailureProcessStoppedWithSigTerm() {

                List<ILoggingEvent> loggingEvents = TestUtils.captureLogging(ProcessHelper.class, () -> {
                    List<ProcessHelper.ProcessInfo> results = new ArrayList<>();
                    Callable<ProcessHelper.ProcessInfo> callable1 = getProcessCallable("test3", 20_000, 123, false, Duration.ofSeconds(30));
                    TaskExecutor<ProcessHelper.ProcessInfo> executor = new TaskExecutor<>(1, "test");
                    executor.add(callable1);
                    TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                        // the executor timeout of 10 seconds happens before process-time of 20s (process timeout is 30s)
                        executor.execute(results::add, Duration.ofSeconds(10));
                    });
                    assertThat(timeout).hasMessage("The executor has timed out after [PT10S]");
                    assertThat(results).isEmpty();
                });
                assertThat(loggingEvents.stream().anyMatch(evt -> {
                    return evt.getLevel().equals(Level.WARN) &&
                    evt.getFormattedMessage().endsWith(" OS process desc[processHelperTestUnixScript.sh] exitCode[SIGTERM(143)] exception[java.lang.InterruptedException]");
                })).isTrue();
            }
            @Test
            @SneakyThrows
            @Order(6)
            /*
             * Note - when the Executor cancels each Future - there is no way for the Executor to get output from that Future.
             * However, the ProcessHelper class does log the output of the terminated Process.
             * We have captured the logging of the ProcessHelper to check that the Process that was running in the executor stopped as expected.
             */
            void testExecutorTimeoutFailureProcessStoppedWithSigKill() {

                List<ILoggingEvent> loggingEvents = TestUtils.captureLogging(ProcessHelper.class, () -> {
                    List<ProcessHelper.ProcessInfo> results = new ArrayList<>();
                    Callable<ProcessHelper.ProcessInfo> callable1 = getProcessCallable("test3", 120_000, 123, true, Duration.ofSeconds(10));
                    TaskExecutor<ProcessHelper.ProcessInfo> executor = new TaskExecutor<>(1, "test");
                    executor.add(callable1);
                    TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                        // the executor timeout of 10 seconds happens before process-time of 20s (process timeout is 30s)
                        executor.execute(results::add, Duration.ofSeconds(10));
                    });
                    assertThat(timeout).hasMessage("The executor has timed out after [PT10S]");
                    assertThat(results).isEmpty();
                });
                assertThat(loggingEvents.stream().anyMatch(evt -> {
                    return evt.getLevel().equals(Level.WARN) &&
                            evt.getFormattedMessage().endsWith(" OS process desc[processHelperTestUnixScript.sh] exitCode[SIGKILL(137)] exception[java.lang.InterruptedException]");
                })).isTrue();
            }
        }
    }
}
