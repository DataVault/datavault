package org.datavaultplatform.common.storage.impl;

import java.time.Clock;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * pre dv5, the storageClassName 'org.datavaultplatform.common.storage.impl.SFTPFileSystem' - was used to create instances of this class.
 * This class is now abstract - no instances of this class should now be created.
 * With dv5, StorageClassNameResolver takes 'org.datavaultplatform.common.storage.impl.SFTPFileSystem' and returns an SFTP Driver class name
 * based on configuration.
 */
@Slf4j
public abstract class SFTPFileSystem extends SFTPFileSystemSSHD {

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
