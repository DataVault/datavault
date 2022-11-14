package org.datavaultplatform.worker.rabbit;

public interface MessageProcessor {

  /**
   * Consume a message from RabbitMQ
   * @param messageInfo - the message to process
   * @return true if the message should be redelivered
   */
  boolean processMessage(MessageInfo messageInfo);

}
