package org.datavaultplatform.common.task;

import org.datavaultplatform.common.PropNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TaskConfigTest {

    final Duration duration1 = Duration.ofMinutes(1);
    final Duration duration2 = Duration.ofMinutes(2);
    final Duration duration3 = Duration.ofMinutes(3);
    final Duration duration4 = Duration.ofMinutes(4);

    final TaskConfig config = new TaskConfig();

    @BeforeEach
    void setup() {
        config.reset();
    }

    @Test
    void testResetValues() {

        assertThat(config.getExecutorPreShutdownNowDuration()).isSameAs(TaskConfig.DEFAULT_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION);

        assertThat(config.getProcessMaxDuration()).isSameAs(TaskConfig.DEFAULT_PROCESS_MAX_DURATION);
        assertThat(config.getProcessSigTermTimeoutDuration()).isSameAs(TaskConfig.DEFAULT_PROCESS_SIGTERM_TIMEOUT_DURATION);
        assertThat(config.getProcessPostSigKillTimeoutDuration()).isSameAs(TaskConfig.DEFAULT_PROCESS_POST_SIGKILL_TIMEOUT_DURATION);
    }

    @Test
    void testSetThenGetThenReset() {
        config.setExecutorProperShutdownEnabled(false);
        config.setExecutorPreShutdownNowDuration(duration1);
        config.setProcessMaxDuration(duration2);
        config.setProcessSigTermTimeoutDuration(duration3);
        config.setProcessPostSigKillTimeoutDuration(duration4);

        assertThat(config.isExecutorProperShutdownEnabled()).isFalse();
        assertThat(config.getExecutorPreShutdownNowDuration()).isSameAs(duration1);
        assertThat(config.getProcessMaxDuration()).isSameAs(duration2);
        assertThat(config.getProcessSigTermTimeoutDuration()).isSameAs(duration3);
        assertThat(config.getProcessPostSigKillTimeoutDuration()).isSameAs(duration4);
        config.reset();
        testResetValues();
    }

    private static Stream<Arguments> durationTestArguments() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of("XXX", false),
                Arguments.of("PT0.000000001S", false),
                Arguments.of("PT0.002S", true),
                Arguments.of("PT3S", true),
                Arguments.of("PT4M", true),
                Arguments.of("PT5H", true)
        );
    }

    @ParameterizedTest
    @MethodSource("durationTestArguments")
    void testExecutorPreShutdownNowDuration(String durationString, boolean expectedValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PropNames.EXECUTOR_PRE_SHUTDOWN_NOW_DURATION, durationString);
        if (expectedValue) {
            config.populate(properties);
            assertThat(config.getExecutorPreShutdownNowDuration()).isEqualTo(Duration.parse(durationString));
        } else {
            assertThat(config.getExecutorPreShutdownNowDuration()).isEqualTo(TaskConfig.DEFAULT_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION);
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            null, false
            '', false
            XXX, false
            true, true
            false, true
            """, nullValues = "null")
    void testExecutorProperShutdownEnabled(String enabledString, boolean expectedValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PropNames.EXECUTOR_PROPER_SHUTDOWN_ENABLED, enabledString);
        if (expectedValue) {
            config.populate(properties);
            assertThat(config.isExecutorProperShutdownEnabled()).isEqualTo(Boolean.parseBoolean(enabledString));
        } else {
            assertThat(config.isExecutorProperShutdownEnabled()).isEqualTo(TaskConfig.DEFAULT_EXECUTOR_PROPER_SHUTDOWN_ENABLED);
        }
    }

    @ParameterizedTest
    @MethodSource("durationTestArguments")
    void testProcessMaxDuration(String durationString, boolean expectedValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PropNames.PROCESS_MAX_DURATION, durationString);
        if (expectedValue) {
            config.populate(properties);
            assertThat(config.getProcessMaxDuration()).isEqualTo(Duration.parse(durationString));
        } else {
            assertThat(config.getProcessMaxDuration()).isEqualTo(TaskConfig.DEFAULT_PROCESS_MAX_DURATION);
        }
    }

    @ParameterizedTest
    @MethodSource("durationTestArguments")
    void testProcessSigTermTimeoutDuration(String durationString, boolean expectedValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PropNames.PROCESS_SIGTERM_TIMEOUT_DURATION, durationString);
        if (expectedValue) {
            config.populate(properties);
            assertThat(config.getProcessSigTermTimeoutDuration()).isEqualTo(Duration.parse(durationString));
        } else {
            assertThat(config.getProcessSigTermTimeoutDuration()).isEqualTo(TaskConfig.DEFAULT_PROCESS_SIGTERM_TIMEOUT_DURATION);
        }
    }

    @ParameterizedTest
    @MethodSource("durationTestArguments")
    void testProcessPostSigKillTimeoutDuration(String durationString, boolean expectedValue) {
        Map<String, String> properties = new HashMap<>();
        properties.put(PropNames.PROCESS_POST_SIGKILL_TIMEOUT_DURATION, durationString);
        if (expectedValue) {
            config.populate(properties);
            assertThat(config.getProcessPostSigKillTimeoutDuration()).isEqualTo(Duration.parse(durationString));
        } else {
            assertThat(config.getProcessPostSigKillTimeoutDuration()).isEqualTo(TaskConfig.DEFAULT_PROCESS_POST_SIGKILL_TIMEOUT_DURATION);
        }
    }
    
    
    @ParameterizedTest
    @CsvSource(textBlock = """
            0,0,1
            1,0,2
            0,1,2
            1,2,4
            2,3,6
            """)
    void testGet(int sigTermMinutes, int sigKillMinutes, int expectedExecutorShutdownMinutes) {
        config.setProcessSigTermTimeoutDuration(Duration.ofMinutes(sigTermMinutes));
        config.setProcessPostSigKillTimeoutDuration(Duration.ofMinutes(sigKillMinutes));
        
        assertThat(config.getProcessSigTermTimeoutDuration().toMinutes()).isEqualTo(sigTermMinutes);
        assertThat(config.getProcessPostSigKillTimeoutDuration().toMinutes()).isEqualTo(sigKillMinutes);

        assertThat(config.getExecutorShutdownDuration().toMinutes()).isEqualTo(expectedExecutorShutdownMinutes);
    }
}