package org.datavaultplatform.worker.config;

import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.queue.Receiver;
import org.datavaultplatform.worker.rabbit.*;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

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

  @Value(RabbitMessageSelectorScheduler.SPEL_DELAY_MS)
  private long workerNextMessageSelectorDelayMs;

  @Value("${spring.application.name}")
  String applicationName;

  @Value("${spring.rabbitmq.host}")
  String rabbitMQhost;
  
  @Value("${spring.rabbitmq.port}")
  int rabbitMQport;
  
  @Value("${spring.rabbitmq.username}")
  String rabbitMQusername;
  
  @Value("${spring.rabbitmq.password}")
  String rabbitMQpassword;
  
  public RabbitConfig(AmqpAdmin rabbitAdmin) {
    this.rabbitAdmin = rabbitAdmin;
  }

  @Bean
  ShutdownHandler shutdownHandler(ConfigurableApplicationContext ctx) {
    return new ExitingShutdownHandler(applicationName, ctx);
  }
  
  @Bean
  public ConnectionFactory connectionFactory() {
    log.warn("RRR CREATING RABBIT CONNECTION FACTORY for [{}/{}]",this.rabbitMQhost, this.rabbitMQport );
    ConnectionFactory result = new ConnectionFactory();
    result.setUsername(this.rabbitMQusername);
    result.setPort(this.rabbitMQport);
    result.setHost(this.rabbitMQhost);
    result.setUsername(this.rabbitMQusername);
    result.setPassword(this.rabbitMQpassword);
    result.setAutomaticRecoveryEnabled(false);
    return result;
  }
  
  @Bean
  TopLevelRabbitMessageProcessor topLevelRabbitMessageProcessor(Receiver receiver, ShutdownHandler shutdownHandler){
    return new TopLevelRabbitMessageProcessor(receiver, shutdownHandler);
  }

  @Bean
  public RabbitMessageSelector hiLoRabbitMessageSelector(
          ConnectionFactory connectionFactory,
          TopLevelRabbitMessageProcessor messageProcessor) {
    return new RabbitMessageSelector(this.hiPriorityQueueName, this.loPriorityQueueName, connectionFactory, messageProcessor);
  }

  @Bean("monitorLogger")
  public Logger monitorLogger() {
    return log;
  }
  
  @Bean("names")
  List<String> names(){
    return List.of("one","two","three");  
  }

  @Bean
  ScheduledMonitor monitor(ApplicationContext ctx) {
    Logger log = ctx.getBean(Logger.class);
    return new ScheduledMonitor(log);
  }
  
  @Bean
  public RabbitMessageSelectorScheduler selectorScheduler(RabbitMessageSelector selector, ScheduledMonitor monitor) {
    return new RabbitMessageSelectorScheduler(this.workerNextMessageSelectorDelayMs, selector, monitor);
  }

  @PostConstruct
  void postConstruct() {
    log.info("hiPriorityQueue[{}]", rabbitAdmin.getQueueProperties(hiPriorityQueueName));
    log.info("loPriorityQueue[{}]", rabbitAdmin.getQueueProperties(loPriorityQueueName));
  }


}
