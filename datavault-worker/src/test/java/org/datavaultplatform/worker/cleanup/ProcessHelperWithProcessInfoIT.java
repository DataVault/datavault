package org.datavaultplatform.worker.cleanup;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.common.task.TaskConfigTL;
import org.datavaultplatform.common.task.TaskExecutor;
import org.datavaultplatform.common.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests ProcessHelper/ProcessInfo on its own and with TaskExecutor 
 * with non-zero exit codes, task-time-outs, and TaskExecutor timeouts.
 * This test does not run on Windows because the way windows process shutdown works is different from Mac/Linux.
 */
@DisabledOnOs(OS.WINDOWS)
public class ProcessHelperWithProcessInfoIT {
    
    public static final Logger LOG = LoggerFactory.getLogger(ProcessHelperWithProcessInfoIT.class);
    
    public static final String TIMEOUT_SIGTERM_REGEX = "OS process desc\\[(.*?)]pid\\[(\\d+)]TimedOutAfter\\[(PT\\d+S)]forcedToShutdown\\[false]";
    public static final String TIMEOUT_SIGKILL_REGEX = "OS process desc\\[(.*?)]pid\\[(\\d+)]TimedOutAfter\\[(PT\\d+S)]forcedToShutdown\\[true]";

    public static final String SCRIPT_PATH = "./target/test-classes/testScripts/processHelperTestUnixScript.sh";

    static final Duration DEFAULT_MAX_TASK_DURATION = Duration.ofSeconds(1);

    private ProcessInfo getProcessInfo(String label, long delayMillis, int exitCode, Duration processMaxDuration, boolean ignoreSigterm) throws Exception {
        String ignoreSigtermStr = ignoreSigterm ? "yes" : "no";
        List<String> commands = List.of(SCRIPT_PATH, label, String.valueOf(delayMillis), String.valueOf(exitCode), ignoreSigtermStr);
        List<String> modifiedCommands;
        if (ignoreSigterm) {
            // workaround: don't add 'script/stdbuf line-buffering options' : we cannot use them when we want to ignore sigterm
            modifiedCommands = commands;
        } else {
            modifiedCommands = TivoliStorageManager.addLineBufferingPrefix(commands);
            LOG.warn("XXX modified commands {}", modifiedCommands);
        }
        var desc = "processHelperTestUnixScript.sh[%s]".formatted(label);
        var helper = new ProcessHelper(desc, processMaxDuration, modifiedCommands.toArray(String[]::new));
        ProcessInfo result = helper.execute();
        if (!result.wasSuccess()) {
            int actualExitCode = result.exitValue();
            String actualExitCodeStr = ProcessExitCodes.getExitCodeString(actualExitCode);
            String expectedExitCodeStr = ProcessExitCodes.getExitCodeString(exitCode);
            throw new ProcessException("label[%s]delayMs[%d]expectedExitCode[%s]actualExitCode[%s]".formatted(label, delayMillis, expectedExitCodeStr, actualExitCodeStr), actualExitCode);
        }
        return result;
    }


    @Test
    void testUserDir() {
        assertThat(System.getProperty("user.dir")).endsWith("/datavault-worker");
    }

    Callable<ProcessInfo> getProcessCallable(String label, long delayMs, int exitCode, boolean ignoreSigTerm) {
        return () -> getProcessInfo(label, delayMs, exitCode, DEFAULT_MAX_TASK_DURATION, ignoreSigTerm);
    }

