package org.datavaultplatform.worker.rabbit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExitingShutdownHandler implements ShutdownHandler {

  private final String applicationName;
  public static final int NORMAL_TERMINATION = 0;

  public ExitingShutdownHandler(String applicationName) {
    this.applicationName = applicationName;
  }

  @Override
  public void handleShutdown(MessageInfo messageInfo) {
    log.warn("SHUTDOWN MESSAGE [{}] - Worker[{}] exiting.", messageInfo, applicationName);
    System.exit(NORMAL_TERMINATION);
  }
}
