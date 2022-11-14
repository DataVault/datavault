package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DepositChunkEntityTest extends BaseEntityTest<DepositChunk, String> {

  @Test
  void testEntity() {
    checkEntity(DepositChunk.class, this::generateID);
  }
}
