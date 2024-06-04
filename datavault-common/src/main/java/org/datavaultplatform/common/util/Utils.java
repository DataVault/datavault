package org.datavaultplatform.common.util;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.Verify;
import org.springframework.util.Assert;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    if (cause instanceof Exception ex) {
      log.error(label + " " + ex.getMessage());
      throw ex;
    }
    log.error("unexpected non-Exception", cause);
    if (cause instanceof Error error) {
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
  
  public static <T> String toCommaSeparatedString(Collection<?> collection) {
    if (collection == null || collection.isEmpty()) {
      return "";
    }
    return collection.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.joining(","));
  }
  
  public static <T> List<T> fromCommaSeparatedString(String value, Function<String,T> parser) {
    Assert.isTrue(parser != null, "The parser cannot be null");
    if(value == null || value.isEmpty()) {
      return Collections.emptyList();
    }
    String[] tokens = value.split("\\s*,\\s*");
    List<T> result = new ArrayList<>();
    for(String token: tokens){
      result.add(parser.apply(token));
    }
    return result;
  }
  
}
