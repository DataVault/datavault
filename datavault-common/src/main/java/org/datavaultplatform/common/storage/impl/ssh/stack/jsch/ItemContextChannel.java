package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import com.jcraft.jsch.ChannelSftp;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.ssh.stack.ItemContext;

@Slf4j
public class ItemContextChannel extends ItemContext<ChannelSftp> {

  @SneakyThrows
  public ItemContextChannel(ChannelSftp channel){
    log.info("initial pwd [{}]", channel.pwd());
    setContext(channel);
  }

}
