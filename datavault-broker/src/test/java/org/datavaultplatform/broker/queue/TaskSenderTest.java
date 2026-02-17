package org.datavaultplatform.broker.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSenderTest {

    private static final String TEST_MESSAGE_ID = "test-message-id";

    @Mock
    Sender mSender;

    TaskSender taskSender;

    @Captor
    ArgumentCaptor<String> argMessage;

    final ObjectMapper mapper = new ObjectMapper();

    String taskJson;
    Task task;
    
    @BeforeEach
    void setup() throws Exception {
        taskSender = new TaskSender(mSender, false, null,  null, null, null);
        task = new Task();
        task.setProperties(Map.of("P1","V1","P2","V2"));
        task.setTaskClass("<class>");
        Event event = new Event();
        event.setJobId("jobId");
        event.setEventClass("<event-class>");
        event.setTimestamp(new Date());
        
        task.setLastEvent(event);
        task.setIsRedeliver(false);
        taskJson = mapper.writeValueAsString(task);
    }
    
    @Test
    void testSendSingleArg() throws JsonProcessingException  {
       doReturn(TEST_MESSAGE_ID).when(mSender).send(anyString(), anyBoolean());
       
       String messageId = taskSender.send(task);
       
       assertThat(messageId).isEqualTo(TEST_MESSAGE_ID);
       
       verify(mSender).send(argMessage.capture(), eq(false));
       
       assertThat(argMessage.getValue()).isEqualTo(taskJson);
       
       verifyNoMoreInteractions(mSender);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void testSendDoubleArg(boolean restart) throws JsonProcessingException  {
        doReturn(TEST_MESSAGE_ID).when(mSender).send(anyString(), anyBoolean());

        String messageId = taskSender.send(task, restart);

        assertThat(messageId).isEqualTo(TEST_MESSAGE_ID);

        verify(mSender).send(argMessage.capture(), eq(restart));

        assertThat(argMessage.getValue()).isEqualTo(taskJson);

        verifyNoMoreInteractions(mSender);
    }

    
}