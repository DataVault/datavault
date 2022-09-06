package org.datavaultplatform.common.storage.impl;

import static org.datavaultplatform.common.storage.impl.SFTPConnectionInfo.PATH_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
import org.datavaultplatform.common.storage.impl.ssh.UtilitySSHD.SFTPMonitorSSD;

@Slf4j
public class SFTPFileSystemSSHD extends Device implements SFTPFileSystemDriver {

  private final SFTPConnectionInfo connectionInfo;

  /**
   * This constructor will be used in production code
   * @param name
   * @param config
   */
  public SFTPFileSystemSSHD(String name, Map<String, String> config) {
    this(name, config, Clock.systemDefaultZone());
  }

  /**
   * This constructor will be used for testing where we can pass in a fixed clock
   * @param name
   * @param config
   */
  public SFTPFileSystemSSHD(String name, Map<String, String> config, Clock clock) {
    super(name, config);
    this.connectionInfo = SFTPConnectionInfo.getConnectionInfo(config, clock);
  }

  @Override
  @SneakyThrows
  public List<FileInfo> list(String path) {
    List<FileInfo> files = new ArrayList<>();
    try (SFTPConnection con = getConnection()) {
      String fullPath = getFullPath(path);

      //TODO - don't know how deep this should go 1 level or all the way down - need to test the other one with nested directories
      for (SftpClient.DirEntry entry : con.sftpClient.readDir(fullPath)) {

        //TODO ?? need to check this ??
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

        // Other useful properties:
        // entry.getAttrs().getSize()
        // entry.getAttrs().isDir()
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
      String fullPath = getFullPath(path);
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
      String fullPath = getFullPath(path);
      try {
        Attributes attrs = con.sftpClient.lstat(fullPath);
        if (attrs.isDirectory()) {

          if (!path.endsWith("/")) {
            path = path + "/";
          }

          return UtilitySSHD.calculateSize(con.sftpClient, path);
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
      String fullPath = getFullPath(path);
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
  public void retrieve(String remoteRelativePath, File localStorageDir, Progress progress) throws Exception {
    try (SFTPConnection con = getConnection()) {
      // Strip any leading separators (we want a path relative to the current dir)
      while (remoteRelativePath.startsWith(PATH_SEPARATOR)) {
        remoteRelativePath = remoteRelativePath.replaceFirst(PATH_SEPARATOR, "");
      }

      String remoteFullPath = getFullPath(remoteRelativePath);

      SFTPMonitorSSD monitor = new SFTPMonitorSSD(progress, connectionInfo.getClock());

      UtilitySSHD.getDir(con.sftpClient, Paths.get(remoteFullPath), localStorageDir, monitor);
    }
  }

  @Override
  public String store(String path, File localFileOrDirectory, Progress progress) throws Exception {
    try (SFTPConnection con = getConnection()) {
      // Strip any leading separators (we want a path relative to the current dir)
      while (path.startsWith(PATH_SEPARATOR)) {
        path = path.replaceFirst(PATH_SEPARATOR, "");
      }

      final Path basePath = Paths.get(getFullPath(path));

      // We are sleeping to ensure we don't get duplicate timestamp folders - assumes single thread - TODO ensure this
      TimeUnit.SECONDS.sleep(2);
      // Create timestamped folder to avoid overwriting files
      String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(connectionInfo.getClock().millis()));
      String timestampDirName = "dv_" + timeStamp;
      Path tsDirPath = basePath.resolve(timestampDirName);

      UtilitySSHD.createDir(con.sftpClient, tsDirPath);

      Path sftpDestDirPath = tsDirPath;

      if (localFileOrDirectory.isDirectory()) {
        // Create top-level directory
        String workingDirName = localFileOrDirectory.getName();

        sftpDestDirPath = tsDirPath.resolve(workingDirName);
        UtilitySSHD.createDir(con.sftpClient, sftpDestDirPath);
      }

      SFTPMonitorSSD monitor = new SFTPMonitorSSD(progress, connectionInfo.getClock());

      UtilitySSHD.send(con.sftpClient, localFileOrDirectory, sftpDestDirPath, monitor);

      return tsDirPath.toString();
    }
  }
  @SneakyThrows
  private String getFullPath(String relativePath){
    String fullPath = Paths.get(connectionInfo.getRootPath())
        .resolve(relativePath).toFile()
        .getCanonicalPath();
    return fullPath;
  }
}
