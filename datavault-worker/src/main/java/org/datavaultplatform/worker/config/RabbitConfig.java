package org.datavaultplatform.worker.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.queue.Receiver;
import org.datavaultplatform.worker.rabbit.*;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnExpression("${worker.rabbit.enabled:true}")
@EnableScheduling
@Slf4j
public class RabbitConfig {

  private final AmqpAdmin rabbitAdmin;

  @Value("${queue.worker.restart}")
  private String hiPriorityQueueName;

  @Value("${queue.name}")
  private String loPriorityQueueName;

  @Value("${worker.poll.rabbitmq.delay.ms:50}")
  private long pollRabbitMQdelayMs;

  @Value("${spring.application.name}")
  String applicationName;

  public RabbitConfig(AmqpAdmin rabbitAdmin) {
    this.rabbitAdmin = rabbitAdmin;
  }

  @Bean
  ShutdownHandler shutdownHandler(ConfigurableApplicationContext ctx) {
    return new ExitingShutdownHandler(applicationName, ctx);
  }

  @Bean
  RabbitQueuePoller hiPriorityPoller(RabbitTemplate rabbitTemplate ){
    return new RabbitQueuePoller(true, this.hiPriorityQueueName, rabbitTemplate, this.pollRabbitMQdelayMs);
  }

  @Bean
  RabbitQueuePoller loPriorityPoller(RabbitTemplate rabbitTemplate ){
    return new RabbitQueuePoller(false, this.loPriorityQueueName, rabbitTemplate, this.pollRabbitMQdelayMs);
  }
  
  @Bean
  TopLevelRabbitMessageProcessor topLevelRabbitMessageProcessor(Receiver receiver, ShutdownHandler shutdownHandler){
    return new TopLevelRabbitMessageProcessor(receiver, shutdownHandler);
  }

  @Bean
  public RabbitMessageSelector hiLoRabbitMessageSelector(
          @Qualifier("hiPriorityPoller") RabbitQueuePoller hiPriorityPoller,
          @Qualifier("loPriorityPoller") RabbitQueuePoller loPriorityPoller,
          TopLevelRabbitMessageProcessor messageProcessor) {
    return new RabbitMessageSelector(hiPriorityPoller, loPriorityPoller, messageProcessor);
  }

  @Bean
  public RabbitMessageSelectorScheduler selectorScheduler(RabbitMessageSelector selector) {
    return new RabbitMessageSelectorScheduler(this.pollRabbitMQdelayMs, selector);
  }

  @PostConstruct
  void postConstruct() {
    log.info("hiPriorityQueue[{}]", rabbitAdmin.getQueueProperties(hiPriorityQueueName));
    log.info("loPriorityQueue[{}]", rabbitAdmin.getQueueProperties(loPriorityQueueName));
  }


}
