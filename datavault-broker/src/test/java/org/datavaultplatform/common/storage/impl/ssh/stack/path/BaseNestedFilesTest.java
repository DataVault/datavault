package org.datavaultplatform.common.storage.impl.ssh.stack.path;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;


@Slf4j
public abstract class BaseNestedFilesTest {

  long fileCounter;

  @TempDir
  File baseDir;

  @BeforeEach
  void setup() {
    fileCounter = 0;
    assertTrue(baseDir.exists() && baseDir.isDirectory());
    log.info("BASE DIR [{}]", baseDir);
  }

  void createFiles(File base, int depth) {
    createFilesInternal(base, depth);
    log.info("Created [{}] files", this.fileCounter);
  }

  private void createFilesInternal(File base, int depth) {
    if (depth > 0) {
      File left = new File(base, "left_" + depth);
      left.mkdirs();
      File right = new File(base, "right_" + depth);
      right.mkdirs();
      createFilesInternal(left, depth - 1);
      createFilesInternal(right, depth - 1);
    }
    File aaa = new File(base, "aaa.txt");
    write(aaa, "aaa");
    File bbb = new File(base, "bbb.txt");
    write(bbb, "bbb");
  }


  @SneakyThrows
  void write(File file, String contents) {
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.write(contents);
    }
    fileCounter++;
  }

  public static final long getExpectedFileCountAtLevel(int level) {
    long total = 0;
    for (int depth = 0; depth <= level; depth++) {
      total += Math.pow(2, depth + 1);
    }
    log.info("expected file count for depth[{}] is [{}]", level, total);
    return total;
  }
}
