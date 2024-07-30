package org.datavaultplatform.broker.queue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
public abstract class RabbitUtils {

  public static final String DEFAULT_EXCHANGE = "";
  
  public static String sendDirectToQueue(RabbitTemplate template, String queueName,
                                         String messageText) {
    return sendToRoutingKey(template, DEFAULT_EXCHANGE, queueName, messageText);  
  }
  
  public static String sendToExchange(RabbitTemplate template, String exchangeName,
                                         String messageText) {
    return sendToRoutingKey(template, exchangeName, null, messageText);
  }

  public static String sendToRoutingKey(RabbitTemplate template, String exchange, String routingKey,
      String messageText) {
    MessageProperties props = new MessageProperties();
    props.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
    String messageId = UUID.randomUUID().toString();
    props.setMessageId(messageId);
    Message message = new Message(messageText.getBytes(StandardCharsets.UTF_8), props);
    template.send(exchange, routingKey, message);
    log.info("Sent [{}] to [{}/{}]", message, exchange, routingKey);
    return messageId;
  }
}
