package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class GroupEntityTest extends BaseEntityTest<Group, String> {

  @Test
  void testEntity() {
    checkEntity(Group.class, this::generateID);
  }
}
