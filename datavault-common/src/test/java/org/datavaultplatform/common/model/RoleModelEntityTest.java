package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RoleModelEntityTest extends BaseEntityTest<RoleModel, Long> {

  @Test
  void testEntity() {
    checkEntity(RoleModel.class, this::generateLongID);
  }
}

