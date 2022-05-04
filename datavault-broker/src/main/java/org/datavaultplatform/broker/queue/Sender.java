package org.datavaultplatform.broker.queue;

import lombok.extern.slf4j.Slf4j;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Sender {

  private final RabbitTemplate template;
  private final Queue dataVaultQueue;

  @Autowired
  public Sender(@Qualifier("dataVaultQueue") Queue dataVaultQueue, RabbitTemplate template) {
    this.template = template;
    this.dataVaultQueue = dataVaultQueue;
  }

  public void send(String messageText) {
    RabbitUtils.sendDirectToQueue(template, dataVaultQueue.getActualName(), messageText);
  }

}
