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
  public static final String RESTART_EXCHANGE_NAME = "${exchange.restarts:restart-worker}";
  public static final String RESTART_WORKER_ONE_QUEUE_NAME = "${queue.name.worker.1:restart-worker-1}";
  public static final String RESTART_WORKER_TWO_QUEUE_NAME = "${queue.name.worker.2:restart-worker-2}";
  public static final String RESTART_WORKER_THREE_QUEUE_NAME = "${queue.name.worker.3:restart-worker-3}";
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
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public Queue restartWorker1Queue(@Value(RESTART_WORKER_ONE_QUEUE_NAME) String restartWorker1queueName) {
    return new Queue(restartWorker1queueName, true, false, false);
  }
  
  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public Queue restartWorker2Queue(@Value(RESTART_WORKER_TWO_QUEUE_NAME) String restartWorker2queueName) {
    return new Queue(restartWorker2queueName, true, false, false);
  }
  //will create Q if it does not exist
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public Queue restartWorker3Queue(@Value(RESTART_WORKER_THREE_QUEUE_NAME) String restartWorker3queueName) {
    return new Queue(restartWorker3queueName, true, false, false);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public FanoutExchange restartWorkerExchange(@Value(RESTART_EXCHANGE_NAME) String restartExchangeName) {
    return new FanoutExchange(restartExchangeName, true, false);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public Binding bindRestartWorkerExchangeToRestartWorkerQueue1(FanoutExchange fanoutExchange, @Qualifier("restartWorker1Queue") Queue restartWorkerQueue1) {
    return BindingBuilder.bind(restartWorkerQueue1).to(fanoutExchange);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public Binding bindRestartWorkerExchangeToRestartWorkerQueue2(FanoutExchange fanoutExchange, @Qualifier("restartWorker2Queue") Queue restartWorkerQueue2) {
    return BindingBuilder.bind(restartWorkerQueue2).to(fanoutExchange);
  }
  
  @Bean
  @ConditionalOnProperty(value="rabbitmq.define.queue.restarts", havingValue = "true",  matchIfMissing = false)
  public Binding bindRestartWorkerExchangeToRestartWorkerQueue3(FanoutExchange fanoutExchange, @Qualifier("restartWorker3Queue") Queue restartWorkerQueue3) {
    return BindingBuilder.bind(restartWorkerQueue3).to(fanoutExchange);
  }
}
