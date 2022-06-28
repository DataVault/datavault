package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class PendingVaultEntityTest extends BaseEntityTest<PendingVault, String> {

  @Test
  void testEntity() {
    checkEntity(PendingVault.class, this::generateID);
  }
}
