package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class AuditChunkStatusEntityTest extends BaseEntityTest<AuditChunkStatus, String> {

  @Test
  void testEntity() {
    checkEntity(AuditChunkStatus.class, this::generateID);
  }
}
