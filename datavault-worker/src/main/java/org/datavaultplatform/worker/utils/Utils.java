package org.datavaultplatform.worker.utils;

import java.io.File;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.Verify;
import org.springframework.util.Assert;

@Slf4j
public class Utils {
  /*
      When we get an execution failure - the Throwable must be Exception or Error.
      In Java, we never declare 'throws Error' - we are not meant to catch Errors.
      We can't just 'throws Throwable' from this method because that would mean
      we have to catch Throwable elsewhere (including Errors)

      We just need to declare 'throws Exception' for this method. We split out the
      Throwable from ExecutionException into Exception(declared) and Error(non-declared).
   */
  public static void handleExecutionException(ExecutionException ee, String label) throws Exception {
    Throwable cause = ee.getCause();
    if (cause instanceof Exception) {
      Exception ex = (Exception) cause;
      log.error(label + " " + ex.getMessage());
      throw ex;
    }
    log.error("unexpected non-Exception", cause);
    if (cause instanceof java.lang.Error) {
      java.lang.Error error = (java.lang.Error) cause;
      log.error(label + " " + error.getMessage());
      throw error;
    }
  }

  public static void checkFileHash(String label, File file, String expectedHash) throws Exception {
    if (expectedHash == null) {
      log.warn("NULL expectedHash for [{}][{}]", label, file);
      return;
    }
    log.info("Calculate Checksum Digest for: " + file.getAbsolutePath());
    Assert.isTrue(file.exists(), () -> "File does not exist: " + file.getAbsolutePath());
    Assert.isTrue(file.isFile(), () -> "File is not a file: " + file.getAbsolutePath());
    String computedHash = Verify.getDigest(file);
    log.info("Checksum Digest for [{}]is[{}] ", file.getAbsolutePath(), computedHash);
    // Compare the SHA hashes
    if (!computedHash.equalsIgnoreCase(expectedHash)) {
      String msg = String.format("Checksum check failed for [%s],(%s), %s != %s",
          file.getCanonicalPath(),
          label,
          computedHash.toUpperCase(),
          expectedHash.toUpperCase());
      throw new Exception(msg);
    }
  }
}
