package org.datavaultplatform.common.storage;

import java.util.Map;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;

/*
 Tests SFTPFileSystem class against SFTP Server using username/password authentication.
 Uses SSHD.
*/
public class SFTPFileSystemUsernamePasswordSSHDIT extends BaseSFTPFileSystemUsernamePasswordIT {

  @Override
  public SFTPFileSystemDriver getSftpFileSystemDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemSSHD("mina-sshd", props, TEST_CLOCK);
  }
}
