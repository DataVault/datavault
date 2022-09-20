package org.datavaultplatform.worker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.worker.queue.RabbitEventSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${worker.rabbit.enabled:true}")
public class EventSenderConfig {

  @Autowired
  RabbitTemplate template;

  @Value(QueueConfig.BROKER_QUEUE_NAME)
  String brokerQueueName;

  @Value("${sender.sequence.start:0}")
  int sequenceStart;

  @Value("${spring.application.name:worker-instance}")
  String workerName;

  @Bean
  public RecordingEventSender eventSender(ObjectMapper mapper) {
    return new RecordingEventSender(new RabbitEventSender(template, brokerQueueName, workerName, sequenceStart, mapper));
  }
}
