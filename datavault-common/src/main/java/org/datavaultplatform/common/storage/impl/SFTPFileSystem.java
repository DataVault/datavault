package org.datavaultplatform.common.storage.impl;

import java.time.Clock;
import java.util.Map;

/**
 * SFTPFileSystem - we HAVE to use this class name "org.datavaultplatform.common.storage.impl.SFTPFileSystem"
 * - we cannot use an interface but we can change the implementation by changing which class it extends.
 * TODO : The intention is : replace the original JSch implementation to use apache-ssd-sftp instead.
 */
public class SFTPFileSystem extends SFTPFileSystemJSch {

  public SFTPFileSystem(String name, Map<String, String> config) {
    super(name, config);
  }

  public SFTPFileSystem(String name, Map<String, String> config, Clock clock) {
    super(name, config, clock);
  }
}
