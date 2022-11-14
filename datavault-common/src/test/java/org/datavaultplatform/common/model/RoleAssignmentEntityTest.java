package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RoleAssignmentEntityTest extends BaseEntityTest<RoleAssignment, Long> {

  @Test
  void testEntity() {
    checkEntity(RoleAssignment.class, this::generateLongID);
  }
}
