package org.datavaultplatform.broker.actuator;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id="sftpfilestores")
@Slf4j
public class SftpFileStoreEndpoint {

  public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

  private final FileStoreService fileStoreService;

  private final Function<String,String> portAdjuster;

  private final StorageClassNameResolver resolver;
  private final List<String> sftpFileStoreEndpointIdsToIgnore;


  public SftpFileStoreEndpoint(FileStoreService fileStoreService, Function<String,String> portAdjuster,
      StorageClassNameResolver resolver, List<String> sftpFileStoreEndpointIdsToIgnore
      ) {
    this.fileStoreService = fileStoreService;
    this.portAdjuster = portAdjuster;
    this.resolver = resolver;
    this.sftpFileStoreEndpointIdsToIgnore = sftpFileStoreEndpointIdsToIgnore;
    log.info("sftpFileStoreEndpointIdsToIgnore #[{}]", sftpFileStoreEndpointIdsToIgnore.size());
    int total = sftpFileStoreEndpointIdsToIgnore.size();
    for (int i = 0; i < total; i++) {
      String id = sftpFileStoreEndpointIdsToIgnore.get(i);
      int number = i + 1;
      log.info(String.format("#[%d/%d] : Ignoring FileStore with Id[%s]", number, total, id));
    }
  }

  @ReadOperation
  public List<SftpFileStoreInfo> getSftpFileStoresInfo() {
    return fileStoreService.getFileStores().stream()
        .filter(FileStore::isSFTPFileSystem)
        .map(this::getFileStoreInfo)
        .sorted(Comparator.comparing(SftpFileStoreInfo::isIgnored)) //ignored first
        .collect(Collectors.toList());
  }

  private SftpFileStoreInfo getFileStoreInfo(FileStore fileStore) {

    String originalPort = fileStore.getProperties().get("port");
    String portToUse = portAdjuster.apply(originalPort);
    fileStore.getProperties().put(PropNames.PORT, portToUse);

    SftpFileStoreInfo info = new SftpFileStoreInfo();
    info.setId(fileStore.getID());
    info.setLabel(fileStore.getLabel());
    info.setUsername(fileStore.getProperties().get(PropNames.USERNAME));
    info.setHost(fileStore.getProperties().get(PropNames.HOST));
    info.setPort(fileStore.getProperties().get(PropNames.PORT));
    info.setRootPath(fileStore.getProperties().get(PropNames.ROOT_PATH));

    if (sftpFileStoreEndpointIdsToIgnore.contains(fileStore.getID())) {
      info.setIgnored(true);
      return info;
    }

    long start = System.currentTimeMillis();
    Future<Boolean> future = EXECUTOR.submit(() -> {
      try {
        UserStore us = UserStore.fromFileStore(fileStore, resolver);
        SFTPFileSystemDriver sftp = (SFTPFileSystemDriver) us;
        sftp.list(".");
        long diff = System.currentTimeMillis() - start;
        log.info("SFTP connection [{}] took [{}]ms", info, diff);
        return true;
      } catch (Exception ex) {
        info.setConnectionException(getExceptionString(ex));
        log.warn("problem trying to connect to {}", info, ex);
        return false;
      }
    });
    boolean connected = false;
    try {
      connected = future.get(30, TimeUnit.SECONDS);
    } catch (Exception ex) {
      info.setConnectionException(getExceptionString(ex));
      log.warn("problem trying to connect to {}", info, ex);
    }
    info.setCanConnect(connected);
    return info;
  }

  private String getExceptionString(Exception ex) {
    return StringUtils.abbreviate(ExceptionUtils.getStackTrace(ex), 1000);
  }
}
