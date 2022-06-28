package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class PendingDataCreatorEntityTest extends BaseEntityTest<PendingDataCreator, String> {

  @Test
  void testEntity() {
    checkEntity(PendingDataCreator.class, this::generateID);
  }
}
