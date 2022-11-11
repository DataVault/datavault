package org.datavaultplatform.common.storage.impl;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;

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
}
