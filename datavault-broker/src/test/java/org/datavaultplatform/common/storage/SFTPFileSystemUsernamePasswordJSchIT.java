package org.datavaultplatform.common.storage;

import java.util.Map;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;

/*
 Tests SFTPFileSystem class against SFTP Server using username/password authentication.
 Uses JSch.
*/
public class SFTPFileSystemUsernamePasswordJSchIT extends BaseSFTPFileSystemUsernamePasswordIT  {

  @Override
  public SFTPFileSystemDriver getSftpFileSystemDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemJSch("sftp-jsch", props, TEST_CLOCK);
  }
}
