package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSenderTest {

    private static final String TEST_MESSAGE_ID = "test-message-id";
    
    ObjectMapper mapper;
    
    @Mock
    Sender mSender;

    TaskSender taskSender;

    @Captor
    ArgumentCaptor<String> argMessage;

    @Captor
    ArgumentCaptor<Boolean> argRestart;

    Task task;

    Task taskWithoutProperties;

    @BeforeEach
    void setup() {

        mapper = new ObjectMapper();
        taskSender = new TaskSender(mSender, null, null, null, null, null);

        Event event = new Event();
        event.setJobId("jobId");
        event.setEventClass(Event.class.getName());
        event.setTimestamp(Date.from(Instant.parse("2026-02-20T14:52:00Z")));

        task = new Task();
        task.setTaskClass("<task-class>");
        task.setLastEvent(event);
        task.setIsRedeliver(false);
        task.setProperties(Map.of("P1", "V1", "P2", "V2"));

        taskWithoutProperties = new Task();
        taskWithoutProperties.setTaskClass("<task-class>");
        taskWithoutProperties.setLastEvent(event);
        taskWithoutProperties.setIsRedeliver(false);
        taskWithoutProperties.setProperties(null);
    }

    @Test
    @SneakyThrows
    void testSendSingleArg() {
        doReturn(TEST_MESSAGE_ID).when(mSender).send(anyString(), anyBoolean());

        String messageId = taskSender.send(task);

        assertThat(messageId).isEqualTo(TEST_MESSAGE_ID);

        verify(mSender).send(argMessage.capture(), eq(false));

        Task task = checkMessageHasAddedProperties(argMessage.getValue());
        assertThat(task.getProperties()).containsEntry("P1", "V1");
        assertThat(task.getProperties()).containsEntry("P2", "V2");

        verifyNoMoreInteractions(mSender);
    }

    @SneakyThrows
    Task checkMessageHasAddedProperties(String taskMessageJson) {
        Task task = mapper.readValue(taskMessageJson, Task.class);
        assertThat(task.getProperties()).containsKey(PropNames.EXECUTOR_PROPER_SHUTDOWN_ENABLED);
        assertThat(task.getProperties()).containsKey(PropNames.EXECUTOR_PRE_SHUTDOWN_NOW_DURATION);
        assertThat(task.getProperties()).containsKey(PropNames.PROCESS_MAX_DURATION);
        assertThat(task.getProperties()).containsKey(PropNames.PROCESS_SIGTERM_TIMEOUT_DURATION);
        assertThat(task.getProperties()).containsKey(PropNames.PROCESS_POST_SIGKILL_TIMEOUT_DURATION);
        return task;
    }

    @Test
    @SneakyThrows
    void testSendSingleArgWithoutProperties() {
        doReturn(TEST_MESSAGE_ID).when(mSender).send(anyString(), anyBoolean());

        String messageId = taskSender.send(taskWithoutProperties);

        assertThat(messageId).isEqualTo(TEST_MESSAGE_ID);

        verify(mSender).send(argMessage.capture(), argRestart.capture());

        Task task = checkMessageHasAddedProperties(argMessage.getValue());
        assertThat(task.getProperties()).doesNotContainEntry("P1", "V1");
        assertThat(task.getProperties()).doesNotContainEntry("P2", "V2");

        verifyNoMoreInteractions(mSender);
    }

    @ParameterizedTest
    @SneakyThrows
    @ValueSource(booleans = {true, false})
    void testSendDoubleArg(boolean restart) {
        doReturn(TEST_MESSAGE_ID).when(mSender).send(anyString(), anyBoolean());

        String messageId = taskSender.send(task, restart);

        assertThat(messageId).isEqualTo(TEST_MESSAGE_ID);

        verify(mSender).send(argMessage.capture(), eq(restart));

        Task task = checkMessageHasAddedProperties(argMessage.getValue());
        assertThat(task.getProperties()).containsEntry("P1", "V1");
        assertThat(task.getProperties()).containsEntry("P2", "V2");

        verifyNoMoreInteractions(mSender);
    }

    @Nested
    class ArgumentTests {

        @Test
        void testNullTask() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                taskSender.send(null);
            });
            assertThat(ex).hasMessage("the task cannot be null");
        }

        @Test
        void testNullTaskClassName() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                Task testClass = new Task();
                testClass.setTaskClass(null);
                taskSender.send(testClass);
            });
            assertThat(ex).hasMessage("the task's taskClass cannot be null");
        }
    }
}