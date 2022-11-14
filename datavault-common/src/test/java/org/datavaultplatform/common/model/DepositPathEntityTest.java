package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DepositPathEntityTest extends BaseEntityTest<DepositPath, String> {

  @Test
  void testEntity() {
    checkEntity(DepositPath.class, this::generateID);
  }
}
