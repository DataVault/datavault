package org.datavaultplatform.common.storage.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.Progress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Slf4j
public class LocalFileSystemTest {

  @TempDir
  private static File TMP_LOCAL_FS_DIR;

  @TempDir
  private static File TMP_TO_STORAGE_DIR;

  @TempDir
  private static File TMP_FROM_STORAGE_DIR;
  LocalFileSystem localFileSystem;

  @BeforeEach
  void setup() throws FileNotFoundException {
    log.info("temp : TMP_TO_STORAGE_DIR [{}]", TMP_TO_STORAGE_DIR.getAbsolutePath());
    log.info("temp : TMP_FROM_STORAGE_DIR [{}]", TMP_FROM_STORAGE_DIR.getAbsolutePath());

    String rootPathValue = TMP_LOCAL_FS_DIR.getAbsolutePath();
    log.info("temp : LocalFS Dir [{}]", rootPathValue);
    Map<String, String> props = new HashMap<>();
    props.put(LocalFileSystem.ROOT_PATH, rootPathValue);
    localFileSystem = new LocalFileSystem(LocalFileSystem.class.getName(), props);
  }

  @Test
  void testLocalFileSystem() throws Exception {
    long usable = localFileSystem.getUsableSpace();
    log.info("usable[{}]", usable);
    assertThat(usable).isGreaterThan(0L);
    File randomFile = new File(TMP_TO_STORAGE_DIR, "file1.txt");
    PrintWriter pw = new PrintWriter(randomFile);
    pw.println("one");
    pw.println("two");
    pw.println("three");
    pw.flush();
    Progress pStore = new Progress();
    String storedFileOne = localFileSystem.store("storagePath1", randomFile, pStore);
    log.info("storedFileOne [{}]", storedFileOne);
    assertEquals(1, pStore.fileCount);
    assertEquals(14, pStore.byteCount);
    checkProgressFinishedBeforeNow(pStore);

    // need to test quick retrieve too and test contents, progress etc
    Progress pRetrieve = new Progress();
    File targetFile = new File(TMP_FROM_STORAGE_DIR, storedFileOne);
    localFileSystem.retrieve("storagePath1/" + storedFileOne, targetFile, pRetrieve);
    //assertEquals(1, pRetrieve.fileCount);
    checkProgressFinishedBeforeNow(pRetrieve);

    List<String> lines = Files.readLines(targetFile, StandardCharsets.UTF_8);
    assertEquals(3, lines.size());
    assertEquals("one", lines.get(0));
    assertEquals("two", lines.get(1));
    assertEquals("three", lines.get(2));
  }

  void checkProgressFinishedBeforeNow(Progress progress) {
    long diff = Instant.now().minus(progress.timestamp, ChronoUnit.MILLIS).toEpochMilli();
    assertTrue(diff >= 0);
  }
}
