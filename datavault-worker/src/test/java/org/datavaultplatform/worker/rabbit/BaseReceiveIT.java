package org.datavaultplatform.worker.rabbit;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public abstract class BaseReceiveIT extends BaseRabbitTCTest {

  @Autowired
  protected AmqpAdmin admin;

  @Autowired
  protected RabbitTemplate template;

  @Autowired
  @Qualifier("workerQueue")
  protected Queue workerQueue;

  @Autowired
  @Qualifier("brokerQueue")
  Queue brokerQueue;

  @Autowired
  @Qualifier("restartWorker1Queue")
  Queue restartWorker1Queue;
  
  protected final List<RabbitMessageInfo> messageInfos = new ArrayList<>();

  protected String sendNormalMessage(long value) {
    MessageProperties props = new MessageProperties();
    props.setMessageId(UUID.randomUUID().toString());
    props.setPriority(NORMAL_PRIORITY);
    String msgBody = String.valueOf(value);
    props.setHeader("custom", msgBody);
    Message msg = new Message(msgBody.getBytes(StandardCharsets.UTF_8), props);
    template.send(workerQueue.getActualName(), msg);
    return props.getMessageId();
  }

  protected String sendShutdownTestMessage(int priority) {
    MessageProperties props = new MessageProperties();
    props.setPriority(priority);
    props.setMessageId(UUID.randomUUID().toString());
    String msgBody = RabbitMessageInfo.SHUTDOWN;
    Message msg = new Message(msgBody.getBytes(StandardCharsets.UTF_8), props);
    template.send(workerQueue.getActualName(), msg);
    return props.getMessageId();
  }

  @BeforeEach
  void checkQueues() {
    Assertions.assertEquals("datavault", this.workerQueue.getActualName());
    Assertions.assertEquals("datavault-event", this.brokerQueue.getActualName());
    Assertions.assertEquals("restart-worker-1", this.restartWorker1Queue.getActualName());
    admin.purgeQueue(workerQueue.getActualName());
    admin.purgeQueue(restartWorker1Queue.getActualName());
    log.info("q[{}]purged prior to test", workerQueue.getActualName());
    log.info("q[{}]purged prior to test", restartWorker1Queue.getActualName());
    this.messageInfos.clear();
  }
}