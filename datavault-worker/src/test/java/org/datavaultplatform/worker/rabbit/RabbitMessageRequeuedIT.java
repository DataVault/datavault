package org.datavaultplatform.worker.rabbit;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.TestUtils;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.queue.Receiver;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
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
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = {DataVaultWorkerInstanceApp.class, RabbitMessageRequeuedIT.TestConfig.class})
@Slf4j
@AddTestProperties
@TestPropertySource(properties = {
        "worker.next.message.selector.delay.ms=1000"
})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
//@Testcontainers(disabledWithoutDocker = true)
public class RabbitMessageRequeuedIT extends BaseRabbitIT {

    static final String REQUEUE = "delayed-so-requeue";

    @Value("${queue.worker.restart}")
    String hiPriorityQueueName;

    @Value("${queue.name}")
    String loPriorityQueueName;
    
    @Autowired
    AmqpAdmin rabbitAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    CountDownLatch zeroMessageOnQueueLatch;
    CountDownLatch testEndedLatch;
    List<ReceivedMessage> receivedMessages;

    @MockBean
    Receiver mReciever;

    @Autowired
    RabbitMessageSelector selector;
    
    @BeforeEach
    void setup() {

        zeroMessageOnQueueLatch = new CountDownLatch(1);
        testEndedLatch = new CountDownLatch(1);
        receivedMessages = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            RabbitMessageInfo rabbitMessageInfo = invocation.getArgument(0, RabbitMessageInfo.class);
            var queueName = rabbitMessageInfo.queueName();
            var message = rabbitMessageInfo.message();
            var channel = rabbitMessageInfo.channel();
            var deliveryTag = rabbitMessageInfo.deliveryTag();

            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            try {
                log.info("MSG : Q[{}] Ch[{}] delTag[{}] msg[{}]", queueName, channel.getChannelNumber(), deliveryTag, msg);

                var receivedMessage = new ReceivedMessage(queueName, message, deliveryTag, channel, LocalDateTime.now());
                receivedMessages.add(receivedMessage);
                if (REQUEUE.equals(msg)) {
                    log.info("GOT REQUEUE MESSAGE - cutting connections to rabbitmq");
                    log.info("Selector - [{}]", selector);
                    zeroMessageOnQueueLatch.await(30, TimeUnit.SECONDS); // wait until the test has confirmed 0 message on queue
                    selector.destroy(); // this causes the rabbit connection to be terminated
                    testEndedLatch.await(30, TimeUnit.SECONDS); //wait until test ended
                }
            } catch (Exception ex) {
                log.error("unexpected exception processing message", ex);
                throw new RuntimeException(ex);
            }
            return null;
        }).when(mReciever).onMessage(any(RabbitMessageInfo.class));
    }

    @Test
    void testProperties() {
        assertThat(loPriorityQueueName).isEqualTo("datavault");
        assertThat(hiPriorityQueueName).isEqualTo("restart-worker-1");
    }

    @Test
    @SneakyThrows
    void testRabbitMessageSelector() {
        // msgId1 should be the only message Q'd - so should be recvd next
        String msgId1 = sendHiPriorityMessage("hiPriority1");
        TestUtils.waitUntil(() -> recvdMessageId(msgId1));

        // msgId2 should be the only message Q'd - so should be recvd next
        String msgId2 = sendHiPriorityMessage("loPriority2");
        TestUtils.waitUntil(() -> recvdMessageId(msgId2));

        var messageIdsInRecvdOrder = receivedMessages.stream()
                .map(recvd -> recvd.message().getMessageProperties().getMessageId())
                .toList();

        //first 2 messages recd in order they were sent
        assertThatIterable(messageIdsInRecvdOrder.stream().skip(0).limit(2).toList()).containsExactly(msgId1, msgId2);

        List<Duration> durations = getDurationsBetweenRecvdMessages();

        // check that there was a 1-second(ish) gap between messages being recvd
        assertThat(durations.stream().map(Duration::toMillis))
                .allMatch(ms -> ms >= 1000 && ms < 1100);

        assertThat(getHiPriorityQueueCount()).isEqualTo(0);
        String msgId3 = sendHiPriorityMessage(REQUEUE);
        TestUtils.waitUntil(() -> recvdMessageId(msgId3));
        assertThat(receivedMessages.size()).isEqualTo(3);

        TestUtils.waitUntil(() -> getHiPriorityQueueCount() == 0);
        zeroMessageOnQueueLatch.countDown(); // now we release the latch, the connection is cut and the message should be re-queued with re-deliver

        // the message should have bounced back into the queue
        TestUtils.waitUntil(() -> getHiPriorityQueueCount() == 1);
        assertThat(getHiPriorityQueueCount()).isEqualTo(1);

        Message requeued = rabbitTemplate.receive(hiPriorityQueueName, 100);
        assertThat(new String(requeued.getBody(), StandardCharsets.UTF_8)).isEqualTo(REQUEUE);
        assertThat(requeued.getMessageProperties().isRedelivered()).isTrue();
        testEndedLatch.countDown();
    }

    List<Duration> getDurationsBetweenRecvdMessages() {
        if (receivedMessages.size() < 2) {
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
        return receivedMessages.stream().anyMatch(recvd -> recvd.message().getMessageProperties().getMessageId().equals(msgId));
    }


    String sendHiPriorityMessage(String messageBody) {
        return sendRabbitMessage(hiPriorityQueueName, messageBody);
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


    long getHiPriorityQueueCount() {
        return rabbitAdmin.getQueueInfo(this.hiPriorityQueueName).getMessageCount();
    }

    record ReceivedMessage(String queueName, Message message, long deliveryTag, Channel channel,
                                  LocalDateTime timestamp) {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public Queue workerQueue(@Value("${queue.name}") String loPriorityQueueName) {
            return new Queue(loPriorityQueueName, true);
        }

        @Bean
        public Queue restartWorker1(@Value("${queue.worker.restart}") String hiPriorityQueueName) {
            return new Queue(hiPriorityQueueName, true);
        }
        
        @Bean
        public Logger monitorLogger() {
            return log;
        }
    }
}
