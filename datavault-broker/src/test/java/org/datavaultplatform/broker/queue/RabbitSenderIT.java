package org.datavaultplatform.broker.queue;

import static org.datavaultplatform.broker.config.RabbitConfig.QUEUE_DATA_VAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.datavaultplatform.broker.queue.BaseRabbitTCTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class RabbitSenderIT extends BaseRabbitTCTest {

  @Autowired
  RabbitTemplate template;

  @Autowired
  RabbitAdmin admin;

  @Autowired
  Sender sender;

  @Autowired
  Queue dataVaultQueue;

  @Value(QUEUE_DATA_VAULT)
  String expectedQueueName;

  @BeforeEach
  void checkSendQueueIsEmptyBeforeTest() {
    QueueInformation info = admin.getQueueInfo(expectedQueueName);
    assertEquals(0, info.getMessageCount());
  }

  @Test
  void testSendToWorker() {
    String rand = UUID.randomUUID().toString();
    sender.send(rand);
    Message message = template.receive(dataVaultQueue.getActualName(), 2500);
    assertEquals(rand, new String(message.getBody(), StandardCharsets.UTF_8));
    MessageProperties props = message.getMessageProperties();
    assertEquals(expectedQueueName, props.getReceivedRoutingKey());
  }


}
