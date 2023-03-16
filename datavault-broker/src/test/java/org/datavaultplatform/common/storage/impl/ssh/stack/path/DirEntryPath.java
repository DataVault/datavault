package org.datavaultplatform.common.storage.impl.ssh.stack.path;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.datavaultplatform.common.storage.impl.ssh.stack.Item;
import org.datavaultplatform.common.storage.impl.ssh.stack.ItemContext;

public class DirEntryPath implements Item<Path, Path> {

  private final File dirEntry;

  public DirEntryPath(File dirEntry) {
    this.dirEntry = dirEntry;
  }

  @Override
  public void process(Stack<Item<Path, Path>> stack, ItemContext<Path> ctx) {
    if (dirEntry.isFile()) {
      ctx.increment(dirEntry.length());
      ctx.incrementCount();
    } else {
      ctx.setContext(dirEntry.toPath());
      Map<Boolean, List<File>> groupedItems = Stream.of(dirEntry.listFiles())
          .collect(Collectors.partitioningBy(File::isDirectory));

      //push directory 'pop'
      stack.push(EndDirItemPath.getInstance());

      //push all directories
      groupedItems.get(true).forEach(dir -> stack.push(new DirEntryPath(dir)));

      //push all 'non directories'
      groupedItems.get(false).forEach(nonDir -> stack.push(new DirEntryPath(nonDir)));
    }
  }
}
