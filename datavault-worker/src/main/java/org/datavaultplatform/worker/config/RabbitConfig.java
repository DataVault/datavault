package org.datavaultplatform.worker.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.rabbit.ExitingShutdownHandler;
import org.datavaultplatform.worker.rabbit.MessageProcessor;
import org.datavaultplatform.worker.rabbit.RabbitListenerUtils;
import org.datavaultplatform.worker.rabbit.RabbitMessageListener;
import org.datavaultplatform.worker.rabbit.ShutdownHandler;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${worker.rabbit.enabled:true}")
@Slf4j
public class RabbitConfig {

  @Autowired
  RabbitListenerEndpointRegistry registry;

  @Autowired
  MessageProcessor messageProcessor;

  @Value("${spring.application.name}")
  String applicationName;

  @Bean
  RabbitListenerUtils rabbitListeners() {
    return new RabbitListenerUtils(registry);
  }

  @Bean
  RabbitMessageListener rabbitMessageListener() {
    return new RabbitMessageListener(messageProcessor, shutdownHandler(), rabbitListeners(), applicationName);
  }

  @Bean
  ShutdownHandler shutdownHandler() {
    return new ExitingShutdownHandler(applicationName);
  }

}