    Callable<ProcessInfo> getProcessCallable(String label, long delayMs, int exitCode, boolean ignoreSigTerm, Duration processMaxDuration) {
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
                    ProcessInfo info = getProcessInfo("non-exec-single-test", 100, 0, DEFAULT_MAX_TASK_DURATION, false);
                    assertThat(info.wasSuccess()).isTrue();
                });
            }

            @Test
            @SneakyThrows
            @Order(2)
            void testExitCodeFailure() {
                ProcessException pe = assertThrows(ProcessException.class, () -> {
                    getProcessInfo("non-exec-exitcode-test", 100, 123, DEFAULT_MAX_TASK_DURATION, false);
                });
                assertThat(pe.getMessage()).isEqualTo("label[non-exec-exitcode-test]delayMs[100]expectedExitCode[123]actualExitCode[123]");
                assertThat(pe.getExitCode()).isEqualTo(123);
            }

            @Test
            @SneakyThrows
            @Order(3)
            void testTimeoutFailureWithSigTerm() {
                TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                    getProcessInfo("non-exec-exit-via-sigterm", 2000, 999, DEFAULT_MAX_TASK_DURATION, false);
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
                    getProcessInfo("non-exec-exit-via-sigkill", 120_000, 999, Duration.ofSeconds(10), true);
                });
                assertThat(timeout.getMessage()).matches(TIMEOUT_SIGKILL_REGEX);
                Duration diffDuration = Duration.ofNanos(System.nanoTime() - startNanos);
                assertThat(diffDuration).isGreaterThanOrEqualTo(TaskConfigTL.get().getProcessSigTermTimeoutDuration());
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
                List<ProcessInfo> results = new ArrayList<>();
                Callable<ProcessInfo> callable1 = getProcessCallable("executor-single-success", 100, 0, false);
                TaskExecutor<ProcessInfo> executor = new TaskExecutor<>(1, "test");
                executor.add(callable1);
                executor.execute(results::add);
                assertThat(results.get(0).wasSuccess()).isTrue();
            }

            @Test
            @Order(2)
            @SneakyThrows
            void testExitCodeFailureWithExecutor() {
                List<ProcessInfo> results = new ArrayList<>();
                Callable<ProcessInfo> callable1 = getProcessCallable("executor-exit-code-failure", 100, 123, false);
                TaskExecutor<ProcessInfo> executor = new TaskExecutor<>(1, "test");
                executor.add(callable1);
                ProcessException pe = assertThrows(ProcessException.class, () -> {
                    executor.execute(results::add);
                });
                assertThat(pe.getMessage()).isEqualTo("label[executor-exit-code-failure]delayMs[100]expectedExitCode[123]actualExitCode[123]");
                assertThat(pe.getExitCode()).isEqualTo(123);
                assertThat(results).isEmpty();
            }

            @Test
            @Order(3)
            @SneakyThrows
            void testProcessTimeoutWithSigTermFailureWithExecutor() {
                List<ProcessInfo> results = new ArrayList<>();
                Callable<ProcessInfo> callable1 = getProcessCallable("executor-timeout-exit-via-sigterm", 40_000, 123, false);
                TaskExecutor<ProcessInfo> executor = new TaskExecutor<>(1, "test");
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
                List<ProcessInfo> results = new ArrayList<>();
                Callable<ProcessInfo> callable1 = getProcessCallable("executor-timeout-exit-via-sigkill", 120_000, 123, true, Duration.ofSeconds(10));
                TaskExecutor<ProcessInfo> executor = new TaskExecutor<>(1, "test");
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

                String label = "executor-process-stopped-via-sigterm";
                List<ILoggingEvent> loggingEvents = TestUtils.captureLogging(ProcessHelper.class, () -> {
                    List<ProcessInfo> results = new ArrayList<>();
                    Callable<ProcessInfo> callable1 = getProcessCallable(label, 20_000, 123, false, Duration.ofSeconds(30));
                    TaskExecutor<ProcessInfo> executor = new TaskExecutor<>(1, "test");
                    executor.add(callable1);
                    TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                        // the executor timeout of 10 seconds happens before process-time of 20s (process timeout is 30s)
                        executor.execute(results::add, Duration.ofSeconds(10));
                    });
                    assertThat(timeout).hasMessage("The executor [test] has timed out after [PT10S]");
                    assertThat(results).isEmpty();
                });
                assertThat(loggingEvents.stream().anyMatch(evt -> {
                    return evt.getLevel().equals(Level.WARN) &&
                    evt.getFormattedMessage().endsWith(" OS process desc[processHelperTestUnixScript.sh[" + label + "]] exitCode[SIGTERM(143)] exception[java.lang.InterruptedException]");
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
                String label = "executor-process-stopped-via-sigkill";
                List<ILoggingEvent> loggingEvents = TestUtils.captureLogging(ProcessHelper.class, () -> {
                    List<ProcessInfo> results = new ArrayList<>();
                    Callable<ProcessInfo> callable1 = getProcessCallable(label, 120_000, 123, true, Duration.ofSeconds(10));
                    TaskExecutor<ProcessInfo> executor = new TaskExecutor<>(1, "test");
                    executor.add(callable1);
                    TimeoutException timeout = assertThrows(TimeoutException.class, () -> {
                        // the executor timeout of 10 seconds happens before process-time of 20s (process timeout is 30s)
                        executor.execute(results::add, Duration.ofSeconds(10));
                    });
                    assertThat(timeout).hasMessage("The executor [test] has timed out after [PT10S]");
                    assertThat(results).isEmpty();
                });
                assertThat(loggingEvents.stream().anyMatch(evt -> {
                    return evt.getLevel().equals(Level.WARN) &&
                            evt.getFormattedMessage().endsWith(" OS process desc[processHelperTestUnixScript.sh[" + label + "]] exitCode[SIGKILL(137)] exception[java.lang.InterruptedException]");
                })).isTrue();
            }
        }
    }
}
