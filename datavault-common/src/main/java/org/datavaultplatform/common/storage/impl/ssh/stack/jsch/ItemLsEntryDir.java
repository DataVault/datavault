package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.ssh.stack.Item;
import org.datavaultplatform.common.storage.impl.ssh.stack.ItemContext;
import org.springframework.util.Assert;

@Slf4j
public class ItemLsEntryDir implements Item<LsEntry, ChannelSftp> {

  private final LsEntry listEntry;

  public ItemLsEntryDir(LsEntry listEntry) {
    Assert.isTrue(listEntry.getAttrs().isDir());
    this.listEntry = listEntry;
  }

  @Override
  @SneakyThrows
  public void process(Stack<Item<LsEntry, ChannelSftp>> stack, ItemContext<ChannelSftp> ctx) {
    ctx.getContext().cd(this.listEntry.getFilename());

    Vector<LsEntry> entries = ctx.getContext().ls("*");

    Map<Boolean, List<LsEntry>> groupedItems = entries.stream()
        .collect(Collectors.partitioningBy((LsEntry item) -> item.getAttrs().isDir()));

    // push 'cd ..' - process this third (last)
    stack.push(ItemLsEntryEndDir.getInstance());

    // push all directories - process these second
    groupedItems.get(true).forEach(dirEntry -> stack.push(new ItemLsEntryDir(dirEntry)));

    // push all 'non directories' - process these first
    groupedItems.get(false).forEach(nonDirEntry -> stack.push(new ItemLsEntryNonDir(nonDirEntry)));
  }
}
