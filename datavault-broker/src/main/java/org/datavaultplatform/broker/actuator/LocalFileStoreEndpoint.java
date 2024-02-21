package org.datavaultplatform.broker.actuator;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id="localfilestores")
@Slf4j
public class LocalFileStoreEndpoint {

  private final ArchiveStoreService archiveStoreService;

  @Autowired
  public LocalFileStoreEndpoint(ArchiveStoreService archiveStoreService) {
    this.archiveStoreService = archiveStoreService;
  }

  @ReadOperation
  public List<LocalFileStoreInfo> getLocalFileStoresInfo() {
    return archiveStoreService.getArchiveStores().stream()
        .filter(ArchiveStore::isLocalFileSystem)
        .map(this::getLocalFileStoreInfo)
        .collect(Collectors.toList());
  }

  private LocalFileStoreInfo getLocalFileStoreInfo(ArchiveStore store) {

    LocalFileStoreInfo info = new LocalFileStoreInfo();
    info.setId(store.getID());
    info.setLabel(store.getLabel());
    info.setRootPath(store.getProperties().get(LocalFileSystem.ROOT_PATH));
    info.setRetrieveEnabled(store.isRetrieveEnabled());

    boolean isValid = false;
    try {
      new LocalFileSystem(store.getLabel(), store.getProperties());
      isValid = true;
    } catch (Exception ex) {
      info.setValidationException(getExceptionString(ex));
      log.warn("problem with LocalFileStore {}", info, ex);
    }
    info.setValid(isValid);
    return info;
  }

  private String getExceptionString(Exception ex) {
    return StringUtils.abbreviate(ExceptionUtils.getStackTrace(ex), 1000);
  }
}
