package org.datavaultplatform.common.storage.impl.ssh.stack.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.ssh.stack.StackProcessor;
import org.junit.jupiter.api.Test;

/*
This tests that we can crawl a tree of files, calculating the total size
by using an explicit stack and not using recursion.
 */
@Slf4j
public class StackProcessorTest extends BaseNestedFilesTest {

  @Test
  void testCrawlFilesUsingStackProcessor() {
    createFiles(this.baseDir, 10);
    assertEquals(getExpectedFileCountAtLevel(10), this.fileCounter);

    ItemContextPath ctx = new ItemContextPath();
    ctx.setContext(this.baseDir.toPath().getParent());

    DirEntryPath initialItem = new DirEntryPath(this.baseDir);

    StackProcessor<Path, Path> stackProcessor = new StackProcessor<>(
        ctx, initialItem);

    stackProcessor.process();
    long size = ctx.getSize();
    long count = ctx.getCount();

    log.info("COUNT [{}]", count);
    log.info("SIZE  [{}]", size);

    assertEquals(count, fileCounter);
    assertEquals(3 * count, size);
  }
}
