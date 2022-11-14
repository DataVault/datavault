package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class AuditEntityTest extends BaseEntityTest<Audit, String> {

  @Test
  void testEntity() {
    checkEntity(Audit.class, this::generateID);
  }
}
