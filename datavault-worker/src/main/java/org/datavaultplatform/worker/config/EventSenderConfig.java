package org.datavaultplatform.worker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.EventSender;
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
@Slf4j
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
  public EventSender eventSender(ObjectMapper mapper,
      @Value("${use.recording.event.sender:false}") boolean useRecordingEventSender) {
    final EventSender result;
    RabbitEventSender rabbitEventSender = new RabbitEventSender(template, brokerQueueName,
        workerName,
        sequenceStart, mapper);
    if (useRecordingEventSender) {
      result = new RecordingEventSender(rabbitEventSender);
    } else {
      result = rabbitEventSender;
    }
    log.info("using event sender class [{}]", result.getClass().getSimpleName());
    return result;
  }
}
