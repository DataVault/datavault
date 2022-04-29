package org.datavaultplatform.broker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@ConditionalOnExpression("${broker.scheduled.enabled:true}")
@EnableScheduling
@Configuration
@ComponentScan("org.datavaultplatform.broker.scheduled")
public class ScheduleConfig {

  @Bean
  public ThreadPoolTaskScheduler scheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("BrokerScheduler");
    return scheduler;
  }

}
