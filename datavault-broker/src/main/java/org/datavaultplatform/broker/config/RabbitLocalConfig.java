package org.datavaultplatform.broker.config;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

@Configuration
@Profile("local")
@Slf4j
public class RabbitLocalConfig {

  public static final String TEMP_QUEUE_NAME = "temp-queue";

  @Autowired
  RabbitTemplate template;

  @Autowired
  ApplicationContext ctx;

  @Bean(TEMP_QUEUE_NAME)
  Queue tempQueue() {
    Queue tempQueue = new Queue("", true, false, true);
    log.info("TEMP QUEUE IS {}", tempQueue);
    return tempQueue;
  }

  @EventListener
  void messageFromTemp(ApplicationReadyEvent event) {
    RabbitTemplate template = event.getApplicationContext().getBean(RabbitTemplate.class);
    Queue tempQueue = event.getApplicationContext().getBean(
        RabbitLocalConfig.TEMP_QUEUE_NAME, Queue.class);
    try {
      String random = UUID.randomUUID().toString();

      // If we send to direct exchange - the routing key is interpreted as queue name
      template.send("", tempQueue.getActualName(), new Message(random.getBytes(StandardCharsets.UTF_8)));

      Message message = template.receive(tempQueue.getActualName(), 5000L);
      String recvd = message == null ? null : new String(message.getBody(), StandardCharsets.UTF_8);
      log.info("INIT MESSAGE RECVD[{}]", recvd);
      Assert.isTrue(random.equals(recvd), () -> "Failed to recv rabbit test message");
    } catch (RuntimeException ex) {
      log.warn("FAILED TO RECV INIT MESSAGE WITHIN 5seconds", ex);
    }
  }

}
