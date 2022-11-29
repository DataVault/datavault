package org.datavaultplatform.common.storage.impl;

import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;

@Slf4j
public class SSHDErrorHandler implements SftpErrorDataHandler {

  private final String context;

  public SSHDErrorHandler(String context){
    this.context = context;
  }

  @Override
  public void errorData(byte[] buf, int start, int len) throws IOException {
    log.error(String.format("context[%s]buf[%s]start[%d]len[%d]", context, Arrays.toString(buf), start, len));
  }

}
