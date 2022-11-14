package org.datavaultplatform.worker.rabbit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageInfo {

  public static final String SHUTDOWN = "shutdown";
  private String id;
  private String value;
  private Boolean isRedeliver;

  boolean isShutdown() {
    return SHUTDOWN.equalsIgnoreCase(value);
  }
}
