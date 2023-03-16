package org.datavaultplatform.common.storage.impl.ssh.stack.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/*
This tests that the number of files created by createFilesInternal
is the same calculated by getExpectedFileCountAtLevel.
These (level, number of files) pairs are important for SftpDirectorySizeTest.
 */
@Slf4j
public class CountFilesInDirectoryTest extends BaseNestedFilesTest {

  @Test
  void testLevel0() {
    createFiles(this.baseDir, 0);
    assertEquals(getExpectedFileCountAtLevel(0), this.fileCounter);
  }

  @Test
  void testLevel1() {
    createFiles(this.baseDir, 1);
    assertEquals(getExpectedFileCountAtLevel(1), this.fileCounter);
  }

  @Test
  void testLevel2() {
    createFiles(this.baseDir, 2);
    assertEquals(getExpectedFileCountAtLevel(2), this.fileCounter);
  }

  @Test
  void testLevel3() {
    createFiles(this.baseDir, 3);
    assertEquals(getExpectedFileCountAtLevel(3), this.fileCounter);
  }

  @Test
  void testLevel4() {
    createFiles(this.baseDir, 4);
    assertEquals(getExpectedFileCountAtLevel(4), this.fileCounter);
  }

  @Test
  void testNumberOfFilesAtLevel10() {
    assertEquals(4_094, getExpectedFileCountAtLevel(10));
  }

  @Test
  void testNumberOfFilesAtLevel11() {
    assertEquals(8_190, getExpectedFileCountAtLevel(11));
  }

  @Test
  void testNumberOfFilesAtLevel12() {
    assertEquals(16_382, getExpectedFileCountAtLevel(12));
  }

  @Test
  void testNumberOfFilesAtLevel16() {
    assertEquals(262_142, getExpectedFileCountAtLevel(16));
  }

  @Test
  @Disabled
  @SneakyThrows
  void testLevels() {
    for (int level = 0; level < 30; level++) {
      long expected = getExpectedFileCountAtLevel(level);
      log.info("level[{}] files[{}]", level, expected);
    }
  }
}
