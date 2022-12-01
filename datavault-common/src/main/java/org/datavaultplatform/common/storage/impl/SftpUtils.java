package org.datavaultplatform.common.storage.impl;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.springframework.util.Assert;

import static org.datavaultplatform.common.storage.impl.SFTPConnectionInfo.PATH_SEPARATOR;

public class SftpUtils {

  /*
   * Ensures that a timestampedDirectoryName can only be obtained every 2 seconds.
   */
  @SneakyThrows
  public static String getTimestampedDirectoryName(Clock clock) {
    synchronized (SftpUtils.class){
      // We are sleeping to ensure we don't get duplicate timestamp folders - assumes single thread - TODO ensure this
      TimeUnit.SECONDS.sleep(2);
      // Create timestamped folder to avoid overwriting files
      String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(clock.millis()));
      String timestampDirName = "dv_" + timeStamp;
      return timestampDirName;
    }
  }

  public static String getFullPath(String rootPath, String relativePath) {

    Assert.isTrue(rootPath != null, "The rootPath cannot be null");
    if (relativePath == null) {
      return rootPath;
    }
    // Strip any leading separators (we want a path relative to the current dir)
    while (relativePath.startsWith(PATH_SEPARATOR)) {
      relativePath = relativePath.replaceFirst(PATH_SEPARATOR, "");
    }

    String fullPath = Paths.get(rootPath)
            .resolve(relativePath)
            .normalize()
            .toFile()
            .getAbsolutePath();

    return fullPath;
  }
}
