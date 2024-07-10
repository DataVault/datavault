package org.datavaultplatform.worker.operations;

import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

@Slf4j
public class TarSimulateVeryLargeFileIT {

  public static final long MEGABYTE = 1024 * 1024;
  public static final long GIGABYTE = 1024 * MEGABYTE;
  public static final long TERABYTE = 1024 * GIGABYTE;


  @SneakyThrows
  @ParameterizedTest
  @ValueSource(longs = {0, 1, TERABYTE})
  void testTarUpFakeLargeFileButIgnoreTarFileOutput(long fakeFileSize) {
    File parentDir = new ClassPathResource("big_data").getFile();
    File bigFile = new File(parentDir, "big_file");
    File proxyBigFile = new File(bigFile.getAbsolutePath()){
      @Override
      public long length() {
        return fakeFileSize;
      }
    };
    assertEquals(fakeFileSize, proxyBigFile.length());

    File proxyParentDir = new File(parentDir.getAbsolutePath()){
      @Override
      public File[] listFiles() {
        return new File[]{ proxyBigFile};
      }
    };
    assertArrayEquals(new File[]{proxyBigFile}, proxyParentDir.listFiles());

    Tar.createTarUsingFakeFile(proxyParentDir, null, fakeFileSize);

    log.info("fake file size     [{}]", fakeFileSize);
    log.info("copied to tar [{}]", Tar.getCopiedToTar());

    assertEquals(Tar.getCopiedToTar(), fakeFileSize);
  }

  int checkFileSize(long fileSize) {
    return Math.toIntExact(fileSize / 512);
  }

  /**
   * In V1.22 of
   * org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
   * there was an 'integer overflow' error when the contents of the tar file reached 1TB.
   * We have worked around this problem by using a tweaked copy of this code which is
   * not at
   * org.datavaultplatform.worker.operations.DatavaultTarArchiveOutputStream
   */
  @Nested
  class IntegerOverflowTests {

    @Test
    void testIntegerOverflowHappens() {
      long fails = TERABYTE;
      ArithmeticException ex = assertThrows(ArithmeticException.class, () -> checkFileSize(fails));
      assertEquals("integer overflow", ex.getMessage());
    }

    @Test
    void testIntegerOverflowDoesNotHappen() {
      long works = TERABYTE - 1;
      int result = checkFileSize(works);
      log.info("int result [{}]", result);
      log.info("int max    [{}]", Integer.MAX_VALUE);
      assertEquals(works / 512, result);
      assertEquals(Integer.MAX_VALUE, result );

    }
  }

}
