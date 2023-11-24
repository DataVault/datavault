package org.datavaultplatform.common.storage.impl.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.ssh.stack.jsch.ItemContextChannel;
import org.datavaultplatform.common.storage.impl.ssh.stack.jsch.ItemLsEntryDir;
import org.datavaultplatform.common.storage.impl.ssh.stack.jsch.ItemLsEntryNonDir;
import org.datavaultplatform.common.storage.impl.ssh.stack.Item;
import org.datavaultplatform.common.storage.impl.ssh.stack.StackProcessor;

@Slf4j
public abstract class UtilityJSchNonRecurse {

  public static long calculateSize(final ChannelSftp channel,
      String remoteFile) throws SftpException {

    if (remoteFile.lastIndexOf('/') != -1) {
      if (remoteFile.length() > 1) {
        remoteFile = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
      }
    }
    channel.cd(remoteFile);
    channel.cd("..");

    ItemContextChannel ctx = new ItemContextChannel(channel);

    LsEntry initialEntry = getInitialEntry(channel, remoteFile);

    Item<LsEntry, ChannelSftp> initialItem =
        initialEntry.getAttrs().isDir()
            ? new ItemLsEntryDir(initialEntry)
            : new ItemLsEntryNonDir(initialEntry);

    StackProcessor<LsEntry,ChannelSftp> processor = new StackProcessor<>(ctx, initialItem);
    processor.process();
    return ctx.getSize();
  }

  @SneakyThrows
  private static LsEntry getInitialEntry(ChannelSftp channel, String remoteFile) {
    Path path = Paths.get(remoteFile);
    int count = path.getNameCount();
    String endPath = path.getName(count - 1).toString();

    Vector<LsEntry> entries = channel.ls("*");
    LsEntry initialEntry = entries.stream()
        .filter((LsEntry entry) -> endPath.equals(entry.getFilename()))
        .findFirst()
        .get();
    return initialEntry;
  }
}
