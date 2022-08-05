package org.datavaultplatform.broker.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${broker.rabbit.enabled:true}")
@Slf4j
public class QueueConfig {

  public static final String WORKER_QUEUE_NAME = "${queue.name}";
  public static final String BROKER_QUEUE_NAME = "${queue.events}";

  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="broker.define.queue.worker", havingValue = "true",  matchIfMissing = false)
  public Queue workerQueue(@Value(WORKER_QUEUE_NAME) String workerQueueName) {
    // Allow for priority messages so that a shutdown message can be prioritised if required.
    Map<String, Object> args = new HashMap<>();
    args.put("x-max-priority", 2);
    return new Queue(workerQueueName, true, false, false, args);
  }

  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="broker.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Queue brokerQueue(@Value(BROKER_QUEUE_NAME) String brokerQueueName) {
    return new Queue(brokerQueueName, true, false, false);
  }
}
