package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import java.util.Stack;
import lombok.SneakyThrows;
import org.datavaultplatform.common.storage.impl.ssh.stack.Item;
import org.datavaultplatform.common.storage.impl.ssh.stack.ItemContext;

public class ItemLsEntryEndDir implements Item<LsEntry, ChannelSftp> {

  private static final ItemLsEntryEndDir INSTANCE = new ItemLsEntryEndDir();

  private ItemLsEntryEndDir() {
  }

  public static ItemLsEntryEndDir getInstance() {
    return INSTANCE;
  }

  /**
   * When we process this Item, the context goes back up a level
   */
  @Override
  @SneakyThrows
  public void process(Stack<Item<LsEntry, ChannelSftp>> stack, ItemContext<ChannelSftp> ctx) {
    ctx.getContext().cd("..");
  }
}
