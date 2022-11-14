package org.datavaultplatform.worker.rabbit;

public interface ShutdownHandler {

  //for testing we mock the Shutdown handler, for real we perform System.exit(1)
  void handleShutdown(MessageInfo messageInfo);

}
