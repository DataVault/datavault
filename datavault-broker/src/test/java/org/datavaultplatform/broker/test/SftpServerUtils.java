package org.datavaultplatform.broker.test;

import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SocketUtils;


@Slf4j
public abstract class SftpServerUtils {

  @SneakyThrows
  public static EmbeddedSftpServer getSftpServer(String publicKey, Path tempSftpFolder){
    int port = SocketUtils.findAvailableTcpPort();
    EmbeddedSftpServer sftpServer = new EmbeddedSftpServer(publicKey, port);
    sftpServer.afterPropertiesSet();
    sftpServer.setHomeFolder(tempSftpFolder);
    sftpServer.start();

    log.info("sftp directory [{}]", tempSftpFolder);
    log.info("server running ? [{}]", sftpServer.isRunning());
    log.info("server host ?  [{}]", sftpServer.getServer().getHost());
    log.info("server port ?  [{}]", sftpServer.getServer().getPort());
    log.info("started 1");
    return sftpServer;
  }
}
