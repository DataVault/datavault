package org.datavaultplatform.broker.queue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("CodeBlock2Expr")
class TaskSenderDurationPropertiesTest {

    static final Map<String, Duration> DURATION_MAP;

    static {
        DURATION_MAP = new HashMap<>();
        DURATION_MAP.put("", null);
        DURATION_MAP.put("PT1m", Duration.ofMinutes(1));
        DURATION_MAP.put("PT15s", Duration.ofSeconds(15));
    }

    /**
     * Creates a very simplified Spring Application Context so we can test conversion of property values into Duration and Boolean parameters
     * for the TaskSender bean.
     * @param additionalProperties the additional spring properties to add to the environment
     * @return an application context with a mock sender and a TaskSender configured with the given properties
     */
    private ConfigurableApplicationContext getApplicationContext(String... additionalProperties) {
        return new SpringApplicationBuilder()
                .logStartupInfo(false)
                .sources(MyConfig.class)
                .web(WebApplicationType.NONE)
                .properties(additionalProperties)
                .run();
    }


    @Test
    void testExecutorProperShutdownEnabled() {
        Map<String,Boolean> enabledMap = new HashMap<>();
        enabledMap.put("true", true);
        enabledMap.put("false", false);
        enabledMap.forEach((key, value) -> {
            checkTaskSenderProperties(taskSender -> {
                assertThat(taskSender.isWorkersExecutorProperShutdownEnabled()).isEqualTo(value);
            }, TaskSender.WORKERS_EXECUTOR_PROPER_SHUTDOWN_ENABLED + "=" + key);
        });
        
        //defaults to true
        checkTaskSenderProperties(taskSender -> {
            assertThat(taskSender.isWorkersExecutorProperShutdownEnabled()).isTrue();
        });
    }

    @Test
    void testProcessMaxDuration() {
        DURATION_MAP.forEach((key, value) -> {
            checkTaskSenderProperties(taskSender -> {
                assertThat(taskSender.getWorkersProcessMaxDuration()).isEqualTo(value);
            }, TaskSender.WORKERS_PROCESS_MAX_DURATION + "=" + key);
        });
    }

    @Test
    void testExecutorPreShutdownNowDuration() {
        DURATION_MAP.forEach((key, value) -> {
            checkTaskSenderProperties(taskSender -> {
                assertThat(taskSender.getWorkersExecutorPreShutdownNowDuration()).isEqualTo(value);
            }, TaskSender.WORKERS_EXECUTOR_PRE_SHUTDOWN_NOW_DURATION + "=" + key);
        });
    }

    @Test
    void testProcessSigtermTimeoutDuration() {
        DURATION_MAP.forEach((key, value) -> {
            checkTaskSenderProperties(taskSender -> {
                assertThat(taskSender.getWorkersProcessSigTermTimeoutDuration()).isEqualTo(value);
            }, TaskSender.WORKERS_PROCESS_SIGTERM_TIMEOUT_DURATION + "=" + key);
        });
    }

    @Test
    void testProcessPostSigkillTimeoutDuration() {
        DURATION_MAP.forEach((key, value) -> {
            checkTaskSenderProperties(taskSender -> {
                assertThat(taskSender.getWorkersProcessPostSigKillTimeoutDuration()).isEqualTo(value);
            }, TaskSender.WORKERS_PROCESS_POST_SIGKILL_TIMEOUT_DURATION + "=" + key);
        });
    }

    void checkTaskSenderProperties(Consumer<TaskSender> checker, String... additionalProperties) {
        List<String> properties = new ArrayList<>(Arrays.asList(additionalProperties));
        properties.add("spring.main.banner-mode=off");
        var context = getApplicationContext(properties.toArray(String[]::new));
        TaskSender taskSender = context.getBean(TaskSender.class);
        assertThat(taskSender.getSender()).isNotNull();
        checker.accept(taskSender);
        context.close();
    }

    @Configuration
    @Import(TaskSender.class)
    static class MyConfig {

        @Bean
        Sender sender() {
            return Mockito.mock(Sender.class);
        }
    }
}
