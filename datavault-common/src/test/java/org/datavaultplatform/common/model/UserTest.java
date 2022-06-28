package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class UserTest extends BaseEntityTest<User, String> {

  @Test
  void testEntity() {
    checkEntity(User.class, this::generateID);
  }
}
