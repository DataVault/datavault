package org.datavaultplatform.worker.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class ExitingShutdownHandler implements ShutdownHandler {

  private final String applicationName;

  private ApplicationEventPublisher applicationEventPublisher;
  
  private final ConfigurableApplicationContext ctx;

  public ExitingShutdownHandler(String applicationName, ConfigurableApplicationContext ctx) {
    this.applicationName = applicationName;
    this.ctx = ctx;
  }

  @Override
  public void handleShutdown(RabbitMessageInfo messageInfo) {
    log.warn("SHUTDOWN MESSAGE [{}] - Worker[{}] exiting.", messageInfo, applicationName);
    ctx.close();
  }
}
