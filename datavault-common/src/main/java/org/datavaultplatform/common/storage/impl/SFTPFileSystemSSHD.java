package org.datavaultplatform.common.storage.impl;

import static org.datavaultplatform.common.storage.impl.SFTPConnectionInfo.PATH_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.apache.sshd.sftp.client.extensions.SpaceAvailableExtension;
import org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo;
import org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatPathExtension;
import org.apache.sshd.sftp.common.SftpConstants;
import org.apache.sshd.sftp.common.extensions.openssh.StatVfsExtensionParser;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.impl.ssh.UtilitySSHD;
import org.datavaultplatform.common.storage.impl.ssh.UtilitySSHD.SFTPMonitorSSHD;

/**
 * An implementation of SFTPFileSystemDriver to use Apache sshd's sftp-client library.
 */
@Slf4j
public class SFTPFileSystemSSHD extends Device implements SFTPFileSystemDriver {


  private final SFTPConnectionInfo connectionInfo;
  private boolean monitoring;

  /*
   * This constructor will be used in production code
   */
  public SFTPFileSystemSSHD(String name, Map<String, String> config) {
    this(name, config, Clock.systemDefaultZone());
  }

  /*
   * This constructor will be used for testing where we can pass in a 'fixed' clock
   */
  public SFTPFileSystemSSHD(String name, Map<String, String> config, Clock clock) {
    super(name, config);
    this.connectionInfo = SFTPConnectionInfo.getConnectionInfo(config, clock);
    this.monitoring = connectionInfo.getMonitoring();
  }

  @Override
  @SneakyThrows
  public List<FileInfo> list(String path) {
    List<FileInfo> files = new ArrayList<>();
    try (SFTPConnection con = getConnection()) {
      String fullPath = con.getFullPath(path);

      //TODO - don't know how deep this should go 1 level or all the way down - need to test the other one with nested directories
      for (SftpClient.DirEntry entry : con.sftpClient.readDir(fullPath)) {

        if (entry.getFilename().equals(".") ||
            entry.getFilename().equals("..")) {
          continue;
        }

        String entryKey = path + PATH_SEPARATOR + entry.getFilename();

        FileInfo info = new FileInfo(entryKey,
            "", // Absolute path - unused?
            entry.getFilename(),
            entry.getAttributes().isDirectory());
        files.add(info);
      }
    }
    return files;
  }
  @Override
  public boolean valid(String path) {
    // same as SFTPFileSystemJSch :-(
    return true;
  }

  private SFTPConnection getConnection() {
    return new SFTPConnection(this.connectionInfo);
  }

  @Override
  public boolean exists(String path) {
    try (SFTPConnection con = getConnection()) {
      String fullPath = con.getFullPath(path);
      try {
        con.sftpClient.lstat(fullPath);
        return true;
      } catch (IOException ex) {
        log.warn("problem checking existence of [{}]", path, ex);
        return false;
      }
    }
  }
  @Override

  public long getSize(String path) throws Exception {
    try (SFTPConnection con = getConnection()) {
      String fullPath = con.getFullPath(path);
      try {
        Attributes attrs = con.sftpClient.lstat(fullPath);
        if (attrs.isDirectory()) {
          return UtilitySSHD.calculateDirSize(con, fullPath);
        } else {
          return attrs.getSize();
        }
      } catch (Exception ex) {
        log.error("problem getting size of [{}]", path, ex);
        throw ex;
      }
    }
  }
  @Override
  public boolean isDirectory(String path) throws Exception {
    try (SFTPConnection con = getConnection()) {
      String fullPath = con.getFullPath(path);
      try {
        Attributes attrs = con.sftpClient.lstat(fullPath);
        return attrs.isDirectory();
      } catch (Exception ex) {
        log.error("problem getting isDirectory of [{}]", path, ex);
        throw ex;
      }
    }
  }
  @Override
  public String getName(String path) {
    if (path.contains(PATH_SEPARATOR)) {
      return path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
    } else {
      return path;
    }
  }

  @Override
  public long getUsableSpace() throws Exception {
    try (SFTPConnection con = getConnection()) {
        try {
          SpaceAvailableExtension spaceAvailable = (SpaceAvailableExtension)con.sftpClient.getExtension(SftpConstants.EXT_SPACE_AVAILABLE);
          return spaceAvailable.available(connectionInfo.getRootPath()).unusedBytesAvailableToUser;
        }catch(Exception ex) {
          OpenSSHStatPathExtension extension2 = (OpenSSHStatPathExtension)con.sftpClient.getExtension(StatVfsExtensionParser.NAME);
          log.info("extension2[{}][{}]", extension2, extension2.getClass());
          OpenSSHStatExtensionInfo stat = extension2.stat(connectionInfo.getRootPath());
          long bytesAvailable = stat.f_bsize * stat.f_bavail;
          return bytesAvailable;
        }
    }
  }

  @Override
  public void retrieve(String remoteRelativePath, File localFileOrDir, Progress progress) throws Exception {
    try (SFTPConnection con = getConnection()) {

      String remoteFullPath = getConnection().getFullPath(remoteRelativePath);

      SFTPMonitorSSHD monitor = new SFTPMonitorSSHD(progress, connectionInfo.getClock(), this.isMonitoring());

      UtilitySSHD.getDir(con.sftpClient, Paths.get(remoteFullPath), localFileOrDir, monitor);
    }
  }

  @Override
  public String store(String path, File localFileOrDirectory, Progress progress) throws Exception {
    try (SFTPConnection con = getConnection()) {

      final Path basePath = Paths.get(con.getFullPath(path));

      // Create timestamped folder to avoid overwriting files
      String timestampDirName = SftpUtils.getTimestampedDirectoryName(connectionInfo.getClock());
      Path tsDirPath = basePath.resolve(timestampDirName);

      UtilitySSHD.createDir(con.sftpClient, tsDirPath);

      Path sftpDestDirPath = tsDirPath;

      if (localFileOrDirectory.isDirectory()) {
        // Create top-level directory
        String workingDirName = localFileOrDirectory.getName();

        sftpDestDirPath = tsDirPath.resolve(workingDirName);
        UtilitySSHD.createDir(con.sftpClient, sftpDestDirPath);
      }

      SFTPMonitorSSHD monitor = new SFTPMonitorSSHD(progress, connectionInfo.getClock(), this.isMonitoring());

      UtilitySSHD.send(con.sftpClient, localFileOrDirectory, sftpDestDirPath, monitor);

      return tsDirPath.toString();
    }
  }

  @Override
  public boolean isMonitoring() {
    return this.monitoring;
  }

  @Override
	public boolean canRead(String path) throws Exception {
		return false;
	}

  @Override
	public boolean canWrite(String path) throws Exception {
		return false;
	}



  public void setMonitoring(boolean value) {
    this.monitoring = value;
  }

}
