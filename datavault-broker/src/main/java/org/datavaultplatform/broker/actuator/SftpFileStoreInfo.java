package org.datavaultplatform.broker.actuator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class SftpFileStoreInfo {

  private String id;
  private String label;
  private String username;
  private String host;
  private String port;
  private String rootPath;
  private boolean ignored;
  private boolean canConnect;
  private String connectionException;
}
