package org.datavaultplatform.common.storage.impl.ssh.stack.path;

import java.nio.file.Path;
import java.util.Stack;
import org.datavaultplatform.common.storage.impl.ssh.stack.Item;
import org.datavaultplatform.common.storage.impl.ssh.stack.ItemContext;

public class EndDirItemPath implements Item<Path,Path> {

  private static final EndDirItemPath INSTANCE = new EndDirItemPath();

  private EndDirItemPath() {
  }

  public static EndDirItemPath getInstance(){
    return INSTANCE;
  }

  /**
   * When we process this Item, the context goes back up a level
   */
  @Override
  public void process(Stack<Item<Path,Path>> stack, ItemContext<Path> ctx) {
    Path parent = ctx.getContext().getParent();
    ctx.setContext(parent);
  }
}
