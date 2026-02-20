package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.task.TaskConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

@SuppressWarnings("CodeBlock2Expr")
class TaskSenderSpringPropertiesTest {

    public static final Logger LOG = LoggerFactory.getLogger(TaskSenderSpringPropertiesTest.class);

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
        Map<String, Boolean> enabledMap = new HashMap<>();
        enabledMap.put("true", true);
        enabledMap.put("false", false);
        enabledMap.forEach((key, value) -> {
            checkTaskSenderProperties(taskSender -> {
                assertThat(taskSender.getWorkersExecutorProperShutdownEnabled()).isEqualTo(value);
            }, TaskSender.WORKERS_EXECUTOR_PROPER_SHUTDOWN_ENABLED + "=" + key);
        });

        //defaults to null
        checkTaskSenderProperties(taskSender -> {
            assertThat(taskSender.getWorkersExecutorProperShutdownEnabled()).isNull();
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

    @Test
    void testNoWorkerShutdownProperties() {

        Map<String, String> expected = new HashMap<>();
        expected.put(PropNames.EXECUTOR_PROPER_SHUTDOWN_ENABLED, null);
        expected.put(PropNames.EXECUTOR_PRE_SHUTDOWN_NOW_DURATION, null);
        expected.put(PropNames.PROCESS_MAX_DURATION, null);
        expected.put(PropNames.PROCESS_SIGTERM_TIMEOUT_DURATION, null);
        expected.put(PropNames.PROCESS_POST_SIGKILL_TIMEOUT_DURATION, null);

        checkWorkerShutdownPropertiesSet(expected);
    }

    @Test
    void testAllWorkerShutdownProperties() {

        Map<String, String> expected = new HashMap<>();
        expected.put(PropNames.EXECUTOR_PROPER_SHUTDOWN_ENABLED, "true");
        expected.put(PropNames.EXECUTOR_PRE_SHUTDOWN_NOW_DURATION, "PT24H"); // 1 day
        expected.put(PropNames.PROCESS_MAX_DURATION, "PT21H"); // 21 hours
        expected.put(PropNames.PROCESS_SIGTERM_TIMEOUT_DURATION, "PT11M"); // 11 mins
        expected.put(PropNames.PROCESS_POST_SIGKILL_TIMEOUT_DURATION, "PT5S"); // 5 sec

        checkWorkerShutdownPropertiesSet(expected,
                "workers.executor.proper.shutdown.enabled=true",
                "workers.executor.pre.shutdown.now.duration=P1D",
                "workers.process.max.duration=PT21H",
                "workers.process.sigterm.timeout.duration=PT11M",
                "workers.process.post.sigkill.timeout.duration=PT5S");
    }

    void checkWorkerShutdownPropertiesSet(Map<String, String> expectedProperties, String... additionalProperties) {
        checkTaskSenderProperties(taskSender -> {
            Sender sender = taskSender.getSender();

            assertThat(Mockito.mockingDetails(sender).isMock()).isTrue();

            List<String> sentMessages = new ArrayList<>();
            doAnswer(invocation -> {
                String message = invocation.getArgument(0);
                sentMessages.add(message);
                return "message-id";
            }).when(sender).send(anyString(), anyBoolean());

            try {
                Task task = new Task();
                task.setTaskClass(TaskConfig.class.getName());
                task.setProperties(Map.of("prop1","value1"));

                taskSender.send(task);

                assertThat(sentMessages).hasSize(1);
                String sentMessage = sentMessages.get(0);

                Task fromMessage = new ObjectMapper().readValue(sentMessage, Task.class);
                Map<String, String> props = fromMessage.getProperties();
                assertThat(props).containsEntry("prop1", "value1");
                assertThat(props).containsAllEntriesOf(expectedProperties);
                
                LOG.info("sending ... {}", new ObjectMapper().writeValueAsString(fromMessage.getProperties()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, additionalProperties);
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
