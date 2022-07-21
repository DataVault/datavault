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
  private static final Long TIMEOUT_MS = 5000L;

  @Autowired
  RabbitTemplate template;

  @Autowired
  ApplicationContext ctx;

  @Bean(TEMP_QUEUE_NAME)
  Queue tempQueue() {
    Queue tempQueue = new Queue("", true, false, false);
    log.info("TEMP QUEUE IS {}", tempQueue);
    return tempQueue;
  }

  @EventListener
  void messageFromTemp(ApplicationReadyEvent event) {
    log.info("RABBIT INIT MESSAGE : send/recv check");
    RabbitTemplate template = event.getApplicationContext().getBean(RabbitTemplate.class);
    Queue tempQueue = event.getApplicationContext().getBean(
        RabbitLocalConfig.TEMP_QUEUE_NAME, Queue.class);
    String tempQname = tempQueue.getActualName();
    try {
      String random = UUID.randomUUID().toString();

      // If we send to direct exchange - the routing key is interpreted as queue name
      template.send("", tempQname, new Message(random.getBytes(StandardCharsets.UTF_8)));
      log.info("INIT MESSAGE : SENT[{}]TO[{}]", random, tempQueue.getActualName());
      Message message = template.receive(tempQname, TIMEOUT_MS);
      String recvd = message == null ? null : new String(message.getBody(), StandardCharsets.UTF_8);
      log.info("INIT MESSAGE : RECVD[{}]FROM[{}]", recvd, tempQname);
      Assert.isTrue(random.equals(recvd), () -> String.format("INIT MESSAGE : FAILED TO RECV expected msg[%s] FROM[%s]", random, tempQname));
    } catch (RuntimeException ex) {
      log.warn("INIT MESSAGE : FAILED TO RECV MESSAGE WITHIN [{}] ms FROM[{}]", TIMEOUT_MS, tempQname, ex);
    }
  }

}
