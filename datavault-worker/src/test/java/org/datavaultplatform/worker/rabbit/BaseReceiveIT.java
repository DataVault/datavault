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
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageAckListener;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public class BaseReceiveIT extends BaseRabbitTCTest {

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
  RabbitListenerEndpointRegistry registry;

  protected final List<MessageInfo> messageInfos = new ArrayList<>();

  protected String sendNormalMessage(long value) {
    MessageProperties props = new MessageProperties();
    props.setMessageId(UUID.randomUUID().toString());
    props.setPriority(NORMAL_PRIORITY);
    String msgBody = String.valueOf(value);
    Message msg = new Message(msgBody.getBytes(StandardCharsets.UTF_8), props);
    template.send(workerQueue.getActualName(), msg);
    return props.getMessageId();
  }

  protected String sendShutdownTestMessage(int priority) {
    MessageProperties props = new MessageProperties();
    props.setPriority(priority);
    props.setMessageId(UUID.randomUUID().toString());
    String msgBody = MessageInfo.SHUTDOWN;
    Message msg = new Message(msgBody.getBytes(StandardCharsets.UTF_8), props);
    template.send(workerQueue.getActualName(), msg);
    return props.getMessageId();
  }

  @BeforeEach
  void checkQueues() {
    Assertions.assertEquals("datavault", this.workerQueue.getActualName());
    Assertions.assertEquals("datavault-event", this.brokerQueue.getActualName());
    admin.purgeQueue(workerQueue.getActualName());
    log.info("q[{}]purged prior to test", workerQueue.getActualName());
    this.messageInfos.clear();

    registry.getListenerContainers().forEach(mlContainer -> {
      AbstractMessageListenerContainer cont = (AbstractMessageListenerContainer) mlContainer;
      cont.setMessageAckListener(new MessageAckListener() {
        @Override
        public void onComplete(boolean success, long deliveryTag, Throwable cause) {
          if (success) {
            log.info("ack successful [%d]", deliveryTag);
          } else {
            log.warn("ack failed [%d]", deliveryTag, cause);
          }
        }
      });
    });

  }
}