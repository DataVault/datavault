package org.datavaultplatform.common.storage.impl;

import java.time.Clock;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * SFTPFileSystem - we HAVE to use this class name "org.datavaultplatform.common.storage.impl.SFTPFileSystem"
 * - we cannot use an interface but we can change the implementation by changing which class it extends.
 * TODO : The intention is : replace the original JSch implementation to use apache-ssd-sftp instead.
 */
@Slf4j
public class SFTPFileSystem extends SFTPFileSystemJSch {

  public SFTPFileSystem(String name, Map<String, String> config) {
    super(name, config);
    constructed();
  }

  /**
   * Used for testing
   */
  public SFTPFileSystem(String name, Map<String, String> config, Clock clock) {
    super(name, config, clock);
    constructed();
  }
  private void constructed() {
    log.info("Created SFTPFileSystem which extends [{}]", this.getClass().getSuperclass().getSimpleName());
  }
}
