package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ArchiveEntityTest extends BaseEntityTest<Archive, String> {

  @Test
  void testEntity() {
    checkEntity(Archive.class, this::generateID);
  }
}
