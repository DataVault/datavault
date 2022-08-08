package org.datavaultplatform.broker.queue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
public abstract class RabbitUtils {

  public static String sendDirectToQueue(RabbitTemplate template, String queueName,
      String messageText) {
    MessageProperties props = new MessageProperties();
    props.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
    String messageId = UUID.randomUUID().toString();
    props.setMessageId(messageId);
    Message message = new Message(messageText.getBytes(StandardCharsets.UTF_8), props);
    template.send(queueName, message);
    log.info("Sent [{}] to [{}]", message, queueName);
    return messageId;
  }
}
