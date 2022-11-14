package org.datavaultplatform.broker.queue;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sender {

  private final RabbitTemplate template;
  private final String workerQueueName;

  @Autowired
  public Sender(@Value(BaseQueueConfig.WORKER_QUEUE_NAME) String workerQueueName, RabbitTemplate template) {
    this.template = template;
    this.workerQueueName = workerQueueName;
  }

  public String send(String messageText) {
    return RabbitUtils.sendDirectToQueue(template, workerQueueName, messageText);
  }

}
