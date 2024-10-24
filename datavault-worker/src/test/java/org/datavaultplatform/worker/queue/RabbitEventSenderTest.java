package org.datavaultplatform.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitEventSenderTest {

    public static final String EVENT_QUEUE_NAME = "TEST_EVENT_QUEUE_NAME";
    public static final String WORKER_NAME = "WORKER_NAME";
    public static final String CANNED_JSON = """
            {
              "canned" : "json"
            }
            """;
 
    RabbitEventSender eventSender;
    
    @Mock
    RabbitTemplate mRabbitTemplate;
    
    @Mock
    ObjectMapper mMapper;
    
    @Captor
    ArgumentCaptor<Object> argEventSent;

    @Captor
    ArgumentCaptor<String> argQueueName;

    @Captor
    ArgumentCaptor<Message> argMessage;
    
    Clock clock;
    
    Date fixedDate;

    @BeforeEach
    void setup() {
        Instant instant = Instant.parse("2007-12-03T10:15:30.00Z");
        this.clock = Clock.fixed(instant, ZoneOffset.UTC);
        this.fixedDate = new Date(instant.toEpochMilli());
        this.eventSender = new RabbitEventSender(mRabbitTemplate, EVENT_QUEUE_NAME, WORKER_NAME, 123, mMapper, clock);    
    }
    
    @Test
    @SneakyThrows
    void testSendMessage() {
        doNothing().when(mRabbitTemplate).send(argQueueName.capture(), argMessage.capture());
        when(mMapper.writeValueAsString(argEventSent.capture())).thenReturn(CANNED_JSON);
        Complete event = new Complete();
        
        eventSender.send(event);
        
        Event eventSent = (Event) argEventSent.getValue();
        assertThat(eventSent).isSameAs(event);
        assertThat(eventSent.getTimestamp().equals(fixedDate));
        assertThat(eventSent.getSequence()).isEqualTo(124);
        
        String jsonMessage = new String(argMessage.getValue().getBody(), StandardCharsets.UTF_8);
        assertThat(jsonMessage).isEqualTo(CANNED_JSON);
    }
}