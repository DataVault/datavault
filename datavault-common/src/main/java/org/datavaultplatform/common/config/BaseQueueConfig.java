package org.datavaultplatform.common.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@Slf4j
public abstract class BaseQueueConfig {

  public static final String WORKER_QUEUE_NAME = "${queue.name}";
  public static final String BROKER_QUEUE_NAME = "${queue.events}";
  public static final String X_MAX_PRIORITY = "x-max-priority";

  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.worker", havingValue = "true",  matchIfMissing = false)
  public Queue workerQueue(@Value(WORKER_QUEUE_NAME) String workerQueueName) {
    // Allow for priority messages so that a shutdown message can be prioritised if required.
    Map<String, Object> args = new HashMap<>();
    args.put(X_MAX_PRIORITY, 2);
    return new Queue(workerQueueName, true, false, false, args);
  }

  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Queue brokerQueue(@Value(BROKER_QUEUE_NAME) String brokerQueueName) {
    return new Queue(brokerQueueName, true, false, false);
  }

  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Queue restartWorker1Queue() {
    return new Queue("restart-worker-1", true, false, false);
  }
  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Queue restartWorker2Queue() {
    return new Queue("restart-worker-2", true, false, false);
  }
  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Queue restartWorker3Queue() {
    return new Queue("restart-worker-3", true, false, false);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public FanoutExchange restartWorkerExchange() {
    return new FanoutExchange("restart-worker", true, false);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Binding bindRestartWorkerExchangeToRestartWorkerQueue1(FanoutExchange fanoutExchange, @Qualifier("restartWorker1Queue") Queue restartWorkerQueue1) {
    return BindingBuilder.bind(restartWorkerQueue1).to(fanoutExchange);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Binding bindRestartWorkerExchangeToRestartWorkerQueue2(FanoutExchange fanoutExchange, @Qualifier("restartWorker2Queue") Queue restartWorkerQueue2) {
    return BindingBuilder.bind(restartWorkerQueue2).to(fanoutExchange);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.broker", havingValue = "true",  matchIfMissing = false)
  public Binding bindRestartWorkerExchangeToRestartWorkerQueue3(FanoutExchange fanoutExchange, @Qualifier("restartWorker3Queue") Queue restartWorkerQueue3) {
    return BindingBuilder.bind(restartWorkerQueue3).to(fanoutExchange);
  }
}
