package org.datavaultplatform.worker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.common.event.EventSender;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.worker.WorkerInstance;
import org.datavaultplatform.worker.queue.RabbitEventSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;

@Configuration
@ConditionalOnExpression("${worker.rabbit.enabled:true}")
public class EventSenderConfig {

  @Autowired
  RabbitTemplate template;

  @Value(QueueConfig.BROKER_QUEUE_NAME)
  String brokerQueueName;

  @Value("${sender.sequence.start:0}")
  int sequenceStart;

  @Autowired
  Environment env;

  @Bean
  public EventSender eventSender(ObjectMapper mapper,
      @Value("${use.recording.event.sender:false}") boolean useRecordingEventSender, Clock clock) {
    final EventSender result;
    
    String fullWorkerName = WorkerInstance.getWorkerName(env);
    
    RabbitEventSender rabbitEventSender = new RabbitEventSender(template, brokerQueueName,
        fullWorkerName,
        sequenceStart, mapper, clock);
    if (useRecordingEventSender) {
      result = new RecordingEventSender(rabbitEventSender);
    } else {
      result = rabbitEventSender;
    }
    return result;
  }
}
