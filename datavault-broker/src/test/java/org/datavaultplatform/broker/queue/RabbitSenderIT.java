package org.datavaultplatform.broker.queue;

import static org.datavaultplatform.broker.config.QueueConfig.WORKER_QUEUE_NAME;
import static org.datavaultplatform.common.config.BaseQueueConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

public class RabbitSenderIT extends BaseRabbitTCTest {

  @Autowired
  RabbitTemplate template;

  @Autowired
  RabbitAdmin admin;

  @Autowired
  Sender sender;

  @Autowired
  @Qualifier("workerQueue")
  Queue dataVaultQueue;

  @Value(RESTART_EXCHANGE_NAME)
  String restartExchangeName;
  
  @Value(WORKER_QUEUE_NAME)
  String expectedQueueName;

  @Value(RESTART_WORKER_ONE_QUEUE_NAME)
  String expectedRestartWorker1QueueName;

  @Value(RESTART_WORKER_TWO_QUEUE_NAME)
  String expectedRestartWorker2QueueName;
  
  @Value(RESTART_WORKER_THREE_QUEUE_NAME)
  String expectedRestartWorker3QueueName;

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
  void checkQueuesAreEmptyBeforeTest() {
    List.of(expectedQueueName, expectedRestartWorker1QueueName, expectedRestartWorker2QueueName, expectedRestartWorker3QueueName).forEach(
            queueName -> {
              QueueInformation info = admin.getQueueInfo(queueName);
              assertEquals(0, info.getMessageCount());
            }
    );
  }

  @Test
  void testSendToWorker1() {
    assertEquals(expectedQueueName, dataVaultQueue.getActualName());

    String rand = UUID.randomUUID().toString();
    String messageId = sender.send(rand);
    checkQueueForMessage(dataVaultQueue, "", expectedQueueName, rand, messageId);
  }

  @Test
  void testSendToWorker2() {
    assertEquals(expectedQueueName, dataVaultQueue.getActualName());

    String rand = UUID.randomUUID().toString();
    String messageId = sender.send(rand,false);
    checkQueueForMessage(dataVaultQueue, "", expectedQueueName, rand, messageId);
  }

  void checkQueueForMessage(Queue queue, String expectedExchange, String expectedRoutingKey, String expectedMessageBody, String expectedMessageId){
    Message message = template.receive(queue.getActualName(), 2500);
    assertEquals(expectedMessageBody, new String(message.getBody(), StandardCharsets.UTF_8));
    MessageProperties props = message.getMessageProperties();
    assertEquals(expectedMessageId, props.getMessageId());
    assertEquals(expectedExchange, props.getReceivedExchange());
    assertEquals(expectedRoutingKey, props.getReceivedRoutingKey());
  }

  @Test
  void testSendToRestartExchange() {

    String rand = UUID.randomUUID().toString();
    String messageId = sender.send(rand,true);

    checkQueueForMessage(restartWorker1Queue, restartExchangeName,"", rand, messageId);
    checkQueueForMessage(restartWorker2Queue, restartExchangeName, "", rand, messageId);
    checkQueueForMessage(restartWorker3Queue, restartExchangeName, "", rand, messageId);
  }


}
