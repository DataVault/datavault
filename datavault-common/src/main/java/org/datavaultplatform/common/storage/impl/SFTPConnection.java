package org.datavaultplatform.common.storage.impl;

import java.nio.file.Paths;
import java.time.Clock;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpErrorDataHandler;
import org.apache.sshd.sftp.client.SftpVersionSelector;
import org.apache.sshd.sftp.client.impl.DefaultSftpClient;

@Slf4j
public class SFTPConnection implements AutoCloseable {

  private static final long DEFAULT_TIMEOUT_SECONDS = 10;
  private static final SftpErrorDataHandler ERROR_HANDLER = (buf, start, len) -> log.error(
      String.format("buf[%s]start[%d]len[%d]", Arrays.toString(buf), start, len));

  public final Clock clock;
  public final SshClient client;
  public final ClientSession session;
  public final SftpClient sftpClient;

  public final String rootPath;

  @SneakyThrows
  public SFTPConnection(SFTPConnectionInfo info) {
    long start = System.currentTimeMillis();
    try {
      clock = info.getClock();
      rootPath = info.getRootPath();
      client = SshClient.setUpDefaultClient();
      client.start();

      session = client.connect(info.getUsername(), info.getHost(), info.getPort())
          .verify(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS).getSession();

      if (StringUtils.isNotBlank(info.getPassword())) {
        session.addPasswordIdentity(info.getPassword());
      } else {
        session.addPublicKeyIdentity(info.getKeyPair());
      }

      session.auth()
          .verify(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

      sftpClient = new DefaultSftpClient(session, SftpVersionSelector.CURRENT,
          ERROR_HANDLER);

      if(log.isTraceEnabled()) {
        SessionContext ctx = sftpClient.getClientChannel().getSessionContext();
        String clientVersion = ctx.getClientVersion();
        String serverVersion = ctx.getServerVersion();
        log.trace("client[{}]server[{}]", clientVersion, serverVersion);

        NavigableMap<String, byte[]> extensions = sftpClient.getServerExtensions();
        log.trace("server extensions[{}]", extensions);
      }
    } finally {
      long diff = System.currentTimeMillis() - start;
      log.info("NEW CONNECTION TOOK [{}]ms",diff);
    }
  }

  @Override
  @SneakyThrows
  public void close() {
    long start = System.currentTimeMillis();
    try {
      IOUtils.closeQuietly(this.sftpClient, this.session, this.client);
    } finally {
      long diff = System.currentTimeMillis() - start;
      log.info("Closing Connection took [{}]ms",diff);
    }
  }

  @SneakyThrows
  public String getFullPath(String relativePath) {
    String fullPath = Paths.get(this.rootPath)
        .resolve(relativePath).toFile()
        .getCanonicalPath();
    return fullPath;
  }

}
