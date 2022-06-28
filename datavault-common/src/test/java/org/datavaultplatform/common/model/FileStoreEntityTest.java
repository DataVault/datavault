package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FileStoreEntityTest extends BaseEntityTest<FileStore, String> {

  @Test
  void testEntity() {
    checkEntity(FileStore.class, this::generateID);
  }
}
