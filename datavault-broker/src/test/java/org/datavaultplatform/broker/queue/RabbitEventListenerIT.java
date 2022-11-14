package org.datavaultplatform.broker.queue;

import static org.datavaultplatform.broker.config.QueueConfig.BROKER_QUEUE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;

public class RabbitEventListenerIT extends BaseRabbitTCTest {

  @SpyBean
  EventListener eventListener;

  @Autowired
  RabbitTemplate template;

  @Autowired
  @Qualifier("brokerQueue")
  Queue eventQueue;

  @Captor
  ArgumentCaptor<Message> argEventMessage;

  @Value(BROKER_QUEUE_NAME)
  String expectedQueueName;

  @Autowired
  RabbitAdmin admin;

  @BeforeEach
  void checkRecvQueueIsEmptyBeforeTest() {
    QueueInformation info = admin.getQueueInfo(expectedQueueName);
    assertEquals(0, info.getMessageCount());
  }


  @Test
  void testRecvFromWorker() {
    doNothing().when(eventListener).onMessage(argEventMessage.capture());

    String rand = UUID.randomUUID().toString();
    //send message direct to 'events queue' and check that we can receive it via Listener
    RabbitUtils.sendDirectToQueue(template, eventQueue.getActualName(), rand);

    Awaitility.await().atMost(10, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .until(() -> argEventMessage.getAllValues().isEmpty() == false);

    Message recvdMessage = argEventMessage.getValue();
    assertEquals(rand, new String(recvdMessage.getBody(), StandardCharsets.UTF_8));
    assertEquals(expectedQueueName, recvdMessage.getMessageProperties().getConsumerQueue());
  }

}
