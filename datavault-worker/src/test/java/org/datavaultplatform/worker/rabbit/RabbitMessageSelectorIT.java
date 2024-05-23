package org.datavaultplatform.worker.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@Slf4j
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "worker.next.message.selector.delay.ms=1000"
})
@Import(RabbitMessageSelectorIT.TestConfig.class)
public class RabbitMessageSelectorIT extends BaseRabbitIT {
    
    @Autowired
    AmqpAdmin rabbitAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    List<ReceivedMessage> receivedMessages;
    @Value("${queue.worker.restart}")
    private String hiPriorityQueueName;

    @Value("${queue.name}")
    private String loPriorityQueueName;

    @MockBean
    TopLevelRabbitMessageProcessor mTopLevelProcessor;
    
    @BeforeEach
    void setup(){
        Mockito.doAnswer(invocation -> {
            RabbitMessageInfo info = (RabbitMessageInfo) invocation.getArguments()[0];
            receivedMessages.add(new ReceivedMessage(info.queueName(), info.message(), LocalDateTime.now()));
            return null;
        }).when(mTopLevelProcessor).onMessage(Mockito.any(RabbitMessageInfo.class));
    }
    
    @Test
    void testRabbitMessageSelector() {
        // msgId1 should be the only message Q'd - so should be recvd next
        String msgId1 = sendHiPriorityMessage("hiPriority1");
        Awaitility.await().until(() -> recvdMessageId(msgId1));

        // msgId2 should be the only message Q'd - so should be recvd next
        String msgId2 = sendHiPriorityMessage("loPriority2");
        Awaitility.await().until(() -> recvdMessageId(msgId2));

        // with both msgId4 and msgId3 Q'd - the hi priority message should be recvd first
        String msgId3 = sendLoPriorityMessage("loPriority3");
        String msgId4 = sendHiPriorityMessage("hiPriority4");
        Awaitility.await().until(() -> recvdMessageId(msgId4));
        assertThat(receivedMessages.size()).isEqualTo(3);

        Awaitility.await().until(() -> recvdMessageId(msgId3));
        assertThat(receivedMessages.size()).isEqualTo(4);

        // with 3 hi and another 3 lo priority messages Q'd - the hi priority should be recvd first
        // and the lo priority messages might be recvd in a different order than they were originally Q'd

        String msgId5 = sendLoPriorityMessage("loPriority5");
        String msgId6 = sendLoPriorityMessage("loPriority6");
        String msgId7 = sendLoPriorityMessage("loPriority7");

        String msgId8 = sendHiPriorityMessage("hiPriority8");
        String msgId9 = sendHiPriorityMessage("hiPriority9");
        String msgId10 = sendHiPriorityMessage("hiPriority10");

        Awaitility.await().until(() -> recvdMessageId(msgId10));
        assertThat(receivedMessages.size()).isEqualTo(7);

        Awaitility.await().until(() -> recvdMessageId(msgId7));
        assertThat(receivedMessages.size()).isEqualTo(10);

        var messageIdsInRecvdOrder = receivedMessages.stream()
                .map(recvd -> recvd.message().getMessageProperties().getMessageId())
                .toList();

        //first 2 messages recd in order they were sent
        assertThatIterable(messageIdsInRecvdOrder.stream().skip(0).limit(2).toList()).containsExactly(msgId1, msgId2);

        //then hi priority message recvd before lo priority message
        assertThatIterable(messageIdsInRecvdOrder.stream().skip(2).limit(2).toList()).containsExactly(msgId4, msgId3);

        //then 3 hi priority message recvd 
        assertThatIterable(messageIdsInRecvdOrder.stream().skip(4).limit(3).toList()).containsExactly(msgId8, msgId9, msgId10);

        // finally, 3 lo priority message recvd
        assertThatIterable(messageIdsInRecvdOrder.stream().skip(7).limit(3).toList()).containsExactly(msgId5, msgId6, msgId7);

        List<Duration> durations = getDurationsBetweenRecvdMessages();

        // check that there was a 1-second(ish) gap between messages being recvd
        assertThat(durations.stream().map(Duration::toMillis))
                .allMatch(ms -> ms >= 1000 && ms < 1200);
    }

    private List<Duration> getDurationsBetweenRecvdMessages() {
        if (this.receivedMessages.size() < 2) {
            throw new IllegalStateException("expected at least 2 elements in the receivedMessages list");
        }
        List<Duration> result = new ArrayList<>();
        for (int i = 0; i < receivedMessages.size() - 2; i++) {
            LocalDateTime start = receivedMessages.get(i).timestamp;
            LocalDateTime end = receivedMessages.get(i + 1).timestamp;
            Duration duration = Duration.between(start, end);
            result.add(duration);
        }
        return result;
    }

    boolean recvdMessageId(String msgId) {
        return this.receivedMessages.stream().anyMatch(recvd -> recvd.message().getMessageProperties().getMessageId().equals(msgId));
    }

    @Test
    void testProperties() {
        assertThat(loPriorityQueueName).isEqualTo("datavault");
        assertThat(hiPriorityQueueName).isEqualTo("restart-worker-1");
    }

    String sendHiPriorityMessage(String messageBody) {
        return sendRabbitMessage(hiPriorityQueueName, messageBody);
    }

    String sendLoPriorityMessage(String messageBody) {
        return sendRabbitMessage(loPriorityQueueName, messageBody);
    }

    String sendRabbitMessage(String queueName, String messageBody) {
        Message rabbitMessage = MessageBuilder
                .withBody(messageBody.getBytes(StandardCharsets.UTF_8))
                .setMessageId(UUID.randomUUID().toString())
                .build();
        String messageId = rabbitMessage.getMessageProperties().getMessageId();
        rabbitTemplate.send(queueName, rabbitMessage);
        return messageId;
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public Queue workerQueue() {
            return new Queue("datavault", true);
        }

        @Bean
        public Queue restartWorker1() {
            return new Queue("restart-worker-1", true);
        }

        @Bean
        public ArrayList<ReceivedMessage> receivedMessages() {
            return new ArrayList<>();
        }
        
    }

    record ReceivedMessage(String queueName, Message message, LocalDateTime timestamp) {
    }
}
