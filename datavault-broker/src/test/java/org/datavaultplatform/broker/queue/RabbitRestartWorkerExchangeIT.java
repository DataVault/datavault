package org.datavaultplatform.broker.queue;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(properties = "logging.level.org.springframework.amqp=DEBUG")
public class RabbitRestartWorkerExchangeIT extends BaseRabbitTCTest {

  @Autowired
  RabbitTemplate template;

  @Autowired
  RabbitAdmin admin;

  @Autowired
  @Qualifier("restartWorker1Queue")
  Queue restartWorker1Queue;

  @Autowired
  @Qualifier("restartWorker2Queue")
  Queue restartWorker2Queue;

  @Autowired
  @Qualifier("restartWorker3Queue")
  Queue restartWorker3Queue;

  @BeforeEach
  void checkSendQueueIsEmptyBeforeTest() {
    checkMessagesOnWorkerRestartQueues(0);
  }
  
    
  void checkMessagesOnWorkerRestartQueues(int expectedCount ){
    QueueInformation info1 = admin.getQueueInfo("restart-worker-1");
    QueueInformation info2 = admin.getQueueInfo("restart-worker-2");
    QueueInformation info3 = admin.getQueueInfo("restart-worker-3");
    assertEquals(expectedCount, info1.getMessageCount());
    assertEquals(expectedCount, info2.getMessageCount());
    assertEquals(expectedCount, info3.getMessageCount());
    
  }
  
  @Test
  @SneakyThrows
  void testSendToFanOutExchange() {
    template.setExchange("restart-worker");
    template.send(new Message("This is a test message".getBytes(StandardCharsets.UTF_8)));
    Thread.sleep(100);
    checkMessagesOnWorkerRestartQueues(1);
  }
}
