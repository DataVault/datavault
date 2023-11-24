package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import java.util.Stack;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.ssh.stack.Item;
import org.datavaultplatform.common.storage.impl.ssh.stack.ItemContext;
import org.springframework.util.Assert;

@Slf4j
public class ItemLsEntryNonDir implements Item<LsEntry, ChannelSftp> {

  private final LsEntry listEntry;

  public ItemLsEntryNonDir(LsEntry listEntry) {
    Assert.isTrue(!listEntry.getAttrs().isDir());
    this.listEntry = listEntry;
  }

  @Override
  @SneakyThrows
  public void process(Stack<Item<LsEntry, ChannelSftp>> stack, ItemContext<ChannelSftp> ctx) {
    ctx.increment(this.listEntry.getAttrs().getSize());
  }
}
