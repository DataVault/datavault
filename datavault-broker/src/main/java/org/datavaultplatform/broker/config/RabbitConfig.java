package org.datavaultplatform.broker.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.queue.EventListener;
import org.datavaultplatform.broker.queue.Sender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnExpression("${broker.rabbit.enabled:true}")
@Import({QueueConfig.class, Sender.class, EventListener.class, RabbitLocalConfig.class})
@Slf4j
public class RabbitConfig {
}
