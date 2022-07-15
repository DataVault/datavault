package org.datavaultplatform.broker.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.queue.EventListener;
import org.datavaultplatform.broker.queue.Sender;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnExpression("${broker.rabbit.enabled:true}")
@Import({Sender.class, EventListener.class, RabbitLocalConfig.class})
@Slf4j
public class RabbitConfig {

  public static final String QUEUE_EVENT = "${queue.events}";
  public static final String QUEUE_DATA_VAULT = "${queue.name}";

  //will create Q if it does not exist
  @Bean
  Queue eventQueue(@Value(QUEUE_EVENT) String eventQueueName) {
    // Allow for priority messages so that a shutdown message can be prioritised if required.
    Map<String, Object> args = new HashMap<>();
    args.put("x-max-priority", 2);
    return new Queue(eventQueueName, true, false, false, args);
  }

  //will create Q if it does not exist
  @Bean
  Queue dataVaultQueue(@Value(QUEUE_DATA_VAULT) String dataVaultQueueName) {
    return new Queue(dataVaultQueueName, true);
  }
}
