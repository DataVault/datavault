package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class BillingInfoEntityTest extends BaseEntityTest<BillingInfo, String> {

  @Test
  void testEntity() {
    checkEntity(BillingInfo.class, this::generateID);
  }
}
