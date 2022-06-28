package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DepositEntityTest extends BaseEntityTest<Deposit, String> {

  @Test
  void testEntity() {
    checkEntity(Deposit.class, this::generateID);
  }
}
